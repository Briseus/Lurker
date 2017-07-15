package torille.fi.lurkforreddit.comments

import dagger.Component
import torille.fi.lurkforreddit.data.RedditRepositoryComponent
import torille.fi.lurkforreddit.di.scope.FragmentScoped

/**
 * Dagger 2 component that provides [RedditRepositoryComponent]
 */
@FragmentScoped
@Component(dependencies = arrayOf(RedditRepositoryComponent::class), modules = arrayOf(CommentPresenterModule::class))
interface CommentComponent {

    fun inject(fragment: CommentFragment)

}
