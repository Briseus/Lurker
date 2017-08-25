package torille.fi.lurkforreddit.subreddit

import torille.fi.lurkforreddit.BasePresenter
import torille.fi.lurkforreddit.BaseView
import torille.fi.lurkforreddit.data.models.view.Post

interface SubredditContract {

    interface View : BaseView<Presenter> {

        fun setProgressIndicator(active: Boolean)

        fun showPosts(posts: List<Post>, nextpage: String)

        fun setListProgressIndicator(active: Boolean)

        fun addMorePosts(posts: List<Post>, nextpage: String)

        fun showCustomTabsUI(url: String)

        fun showMedia(post: Post)

        fun showCommentsUI(clickedPost: Post)

        fun launchCustomActivity(clickedPost: Post)

        fun onError(errorText: String)

        fun setListErrorButton(active: Boolean)
    }

    interface Presenter : BasePresenter<View> {

        fun openComments(clickedPost: Post)

        fun openCustomTabs(url: String)

        fun openMedia(post: Post)

        fun loadPosts(subredditUrl: String)

        fun loadMorePosts()

        fun retry()
    }
}
