package torille.fi.lurkforreddit.subreddit;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.Subreddit;
import torille.fi.lurkforreddit.data.models.SubredditChildren;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;
import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;

public class SubredditActivity extends AppCompatActivity {

    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_SUBREDDITNAME = "subredditname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_arrow_back_white_24px, null));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Subreddit subreddit = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SUBREDDIT));

        if (subreddit != null) {

            loadBannerImage(subreddit);

            if (savedInstanceState == null) {
                initFragment(SubredditFragment.newInstance(subreddit));
            }
        } else {
            handleIntent(getIntent(), savedInstanceState);
        }


    }

    public void handleIntent(Intent intent, @Nullable final Bundle savedInstanceState) {
        if (intent != null) {

            String subredditName = intent.getStringExtra(EXTRA_SUBREDDITNAME);

            if (subredditName != null) {
                String token = SharedPreferencesHelper.getToken();
                Call<SubredditChildren> call = RedditService
                        .getInstance(token)
                        .getSubredditInfo(subredditName);

                call.enqueue(new Callback<SubredditChildren>() {
                    @Override
                    public void onResponse(Call<SubredditChildren> call, Response<SubredditChildren> response) {

                        if (response.isSuccessful()) {
                            Timber.d("Got " + response.body().toString());
                            Subreddit subreddit = response.body().getSubreddit();
                            loadBannerImage(subreddit);
                            if (savedInstanceState == null) {
                                initFragment(SubredditFragment.newInstance(subreddit));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SubredditChildren> call, Throwable t) {
                        Timber.e(t);
                        Toast.makeText(getApplicationContext(), "Subreddit not found", Toast.LENGTH_SHORT).show();
                    }
                });

            } else if (intent.getData() != null) {
                Uri appLinkData = intent.getData();

                List<String> pathList = appLinkData.getPathSegments();
                Timber.d("Got path list " + pathList.toString());

                if (pathList.size() == 2) {
                    String name = pathList.get(1);
                    Subreddit subreddit = new Subreddit();
                    subreddit.setName(name);
                    subreddit.setUrl("/r/" + name.toUpperCase());

                    loadBannerImage(subreddit);

                    if (savedInstanceState == null) {
                        initFragment(SubredditFragment.newInstance(subreddit));
                    }
                    //if path is comments
                }

            }
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, null);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadBannerImage(Subreddit subreddit) {
        String title = subreddit.getDisplay_name();
        if (title != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        SimpleDraweeView banner = (SimpleDraweeView) findViewById(R.id.banner);
        boolean hasBannerSource = (subreddit.getBanner() != null && !subreddit.getBanner().isEmpty());
        boolean hasCustomColor = (subreddit.getKey_color() != null && !subreddit.getKey_color().isEmpty());
        int color;

        if (hasBannerSource) {
            banner.setImageURI(subreddit.getBanner());
        }

        if (hasCustomColor) {
            color = Color.parseColor(subreddit.getKey_color());
        } else {
            color = ContextCompat.getColor(this, R.color.colorAccent);
        }
        banner.setBackgroundColor(color);


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
