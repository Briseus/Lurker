package torille.fi.lurkforreddit.subreddit;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import javax.inject.Inject;

import dagger.Lazy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;
import torille.fi.lurkforreddit.utils.TextHelper;

public class SubredditActivity extends AppCompatActivity {

    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_SUBREDDITNAME = "subredditname";

    @Inject
    Lazy<RedditService.Reddit> mRedditApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);
        ((MyApplication) getApplication()).getmNetComponent().inject(this);

        Intent intent = getIntent();
        Subreddit subreddit = intent.getParcelableExtra(EXTRA_SUBREDDIT);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px, null));
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (subreddit != null) {
                actionBar.setTitle(subreddit.displayName());
            } else {
                actionBar.setTitle(null);
            }

        }

        SubredditFragment subredditFragment = (SubredditFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (subreddit == null) {
            handleIntent(intent);
        } else if (subredditFragment == null) {
            Timber.d(subreddit.toString());
            loadBannerImage(subreddit.bannerUrl(), subreddit.keyColor());
            subredditFragment = SubredditFragment.newInstance(subreddit);
            initFragment(subredditFragment);
        }


    }

    private void searchForReddit(String subredditName) {
        Timber.d("Searching for " + subredditName);
        Call<SubredditChildren> call = mRedditApi.get().getSubredditInfo(subredditName);

        call.enqueue(new Callback<SubredditChildren>() {
            @Override
            public void onResponse(Call<SubredditChildren> call, Response<SubredditChildren> response) {

                if (response.isSuccessful()) {
                    Timber.d("Got " + response.body().toString());
                    Subreddit subreddit = TextHelper.formatSubreddit(response.body());

                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        getSupportActionBar().setTitle(subreddit.displayName());
                    }

                    loadBannerImage(subreddit.bannerUrl(), subreddit.keyColor());
                    SubredditFragment fragment = SubredditFragment.newInstance(subreddit);
                    initFragment(fragment);
                }
            }

            @Override
            public void onFailure(Call<SubredditChildren> call, Throwable t) {
                Timber.e(t);
                Toast.makeText(getApplicationContext(), "SubredditResponse not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String subredditName = intent.getStringExtra(EXTRA_SUBREDDITNAME);
            if (subredditName != null) {
                searchForReddit(subredditName);
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadBannerImage(@Nullable String bannerUrl, @Nullable String keyColor) {
        SimpleDraweeView banner = (SimpleDraweeView) findViewById(R.id.banner);

        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            Timber.d("Setting bannerUrl to " + bannerUrl);
            banner.setImageURI(bannerUrl);
        } else if (keyColor != null && !keyColor.isEmpty()) {
            Timber.d("Setting banner background to " + keyColor);
            banner.setBackgroundColor(Color.parseColor(keyColor));
        }

    }

    private void initFragment(Fragment fragment) {
        // Add the NotesDetailFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, fragment);
        transaction.commit();
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
