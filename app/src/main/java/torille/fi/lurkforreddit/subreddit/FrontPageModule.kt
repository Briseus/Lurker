package torille.fi.lurkforreddit.subreddit

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.di.scope.ActivityScoped

@Module
abstract class FrontPageModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        fun provideSubreddits(): Subreddit {
            return Subreddit(subId = "frontpage", displayName = "Popular")
        }
    }
}
