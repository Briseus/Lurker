package torille.fi.lurkforreddit.utils

import android.content.Intent
import android.os.Bundle
import android.view.View
import dagger.Lazy
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_applink.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.comments.CommentActivity
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import javax.inject.Inject

/**
 * Activity that handles logic App links and launches the right activity
 * as intent filters cant separate /r/worldnews and /r/worldnews/comments/ regexps
 * to my knowledge
 */

class AppLinkActivity : DaggerAppCompatActivity() {

    @Inject lateinit var mRedditApi: Lazy<RedditService.Reddit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applink)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val appLinkData = intent.data
            val paths = appLinkData.pathSegments
            val size = paths.size
            //If its a subreddit for example /r/worldnews
            if (size == 2) {
                searchForSubredditReddit(appLinkData.toString())
            } else if (size == 5 || size == 6) {
                Timber.d("Got normal comment thread")
                val commentIntent = Intent(applicationContext, CommentActivity::class.java)

                val parts = appLinkData.pathSegments.size
                //Normal comment threads if 5 or 6 segments
                when (parts) {
                    5 -> commentIntent.putExtra(CommentActivity.IS_SINGLE_COMMENT_THREAD, false)
                    6 -> commentIntent.putExtra(CommentActivity.IS_SINGLE_COMMENT_THREAD, true)
                }
                val post = Post(permaLink = appLinkData.path)
                commentIntent.putExtra(CommentActivity.EXTRA_CLICKED_POST, post)
                startActivity(commentIntent)
                finish()
            } else {
                setNotFound(this@AppLinkActivity.getString(R.string.does_not_exist))
            }
        }

    }

    private fun setNotFound(msg: String) {
        loading.visibility = View.GONE
        notFound.visibility = View.VISIBLE
        notFound.text = msg
    }

    private fun searchForSubredditReddit(subredditName: String) {
        Timber.d("Searching for " + subredditName)
        val call = mRedditApi.get().getSubredditInfo(subredditName)

        call.enqueue(object : Callback<SubredditChildren> {
            override fun onResponse(call: Call<SubredditChildren>, response: Response<SubredditChildren>) {

                if (response.isSuccessful) {
                    Timber.d("Got " + response.body()!!.toString())
                    val subreddit = TextHelper.formatSubreddit(Observable.fromArray(response.body())).blockingSingle()
                    val subIntent = Intent(applicationContext, SubredditActivity::class.java)
                    subIntent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit)
                    startActivity(subIntent)
                    finish()
                } else {
                    setSubredditNotFound()
                }
            }

            override fun onFailure(call: Call<SubredditChildren>, t: Throwable) {
                Timber.e(t)
                setSubredditNotFound()
            }

            fun setSubredditNotFound() {
                setNotFound("$subredditName ${this@AppLinkActivity.getString(R.string.not_found)}")
            }
        })


    }
}
