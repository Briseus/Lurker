package torille.fi.lurkforreddit.di.components

import com.google.gson.Gson

import javax.inject.Singleton

import dagger.Component
import okhttp3.OkHttpClient
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.VideositeService
import torille.fi.lurkforreddit.di.modules.AppModule
import torille.fi.lurkforreddit.di.modules.NetModule
import torille.fi.lurkforreddit.di.modules.RedditAuthModule
import torille.fi.lurkforreddit.di.modules.RedditModule
import torille.fi.lurkforreddit.di.modules.StreamableModule
import torille.fi.lurkforreddit.media.FullscreenFragment
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import torille.fi.lurkforreddit.subreddits.SubredditsActivity
import torille.fi.lurkforreddit.utils.Store

/**
 * Provides network related component
 */

@Singleton
@Component(modules = arrayOf(AppModule::class, NetModule::class, RedditAuthModule::class, RedditModule::class, StreamableModule::class))
interface NetComponent {

    fun okHttpClient(): OkHttpClient

    fun gson(): Gson

    fun store(): Store

    fun redditAuthApi(): RedditService.Auth

    fun redditApi(): RedditService.Reddit

    fun streamableApi(): VideositeService.Streamable


    fun inject(application: MyApplication)

    fun inject(activity: SubredditsActivity)

    fun inject(activity: SubredditActivity)

    fun inject(fragment: FullscreenFragment)

}
