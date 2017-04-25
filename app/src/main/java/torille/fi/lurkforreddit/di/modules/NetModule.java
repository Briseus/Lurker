package torille.fi.lurkforreddit.di.modules;

import android.app.Application;

import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;
import torille.fi.lurkforreddit.BuildConfig;
import torille.fi.lurkforreddit.data.models.jsonResponses.MyAdapterFactory;
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
    GsonConverterFactory provideGsonTypeAdapter() {
        return GsonConverterFactory.create(
                new GsonBuilder()
                        .registerTypeAdapterFactory(MyAdapterFactory.create())
                        .create());
    }


}
