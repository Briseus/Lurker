package torille.fi.lurkforreddit.data

import dagger.Component
import torille.fi.lurkforreddit.RedditRepositoryModule
import torille.fi.lurkforreddit.di.components.NetComponent
import torille.fi.lurkforreddit.di.scope.RedditScope
import torille.fi.lurkforreddit.subreddits.SubredditsActivity

/**
 * Dagger 2 Component that provides access to Reddit API
 */
@RedditScope
@Component(modules = arrayOf(RedditRepositoryModule::class), dependencies = arrayOf(NetComponent::class))
interface RedditRepositoryComponent {

    fun provideRedditRepository(): RedditRepository

    fun inject(activity: SubredditsActivity)
}
