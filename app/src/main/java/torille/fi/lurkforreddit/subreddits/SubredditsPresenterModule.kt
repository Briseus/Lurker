package torille.fi.lurkforreddit.subreddits

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.di.scope.FragmentScoped


/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [SubredditsPresenter].
 */
@Module
abstract class SubredditsPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun subredditsFragment(): SubredditsFragment

    @FragmentScoped
    @Binds
    abstract fun subredditsPresenter(presenter: SubredditsPresenter): SubredditsContract.Presenter

}
