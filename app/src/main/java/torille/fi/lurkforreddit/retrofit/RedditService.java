package torille.fi.lurkforreddit.retrofit;

import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import torille.fi.lurkforreddit.BuildConfig;
import torille.fi.lurkforreddit.data.models.jsonResponses.MyAdapterFactory;
import torille.fi.lurkforreddit.utils.NetworkHelper;
import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;

/**
 * Service to create calls to fetch data from Reddit with token refresh
 */

public class RedditService {

    private static final String API_BASE_URL = "https://oauth.reddit.com/";
    private static RedditService instance;
    private final RedditClient redditClient;

    private final static GsonConverterFactory gsonFactory = GsonConverterFactory.create(
            new GsonBuilder()
                    .registerTypeAdapterFactory(MyAdapterFactory.create())
                    .create());

    private final static OkHttpClient okHttpClient = new OkHttpClient();

    private RedditService() {

        final HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logger.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        } else {
            logger.setLevel(HttpLoggingInterceptor.Level.NONE);
        }


        final Authenticator authenticator = new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                if (responseCount(response) >= 3) {
                    Timber.e("Too many retries");
                    return null;
                }
                String newAccessToken = NetworkHelper.authenticateApp();
                if (newAccessToken.length() < 3) {
                    Timber.e("New token " + newAccessToken + " was too short");
                    return null;
                }
                return response.request().newBuilder()
                        .addHeader("Authorization", "bearer " + newAccessToken)
                        .build();
            }
        };
        final Interceptor rawJsonInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("raw_json", "1")
                        .build();

                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
        final Interceptor tokenInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                if (original.header("Authorization") != null) {
                    return chain.proceed(original);
                }

                String token = SharedPreferencesHelper.getToken();

                if (token == null || token.isEmpty()) {
                    Timber.d("Token was not set, going to get token");
                    token = NetworkHelper.getToken();
                    Timber.d("Got new token " + token);
                }

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "bearer " + token)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(gsonFactory)
                .client(okHttpClient
                        .newBuilder()
                        .addNetworkInterceptor(logger)
                        .addNetworkInterceptor(rawJsonInterceptor)
                        .addNetworkInterceptor(tokenInterceptor)
                        .authenticator(authenticator).build())
                .build();

        redditClient = retrofit.create(RedditClient.class);
    }

    public static RedditClient getInstance() {

        if (instance == null) {
            instance = new RedditService();
        }
        return instance.redditClient;
    }

    public static OkHttpClient getClient() {
        return okHttpClient;
    }

    public static GsonConverterFactory getGsonFactory() {
        return gsonFactory;
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

}
