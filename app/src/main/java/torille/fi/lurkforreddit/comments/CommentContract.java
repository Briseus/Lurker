package torille.fi.lurkforreddit.comments;

import java.util.List;

import torille.fi.lurkforreddit.data.CommentChild;

/**
 * Created by eva on 2/13/17.
 */

public interface CommentContract {

    interface View {
        void showComments(List<CommentChild> commentChildren);
    }

    interface UserActionsListener {
        void loadComments(String permaLinkUrl);
    }
}
