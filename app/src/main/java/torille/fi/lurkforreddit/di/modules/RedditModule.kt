package torille.fi.lurkforreddit.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.utils.NetworkHelper
import torille.fi.lurkforreddit.utils.Store
import java.io.IOException
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides retrofit for Reddit API
 */
@Module
class RedditModule(private val baseUrl: String) {

    @Provides
    @Singleton
    @Named("rawJson")
    internal fun providesRawJsonInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val originalHttpUrl = originalRequest.url()

            val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("raw_json", "1")
                    .build()

            val requestBuilder = originalRequest.newBuilder()
                    .url(url)

            val request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    @Named("tokenInterceptor")
    internal fun providesTokenInterceptor(store: Store,
                                          networkHelper: NetworkHelper): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()

            if (original.header("Authorization").isNullOrBlank()) {
                var token: String = store.token

                if (token.isEmpty()) {
                    Timber.d("Token was not set, going to get token")
                    token = networkHelper.getToken()
                    Timber.d("Got new token $token")
                }

                val requestBuilder = original.newBuilder()
                        .header("Authorization", "bearer " + token)
                        .method(original.method(), original.body())

                val request = requestBuilder.build()
                chain.proceed(request)
            } else {
                chain.proceed(original)
            }
        }
    }

    @Provides
    @Singleton
    internal fun providesAuthenticator(networkHelper: NetworkHelper): Authenticator {
        return object : Authenticator {
            @Throws(IOException::class)
            override fun authenticate(route: Route, response: Response): Request? {
                if (responseCount(response) >= 3) {
                    Timber.e("Too many retries")
                    return null
                }
                Timber.d("Fetching new token")
                val newAccessToken = networkHelper.authenticateApp()

                if (newAccessToken.length < 3) {
                    Timber.e("New token $newAccessToken was too short")
                    return null
                }
                return response.request().newBuilder()
                        .addHeader("Authorization", "bearer " + newAccessToken)
                        .build()
            }

            internal fun responseCount(response: Response): Int {
                var result = 1
                while (response == response.priorResponse()) {
                    result++
                }
                return result
            }
        }
    }

    @Provides
    @Singleton
    internal fun providesRedditService(gsonConverterFactory: GsonConverterFactory,
                                       okHttpClient: OkHttpClient,
                                       rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                       authenticator: Authenticator,
                                       @Named("rawJson") rawJsonInterceptor: Interceptor,
                                       @Named("tokenInterceptor") tokenInterceptor: Interceptor)
            : RedditService.Reddit {


        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient
                        .newBuilder()
                        .addInterceptor(rawJsonInterceptor)
                        .addNetworkInterceptor(tokenInterceptor)
                        .authenticator(authenticator)
                        .build())
                .build()

        return retrofit.create(RedditService.Reddit::class.java)
    }

}
