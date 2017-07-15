package torille.fi.lurkforreddit.subreddits

import dagger.Component
import torille.fi.lurkforreddit.data.RedditRepositoryComponent
import torille.fi.lurkforreddit.di.scope.FragmentScoped

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * [CommentPresenter].
 */
@FragmentScoped
@Component(dependencies = arrayOf(RedditRepositoryComponent::class), modules = arrayOf(SubredditsPresenterModule::class))
interface SubredditsComponent {

    fun inject(fragment: SubredditsFragment)

    fun subredditsPresenter(): SubredditsPresenter

}
