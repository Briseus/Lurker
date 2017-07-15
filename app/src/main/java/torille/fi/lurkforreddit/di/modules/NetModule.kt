package torille.fi.lurkforreddit.di.modules

import android.app.Application

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import torille.fi.lurkforreddit.BuildConfig
import torille.fi.lurkforreddit.utils.CommentsStreamingParser
import torille.fi.lurkforreddit.utils.Store

/**
 * Provides shared network classes
 */
@Module
class NetModule {

    @Provides
    @Singleton
    internal fun provideOkHttpCache(application: Application): Cache {
        val cacheSize = 10 * 1024 * 1024 //10MiB
        return Cache(application.cacheDir, cacheSize.toLong())
    }

    @Provides
    @Singleton
    internal fun providesSharedPreferences(application: Application): Store {
        return Store(application.applicationContext)
    }

    @Provides
    @Singleton
    internal fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logger.level = HttpLoggingInterceptor.Level.HEADERS
        } else {
            logger.level = HttpLoggingInterceptor.Level.NONE
        }
        return logger
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(cache)
                .build()
    }

    @Provides
    @Singleton
    internal fun provideRxJava2CallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.createAsync()
    }

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        return GsonBuilder()
                .create()
    }

    @Provides
    @Singleton
    internal fun provideGsonTypeAdapter(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @Provides
    @Singleton
    internal fun provideCommentsStreamingParser(gson: Gson): CommentsStreamingParser {
        return CommentsStreamingParser(gson)
    }


}
