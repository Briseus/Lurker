package torille.fi.lurkforreddit.di.modules;

import android.support.annotation.NonNull;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.utils.NetworkHelper;
import torille.fi.lurkforreddit.utils.Store;

/**
 * Created by eva on 22.4.2017.
 */
@Module
public class RedditModule {

    private final String mBaseUrl;

    public RedditModule(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    @Provides
    @Singleton
    RedditService.Reddit providesRedditService(GsonConverterFactory gsonConverterFactory,
                                               OkHttpClient okHttpClient,
                                               final Store store,
                                               Cache cache,
                                               HttpLoggingInterceptor logger,
                                               final NetworkHelper networkHelper,
                                               RxJava2CallAdapterFactory rxJava2CallAdapterFactory) {


        final Authenticator authenticator = new Authenticator() {
            @Override
            public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
                if (responseCount(response) >= 3) {
                    Timber.e("Too many retries");
                    return null;
                }
                String newAccessToken = networkHelper.authenticateApp();

                if (newAccessToken.length() < 3) {
                    Timber.e("New token " + newAccessToken + " was too short");
                    return null;
                }
                return response.request().newBuilder()
                        .addHeader("Authorization", "bearer " + newAccessToken)
                        .build();
            }

            int responseCount(Response response) {
                int result = 1;
                while ((response = response.priorResponse()) != null) {
                    result++;
                }
                return result;
            }
        };
        final Interceptor rawJsonInterceptor = chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();

            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("raw_json", "1")
                    .build();

            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);

            Request request = requestBuilder.build();
            return chain.proceed(request);
        };
        final Interceptor tokenInterceptor = chain -> {
            Request original = chain.request();

            if (original.header("Authorization") != null) {
                return chain.proceed(original);
            }

            String token = store.getToken();

            if (token == null || token.isEmpty()) {
                Timber.d("Token was not set, going to get token");
                token = networkHelper.getToken();
                Timber.d("Got new token " + token);
            }

            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "bearer " + token)
                    .method(original.method(), original.body());

            Request request = requestBuilder.build();
            return chain.proceed(request);
        };

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(gsonConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient
                        .newBuilder()
                        .cache(cache)
                        .addNetworkInterceptor(logger)
                        .addNetworkInterceptor(rawJsonInterceptor)
                        .addNetworkInterceptor(tokenInterceptor)
                        .authenticator(authenticator)
                        .build())
                .build();

        return retrofit.create(RedditService.Reddit.class);
    }

}
