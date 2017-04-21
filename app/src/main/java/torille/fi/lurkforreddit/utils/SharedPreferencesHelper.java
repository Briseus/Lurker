package torille.fi.lurkforreddit.utils;

import android.content.Context;
import android.content.SharedPreferences;

import torille.fi.lurkforreddit.R;

/**
 * Helper to store and get data from {@link SharedPreferences}
 */

public class SharedPreferencesHelper {

    private static SharedPreferences mSharedPreferences;
    private static String clientId;

    private static final String CONFIG = "config";
    private static final String TOKEN = "token";
    private static final String REFRESH_TOKEN = "refreshtoken";
    private static final String LOGGED_IN = "logged_in";

    private SharedPreferencesHelper() {
    }

    public static void init(Context context) {
        context = context.getApplicationContext();
        mSharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        clientId = context.getResources().getString(R.string.client_id);
    }

    public static String getToken() {
        String token = mSharedPreferences.getString(TOKEN, null);
        if (token == null) {
            return "";
        }
        return token;
    }

    public static void setToken(String token) {
        mSharedPreferences.edit().putString(TOKEN, token).apply();
    }

    public static void setRefreshToken(String token) {
        mSharedPreferences.edit().putString(REFRESH_TOKEN, token).apply();
    }

    public static String getRefreshToken() {
        return mSharedPreferences.getString(REFRESH_TOKEN, null);
    }

    public static Boolean isLoggedIn() {
        return mSharedPreferences.getBoolean(LOGGED_IN, false);
    }

    public static void loggedIn(Boolean bool) {
        mSharedPreferences.edit().putBoolean(LOGGED_IN, bool).apply();
    }

    public static String getClientId() {
        return clientId;
    }


}
