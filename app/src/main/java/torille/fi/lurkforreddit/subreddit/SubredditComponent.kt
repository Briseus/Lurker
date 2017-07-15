package torille.fi.lurkforreddit.subreddit

import dagger.Component
import torille.fi.lurkforreddit.data.RedditRepositoryComponent
import torille.fi.lurkforreddit.di.scope.FragmentScoped

@FragmentScoped
@Component(dependencies = arrayOf(RedditRepositoryComponent::class), modules = arrayOf(SubredditPresenterModule::class))
interface SubredditComponent {

    fun inject(fragment: SubredditFragment)

}
