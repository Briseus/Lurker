package torille.fi.lurkforreddit.comments;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.RedditRepository;

/**
 * Created by eva on 2/13/17.
 */

public class CommentPresenter implements CommentContract.UserActionsListener {

    private final RedditRepository mRedditRepository;

    private final CommentContract.View mCommentsView;

    public CommentPresenter(@NonNull RedditRepository redditRepository,
                            @NonNull CommentContract.View commentsView) {
        mRedditRepository = redditRepository;
        mCommentsView = commentsView;
    }

    @Override
    public void loadComments(@NonNull String permaLinkUrl) {
        mCommentsView.showProgressbarAt(1, 0);
        mRedditRepository.getCommentsForPost(permaLinkUrl, new RedditRepository.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<CommentChild> commentChildren) {
                mCommentsView.hideProgressbarAt(1);
                mCommentsView.showComments(commentChildren);
            }

            @Override
            public void onMoreCommentsLoaded(List<CommentChild> comments, int position) {

            }
        }, new RedditRepository.ErrorCallback() {
            @Override
            public void onError(String errorText) {
                mCommentsView.hideProgressbarAt(1);
                mCommentsView.showError(errorText);
            }
        });
    }

    @Override
    public void loadMoreCommentsAt(CommentChild parentComment, String linkId, final int position) {
        final int level = parentComment.getType();
        mCommentsView.showProgressbarAt(position, level);

        mRedditRepository.getMoreCommentsForPostAt(parentComment, linkId, position, new RedditRepository.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<CommentChild> commentChildren) {

            }

            @Override
            public void onMoreCommentsLoaded(List<CommentChild> comments, int position) {
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
