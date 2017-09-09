package torille.fi.lurkforreddit.utils

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import timber.log.Timber
import torille.fi.lurkforreddit.comments.CommentActivity
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.subreddit.SubredditActivity

/**
 * Activity that handles logic App links and launches the right activity
 * as intent filters cant separate /r/worldnews and /r/worldnews/comments/ regexps
 * to my knowledge
 */

class AppLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    internal fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val appLinkData = intent.data
            val paths = appLinkData.pathSegments
            val size = paths.size
            //If its a subreddit for example /r/worldnews
            if (size == 2) {
                val subIntent = Intent(applicationContext, SubredditActivity::class.java)
                subIntent.data = appLinkData
                startActivity(subIntent)
                finish()
            } else if (size == 5 || size == 6) {
                Timber.d("Got normal comment thread")
                val commentIntent = Intent(applicationContext, CommentActivity::class.java)

                val parts = appLinkData.pathSegments.size
                //Normal comment threads if 5 or 6 segments
                when (parts) {
                    5 -> commentIntent.putExtra(CommentActivity.IS_SINGLE_COMMENT_THREAD, false)
                    6 -> commentIntent.putExtra(CommentActivity.IS_SINGLE_COMMENT_THREAD, true)
                    else -> {
                        // TODO("add indicator if malformed url")
                    }
                }
                val post = Post(permaLink = appLinkData.path)
                commentIntent.putExtra(CommentActivity.EXTRA_CLICKED_POST, post)
                startActivity(commentIntent)
                finish()
            }//TODO add indicator if malformed url
            //Normal comment threads if 5 or 6 segments
        }

    }
}
