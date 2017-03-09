package torille.fi.lurkforreddit.retrofit;

import android.support.annotation.NonNull;
import android.util.Log;

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
import torille.fi.lurkforreddit.utils.NetworkHelper;

/**
 * Service to create calls to fetch data from Reddit with token refresh
 */

public class RedditService {

    private static final String API_BASE_URL = "https://oauth.reddit.com/";
    private static RedditService instance;
    private RedditClient redditClient;
    private static String TOKEN;

    private final static OkHttpClient okHttpClient = new OkHttpClient();

    private RedditService() {

        final HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        final Authenticator authenticator = new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                if (responseCount(response) >= 3) {
                    Log.e("Authentication", "Too many retries");
                    return null;
                }
                String newAccessToken = NetworkHelper.authenticateApp();
                if (newAccessToken.length() < 3) {
                    Log.e("Authentication", "New token " + newAccessToken + " was too short");
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

                // Request customization: add request headers
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
                /*
                HttpUrl url = original.url().newBuilder().addQueryParameter("raw_json", "1").build();
                original = original.newBuilder().url(url).build();*/

                if (original.header("Authorization") != null) {
                    return chain.proceed(original);
                }
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "bearer " + TOKEN)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient
                        .newBuilder()
                        .addNetworkInterceptor(rawJsonInterceptor)
                        .addNetworkInterceptor(tokenInterceptor)
                        .authenticator(authenticator).build())
                .build();

        redditClient = retrofit.create(RedditClient.class);
    }

    public static RedditClient getInstance(@NonNull String token) {
        TOKEN = token;
        if (instance == null) {
            instance = new RedditService();
        }
        return instance.redditClient;
    }

    public static OkHttpClient getClient() {
        return okHttpClient;
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

}
