package torille.fi.lurkforreddit.subreddits

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.comments.CommentPresenter
import torille.fi.lurkforreddit.data.RedditRepository

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [CommentPresenter].
 */
@Module
class SubredditsPresenterModule {

    @Provides
    fun provideSubredditsPresenter(redditRepository: RedditRepository): SubredditsContract.Presenter<SubredditsContract.View> {
        return SubredditsPresenter(redditRepository)
    }

}
