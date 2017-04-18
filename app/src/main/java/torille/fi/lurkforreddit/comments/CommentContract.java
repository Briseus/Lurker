package torille.fi.lurkforreddit.comments;

import java.util.List;

import torille.fi.lurkforreddit.data.models.CommentChild;

/**
 * Created by eva on 2/13/17.
 */

public interface CommentContract {

    interface View {
        void showComments(List<CommentChild> commentChildren);

        void showProgressbarAt(int position, int level);

        void hideProgressbarAt(int position);

        void addCommentsAt(List<CommentChild> comments, int position);

        void showError(String errorText);

        void showErrorAt(int position);

        void setProgressIndicator(boolean active);
    }

    interface UserActionsListener {
        void loadComments(String permaLinkUrl);

        void loadMoreCommentsAt(CommentChild parentComment, String linkId, int position);
    }
}
