package torille.fi.lurkforreddit.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import timber.log.Timber;
import torille.fi.lurkforreddit.comments.CommentActivity;
import torille.fi.lurkforreddit.subreddit.SubredditActivity;

/**
 * Activity that handles logic App links and launches the right activity
 * as intent filters cant separate /r/worldnews and /r/worldnews/comments/ regexps
 * to my knowledge
 */

public class AppLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
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
            //If its a subreddit for example /r/worldnews
            if (size == 2) {
                Intent subIntent = new Intent(getApplicationContext(), SubredditActivity.class);
                subIntent.setData(appLinkData);
                startActivity(subIntent);
                finish();
            }
            //TODO add indicator if malformed url
            //Normal comment threads if 5 or 6 segments
            else if (size == 5 || size == 6) {
                Timber.d("Got normal comment thread");
                Intent commentIntent = new Intent(getApplicationContext(), CommentActivity.class);
                commentIntent.setData(appLinkData);
                startActivity(commentIntent);
                finish();
            }
        }

    }
}
