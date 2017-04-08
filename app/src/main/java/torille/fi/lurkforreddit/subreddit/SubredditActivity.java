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

import com.facebook.drawee.view.SimpleDraweeView;

import org.parceler.Parcels;

import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.Subreddit;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;

public class SubredditActivity extends AppCompatActivity {

    public static final String EXTRA_SUBREDDIT = "subreddit";

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
            getSupportActionBar().setTitle(subreddit.getDisplay_name());
            loadBannerImage(subreddit);

            if (savedInstanceState == null) {
                initFragment(SubredditFragment.newInstance(subreddit));
            }
        } else {
            handleIntent(getIntent(), savedInstanceState);
        }


    }

    public void handleIntent(Intent intent, @Nullable Bundle savedInstanceState) {
        if (intent != null && intent.getData() != null) {
            Uri appLinkData = intent.getData();

            String name = appLinkData.getLastPathSegment();
            Subreddit subreddit = new Subreddit();
            subreddit.setName(name);
            subreddit.setUrl("/r/" + name.toUpperCase());

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(name);
            }

            if (savedInstanceState == null) {
                initFragment(SubredditFragment.newInstance(subreddit));
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

    private void initFragment(Fragment subredditFragment) {
        // Add the NotesDetailFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, subredditFragment);
        transaction.commit();
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}
