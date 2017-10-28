package torille.fi.lurkforreddit.media

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.view.Menu
import android.view.MenuItem
import android.view.View
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_fullscreen.*
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Post
import javax.inject.Inject

class FullscreenActivity : DaggerAppCompatActivity() {

    @Inject lateinit var post: Post

    private lateinit var shareActionProvider: ShareActionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (null == savedInstanceState) {
            initFragment(FullscreenFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fullscreen_main, menu)
        val item = menu.findItem(R.id.menu_item_share)
        shareActionProvider = MenuItemCompat.getActionProvider(item) as ShareActionProvider
        setShareIntent()
        return true
    }

    private fun setShareIntent() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, post.url)
        shareActionProvider.setShareIntent(intent)
    }

    private fun initFragment(fragment: Fragment) {
        // Add the NotesFragment to the layout
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fullscreen_content, fragment)
        transaction.commit()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (fullscreen_content != null) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fullscreen_content.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            } else {
                fullscreen_content.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }

    companion object {
        val EXTRA_POST = "post"
        val EXTRA_URL = "url"
    }
}
