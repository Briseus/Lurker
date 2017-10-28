package torille.fi.lurkforreddit

import android.content.ComponentCallbacks2
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.DefaultExecutorSupplier
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import okhttp3.OkHttpClient
import timber.log.Timber
import torille.fi.lurkforreddit.di.components.AppComponent
import torille.fi.lurkforreddit.di.components.DaggerAppComponent
import torille.fi.lurkforreddit.di.modules.*
import javax.inject.Inject

class MyApplication : DaggerApplication() {

    @Inject
    internal lateinit var okHttpClient: OkHttpClient

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val clientId = resources.getString(R.string.client_id)
        val appComponent: AppComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .netModule(NetModule())
                .redditAuthModule(RedditAuthModule(clientId, "https://www.reddit.com/api/v1/"))
                .redditModule(RedditModule("https://oauth.reddit.com/"))
                .streamableModule(StreamableModule("https://api.streamable.com/"))
                .redditRepositoryModule(RedditRepositoryModule())
                .build()
        appComponent.inject(this)
        return appComponent
    }

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        val config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, okHttpClient)
                .setResizeAndRotateEnabledForNetwork(false)
                .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
                .setDownsampleEnabled(true)
                .setExecutorSupplier(DefaultExecutorSupplier(Runtime.getRuntime().availableProcessors()))
                .experiment().setWebpSupportEnabled(true)
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

}
