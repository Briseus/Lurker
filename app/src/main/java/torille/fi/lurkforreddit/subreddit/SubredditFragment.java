package torille.fi.lurkforreddit.subreddit;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import torille.fi.lurkforreddit.Injection;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.comments.CommentActivity;
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper;
import torille.fi.lurkforreddit.data.Post;
import torille.fi.lurkforreddit.data.PostDetails;
import torille.fi.lurkforreddit.data.Subreddit;
import torille.fi.lurkforreddit.media.FullscreenActivity;
import torille.fi.lurkforreddit.utils.DisplayHelper;
import torille.fi.lurkforreddit.utils.MediaHelper;
import torille.fi.lurkforreddit.utils.TextHelper;

import static android.view.View.GONE;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubredditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubredditFragment extends Fragment implements SubredditContract.View {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARGUMENT_SUBREDDIT = "subreddit";

    private SubredditContract.UserActionsListener mActionsListener;

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
        args.putParcelable(ARGUMENT_SUBREDDIT, Parcels.wrap(subreddit));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionsListener = new SubredditPresenter(Injection.provideRedditRepository(),
                this);
        mCustomTabActivityHelper = new CustomTabActivityHelper();
        DisplayHelper.init(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_subreddit, container, false);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.posts_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        // TODO Single on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!refreshing && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        refreshing = true;
                        Log.d("SubredditFragment", "Last item reached, getting more!");
                        mActionsListener.loadMorePosts(getSubredditUrl(), mNextPageId);
                    }
                }
            }
        });

        ViewPreloadSizeProvider<Post> preloadSizeProvider = new ViewPreloadSizeProvider<>();
        RequestBuilder<Drawable> preloadRequest = Glide.with(getActivity()).asDrawable().transition(withCrossFade()).apply(centerCropTransform(getActivity()));

        mListAdapter = new PostsAdapter(new ArrayList<Post>(0),
                mBrowserListener,
                mOpenCommentsListener,
                mOpenMediaListener,
                preloadRequest,
                preloadSizeProvider,
                ContextCompat.getColor(getContext(), R.color.colorAccent));

        RecyclerViewPreloader<Post> preloader = new RecyclerViewPreloader<>(Glide.with(getActivity()), mListAdapter, preloadSizeProvider, 5);
        recyclerView.addOnScrollListener(preloader);

        recyclerView.setAdapter(mListAdapter);
        recyclerView.setHasFixedSize(true);

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
        mCustomTabActivityHelper.bindCustomTabsService(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(getActivity());
    }

    public String getSubredditUrl() {
        Subreddit subreddit = Parcels.unwrap(getArguments().getParcelable(ARGUMENT_SUBREDDIT));
        if (subreddit != null && subreddit.getUrl() != null) {
            return subreddit.getUrl();
        } else {
            Toast.makeText(getContext(), "Subreddit id was null! ", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    public void loadIfEmpty() {
        if (mListAdapter.getItemCount() == 0) {
            mActionsListener.loadPosts(getSubredditUrl());
        }

    }

    /**
     * Listeners for clicks in the Recyclerview
     */
    openBrowserListener mBrowserListener = new openBrowserListener() {
        @Override
        public void onButtonClick(String url) {
            mActionsListener.openCustomTabs(url);
        }
    };
    openCommentsListener mOpenCommentsListener = new openCommentsListener() {
        @Override
        public void onClick(Post clickedPost) {
            mActionsListener.openComments(clickedPost);
        }
    };
    openMediaListener mOpenMediaListener = new openMediaListener() {
        @Override
        public void onClick(Post post) {
            mActionsListener.openMedia(post);
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
        CustomTabActivityHelper.openCustomTab(getActivity(),
                MediaHelper.createCustomTabIntent(getActivity(),
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
        if (MediaHelper.isContentMedia(post) || MediaHelper.checkDomainForMedia(post)) {
            Intent intent = new Intent(getContext(), FullscreenActivity.class);
            intent.putExtra(FullscreenActivity.EXTRA_POST, Parcels.wrap(post));
            startActivity(intent);
        } else if (MediaHelper.launchCustomActivity(post)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getPostDetails().getUrl()));
            startActivity(intent);
        } else {
            showCustomTabsUI(post.getPostDetails().getUrl());
        }

    }

    @Override
    public void showCommentsUI(Post clickedPost) {
        Intent intent = new Intent(getContext(), CommentActivity.class);
        intent.putExtra(CommentActivity.EXTRA_CLICKED_POST, Parcels.wrap(clickedPost));
        startActivity(intent);
    }

    private static class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ListPreloader.PreloadModelProvider<Post> {
        private final static int VIEW_ITEM = 1;
        private final static int VIEW_PROGRESS = 0;
        private List<Post> mPosts;
        private openBrowserListener mBrowserListener;
        private openCommentsListener mCommentsListener;
        private openMediaListener mMediaListener;
        private int mDefaultAccentColor;
        private RequestBuilder<Drawable> preloadRequest;
        private ViewPreloadSizeProvider<Post> preloadSizeProvider;

        //TODO Add click listeners
        public PostsAdapter(List<Post> posts, openBrowserListener listener, openCommentsListener commentsListener, openMediaListener mOpenMediaListener, RequestBuilder<Drawable> glide, ViewPreloadSizeProvider<Post> preloadSizeProvider, int color) {
            this.mPosts = posts;
            this.mBrowserListener = listener;
            this.mCommentsListener = commentsListener;
            this.mMediaListener = mOpenMediaListener;
            this.preloadRequest = glide;
            this.preloadSizeProvider = preloadSizeProvider;
            this.mDefaultAccentColor = color;
            setHasStableIds(true);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_ITEM:
                    PostViewHolder vh = new PostViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_post, parent, false));
                    preloadSizeProvider.setView(vh.image);
                    return vh;
                case VIEW_PROGRESS:
                default:
                    return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_progressbar, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PostViewHolder) {
                ((PostViewHolder) holder).bind(mPosts.get(position).getPostDetails());

            } else if (holder instanceof ProgressViewHolder) {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            }


        }

        @Override
        public int getItemViewType(int position) {
            if (mPosts.get(position).getKind().equals("t3")) {
                return VIEW_ITEM;
            } else {
                return VIEW_PROGRESS;
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
            final int progressBarPosition = mPosts.size() - 1;
            mPosts.set(progressBarPosition, newPosts.get(0));
            newPosts.remove(0);
            notifyItemChanged(progressBarPosition);
            mPosts.addAll(progressBarPosition, newPosts);
            notifyItemRangeInserted(progressBarPosition, newPosts.size());


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
            if (active) {
                android.os.Handler handler = new android.os.Handler();
                Post dummy = new Post();
                PostDetails dummyDetails = new PostDetails();
                dummyDetails.setId("Progressbar");
                dummy.setPostDetails(dummyDetails);
                dummy.setKind("Progressbar");
                mPosts.add(dummy);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemInserted(mPosts.size() - 1);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        @Override
        public long getItemId(int position) {
            return mPosts.get(position).getPostDetails().getId().hashCode();
        }

        public Post getItem(int position) {
            return mPosts.get(position);
        }

        @Override
        public List<Post> getPreloadItems(int position) {
            return Collections.singletonList(getItem(position));
        }

        @Override
        public RequestBuilder<Drawable> getPreloadRequestBuilder(Post post) {
            if (post.getPostDetails().getPreviewImage() != null && post.getPostDetails().getPreviewImage().isEmpty()) {
                return preloadRequest;
            }
            return preloadRequest.load(post.getPostDetails().getPreviewImage());
        }

        public class PostViewHolder extends RecyclerView.ViewHolder {
            final View view;
            final TextView title;
            final TextView domain;
            final TextView details;
            final Button score;
            final Button comments;
            final ImageButton openBrowser;
            final ImageView image;

            public PostViewHolder(View postView) {
                super(postView);
                view = postView;
                title = (TextView) postView.findViewById(R.id.post_title);
                domain = (TextView) postView.findViewById(R.id.post_domain);
                details = (TextView) postView.findViewById(R.id.post_details);
                score = (Button) postView.findViewById(R.id.post_likes);
                comments = (Button) postView.findViewById(R.id.post_messages);
                openBrowser = (ImageButton) postView.findViewById(R.id.post_open_browser);
                image = (ImageView) postView.findViewById(R.id.post_image);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMediaListener.onClick(getItem(getAdapterPosition()));
                    }
                });
                openBrowser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBrowserListener.onButtonClick(getItem(getAdapterPosition()).getPostDetails().getUrl());
                    }
                });
                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCommentsListener.onClick(getItem(getAdapterPosition()));
                    }
                });
                score.getBackground().setTint(mDefaultAccentColor);
            }

            void bind(PostDetails postDetails) {
                if (postDetails.isStickied()) {
                    title.setText(TextHelper.fromHtml(postDetails.getTitle() + "<font color='#64FFDA'> Stickied </font>"));
                } else {
                    title.setText(postDetails.getTitle());
                }

                domain.setText(postDetails.getDomain());
                score.setText(postDetails.getPreviewScore());

                //details.setText(postDetails.getPreviewText());

                comments.setText(String.valueOf(postDetails.getNumberOfComments()));

                switch (postDetails.getThumbnail()) {
                    case "default":
                    case "self":
                    case "":
                    case "image":
                        image.setVisibility(GONE);
                        break;
                    case "nsfw":
                        title.setText(TextHelper.fromHtml(postDetails.getTitle() + "<font color='#FF1744'> NSFW</font>"));
                        image.setVisibility(GONE);
                        break;
                    default:
                        if (postDetails.getPreviewImage().isEmpty()) {
                            image.setVisibility(GONE);
                        } else {
                            image.setVisibility(View.VISIBLE);
                            preloadRequest.load(postDetails.getPreviewImage()).into(image);
                        }

                }
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

    public interface openBrowserListener {
        void onButtonClick(String url);
    }

    public interface openCommentsListener {
        void onClick(Post clickedPost);
    }

    public interface openMediaListener {
        void onClick(Post post);
    }
}
