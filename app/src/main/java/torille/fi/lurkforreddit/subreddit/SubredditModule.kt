package torille.fi.lurkforreddit.subreddit

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.di.scope.ActivityScoped


@Module
abstract class SubredditModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        fun provideSubreddit(subredditActivity: SubredditActivity): Subreddit {
            return subredditActivity.intent.getParcelableExtra(SubredditFragment.ARGUMENT_SUBREDDIT)
        }

    }
}