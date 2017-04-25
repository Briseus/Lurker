package torille.fi.lurkforreddit.comments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * Primary Activity for Comments
 * Created by eva on 2/13/17.
 */

public class CommentActivity extends AppCompatActivity {

    public static final String EXTRA_CLICKED_POST = "post";

    /*@Inject
    CommentPresenter mCommentPresenter;*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post originalPost = getIntent().getParcelableExtra(EXTRA_CLICKED_POST);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(originalPost.title());
            Drawable icon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24px, null);
            actionBar.setHomeAsUpIndicator(icon);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CommentFragment commentFragment = (CommentFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);

        if (commentFragment == null) {
            commentFragment = CommentFragment.newInstance(originalPost);
            initFragment(commentFragment);

        }
        /*DaggerCommentComponent.builder()
                .commentPresenterModule(new CommentPresenterModule(originalPost))
                .redditRepositoryComponent(((MyApplication) getApplicationContext()).getmRedditRepositoryComponent())
                .build()
                .inject(this);
*/
    }

    private void initFragment(Fragment commentFragment) {
        // Add the NotesDetailFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, commentFragment);
        transaction.commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
