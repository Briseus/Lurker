package torille.fi.lurkforreddit.subreddit

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.di.scope.FragmentScoped

@Module
abstract class SubredditPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun subredditFragment(): SubredditFragment

    @FragmentScoped
    @Binds
    abstract fun subredditPresenter(presenter: SubredditPresenter): SubredditContract.Presenter

}
