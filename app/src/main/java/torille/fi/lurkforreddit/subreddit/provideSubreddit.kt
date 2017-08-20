package torille.fi.lurkforreddit.subreddit

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.di.scope.FragmentScoped
import torille.fi.lurkforreddit.subreddits.SubredditsActivity
import javax.inject.Named

/**
 * Created by eva on 19.8.2017.
 */
@Module
abstract class provideSubreddit {

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
        fun provideSubreddit(subredditsActivity: SubredditsActivity): Subreddit {
            return subredditsActivity.intent.getParcelableExtra(SubredditFragment.ARGUMENT_SUBREDDIT)
        }

    }
}
