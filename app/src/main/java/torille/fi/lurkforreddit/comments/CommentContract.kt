package torille.fi.lurkforreddit.comments

import torille.fi.lurkforreddit.BasePresenter
import torille.fi.lurkforreddit.BaseView
import torille.fi.lurkforreddit.data.models.view.Comment

interface CommentContract {

    interface View : BaseView<Presenter> {

        fun showComments(comments: List<Any>)

        fun showProgressbarAt(position: Int, level: Int)

        fun hideProgressbarAt(position: Int)

        fun addCommentsAt(comments: List<Comment>, position: Int)

        fun showError(errorText: String)

        fun showErrorAt(position: Int)

        fun setProgressIndicator(active: Boolean)
    }

    interface Presenter : BasePresenter<View> {

        fun loadComments(permaLinkUrl: String, isSingleCommentThread: Boolean)

        fun loadMoreCommentsAt(parentComment: Comment, linkId: String, position: Int)
    }
}
