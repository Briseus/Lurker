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

    public SharedPreferencesHelper(Context context) {
        mSharedPreferences = context.getApplicationContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        context = context.getApplicationContext();
        mSharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        clientId = context.getResources().getString(R.string.client_id);
    }

    public static String getToken() {
        return mSharedPreferences.getString("token", null);
    }

    public static void setToken(String token) {
        mSharedPreferences.edit().putString("token", token).apply();
    }

    public static void setRefreshToken(String token) {
        mSharedPreferences.edit().putString("refreshtoken", token).apply();
    }

    public static String getRefreshToken() {
        return mSharedPreferences.getString("refreshtoken", null);
    }

    public static Boolean isLoggedIn() {
        return mSharedPreferences.getBoolean("logged_in", false);
    }

    public static void loggedIn(Boolean bool) {
        mSharedPreferences.edit().putBoolean("logged_in", bool).apply();
    }

    public static String getSubreddit(String sub) {
        if (sub != null) {
            return mSharedPreferences.getString(sub, null);
        }
        return "";
    }

    public static void removeSubreddit(String sub) {
        mSharedPreferences.edit().remove(sub).apply();
    }

    public static void storeReddit(String sub, String json) {
        if (sub.equals("multireddits") || sub.equals("subreddits")) {
            mSharedPreferences.edit().putString(sub, json).apply();
        }

    }

    public static String getClientId() {
        return clientId;
    }


}
