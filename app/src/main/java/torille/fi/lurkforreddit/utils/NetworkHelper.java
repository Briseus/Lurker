package torille.fi.lurkforreddit.utils;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.models.jsonResponses.RedditToken;
import torille.fi.lurkforreddit.data.RedditService;

import static java.util.UUID.randomUUID;

/**
 * Helper for creating auth calls
 */
@Singleton
final public class NetworkHelper {

    private final Store mStore;

    private final RedditService.Auth mAuthApi;

    @Inject
    public NetworkHelper(@NonNull Store store,
                         @NonNull RedditService.Auth api) {
        mStore = store;
        mAuthApi = api;
    }

    private static String createUUID() {
        return randomUUID().toString();
    }

    public static String nextStateId() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public String authenticateApp() throws IOException {

        if (mStore.isLoggedIn()) {
            Timber.d("Was logged in as user, refreshing token");
            Timber.d("Using refreshtoken: " + mStore.getRefreshToken());
            final String type = "refresh_token";
            final Call<RedditToken> call = mAuthApi.refreshUserToken(type, mStore.getRefreshToken());

            Response<RedditToken> response = call.execute();
            if (response.isSuccessful()) {
                Timber.d("Got response: " + response.body().toString());
                String accessToken = response.body().access_token();
                Timber.d("New token: " + accessToken);
                mStore.setToken(accessToken);
                return accessToken;
            } else {
                Timber.e("Got something else as response " + response.errorBody().string());
                return "";
            }

        } else {

            Timber.d("User was not logged in");
            final String UUID = createUUID();
            final String grant_type = "https://oauth.reddit.com/grants/installed_client";
            final Call<RedditToken> call = mAuthApi.getAuthToken(grant_type, UUID);

            Response<RedditToken> response = call.execute();
            if (response.isSuccessful()) {
                final String access_token = response.body().access_token();
                mStore.setToken(access_token);
                Timber.d("Got new token " + access_token);
                return access_token;
            } else {
                Timber.e("Got something else as response " + response.errorBody().string());
                return "";
            }

        }
    }

    public String getToken() throws IOException {

        final String UUID = createUUID();
        final String grant_type = "https://oauth.reddit.com/grants/installed_client";
        Call<RedditToken> call = mAuthApi.getAuthToken(grant_type, UUID);
        Timber.d("Getting token");
        Response<RedditToken> response = call.execute();
        if (response.isSuccessful()) {
            final String access_token = response.body().access_token();
            mStore.setToken(access_token);
            Timber.d("Got new token " + access_token);
            return access_token;
        } else {
            Timber.e("Got something else as response " + response.errorBody().string());
            return "";
        }
    }


}
