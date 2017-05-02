package torille.fi.lurkforreddit.comments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import timber.log.Timber;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * Primary Activity for Comments
 * Created by eva on 2/13/17.
 */

public class CommentActivity extends AppCompatActivity {

    public static final String EXTRA_CLICKED_POST = "post";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_arrow_back_white_24px, null);
            actionBar.setTitle("");
            actionBar.setHomeAsUpIndicator(icon);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        CommentFragment commentFragment = (CommentFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);
        Post originalPost = getIntent().getParcelableExtra(EXTRA_CLICKED_POST);

        if (commentFragment == null && originalPost != null) {
            commentFragment = CommentFragment.newInstance(originalPost, false);
            initFragment(commentFragment);

        } else if (originalPost == null) {
            handleIntent(getIntent());
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    void handleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Uri appLinkData = intent.getData();
            List<String> paths = appLinkData.getPathSegments();
            int size = paths.size();
            //TODO add indicator if malformed url
            //Normal comment threads if 5 or 6 segments
            if (size == 5 || size == 6) {
                Timber.d("Got normal comment thread");
                Post post = Post.builder()
                        .setUrl("")
                        .setPreviewImage("")
                        .setId("")
                        .setScore("")
                        .setIsSelf(false)
                        .setDomain("")
                        .setTitle("")
                        .setAuthor("")
                        .setCreatedUtc(0)
                        .setPermaLink(appLinkData.getPath())
                        .setSelfText(null)
                        .setThumbnail("")
                        .setNumberOfComments("")
                        .build();
                if (size == 5) {
                    initFragment(CommentFragment.newInstance(post, false));
                } else {
                    initFragment(CommentFragment.newInstance(post, true));
                }

            }
        }

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
