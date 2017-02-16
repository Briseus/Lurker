package torille.fi.lurkforreddit.comments;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.CommentChild;
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
        mRedditRepository.getCommentsForPost(permaLinkUrl, new RedditRepository.LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<CommentChild> commentChildren) {
                mCommentsView.showComments(commentChildren);
            }
        });
    }
}
