package torille.fi.lurkforreddit.utils;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
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
            return mAuthApi.refreshUserToken(type, mStore.getRefreshToken()).map(redditToken -> {
                String accessToken = redditToken.access_token();
                Timber.d("New token: " + accessToken);
                mStore.setToken(accessToken);
                return accessToken;
            }).blockingSingle();

        } else {
            Timber.d("User was not logged in");
            return getToken();
        }
    }

    public String getToken() throws IOException {

        final String UUID = createUUID();
        final String grant_type = "https://oauth.reddit.com/grants/installed_client";
        Timber.d("Getting token");
        return mAuthApi.getAuthToken(grant_type, UUID).map(redditToken -> {
            final String access_token = redditToken.access_token();
            Timber.d("Got new token " + access_token);
            mStore.setToken(access_token);
            return  access_token;
        }).blockingSingle();
    }


}
