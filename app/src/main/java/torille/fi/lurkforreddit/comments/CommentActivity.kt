package torille.fi.lurkforreddit.comments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Post

class CommentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val toolbar = findViewById<Toolbar>(R.id.appBarLayout)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            val icon = resources.getDrawable(R.drawable.ic_arrow_back_white_24px, null)
            actionBar.title = ""
            actionBar.setHomeAsUpIndicator(icon)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        var commentFragment: CommentFragment? = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as? CommentFragment
        val originalPost = intent.getParcelableExtra<Post>(EXTRA_CLICKED_POST)

        if (commentFragment == null && originalPost != null) {
            commentFragment = CommentFragment.newInstance(originalPost, false)
            initFragment(commentFragment)

        } else if (originalPost == null) {
            handleIntent(intent)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("Got new intent")
        handleIntent(intent)
    }

    internal fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val appLinkData = intent.data
            val paths = appLinkData.pathSegments
            val size = paths.size
            // TODO("add indicator if malformed url")
            //Normal comment threads if 5 or 6 segments
            if (size == 5 || size == 6) {
                Timber.d("Got comment thread")
                val post = Post(permaLink = appLinkData.path)
                if (size == 5) {
                    initFragment(CommentFragment.newInstance(post, false))
                } else {
                    initFragment(CommentFragment.newInstance(post, true))
                }

            }
        }

    }

    private fun initFragment(commentFragment: Fragment) {
        // Add the NotesDetailFragment to the layout
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.contentFrame, commentFragment)
        transaction.commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val EXTRA_CLICKED_POST = "post"
    }

}
