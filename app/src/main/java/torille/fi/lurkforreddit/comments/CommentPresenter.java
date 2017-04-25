package torille.fi.lurkforreddit.comments;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * Created by eva on 2/13/17.
 */

public class CommentPresenter implements CommentContract.Presenter<CommentContract.View> {

    private final RedditRepository mRedditRepository;

    private CommentContract.View mCommentsView;

    private final Post mPost;

    @Inject
    CommentPresenter(@NonNull RedditRepository redditRepository,
                     @NonNull Post clickedPost) {
        mRedditRepository = redditRepository;
        mPost = clickedPost;
    }

    @Override
    public void loadComments(@NonNull String permaLinkUrl) {
        mCommentsView.setProgressIndicator(true);
        mRedditRepository.getCommentsForPost(permaLinkUrl, new RedditDataSource.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
                mCommentsView.setProgressIndicator(false);
                mCommentsView.showComments(comments);
            }

            @Override
            public void onMoreCommentsLoaded(List<Comment> comments, int position) {

            }
        }, new RedditDataSource.ErrorCallback() {
            @Override
            public void onError(String errorText) {
                mCommentsView.setProgressIndicator(false);
                mCommentsView.showError(errorText);
            }
        });
    }

    @Override
    public void loadMoreCommentsAt(Comment parentComment, String linkId, final int position) {
        final int level = parentComment.commentLevel();
        mCommentsView.showProgressbarAt(position, level);

        mRedditRepository.getMoreCommentsForPostAt(parentComment, linkId, position, new RedditDataSource.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {

            }

            @Override
            public void onMoreCommentsLoaded(List<Comment> comments, int position) {
                mCommentsView.hideProgressbarAt(position);
                mCommentsView.addCommentsAt(comments, position);
            }
        }, new RedditDataSource.ErrorCallback() {
            @Override
            public void onError(String errorText) {
                mCommentsView.showError(errorText);
                mCommentsView.showErrorAt(position);
            }
        });
    }

    @Override
    public void setView(CommentContract.View view) {
        mCommentsView = view;
    }

    @Override
    public void start() {
        loadComments(mPost.permaLink());
    }
}
