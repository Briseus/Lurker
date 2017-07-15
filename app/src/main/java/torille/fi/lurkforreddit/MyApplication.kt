package torille.fi.lurkforreddit

import android.app.Application
import android.content.ComponentCallbacks2
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.squareup.leakcanary.LeakCanary
import okhttp3.OkHttpClient
import timber.log.Timber
import torille.fi.lurkforreddit.data.DaggerRedditRepositoryComponent
import torille.fi.lurkforreddit.data.RedditRepositoryComponent
import torille.fi.lurkforreddit.di.components.DaggerNetComponent
import torille.fi.lurkforreddit.di.components.NetComponent
import torille.fi.lurkforreddit.di.modules.*
import javax.inject.Inject

class MyApplication : Application() {

    private lateinit var mRedditRepositoryComponent: RedditRepositoryComponent
    private lateinit var mNetComponent: NetComponent

    @Inject
    internal lateinit var mOkHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        val client_id = resources.getString(R.string.client_id)

        mNetComponent = DaggerNetComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule())
                .redditAuthModule(RedditAuthModule(client_id, "https://www.reddit.com/api/v1/"))
                .redditModule(RedditModule("https://oauth.reddit.com/"))
                .streamableModule(StreamableModule("https://api.streamable.com/"))
                .build()

        mNetComponent.inject(this)

        mRedditRepositoryComponent = DaggerRedditRepositoryComponent.builder()
                .netComponent(mNetComponent)
                .build()

        val config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, mOkHttpClient)
                .setResizeAndRotateEnabledForNetwork(true)
                .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
                .setDownsampleEnabled(true)
                .build()

        Fresco.initialize(this, config)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // App went to background
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            Timber.d("App went to background, clearing fresco memory cache")
            // Fresco recommends to clear bitmap cache when app is backgrounded
            Fresco.getImagePipeline().clearMemoryCaches()
        }
    }

    fun getmRedditRepositoryComponent(): RedditRepositoryComponent {
        return mRedditRepositoryComponent
    }

    fun getmNetComponent(): NetComponent {
        return mNetComponent
    }
}
