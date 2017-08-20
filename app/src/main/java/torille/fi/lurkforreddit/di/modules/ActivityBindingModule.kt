package torille.fi.lurkforreddit.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.comments.CommentActivity
import torille.fi.lurkforreddit.comments.CommentPresenterModule
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.media.FullscreenFragment
import torille.fi.lurkforreddit.media.FullscreenPresenter
import torille.fi.lurkforreddit.search.SearchPresenterModule
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import torille.fi.lurkforreddit.subreddit.SubredditPresenterModule
import torille.fi.lurkforreddit.subreddit.provideSubreddit
import torille.fi.lurkforreddit.subreddits.SubredditsActivity
import torille.fi.lurkforreddit.subreddits.SubredditsPresenterModule

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(SubredditPresenterModule::class))
    abstract fun subredditActivity(): SubredditActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(SubredditsPresenterModule::class, SearchPresenterModule::class, provideSubreddit::class))
    abstract fun subredditsActivity(): SubredditsActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(CommentPresenterModule::class))
    abstract fun commentActivity(): CommentActivity


}