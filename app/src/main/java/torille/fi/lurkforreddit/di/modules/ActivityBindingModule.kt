package torille.fi.lurkforreddit.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.comments.CommentActivity
import torille.fi.lurkforreddit.comments.CommentPresenterModule
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.media.FullscreenActivity
import torille.fi.lurkforreddit.media.FullscreenPresenterModule
import torille.fi.lurkforreddit.search.SearchPresenterModule
import torille.fi.lurkforreddit.subreddit.FrontPageModule
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import torille.fi.lurkforreddit.subreddit.SubredditModule
import torille.fi.lurkforreddit.subreddit.SubredditPresenterModule
import torille.fi.lurkforreddit.subreddits.SubredditsActivity
import torille.fi.lurkforreddit.subreddits.SubredditsPresenterModule
import torille.fi.lurkforreddit.utils.AppLinkActivity

@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(SubredditPresenterModule::class, SubredditModule::class))
    abstract fun subredditActivity(): SubredditActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(SubredditsPresenterModule::class, SearchPresenterModule::class, SubredditPresenterModule::class, FrontPageModule::class))
    abstract fun subredditsActivity(): SubredditsActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(CommentPresenterModule::class))
    abstract fun commentActivity(): CommentActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = arrayOf(FullscreenPresenterModule::class))
    abstract fun fullscreenActivity(): FullscreenActivity

    @ActivityScoped
    @ContributesAndroidInjector()
    abstract fun appLinkActivity(): AppLinkActivity
}