package torille.fi.lurkforreddit.comments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import torille.fi.lurkforreddit.Injection;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.Comment;
import torille.fi.lurkforreddit.data.CommentChild;
import torille.fi.lurkforreddit.data.Post;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment implements CommentContract.View {

    private static final String ARGUMENT_CLICKED_POST = "post";
    private CommentContract.UserActionsListener mActionsListener;
    private CommentRecyclerViewAdapter mCommentAdapter;
    private Post mPost;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clickedPost the Post which comments are to be loaded
     * @return A new instance of fragment CommentFragment.
     */
    public static CommentFragment newInstance(Post clickedPost) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_CLICKED_POST, Parcels.wrap(clickedPost));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPost = Parcels.unwrap(getArguments().getParcelable(ARGUMENT_CLICKED_POST));
        mCommentAdapter = new CommentRecyclerViewAdapter(mPost, addDummyComment(), mClickListener);
        mActionsListener = new CommentPresenter(Injection.provideRedditRepository(), this);
    }

    private List<CommentChild> addDummyComment() {

        Comment dummyComment = new Comment();
        dummyComment.setId("og");
        CommentChild dummyFirstCommentForPost = new CommentChild("og", dummyComment);
        ArrayList<CommentChild> list = new ArrayList<>();
        list.add(dummyFirstCommentForPost);
        return list;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        loadIfEmpty();
    }

    private void loadIfEmpty() {
        if (mCommentAdapter.getItemCount() == 1) {
            mActionsListener.loadComments(mPost.getPostDetails().getPermalink());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comments, container, false);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.comments_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new CommentsItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.comment_item_decorator)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mCommentAdapter);
        return root;
    }

    @Override
    public void showComments(List<CommentChild> commentChildren) {
        mCommentAdapter.addComments(commentChildren);
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
    public void addCommentsAt(List<CommentChild> comments, int position) {
        if (!comments.isEmpty()) {
            mCommentAdapter.addAllCommentsTo(position, comments);
        }
    }

    /**
     * Listener for clicks on notes in the RecyclerView.
     */
    final CommentClickListener mClickListener = new CommentClickListener() {
        @Override
        public void onClick(CommentChild parentComment, String linkId, int position) {
            mActionsListener.loadMoreCommentsAt(parentComment, linkId, position);
        }
    };

    static class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int COMMENT_ORIGINAL = -1;
        private static final int COMMENT_PROGRESSBAR = -2;
        private static final int COMMENT_LOAD_MORE = -3;
        private final List<CommentChild> mComments;
        private final Post mClickedPost;
        private final CommentClickListener mClickListener;

        CommentRecyclerViewAdapter(Post clickedPost, List<CommentChild> commentChildren, CommentClickListener listener) {
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
                            .inflate(R.layout.item_comment, parent, false));
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

            switch (mComments.get(position).getKind()) {
                case "more":
                    return COMMENT_LOAD_MORE;
                case "Progressbar":
                    return COMMENT_PROGRESSBAR;
                case "og":
                    return COMMENT_ORIGINAL;
                default:
                    return mComments.get(position).getType();
            }


        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        @Override
        public long getItemId(int position) {
            return mComments.get(position).getData().getId().hashCode();
        }

        void addComments(List<CommentChild> commentChildren) {
            int oldDataEnd = mComments.size();
            mComments.addAll(commentChildren);
            notifyItemRangeInserted(oldDataEnd, commentChildren.size());
        }

        /**
         * Creates a progressbar with certain padding
         *
         * @param level deepness of the comment
         *              the bigger the level the bigger the left padding
         * @return progressbar with padding info
         */
        CommentChild createProgressbar(int level) {
            CommentChild dummyComment = new CommentChild();
            Comment dummy = new Comment();
            dummy.setId("Progressbar");
            dummyComment.setData(dummy);
            dummyComment.setKind("Progressbar");
            dummyComment.setType(level);
            return dummyComment;
        }

        void addProgressbar(int position, int level) {
            Log.d("more", "Adding to " + position);
            if (position > 1) {
                mComments.set(position, createProgressbar(level));
                notifyItemChanged(position);
            } else {
                mComments.add(position, createProgressbar(level));
                notifyItemInserted(position);
            }

        }

        void removeAt(int position) {
            mComments.remove(position);
            notifyItemRemoved(position);
        }

        void addAllCommentsTo(int position, @NonNull List<CommentChild> comments) {
            mComments.addAll(position, comments);
            notifyItemRangeInserted(position, comments.size());
        }

        public void removeAndAddProgressBar(int position, int level) {
            mComments.set(position, createProgressbar(level));
            notifyItemChanged(position);
        }

        public void changeCommentAt(int position, CommentChild comment) {
            mComments.set(position, comment);
            notifyItemChanged(position);
        }

        class PostViewHolder extends RecyclerView.ViewHolder {
            final TextView mAuthor;
            final TextView mSelftext;
            final TextView mTitle;
            final SimpleDraweeView mImage;
            final TextView mScoreButton;

            PostViewHolder(View view) {
                super(view);
                mAuthor = (TextView) view.findViewById(R.id.comment_post_author);
                mSelftext = (TextView) view.findViewById(R.id.comment_post_selftext);
                mTitle = (TextView) view.findViewById(R.id.comment_post_title);
                mImage = (SimpleDraweeView) view.findViewById(R.id.comment_post_image);
                mScoreButton = (TextView) view.findViewById(R.id.comment_post_score);
            }

            void bind() {
                String time = (String) DateUtils.getRelativeTimeSpanString(mClickedPost.getPostDetails().getCreatedUtc() * 1000);
                String author = "Submitted " + time + " by " + mClickedPost.getPostDetails().getAuthor();
                mAuthor.setText(author);
                mScoreButton.setText(String.valueOf(mClickedPost.getPostDetails().getScore()));
                mTitle.setText(mClickedPost.getPostDetails().getTitle());
                if (mClickedPost.getPostDetails().getPreviewImage().isEmpty()) {
                    mImage.setVisibility(View.GONE);
                } else {
                    DraweeController controller = Fresco.newDraweeControllerBuilder()
                            .setOldController(mImage.getController())
                            .setImageRequest(ImageRequest.fromUri(mClickedPost.getPostDetails().getPreviewImage()))
                            .build();
                    mImage.setController(controller);
                }
                if (mClickedPost.getPostDetails().getSelftextHtml() != null && !mClickedPost.getPostDetails().getSelftextHtml().isEmpty()) {
                    mSelftext.setText(TextHelper.trimTrailingWhitespace(TextHelper.fromHtml(mClickedPost.getPostDetails().getSelftextHtml())));
                } else {
                    mSelftext.setVisibility(View.GONE);
                }
            }
        }

        class CommentViewHolder extends RecyclerView.ViewHolder {
            final TextView mCommentText;
            final TextView mCommentScore;
            final TextView mCommentAuthor;
            CommentChild mComment;

            CommentViewHolder(View view) {
                super(view);
                mCommentText = (TextView) view.findViewById(R.id.comment_text);
                mCommentText.setMovementMethod(LinkMovementMethod.getInstance());
                mCommentText.setLinksClickable(true);
                mCommentScore = (TextView) view.findViewById(R.id.comment_post_score);
                mCommentAuthor = (TextView) view.findViewById(R.id.comment_author);
            }

            void bind(CommentChild commentChild) {
                this.mComment = commentChild;
                mCommentText.setText(mComment.getData().getFormattedComment());
                mCommentAuthor.setText(mComment.getData().getFormatAuthor());
                mCommentScore.setText(String.valueOf(mComment.getData().getScore()));
            }


        }

        class CommentLoadMoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView mClickMore;
            CommentChild mComment;

            CommentLoadMoreViewHolder(View view) {
                super(view);
                mClickMore = (TextView) view.findViewById(R.id.comment_loadmore);
                view.setOnClickListener(this);
            }

            void bind(CommentChild commentChild) {
                this.mComment = commentChild;
                Comment comment = mComment.getData();
                String text;
                if (comment.getId().equals("_")) {
                    text = "Continue this thread ->";
                } else {
                    text = "Load more comments (" + commentChild.getData().getCount() + ")";
                }
                mClickMore.setText(text);
            }

            @Override
            public void onClick(View v) {
                Log.d("more", mComment.toString());
                if (!mComment.getData().getId().equals("_")) {
                    mClickListener.onClick(mComment, mClickedPost.getPostDetails().getName(), getAdapterPosition());
                }
            }


        }

        class ProgressViewHolder extends RecyclerView.ViewHolder {
            final ProgressBar progressBar;
            CommentChild mComment;

            ProgressViewHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            }

            void bind(CommentChild commentChild) {
                this.mComment = commentChild;
            }
        }
    }

    interface CommentClickListener {
        void onClick(CommentChild parentComment, String linkId, int position);
    }

}
