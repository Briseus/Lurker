package torille.fi.lurkforreddit.search

import dagger.Component
import torille.fi.lurkforreddit.data.RedditRepositoryComponent
import torille.fi.lurkforreddit.di.scope.FragmentScoped

@FragmentScoped
@Component(dependencies = arrayOf(RedditRepositoryComponent::class), modules = arrayOf(SearchPresenterModule::class))
interface SearchComponent {

    fun inject(searchFragment: SearchFragment)

    fun searchPresenter(): SearchPresenter

}
