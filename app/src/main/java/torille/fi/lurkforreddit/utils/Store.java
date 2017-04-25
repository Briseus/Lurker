package torille.fi.lurkforreddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by eva on 23.4.2017.
 */
@Singleton
final public class Store {

    private SharedPreferences mSharedPreferences;

    private static final String TOKEN = "token";
    private static final String REFRESH_TOKEN = "refreshtoken";
    private static final String LOGGED_IN = "logged_in";

    @Inject
    public Store(@NonNull Context context) {
        context = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getToken() {
        String token = mSharedPreferences.getString(TOKEN, null);
        if (token == null) {
            return "";
        }
        return token;
    }

    public void setToken(String token) {
        mSharedPreferences.edit().putString(TOKEN, token).apply();
    }

    public void setRefreshToken(String token) {
        mSharedPreferences.edit().putString(REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return mSharedPreferences.getString(REFRESH_TOKEN, null);
    }

    public Boolean isLoggedIn() {
        return mSharedPreferences.getBoolean(LOGGED_IN, false);
    }

    public void loggedIn(Boolean loggedIn) {
        mSharedPreferences.edit().putBoolean(LOGGED_IN, loggedIn).apply();
    }
}
