package torille.fi.lurkforreddit.search

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.di.scope.FragmentScoped

@Module
abstract class SearchPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun searchFragment(): SearchFragment

    @FragmentScoped
    @Binds
    abstract fun searchPresenter(searchPresenter: SearchPresenter): SearchContract.Presenter

}
