package torille.fi.lurkforreddit.comments;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Comment;

/**
 * Created by eva on 2/13/17.
 */

class CommentPresenter implements CommentContract.UserActionsListener {

    private final RedditRepository mRedditRepository;

    private final CommentContract.View mCommentsView;

    CommentPresenter(@NonNull RedditRepository redditRepository,
                     @NonNull CommentContract.View commentsView) {
        mRedditRepository = redditRepository;
        mCommentsView = commentsView;
    }

    @Override
    public void loadComments(@NonNull String permaLinkUrl) {
        mCommentsView.setProgressIndicator(true);
        mRedditRepository.getCommentsForPost(permaLinkUrl, new RedditRepository.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
                mCommentsView.setProgressIndicator(false);
                mCommentsView.showComments(comments);
            }

            @Override
            public void onMoreCommentsLoaded(List<Comment> comments, int position) {

            }
        }, new RedditRepository.ErrorCallback() {
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

        mRedditRepository.getMoreCommentsForPostAt(parentComment, linkId, position, new RedditRepository.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {

            }

            @Override
            public void onMoreCommentsLoaded(List<Comment> comments, int position) {
                mCommentsView.hideProgressbarAt(position);
                mCommentsView.addCommentsAt(comments, position);
            }
        }, new RedditRepository.ErrorCallback() {
            @Override
            public void onError(String errorText) {
                mCommentsView.showError(errorText);
                mCommentsView.showErrorAt(position);
            }
        });
    }
}
