package torille.fi.lurkforreddit;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import timber.log.Timber;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, RedditService.getClient())
                .setDownsampleEnabled(true)
                .build();

        Fresco.initialize(this, config);

        SharedPreferencesHelper.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
