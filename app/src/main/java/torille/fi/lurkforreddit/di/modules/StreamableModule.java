package torille.fi.lurkforreddit.di.modules;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import torille.fi.lurkforreddit.data.StreamableService;

/**
 * Created by eva on 25.4.2017.
 */
@Module
public class StreamableModule {

    private String mBaseUrl;

    public StreamableModule(@NonNull String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    @Singleton
    @Provides
    StreamableService provideStreamableApi(OkHttpClient okHttpClient,
                                           GsonConverterFactory gsonConverterFactory,
                                           HttpLoggingInterceptor loggingInterceptor) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(gsonConverterFactory)
                .client(okHttpClient.newBuilder()
                        .addNetworkInterceptor(loggingInterceptor)
                        .build())
                .build();

        return retrofit.create(StreamableService.class);
    }

}
