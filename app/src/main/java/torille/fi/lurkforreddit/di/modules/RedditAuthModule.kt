package torille.fi.lurkforreddit.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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
class RedditAuthModule(private val clientId: String, private val baseUrl: String) {

    @Provides
    @Singleton
    internal fun providesRedditService(gsonConverterFactory: GsonConverterFactory,
                                       rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                       okHttpClient: OkHttpClient): RedditService.Auth {

        val basic = Credentials.basic(clientId, "")

        val authHeader = Interceptor { chain ->
            val originalRequest = chain.request()

            val requestBuilder = originalRequest.newBuilder()
                    .header("Authorization", basic)
                    .header("Accept", "application/json")
                    .method(originalRequest.method(), originalRequest.body())

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val okHttpInstance = okHttpClient
                .newBuilder()
                .addNetworkInterceptor(authHeader)
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
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
