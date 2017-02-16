package torille.fi.lurkforreddit;

import android.app.Application;

import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferencesHelper.init(this);
    }
}
