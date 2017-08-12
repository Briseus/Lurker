package torille.fi.lurkforreddit.comments

import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.facebook.drawee.view.SimpleDraweeView
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.media.FullscreenActivity
import torille.fi.lurkforreddit.utils.MediaHelper

class CommentActivity : AppCompatActivity() {

    private val mCustomTabActivityHelper: CustomTabActivityHelper = CustomTabActivityHelper()

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
            setupImage(originalPost)
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

    internal fun setupImage(postForComments: Post) {
        val parallaxImageDrawee = findViewById<SimpleDraweeView>(R.id.parallaxImage)

        val parallaxImage = postForComments.previewImage
        Timber.d("Got previewImage url = $parallaxImage")
        if (parallaxImage.isNotEmpty()) {
            parallaxImageDrawee.setOnClickListener {
                val postUrl = postForComments.url
                if (MediaHelper.isContentMedia(postUrl, postForComments.domain)) {
                    val intent = Intent(this, FullscreenActivity::class.java)
                    intent.putExtra(FullscreenActivity.EXTRA_POST, postForComments)
                    startActivity(intent)
                } else {
                    CustomTabActivityHelper.openCustomTab(this,
                            MediaHelper.createCustomTabIntent(this,
                                    mCustomTabActivityHelper.session),
                            postUrl
                    ) { _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(postUrl))
                        startActivity(intent)
                    }
                }
            }
            val focusPoint = PointF(0.5f, 0f)
            parallaxImageDrawee.hierarchy.setActualImageFocusPoint(focusPoint)
            parallaxImageDrawee.setImageURI(parallaxImage)
        } else{
            parallaxImageDrawee.visibility = View.GONE
        }
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
                setupImage(post)
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

    override fun onStart() {
        super.onStart()
        mCustomTabActivityHelper.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        mCustomTabActivityHelper.unbindCustomTabsService(this)
    }

    companion object {
        val EXTRA_CLICKED_POST = "post"
    }

}
