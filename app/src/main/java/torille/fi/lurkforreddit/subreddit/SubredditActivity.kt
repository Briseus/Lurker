package torille.fi.lurkforreddit.subreddit

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.test.espresso.IdlingResource
import android.support.v4.app.Fragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_subreddit.*
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.utils.test.EspressoIdlingResource
import javax.inject.Inject

class SubredditActivity : DaggerAppCompatActivity() {

    @Inject lateinit var subredditPresenter: SubredditPresenter
    @Inject lateinit var subreddit: Subreddit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subreddit)
        setSupportActionBar(appBarLayout)

        val actionBar = supportActionBar

        actionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_arrow_back_white_24px, null))
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = subreddit.displayName

        val subredditFragment: SubredditFragment? = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as? SubredditFragment

        if (subredditFragment == null) {
            Timber.d(subreddit.toString())
            loadBannerImage(subreddit.bannerUrl, subreddit.keyColor)
            initFragment(SubredditFragment.newInstance(subreddit))
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadBannerImage(bannerUrl: String?, keyColor: String?) {

        if (!bannerUrl.isNullOrEmpty()) {
            Timber.d("Setting bannerUrl to " + bannerUrl)
            banner.setImageURI(bannerUrl)
        } else if (!keyColor.isNullOrEmpty()) {
            Timber.d("Setting banner background to " + keyColor)
            banner.setBackgroundColor(Color.parseColor(keyColor))
        }

    }

    private fun initFragment(fragment: Fragment) {
        // Add the NotesDetailFragment to the layout
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.contentFrame, fragment)
        transaction.commit()
    }

    val countingIdlingResource: IdlingResource
        @VisibleForTesting
        get() = EspressoIdlingResource.idlingResource

    companion object {
        val EXTRA_SUBREDDIT = "subreddit"
    }
}
