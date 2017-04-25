package torille.fi.lurkforreddit.subreddits;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper;
import torille.fi.lurkforreddit.data.models.jsonResponses.RedditToken;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.search.SearchFragment;
import torille.fi.lurkforreddit.subreddit.SubredditFragment;
import torille.fi.lurkforreddit.utils.MediaHelper;
import torille.fi.lurkforreddit.utils.NetworkHelper;
import torille.fi.lurkforreddit.utils.Store;

/**
 * Created by eva on 2/8/17.
 */

public class SubredditsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String STATE = NetworkHelper.nextStateId();
    private static final String REDIRECT_URI = "lurk://redirecturi";
    private static final String RESPONSE_TYPE = "code";
    private static final String DURATION = "permanent";
    private static final String SCOPE = "identity,mysubreddits,read,account";

    @Inject
    Store store;

    @Inject
    RedditService.Auth mRedditAuthApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).getmRedditRepositoryComponent().inject(this);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        SubredditFragment subredditFragment = (SubredditFragment) getSupportFragmentManager().findFragmentById(R.id.content);

        if (subredditFragment == null) {
            getFrontPageFragment();

        }


    }

    private void initFragment(Fragment subredditsFragment) {
        // Add the NotesFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, subredditsFragment);
        transaction.commit();
    }

    private void getFrontPageFragment() {
        Subreddit frontpage = Subreddit.builder()
                .setUrl("")
                .setBannerUrl("")
                .setId(null)
                .setDisplayName("Popular")
                .setKeyColor(null)
                .build();

        SubredditFragment subredditFragment = SubredditFragment.newInstance(frontpage);

        initFragment(subredditFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIntent(getIntent());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_login);
        if (store.isLoggedIn()) {
            item.setTitle(R.string.menu_item_login_logout);
        } else {
            item.setTitle(R.string.menu_item_login);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri uri = intent.getData();
            intent.setData(null);
            Timber.d("Uri is " + uri);
            String state = uri.getQueryParameter("state");
            // check that state matches
            if (state.equals(STATE)) {
                String code = uri.getQueryParameter("code");
                if (code != null) {
                    Timber.d("Code was " + code);
                    getToken(code);
                } else if (uri.getQueryParameter("error") != null) {
                    // show an error message here
                    Timber.e("Got error " + uri.getQueryParameter("error"));
                    Toast.makeText(this, "Got error " + uri.getQueryParameter("error"), Toast.LENGTH_LONG).show();
                }
            } else {
                Timber.e(state + " does not match " + STATE);
                Toast.makeText(this, state + " does not match " + STATE, Toast.LENGTH_LONG).show();
            }


        }
    }

    private void getToken(String code) {
        final String grant_type = "authorization_code";
        Call<RedditToken> call = mRedditAuthApi.getUserAuthToken(grant_type, code, REDIRECT_URI);
        call.enqueue(new Callback<RedditToken>() {
            @Override
            public void onResponse(Call<RedditToken> call, Response<RedditToken> response) {
                if (response.isSuccessful()) {
                    Timber.d("Got " + response.body().toString());
                    store.setToken(response.body().access_token());
                    store.setRefreshToken(response.body().refresh_token());
                    store.loggedIn(true);
                    Toast.makeText(SubredditsActivity.this, R.string.toast_login_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SubredditsActivity.this, R.string.toast_login_failed + "" + response.errorBody(), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<RedditToken> call, Throwable t) {
                Timber.e(t);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_frontpage:
                getFrontPageFragment();
                return true;
            case R.id.action_subreddits:
                initFragment(SubredditsFragment.newInstance());
                return true;
            case R.id.action_search:
                initFragment(SearchFragment.newInstance());
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_login:
                if (store.isLoggedIn()) {
                    logOut();
                } else {
                    logIn();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        store.loggedIn(false);
        store.setToken(null);
        store.setRefreshToken(null);
        Toast.makeText(this, R.string.toast_logout_success, Toast.LENGTH_SHORT).show();
    }

    private void logIn() {
        String CLIENT_ID = getResources().getString(R.string.client_id);

        //TODO Switch to constant
        final String url = "https://www.reddit.com/api/v1/"
                + "authorize.compact?client_id=" + CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&state=" + STATE
                + "&redirect_uri=" + REDIRECT_URI
                + "&duration=" + DURATION
                + "&scope=" + SCOPE;
        CustomTabActivityHelper helper = new CustomTabActivityHelper();

        CustomTabActivityHelper.openCustomTab(this,
                MediaHelper.createCustomTabIntent(this,
                        helper.getSession()),
                url,
                new CustomTabActivityHelper.CustomTabFallback() {
                    @Override
                    public void openUri(Activity activity, String uri) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
                });
    }
}
