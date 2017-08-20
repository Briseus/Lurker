package torille.fi.lurkforreddit.subreddit

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.di.scope.FragmentScoped
import torille.fi.lurkforreddit.subreddits.SubredditsActivity
import javax.inject.Named

@Module
abstract class SubredditPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun subredditFragment(): SubredditFragment

    @FragmentScoped
    @Binds
    abstract fun subredditPresenter(presenter: SubredditPresenter): SubredditContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        @Named("sub")
        fun provideSubreddit(subredditActivity: SubredditActivity): Subreddit {
            return subredditActivity.intent.getParcelableExtra(SubredditFragment.ARGUMENT_SUBREDDIT)
        }

    }
    /*
    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        @Named("sub")
        fun provideSubreddit(subredditActivity: SubredditActivity): Subreddit {
            return subredditActivity.intent.getParcelableExtra(SubredditFragment.ARGUMENT_SUBREDDIT)
        }

    }
    */

}
