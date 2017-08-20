package torille.fi.lurkforreddit.subreddits

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.di.scope.FragmentScoped
import torille.fi.lurkforreddit.subreddit.SubredditFragment
import javax.inject.Named


/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [SubredditsPresenter].
 */
@Module
abstract class SubredditsPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun subredditsFragment(): SubredditsFragment

    @ActivityScoped
    @Binds
    abstract fun subredditsPresenter(presenter: SubredditsPresenter): SubredditsContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        @Named("subs")
        fun provideSubreddits(subredditsActivity: SubredditsActivity): Subreddit {
            return subredditsActivity.intent.getParcelableExtra(SubredditFragment.ARGUMENT_SUBREDDIT)
        }
    }
}
