package torille.fi.lurkforreddit.di.modules

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import torille.fi.lurkforreddit.data.VideositeService

/**
 * Provide module for streamable videos
 */
@Module
class StreamableModule(private val mBaseUrl: String) {

    @Singleton
    @Provides
    internal fun provideStreamableApi(okHttpClient: OkHttpClient,
                                      gsonConverterFactory: GsonConverterFactory,
                                      loggingInterceptor: HttpLoggingInterceptor,
                                      rxJava2CallAdapterFactory: RxJava2CallAdapterFactory): VideositeService.Streamable {
        val retrofit = Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient.newBuilder()
                        .addNetworkInterceptor(loggingInterceptor)
                        .build())
                .build()

        return retrofit.create(VideositeService.Streamable::class.java)
    }

}
