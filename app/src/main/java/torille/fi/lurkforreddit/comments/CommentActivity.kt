package torille.fi.lurkforreddit.comments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.media.FullscreenActivity
import torille.fi.lurkforreddit.utils.MediaHelper
import javax.inject.Inject

class CommentActivity : DaggerAppCompatActivity() {

    @Inject lateinit var post: Post

    private val customTabActivityHelper: CustomTabActivityHelper = CustomTabActivityHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        setSupportActionBar(appBarLayout)

        val actionBar = supportActionBar
        if (actionBar != null) {
            val icon = resources.getDrawable(R.drawable.ic_arrow_back_white_24px, null)
            actionBar.title = ""
            actionBar.setHomeAsUpIndicator(icon)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        setupImage()

        val commentFragment: CommentFragment? = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as? CommentFragment

        if (commentFragment == null) {
            initFragment(CommentFragment())
        }


    }

    private fun setupImage() {

        val parallaxImageUrl = post.previewImage
        Timber.d("Got previewImage url = $parallaxImageUrl")
        if (parallaxImageUrl.isNotEmpty()) {
            parallaxImage.setOnClickListener {
                val postUrl = post.url
                if (MediaHelper.isContentMedia(postUrl, post.domain)) {
                    val intent = Intent(this, FullscreenActivity::class.java)
                    intent.putExtra(FullscreenActivity.EXTRA_POST, post)
                    startActivity(intent)
                } else {
                    val activity = this
                    launch(UI) {
                        CustomTabActivityHelper.openCustomTab(activity,
                                MediaHelper.createCustomTabIntentAsync(activity,
                                        customTabActivityHelper.session).await(),
                                postUrl
                        ) { _, _ ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(postUrl))
                            startActivity(intent)
                        }
                    }

                }
            }
            parallaxImage.setImageURI(parallaxImageUrl)
        } else {
            parallaxImage.visibility = View.GONE
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
        customTabActivityHelper.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        customTabActivityHelper.unbindCustomTabsService(this)
    }

    companion object {
        val EXTRA_CLICKED_POST = "post"
        val IS_SINGLE_COMMENT_THREAD = "single"
    }

}
