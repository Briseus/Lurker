package torille.fi.lurkforreddit.retrofit;

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
    public static final String API_BASE_URL = "https://oauth.reddit.com/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass, final String token) {
        if (token != null) {

            httpClient.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {

                    Request original = chain.request();
                    HttpUrl url = original.url().newBuilder().addQueryParameter("raw_json", "1").build();
                    original = original.newBuilder().url(url).build();

                    if (original.header("Authorization") != null) {
                        return chain.proceed(original);
                    }
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "bearer " + token)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        final HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient client = httpClient
                .addInterceptor(logger)
                .authenticator(new Authenticator() {
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
                }).build();

        Retrofit retrofit = builder.client(client).build();

        return retrofit.create(serviceClass);
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
