package torille.fi.lurkforreddit.subreddit;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.parceler.Parcels;

import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.Subreddit;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;

public class SubredditActivity extends AppCompatActivity {

    public static final String EXTRA_SUBREDDIT = "subreddit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_subreddit);

        Subreddit subreddit = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SUBREDDIT));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        toolbar.setTitle(subreddit.getUrl());
        loadBannerImage(subreddit);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initFragment(SubredditFragment.newInstance(subreddit));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadBannerImage(Subreddit subreddit) {
        ImageView banner = (ImageView) findViewById(R.id.banner);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        boolean hasBannerSource = (subreddit.getBanner() != null && !subreddit.getBanner().isEmpty());
        boolean hasCustomColor = (subreddit.getKey_color() != null && !subreddit.getKey_color().isEmpty());

        if ( hasBannerSource && hasCustomColor) {
            int color = Color.parseColor(subreddit.getKey_color());
            collapsingToolbarLayout.setContentScrimColor(color);
            Glide.with(this).load(subreddit.getBanner()).centerCrop().crossFade().into(banner);
        } else if (hasBannerSource) {
            Glide.with(this).load(subreddit.getBanner()).centerCrop().crossFade().into(banner);
        } else if (hasCustomColor) {
            int color = Color.parseColor(subreddit.getKey_color());
            banner.setBackgroundColor(color);
            collapsingToolbarLayout.setContentScrimColor(color);
        }
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
