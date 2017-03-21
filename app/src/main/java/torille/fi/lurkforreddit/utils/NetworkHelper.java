package torille.fi.lurkforreddit.utils;

import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import retrofit2.Call;
import retrofit2.Response;
import torille.fi.lurkforreddit.retrofit.RedditAuthService;
import torille.fi.lurkforreddit.retrofit.RedditClient;
import torille.fi.lurkforreddit.data.models.RedditToken;

import static java.util.UUID.randomUUID;

/**
 * Helper for creating auth calls
 */

final public class NetworkHelper {

    private NetworkHelper() {}

    private static String createUUID() {
        return randomUUID().toString();
    }

    public static String nextStateId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public static String authenticateApp() {
        final String client_id = SharedPreferencesHelper.getClientId();
        RedditClient redditClient = RedditAuthService.createService(RedditClient.class, client_id, "");
        // not is user logged in or not
        if (SharedPreferencesHelper.isLoggedIn()) {
            Log.d("Authentication", "Was logged in as user, refreshing token");
            Log.d("Authentication", "Using refreshtoken: " + SharedPreferencesHelper.getRefreshToken());
            final String type = "refresh_token";
            final Call<RedditToken> call = redditClient.refreshUserToken(type, SharedPreferencesHelper.getRefreshToken());
            try {
                Response<RedditToken> response = call.execute();
                if (response.isSuccessful()) {
                    SharedPreferencesHelper.setToken(response.body().getAccess_token());
                    Log.d("Authentication", "Got response: " + response.body().toString());
                    Log.d("Authentication", "New token: " + response.body().getAccess_token());
                    return response.body().getAccess_token();
                } else {
                    Log.e("Authentication", "Got something else as response " + response.errorBody().string());
                    return "";
                }
            } catch (IOException e) {
                Log.e("Authentication", "Io error while refreshing token " + e.getCause());
                return "";
            }
        } else {
            Log.d("Authentication", "Was not logged in as user");
            final String UUID = createUUID();
            final String grant_type = "https://oauth.reddit.com/grants/installed_client";

            final Call<RedditToken> call = redditClient.getAuthToken(grant_type, UUID);
            try {
                Response<RedditToken> response = call.execute();
                if (response.isSuccessful()) {
                    final String access_token = response.body().getAccess_token();
                    SharedPreferencesHelper.setToken(access_token);
                    Log.d("Authentication", "Got new token " + access_token);
                    return access_token;
                } else {
                    Log.e("Authentication", "Got something else as response " + response.errorBody().string());
                    return "";
                }
            } catch (IOException e) {
                Log.e("Authentication", "Io error while refreshing token " + e.getCause());
            }
        }

        return "";
    }

    public static Call<RedditToken> createAuthCall() {

        final String UUID = NetworkHelper.createUUID();
        final String grant_type = "https://oauth.reddit.com/grants/installed_client";
        final String client_id = SharedPreferencesHelper.getClientId();
        Log.d("Authentication", "Creating auth call client id " + client_id);
        final RedditClient redditClient = RedditAuthService.createService(RedditClient.class, client_id, "");
        return redditClient.getAuthToken(grant_type, UUID);
    }




}
