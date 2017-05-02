package torille.fi.lurkforreddit.comments;

import java.util.List;

import torille.fi.lurkforreddit.BasePresenter;
import torille.fi.lurkforreddit.BaseView;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * Created by eva on 2/13/17.
 */

public interface CommentContract {

    interface View extends BaseView {

        void showComments(List<Object> comments);

        void showProgressbarAt(int position, int level);

        void hideProgressbarAt(int position);

        void addCommentsAt(List<Comment> comments, int position);

        void showError(String errorText);

        void showErrorAt(int position);

        void setProgressIndicator(boolean active);
    }

    interface Presenter<T extends BaseView> extends BasePresenter<CommentContract.View> {

        void loadComments(String permaLinkUrl, boolean isSingleCommentThread);

        void loadMoreCommentsAt(Comment parentComment, String linkId, int position);
    }
}
