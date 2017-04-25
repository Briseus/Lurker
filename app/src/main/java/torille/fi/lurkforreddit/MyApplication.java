package torille.fi.lurkforreddit;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.DaggerRedditRepositoryComponent;
import torille.fi.lurkforreddit.data.RedditRepositoryComponent;
import torille.fi.lurkforreddit.di.components.DaggerNetComponent;
import torille.fi.lurkforreddit.di.components.NetComponent;
import torille.fi.lurkforreddit.di.modules.AppModule;
import torille.fi.lurkforreddit.di.modules.NetModule;
import torille.fi.lurkforreddit.di.modules.RedditAuthModule;
import torille.fi.lurkforreddit.di.modules.RedditModule;
import torille.fi.lurkforreddit.di.modules.StreamableModule;

public class MyApplication extends Application {

    private RedditRepositoryComponent mRedditRepositoryComponent;
    private NetComponent mNetComponent;

    @Inject
    OkHttpClient mOkHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        String client_id = getResources().getString(R.string.client_id);

        mNetComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule())
                .redditAuthModule(new RedditAuthModule(client_id, "https://www.reddit.com/api/v1/"))
                .redditModule(new RedditModule("https://oauth.reddit.com/"))
                .streamableModule(new StreamableModule("https://api.streamable.com/"))
                .build();

        mNetComponent.inject(this);

        mRedditRepositoryComponent = DaggerRedditRepositoryComponent.builder()
                .netComponent(mNetComponent)
                .build();

        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, mOkHttpClient)
                .setDownsampleEnabled(true)
                .build();

        Fresco.initialize(this, config);


        //SharedPreferencesHelper.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

    }

    public RedditRepositoryComponent getmRedditRepositoryComponent() {
        return mRedditRepositoryComponent;
    }

    public NetComponent getmNetComponent() {
        return mNetComponent;
    }
}
