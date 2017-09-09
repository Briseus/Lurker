package torille.fi.lurkforreddit.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
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
class RedditModule(private val mBaseUrl: String) {

    @Provides
    @Singleton
    @Named("rawJson")
    internal fun providesRawJsonInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val originalHttpUrl = original.url()

            val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("raw_json", "1")
                    .build()

            val requestBuilder = original.newBuilder()
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

            if (original.header("Authorization") != null) {
                chain.proceed(original)
            } else {
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
                @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
                var newResponse: Response? = response
                var result = 1
                while ({ newResponse = response.priorResponse(); newResponse }() != null) {
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
                                       cache: Cache,
                                       logger: HttpLoggingInterceptor,
                                       rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                       authenticator: Authenticator,
                                       @Named("rawJson") rawJsonInterceptor: Interceptor,
                                       @Named("tokenInterceptor") tokenInterceptor: Interceptor)
            : RedditService.Reddit {


        val retrofit = Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient
                        .newBuilder()
                        .cache(cache)
                        .addInterceptor(logger)
                        .addInterceptor(rawJsonInterceptor)
                        .addNetworkInterceptor(tokenInterceptor)
                        .authenticator(authenticator)
                        .build())
                .build()

        return retrofit.create(RedditService.Reddit::class.java)
    }

}
