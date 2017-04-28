package torille.fi.lurkforreddit.comments;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
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

    private CommentComponent commentComponent;

    @Inject
    CommentContract.Presenter<CommentContract.View> mActionsListener;

    private CommentRecyclerViewAdapter mCommentAdapter;
    private Post mPost;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clickedPost the PostResponse which comments are to be loaded
     * @return A new instance of fragment CommentFragment.
     */
    public static CommentFragment newInstance(@NonNull Post clickedPost) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_CLICKED_POST, clickedPost);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPost = getArguments().getParcelable(ARGUMENT_CLICKED_POST);

        commentComponent = DaggerCommentComponent.builder()
                .redditRepositoryComponent(((MyApplication) getActivity().getApplication()).getmRedditRepositoryComponent())
                .commentPresenterModule(new CommentPresenterModule(mPost))
                .build();

        mCommentAdapter = new CommentRecyclerViewAdapter(mPost, addDummyComment(), mClickListener);
    }

    private List<Comment> addDummyComment() {

        Comment comment = Comment.builder()
                .setId("og")
                .setKind(Comment.kind.OG)
                .setName("")
                .setParentId("")
                .setCommentLevel(0)
                .setCommentLinkId("")
                .setCommentText("")
                .setAuthor("")
                .setFormattedTime("")
                .setFormattedScore("")
                .setChildCommentIds(null)
                .setReplies(null)
                .build();

        ArrayList<Comment> list = new ArrayList<>();
        list.add(comment);
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
                mActionsListener.loadComments(mPost.permaLink());
            }
        });
        return root;
    }

    @Override
    public void showComments(List<Comment> comments) {
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
    };

    static class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int COMMENT_ORIGINAL = -1;
        private static final int COMMENT_PROGRESSBAR = -2;
        private static final int COMMENT_LOAD_MORE = -3;
        private final List<Comment> mComments;
        private final Post mClickedPost;
        private final CommentClickListener mClickListener;

        CommentRecyclerViewAdapter(Post clickedPost, List<Comment> commentChildren, CommentClickListener listener) {
            mClickedPost = clickedPost;
            mComments = commentChildren;
            mClickListener = listener;
            setHasStableIds(true);
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
                default:
                    return new CommentViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_comment_constraint, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PostViewHolder) {
                ((PostViewHolder) holder).bind();
            } else if (holder instanceof CommentViewHolder) {
                ((CommentViewHolder) holder).bind(mComments.get(position));
            } else if (holder instanceof CommentLoadMoreViewHolder) {
                ((CommentLoadMoreViewHolder) holder).bind(mComments.get(position));
            } else if (holder instanceof ProgressViewHolder) {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
                ((ProgressViewHolder) holder).bind(mComments.get(position));
            }
        }


        //TODO When comment kind is "continue" return load more type
        @Override
        public int getItemViewType(int position) {
            Comment.kind kind = mComments.get(position).kind();
            switch (kind) {
                case MORE:
                    return COMMENT_LOAD_MORE;
                case PROGRESSBAR:
                    return COMMENT_PROGRESSBAR;
                case OG:
                    return COMMENT_ORIGINAL;
                default:
                    return mComments.get(position).commentLevel();
            }


        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        @Override
        public long getItemId(int position) {
            return mComments.get(position).id().hashCode();
        }

        void replaceData(List<Comment> comments) {
            int size = mComments.size();
            int newSize = comments.size();

            mComments.removeAll(mComments.subList(1, size));
            notifyItemRangeRemoved(1, size);
            mComments.addAll(comments);
            notifyItemRangeInserted(1, newSize);

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
            Comment error = mComments.get(position);
            Comment comment = error.withKindIdTextAndLevel(Comment.kind.MORE,
                    "Retry",
                    "Retry",
                    error.commentLevel());
            mComments.set(position, comment);
            notifyItemChanged(position, error);

        }

        void removeAt(final int position) {
            mComments.remove(position);
            notifyItemRemoved(position);

        }

        void addAllCommentsTo(final int position, @NonNull final List<Comment> comments) {
            mComments.addAll(position, comments);
            notifyItemRangeInserted(position, comments.size());

        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            final TextView mAuthor;
            final TextView mSelftext;
            final TextView mTitle;
            final SimpleDraweeView mImage;
            final Button mScoreButton;

            PostViewHolder(View view) {
                super(view);
                mAuthor = (TextView) view.findViewById(R.id.comment_post_author);
                mSelftext = (TextView) view.findViewById(R.id.comment_post_selftext);
                mTitle = (TextView) view.findViewById(R.id.comment_post_title);
                mImage = (SimpleDraweeView) view.findViewById(R.id.comment_post_image);
                mScoreButton = (Button) view.findViewById(R.id.comment_post_score);
                mSelftext.setTransformationMethod(new CustomLinkTransformationMethod());
                mSelftext.setMovementMethod(LinkMovementMethod.getInstance());
                mSelftext.setLinksClickable(true);
            }

            void bind() {
                String author = "Submitted "
                        + DateUtils.getRelativeTimeSpanString(mClickedPost.createdUtc() * 1000)
                        + " by " + mClickedPost.author();
                mAuthor.setText(author);
                mScoreButton.setText(mClickedPost.score());
                mTitle.setText(mClickedPost.title());

                if (mClickedPost.previewImage().isEmpty()) {
                    mImage.setVisibility(View.GONE);
                } else {

                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setOldController(mImage.getController())
                            .setImageRequest(ImageRequest.fromUri(mClickedPost.previewImage()))
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
                if (!mComment.id().equals("_")) {
                    mClickListener.onClick(mComment, mClickedPost.id(), getAdapterPosition());
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
    }

}
