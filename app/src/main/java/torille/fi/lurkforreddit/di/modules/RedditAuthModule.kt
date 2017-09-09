package torille.fi.lurkforreddit.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.utils.NetworkHelper
import torille.fi.lurkforreddit.utils.Store
import javax.inject.Singleton

/**
 * Dagger 2 Module for Reddit Auth API
 */
@Module
class RedditAuthModule(private val mClientId: String, private val mBaseUrl: String) {

    @Provides
    @Singleton
    internal fun providesRedditService(gsonConverterFactory: GsonConverterFactory,
                                       rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                       okHttpClient: OkHttpClient,
                                       cache: Cache,
                                       logger: HttpLoggingInterceptor): RedditService.Auth {

        val basic = Credentials.basic(mClientId, "")


        val authHeader = Interceptor { chain ->
            val original = chain.request()

            val requestBuilder = original.newBuilder()
                    .header("Authorization", basic)
                    .header("Accept", "application/json")
                    .method(original.method(), original.body())

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val okHttpInstance = okHttpClient
                .newBuilder()
                .cache(cache)
                .addNetworkInterceptor(logger)
                .addNetworkInterceptor(authHeader)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpInstance)
                .build()

        return retrofit.create(RedditService.Auth::class.java)
    }

    @Provides
    @Singleton
    internal fun provideNetworkHelper(store: Store, authApi: RedditService.Auth): NetworkHelper {
        return NetworkHelper(store, authApi)
    }

}
