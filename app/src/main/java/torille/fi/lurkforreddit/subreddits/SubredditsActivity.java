package torille.fi.lurkforreddit.subreddits;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.Subreddit;
import torille.fi.lurkforreddit.subreddit.SubredditFragment;

/**
 * Created by eva on 2/8/17.
 */

public class SubredditsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        if (null == savedInstanceState) {
            initFrontpage();
        }
    }

    private void initFragment(Fragment subredditsFragment) {
        // Add the NotesFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, subredditsFragment);
        transaction.commit();
    }

    private void initFrontpage() {
        Subreddit frontpage = new Subreddit();
        frontpage.setUrl("");
        initFragment(SubredditFragment.newInstance(frontpage));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_frontpage:
                initFrontpage();
                return true;
            case R.id.action_subreddits:
                initFragment(SubredditsFragment.newInstance());
                return true;
        }
        return false;
    }
}
