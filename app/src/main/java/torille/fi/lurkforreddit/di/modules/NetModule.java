package torille.fi.lurkforreddit.di.modules;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import torille.fi.lurkforreddit.BuildConfig;
import torille.fi.lurkforreddit.data.models.jsonResponses.MyAdapterFactory;
import torille.fi.lurkforreddit.utils.CommentsStreamingParser;
import torille.fi.lurkforreddit.utils.Store;

/**
 * Created by eva on 22.4.2017.
 */
@Module
public class NetModule {

    public NetModule() {

    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024; //10MiB
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    Store providesSharedPreferences(Application application) {
        return new Store(application.getApplicationContext());
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideLoggingInterceptor() {
        final HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            logger.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        } else {
            logger.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return logger;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        return new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }

    @Provides
    @Singleton
    RxJava2CallAdapterFactory provideRxJava2CallAdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(MyAdapterFactory.create())
                .create();
    }

    @Provides
    @Singleton
    GsonConverterFactory provideGsonTypeAdapter(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    CommentsStreamingParser provideCommentsStreamingParser(Gson gson) {
        return new CommentsStreamingParser(gson);
    }


}
