package torille.fi.lurkforreddit.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import torille.fi.lurkforreddit.BuildConfig;
import torille.fi.lurkforreddit.data.models.jsonResponses.StreamableVideo;

/**
 * Created by eva on 9.4.2017.
 */


public class StreamableService {

    private static final String API_BASE_URL = "https://api.streamable.com/";
    private static StreamableService instance;
    private final Streamable client;

    private StreamableService() {
        final HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logger.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        } else {
            logger.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        OkHttpClient okHttpClient = RedditService.getClient()
                .newBuilder()
                .addNetworkInterceptor(logger)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(RedditService.getGsonFactory())
                .client(okHttpClient)
                .build();

        client = retrofit.create(Streamable.class);

    }

    public static Streamable getInstance() {
        if (instance == null) {
            instance = new StreamableService();
        }
        return instance.client;
    }

    public interface Streamable {
        @GET("videos/{identifier}")
        Call<StreamableVideo> getVideo(@Path("identifier") String identifier);
    }

}

