package torille.fi.lurkforreddit.subreddits

import torille.fi.lurkforreddit.BasePresenter
import torille.fi.lurkforreddit.BaseView
import torille.fi.lurkforreddit.data.models.view.Subreddit

/**
 * Binds view and presenter
 */

interface SubredditsContract {

    interface View : BaseView<Presenter> {

        fun setProgressIndicator(active: Boolean)

        fun showSubreddits(subreddits: List<Subreddit>)

        fun loadSelectedSubreddit(subreddit: Subreddit)

        fun onError(errorText: String)

    }

    interface Presenter : BasePresenter<View> {

        fun loadSubreddits(forcedUpdate: Boolean)

        fun openSubreddit(subreddit: Subreddit)

    }

}
