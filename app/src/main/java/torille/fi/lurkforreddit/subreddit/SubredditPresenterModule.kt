package torille.fi.lurkforreddit.subreddit

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.Subreddit

@Module
class SubredditPresenterModule(private val mSubreddit: Subreddit) {

    @Provides
    fun provideSubredditPresenter(redditRepository: RedditRepository,
                                  subreddit: Subreddit): SubredditContract.Presenter<SubredditContract.View> {
        return SubredditPresenter(redditRepository, subreddit)
    }

    @Provides
    internal fun provideSubreddit(): Subreddit {
        return mSubreddit
    }

}
