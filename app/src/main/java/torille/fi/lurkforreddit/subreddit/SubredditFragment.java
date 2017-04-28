package torille.fi.lurkforreddit.subreddit;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.comments.CommentActivity;
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.media.FullscreenActivity;
import torille.fi.lurkforreddit.utils.DisplayHelper;
import torille.fi.lurkforreddit.utils.MediaHelper;

import static android.view.View.GONE;
import static android.view.View.resolveSize;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubredditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubredditFragment extends Fragment implements SubredditContract.View {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARGUMENT_SUBREDDIT = "subreddit";

    @Inject
    SubredditContract.Presenter<SubredditContract.View> mActionsListener;

    private SubredditComponent subredditComponent;

    private PostsAdapter mListAdapter;
    private LinearLayoutManager mLayoutManager;
    private boolean refreshing;
    private String mNextPageId;
    private CustomTabActivityHelper mCustomTabActivityHelper;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param subreddit Chosen subreddit.
     * @return A new instance of fragment SubredditFragment.
     */
    public static SubredditFragment newInstance(Subreddit subreddit) {
        SubredditFragment fragment = new SubredditFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_SUBREDDIT, subreddit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Subreddit subreddit = getArguments().getParcelable(ARGUMENT_SUBREDDIT);
        subredditComponent = DaggerSubredditComponent.builder()
                .redditRepositoryComponent(((MyApplication) getActivity().getApplication()).getmRedditRepositoryComponent())
                .subredditPresenterModule(new SubredditPresenterModule(subreddit))
                .build();
        mListAdapter = new PostsAdapter(new ArrayList<Post>(25),
                mClickListener,
                ContextCompat.getColor(getContext(), R.color.colorAccent),
                Fresco.getImagePipeline());
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        DisplayHelper.init(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_subreddit, container, false);
        subredditComponent.inject(this);
        mActionsListener.setView(this);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.posts_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private static final int PREFETCH_SIZE = 2;
            int pastVisiblesItems, visibleItemCount, totalItemCount;
            int lastFetch = 0;
            int scrolledItems;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                    scrolledItems = (visibleItemCount + pastVisiblesItems);

                    if (lastFetch == 0) {
                        mListAdapter.prefetchImages(2, PREFETCH_SIZE, totalItemCount);
                    }

                    if (!refreshing && scrolledItems > lastFetch) {
                        lastFetch = scrolledItems + (PREFETCH_SIZE - 1);
                        mListAdapter.prefetchImages(lastFetch, PREFETCH_SIZE, totalItemCount);
                    }

                    if (!refreshing && scrolledItems >= totalItemCount) {
                        refreshing = true;
                        Timber.d("Last item reached, getting more!");
                        mActionsListener.loadMorePosts(getSubredditUrl(), mNextPageId);
                    }
                }
            }
        });
        recyclerView.setAdapter(mListAdapter);

        // Pull-to-refresh
        SwipeRefreshLayout swipeRefreshLayout =
                (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListAdapter.clear();
                loadIfEmpty();

            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadIfEmpty();
    }

    @Override
    public void onStart() {
        super.onStart();
        setProgressIndicator(false);
        mCustomTabActivityHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        Fresco.getImagePipeline().clearMemoryCaches();
        mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActionsListener.dispose();
    }

    private String getSubredditUrl() {
        Subreddit subreddit = getArguments().getParcelable(ARGUMENT_SUBREDDIT);
        if (subreddit != null && subreddit.url() != null) {
            return subreddit.url();
        } else {
            Toast.makeText(getContext(), "SubredditResponse id was null! ", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private void loadIfEmpty() {
        if (mListAdapter.getItemCount() == 0) {
            mActionsListener.start();
        }

    }

    /**
     * Listeners for clicks in the Recyclerview
     */
    private final postClickListener mClickListener = new postClickListener() {
        @Override
        public void onButtonClick(String url) {
            mActionsListener.openCustomTabs(url);
        }

        @Override
        public void onPostClick(Post clickedPost) {
            mActionsListener.openComments(clickedPost);
        }

        @Override
        public void onMediaClick(Post post) {
            mActionsListener.openMedia(post);
        }

        @Override
        public void onRetryClick() {
            mActionsListener.retry();
        }
    };

    @Override
    public void setProgressIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showPosts(List<Post> posts, String nextpage) {
        mListAdapter.addAll(posts);
        mNextPageId = nextpage;
    }

    @Override
    public void setListProgressIndicator(boolean active) {
        mListAdapter.setRefreshing(active);
    }

    @Override
    public void addMorePosts(List<Post> posts, String nextpage) {
        mListAdapter.addMorePosts(posts);
        refreshing = false;
        mNextPageId = nextpage;
    }


    @Override
    public void showCustomTabsUI(final String url) {
        Activity activity = getActivity();
        CustomTabActivityHelper.openCustomTab(activity,
                MediaHelper.createCustomTabIntent(activity,
                        mCustomTabActivityHelper.getSession()),
                url,
                new CustomTabActivityHelper.CustomTabFallback() {
                    @Override
                    public void openUri(Activity activity, String uri) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void showMedia(Post post) {
        Intent intent = new Intent(getContext(), FullscreenActivity.class);
        intent.putExtra(FullscreenActivity.EXTRA_POST, post);
        startActivity(intent);
    }

    @Override
    public void showCommentsUI(Post clickedPost) {
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra(CommentActivity.EXTRA_CLICKED_POST, clickedPost);
        startActivity(intent);
    }

    @Override
    public void launchCustomActivity(Post clickedPost) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedPost.url()));
        startActivity(intent);
    }

    @Override
    public void onError(String errorText) {
        Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void setListErrorButton(boolean active) {
        mListAdapter.setListLoadingError(active);
    }


    private static class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final static int VIEW_ITEM = 1;
        private final static int VIEW_PROGRESS = 0;
        private final static int VIEW_ERROR = 2;

        private final static String PROGRESSBAR = "progressbar";
        private final static String ERROR = "error";

        private final List<Post> mPosts;
        private final postClickListener mClicklistener;
        private final int mDefaultAccentColor;
        private final ImagePipeline imagePipeline;

        PostsAdapter(@NonNull List<Post> posts, @NonNull postClickListener listener, int color, @NonNull ImagePipeline pipeline) {
            mPosts = posts;
            mClicklistener = listener;
            mDefaultAccentColor = color;
            imagePipeline = pipeline;
            setHasStableIds(true);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_ITEM:
                    return new PostViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_post, parent, false));
                case VIEW_ERROR:
                    return new ErrorViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_error, parent, false));
                case VIEW_PROGRESS:
                default:
                    return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_progressbar, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PostViewHolder) {
                ((PostViewHolder) holder).bind(mPosts.get(position));
            } else if (holder instanceof ProgressViewHolder) {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            }


        }

        /**
         * Prefetch to bitmap cache in fresco to make
         * images appear without fading or waiting
         *
         * @param fromIndex     tells the position in mPosts
         * @param prefetchCount how much to cache ahead
         * @param listMaxSize   the maxsize which you cant go over
         */
        private void prefetchImages(final int fromIndex, final int prefetchCount, final int listMaxSize) {


            final int to = (fromIndex + prefetchCount);
                /*
                make toIndex value of "to" only if its not bigger than maxsize
                so you dont get indexOutOfBounds errors
                 */
            final int toIndex = to > listMaxSize ? listMaxSize : to;

            for (int i = fromIndex; i < toIndex; i++) {
                final String url = mPosts.get(i).previewImage();
                imagePipeline.prefetchToBitmapCache(ImageRequest.fromUri(url), null);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mPosts.get(position).id().equals(PROGRESSBAR)) {
                return VIEW_PROGRESS;
            } else if (mPosts.get(position).id().equals(ERROR)) {
                return VIEW_ERROR;
            } else {
                return VIEW_ITEM;
            }

        }

        void addAll(List<Post> list) {
            int oldDataEnd = 0;
            if (!mPosts.isEmpty()) {
                oldDataEnd = mPosts.size() - 1;
            }
            mPosts.addAll(list);
            notifyItemRangeInserted(oldDataEnd, list.size());
        }

        void addMorePosts(@NonNull final List<Post> newPosts) {
            final int index = mPosts.size();
            mPosts.addAll(index, newPosts);
            notifyItemRangeInserted(index, newPosts.size());


        }

        void clear() {
            if (mPosts != null) {
                final int oldSize = mPosts.size();
                mPosts.clear();
                notifyItemRangeRemoved(0, oldSize);

            }
        }

        /**
         * To use setStableIds and get viewtype,
         * add identifiers to a dummy post
         * that is used in the progressbar
         **/
        void setRefreshing(boolean active) {
            final int index = mPosts.size() - 1;
            if (active) {
                mPosts.add(createPost(PROGRESSBAR));
                android.os.Handler handler = new android.os.Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemInserted(index);
                    }
                });

            } else {
                mPosts.remove(index);
                notifyItemRemoved(index);

            }

        }

        void setListLoadingError(boolean active) {
            final int index = mPosts.size() - 1;
            if (active) {
                mPosts.set(index, createPost(ERROR));
                notifyItemChanged(index);
            } else {
                mPosts.remove(index);
                notifyItemRemoved(index);
            }

        }

        private Post createPost(@NonNull String id) {
            return Post.builder()
                    .setDomain("")
                    .setUrl("")
                    .setId(id)
                    .setScore("")
                    .setSelfText(null)
                    .setPreviewImage("")
                    .setThumbnail("")
                    .setTitle("")
                    .setNumberOfComments("")
                    .setIsSelf(false)
                    .setPermaLink("")
                    .setCreatedUtc(0)
                    .setAuthor("")
                    .build();
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        @Override
        public long getItemId(int position) {
            return mPosts.get(position).id().hashCode();
        }

        final Post getItem(int position) {
            return mPosts.get(position);
        }


        class PostViewHolder extends RecyclerView.ViewHolder {

            final TextView title;
            final TextView domain;
            final Button score;
            final Button comments;
            final ImageButton openBrowser;
            final SimpleDraweeView image;
            final BaseControllerListener<ImageInfo> baseControllerListener;

            PostViewHolder(View postView) {
                super(postView);
                title = (TextView) postView.findViewById(R.id.post_title);
                domain = (TextView) postView.findViewById(R.id.post_domain);
                score = (Button) postView.findViewById(R.id.post_likes);
                comments = (Button) postView.findViewById(R.id.post_messages);
                openBrowser = (ImageButton) postView.findViewById(R.id.post_open_browser);
                image = (SimpleDraweeView) postView.findViewById(R.id.post_image);
                postView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClicklistener.onMediaClick(getItem(getAdapterPosition()));
                    }
                });
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClicklistener.onMediaClick(getItem(getAdapterPosition()));
                    }
                });
                openBrowser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClicklistener.onButtonClick(getItem(getAdapterPosition()).url());
                    }
                });
                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClicklistener.onPostClick(getItem(getAdapterPosition()));
                    }
                });
                score.getBackground().setTint(mDefaultAccentColor);
                baseControllerListener = new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        super.onFailure(id, throwable);
                        Timber.e("Failed to load image id: " + id + " error " + throwable.getLocalizedMessage());
                        image.setImageURI(getItem(getAdapterPosition()).thumbnail());
                    }
                };
            }

            final void bind(final Post postDetails) {

                title.setText(postDetails.title());
                domain.setText(postDetails.domain());
                score.setText(postDetails.score());
                comments.setText(postDetails.numberOfComments());

                String previewImage = postDetails.previewImage();
                if (previewImage.isEmpty()) {
                    image.setVisibility(GONE);
                } else {
                    image.setVisibility(View.VISIBLE);

                    DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                            .setControllerListener(baseControllerListener)
                            .setImageRequest(ImageRequest.fromUri(previewImage))
                            .setOldController(image.getController())
                            .build();

                    image.setController(draweeController);

                }


            }

        }

        private class ErrorViewHolder extends RecyclerView.ViewHolder {
            final Button retryButton;

            ErrorViewHolder(View v) {
                super(v);
                retryButton = (Button) v.findViewById(R.id.button);
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClicklistener.onRetryClick();
                    }
                });
            }
        }

        private static class ProgressViewHolder extends RecyclerView.ViewHolder {
            final ProgressBar progressBar;

            ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            }
        }
    }

    interface postClickListener {

        void onButtonClick(String url);

        void onPostClick(Post clickedPost);

        void onMediaClick(Post post);

        void onRetryClick();
    }
}
