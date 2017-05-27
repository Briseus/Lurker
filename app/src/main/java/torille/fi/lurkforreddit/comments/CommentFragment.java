package torille.fi.lurkforreddit.comments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment implements CommentContract.View {

    private static final String ARGUMENT_CLICKED_POST = "post";
    private static final String ARGUMENT_IS_SINGLE_COMMENT_THREAD = "single";

    private CommentComponent commentComponent;

    @Inject
    CommentContract.Presenter<CommentContract.View> mActionsListener;

    private CommentRecyclerViewAdapter mCommentAdapter;
    private Post mPost;
    private boolean mIsSingleCommentThread;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clickedPost the PostResponse which comments are to be loaded
     * @return A new instance of fragment CommentFragment.
     */
    public static CommentFragment newInstance(@NonNull Post clickedPost, boolean isSingleCommentThread) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_CLICKED_POST, clickedPost);
        args.putBoolean(ARGUMENT_IS_SINGLE_COMMENT_THREAD, isSingleCommentThread);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPost = getArguments().getParcelable(ARGUMENT_CLICKED_POST);
        mIsSingleCommentThread = getArguments().getBoolean(ARGUMENT_IS_SINGLE_COMMENT_THREAD, false);

        commentComponent = DaggerCommentComponent.builder()
                .redditRepositoryComponent(((MyApplication) getActivity().getApplication()).getmRedditRepositoryComponent())
                .commentPresenterModule(new CommentPresenterModule(mPost, mIsSingleCommentThread))
                .build();

        mCommentAdapter = new CommentRecyclerViewAdapter(setupList(mPost), mClickListener);
    }

    private List<Object> setupList(Post post) {
        ArrayList<Object> list = new ArrayList<>();
        list.add(post);
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadIfEmpty();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActionsListener.dispose();
    }

    private void loadIfEmpty() {
        if (mCommentAdapter.getItemCount() == 1) {
            mActionsListener.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_comments, container, false);
        commentComponent.inject(this);
        mActionsListener.setView(this);
        Context context = getContext();

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.comments_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CommentsItemDecoration(ContextCompat.getDrawable(context, R.drawable.comment_item_decorator)));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mCommentAdapter);

        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mActionsListener.loadComments(mPost.permaLink(), mIsSingleCommentThread);
            }
        });
        return root;
    }

    @Override
    public void showComments(List<Object> comments) {
        mCommentAdapter.replaceData(comments);
    }

    @Override
    public void showProgressbarAt(int position, int level) {
        mCommentAdapter.addProgressbar(position, level);
    }

    @Override
    public void hideProgressbarAt(int position) {
        mCommentAdapter.removeAt(position);
    }

    @Override
    public void addCommentsAt(List<Comment> comments, int position) {
        if (!comments.isEmpty()) {
            mCommentAdapter.addAllCommentsTo(position, comments);
        }
    }

    @Override
    public void showError(String errorText) {
        Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorAt(int position) {
        mCommentAdapter.changeToErrorAt(position);
    }

    @Override
    public void setProgressIndicator(final boolean active) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });

    }

    /**
     * Listener for clicks on notes in the RecyclerView.
     */
    private final CommentClickListener mClickListener = new CommentClickListener() {
        @Override
        public void onClick(Comment parentComment, String linkId, int position) {
            mActionsListener.loadMoreCommentsAt(parentComment, linkId, position);
        }

        @Override
        public void onContinueThreadClick(String permaLinkurl) {

            Intent intent = new Intent(getContext(), CommentActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(permaLinkurl));
            startActivity(intent);
        }
    };

    static class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int COMMENT_ORIGINAL = -1;
        private static final int COMMENT_PROGRESSBAR = -2;
        private static final int COMMENT_LOAD_MORE = -3;
        private static final int COMMENT_NORMAL = -4;
        private static final int COMMENT_TOP = -5;

        private final List<Object> mComments;
        private final CommentClickListener mClickListener;

        CommentRecyclerViewAdapter(List<Object> commentChildren, CommentClickListener listener) {
            mComments = commentChildren;
            mClickListener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case COMMENT_ORIGINAL:
                    return new PostViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_comment_clicked_post, parent, false));
                case COMMENT_LOAD_MORE:
                    return new CommentLoadMoreViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_comment_loadmore, parent, false));
                case COMMENT_PROGRESSBAR:
                    return new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_progressbar, parent, false));
                case COMMENT_TOP:
                    return new TopSingleCommentThreadViewHolder(LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.item_comment_top_comment, parent, false));
                case COMMENT_NORMAL:
                default:
                    return new CommentViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_comment_constraint, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CommentViewHolder) {
                ((CommentViewHolder) holder).bind((Comment) mComments.get(position));
            } else if (holder instanceof ProgressViewHolder) {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
                ((ProgressViewHolder) holder).bind((Comment) mComments.get(position));
            } else if (holder instanceof CommentLoadMoreViewHolder) {
                ((CommentLoadMoreViewHolder) holder).bind((Comment) mComments.get(position));
            } else if (holder instanceof PostViewHolder) {
                ((PostViewHolder) holder).bind((Post) mComments.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {

            if (position == 0) {
                return COMMENT_ORIGINAL;
            } else if (mComments.get(position) instanceof Comment) {
                Comment.kind kind = ((Comment) mComments.get(position)).kind();
                switch (kind) {
                    case SINGLECOMMENTTOP:
                        return COMMENT_TOP;
                    case MORE:
                        return COMMENT_LOAD_MORE;
                    case PROGRESSBAR:
                        return COMMENT_PROGRESSBAR;
                    default:
                        return COMMENT_NORMAL;
                }
            }
            return COMMENT_NORMAL;

        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        void replaceData(List<Object> comments) {
            mComments.clear();
            mComments.addAll(comments);
            notifyDataSetChanged();

        }

        /**
         * Creates a progressbar with certain padding
         *
         * @param level deepness of the comment
         *              the bigger the level the bigger the left padding
         * @return progressbar with padding info
         */
        Comment createProgressbar(int level) {

            return Comment.builder()
                    .setId("Progressbar")
                    .setKind(Comment.kind.PROGRESSBAR)
                    .setCommentLevel(level)
                    .setName("")
                    .setParentId("")
                    .setCommentLinkId("")
                    .setAuthor("")
                    .setCommentText("")
                    .setChildCommentIds(null)
                    .setReplies(null)
                    .setFormattedScore("")
                    .setFormattedTime("")
                    .build();
        }

        void addProgressbar(final int position, final int level) {
            Timber.d("Adding to " + position);
            if (position > 1) {
                mComments.set(position, createProgressbar(level));
                notifyItemChanged(position);
            } else {
                mComments.add(position, createProgressbar(level));
                notifyItemInserted(position);

            }

        }

        void changeToErrorAt(int position) {
            Comment error = (Comment) mComments.get(position);
            Comment comment = error.withKindIdTextAndLevel(Comment.kind.MORE,
                    "Retry",
                    "Retry",
                    error.commentLevel());
            mComments.set(position, comment);
            notifyItemChanged(position, error);

        }

        void removeAt(int position) {
            mComments.remove(position);
            notifyItemRemoved(position);

        }

        void addAllCommentsTo(int position, @NonNull final List<Comment> comments) {
            mComments.addAll(position, comments);
            notifyItemRangeInserted(position, comments.size());

        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            final View view;
            final TextView mAuthor;
            final TextView mSelftext;
            final TextView mTitle;
            final TextView mFlairText;
            final SimpleDraweeView mImage;

            PostViewHolder(View view) {
                super(view);
                this.view = view;
                mAuthor = (TextView) view.findViewById(R.id.comment_post_author);
                mSelftext = (TextView) view.findViewById(R.id.comment_post_selftext);
                mTitle = (TextView) view.findViewById(R.id.comment_post_title);
                mFlairText = (TextView) view.findViewById(R.id.comment_post_flair);
                mImage = (SimpleDraweeView) view.findViewById(R.id.comment_post_image);
                mSelftext.setTransformationMethod(new CustomLinkTransformationMethod());
                mSelftext.setMovementMethod(LinkMovementMethod.getInstance());
                mSelftext.setLinksClickable(true);
            }

            void bind(Post mClickedPost) {

                if (mClickedPost.title().length() == 0) {
                    mAuthor.setText("");
                    mTitle.setText("");
                } else {
                    final String author = mClickedPost.score() + " points | "
                            + "Submitted "
                            + DateUtils.getRelativeTimeSpanString(mClickedPost.createdUtc() * 1000)
                            + " by " + mClickedPost.author();
                    mAuthor.setText(author);
                    mTitle.setText(mClickedPost.title());
                }
                if (mClickedPost.flairText().length() == 0) {
                    mFlairText.setVisibility(View.GONE);
                } else {
                    mFlairText.setVisibility(View.VISIBLE);
                    mFlairText.setText(mClickedPost.flairText());
                }

                if (mClickedPost.previewImage().isEmpty()) {
                    mImage.setVisibility(View.GONE);
                } else {
                    mImage.setVisibility(View.VISIBLE);
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setOldController(mImage.getController())
                            .setImageRequest(ImageRequest.fromUri(mClickedPost.previewImage()))
                            .setControllerListener(new BaseControllerListener<ImageInfo>() {
                                @Override
                                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                                    super.onFinalImageSet(id, imageInfo, animatable);
                                    float ap = ((float) imageInfo.getWidth() / imageInfo.getHeight());
                                    mImage.setAspectRatio(ap);
                                }
                            })
                            .build();
                    mImage.setController(controller);
                }
                CharSequence selftext = mClickedPost.selfText();
                if (selftext != null) {
                    mSelftext.setText(selftext);
                } else {
                    mSelftext.setVisibility(View.GONE);
                }


            }
        }

        class TopSingleCommentThreadViewHolder extends CommentViewHolder {

            TopSingleCommentThreadViewHolder(View view) {
                super(view);
            }

        }

        class CommentViewHolder extends CommentNormalViewHolder {
            final TextView mCommentText;
            final TextView mCommentScore;
            final TextView mCommentAuthor;

            CommentViewHolder(View view) {
                super(view);
                mCommentText = (TextView) view.findViewById(R.id.comment_text);
                mCommentText.setLinksClickable(true);
                mCommentText.setTransformationMethod(new CustomLinkTransformationMethod());
                mCommentText.setMovementMethod(LinkMovementMethod.getInstance());
                mCommentScore = (TextView) view.findViewById(R.id.comment_post_score);
                mCommentAuthor = (TextView) view.findViewById(R.id.comment_author);
            }

            void bind(Comment comment) {
                mComment = comment;
                mCommentText.setText(mComment.commentText());
                mCommentAuthor.setText(mComment.author());
                mCommentScore.setText(mComment.formattedScore());
            }


        }

        class CommentNormalViewHolder extends RecyclerView.ViewHolder {
            Comment mComment;

            CommentNormalViewHolder(View itemView) {
                super(itemView);
            }

        }

        class CommentLoadMoreViewHolder extends CommentNormalViewHolder implements View.OnClickListener {
            final TextView mClickMore;

            CommentLoadMoreViewHolder(View view) {
                super(view);
                mClickMore = (TextView) view.findViewById(R.id.comment_loadmore);
                view.setOnClickListener(this);
            }

            void bind(Comment commentChild) {
                mComment = commentChild;
                mClickMore.setText(mComment.commentText());
            }

            @Override
            public void onClick(View v) {
                Timber.d(mComment.toString());
                Post post = (Post) mComments.get(0);
                if (mComment.id().equals("_")) {

                    //remove t1_ from start of id
                    String parentId = mComment.parentId().substring(3);
                    String permalinkToComment = "https://www.reddit.com" + post.permaLink() + parentId;
                    Timber.d(permalinkToComment);
                    mClickListener.onContinueThreadClick(permalinkToComment);
                } else {
                    mClickListener.onClick(mComment, post.id(), getAdapterPosition());
                }
            }


        }

        class ProgressViewHolder extends CommentNormalViewHolder {
            final ProgressBar progressBar;

            ProgressViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            }

            void bind(Comment commentChild) {
                mComment = commentChild;
            }
        }
    }

    interface CommentClickListener {
        void onClick(Comment parentComment, String linkId, int position);

        void onContinueThreadClick(String permaLinkurl);
    }

}
