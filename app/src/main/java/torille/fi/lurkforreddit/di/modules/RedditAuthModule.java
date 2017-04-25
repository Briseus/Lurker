package torille.fi.lurkforreddit.di.modules;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.utils.NetworkHelper;
import torille.fi.lurkforreddit.utils.Store;

/**
 * Created by eva on 22.4.2017.
 */
@Module
public class RedditAuthModule {

    private final String mClientId;
    private final String mBaseUrl;

    public RedditAuthModule(String cliendId, String baseUrl) {
        this.mClientId = cliendId;
        this.mBaseUrl = baseUrl;
    }

    @Provides
    @Singleton
    public RedditService.Auth providesRedditService(GsonConverterFactory gsonConverterFactory,
                                                    OkHttpClient okHttpClient,
                                                    Cache cache,
                                                    HttpLoggingInterceptor logger) {
        final String password = "";
        final String basic = Credentials.basic(mClientId, password);

        Interceptor authHeader = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Accept", "application/json")
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(gsonConverterFactory)
                .client(okHttpClient
                        .newBuilder()
                        .cache(cache)
                        .addNetworkInterceptor(logger)
                        .addNetworkInterceptor(authHeader)
                        .build())
                .build();

        return retrofit.create(RedditService.Auth.class);
    }

    @Provides
    @Singleton
    public NetworkHelper provideNetworkHelper(Store store, RedditService.Auth authApi) {
        return new NetworkHelper(store, authApi);
    }

}
