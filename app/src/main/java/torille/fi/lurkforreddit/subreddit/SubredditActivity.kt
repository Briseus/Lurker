package torille.fi.lurkforreddit.subreddit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.annotation.VisibleForTesting
import android.support.test.espresso.IdlingResource
import android.support.v4.app.Fragment
import android.widget.Toast
import dagger.Lazy
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_subreddit.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.utils.TextHelper
import torille.fi.lurkforreddit.utils.test.EspressoIdlingResource
import javax.inject.Inject
import javax.inject.Named

class SubredditActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var subredditPresenter: SubredditPresenter

    @Inject
    lateinit var mRedditApi: Lazy<RedditService.Reddit>

    @field:[Inject Named("sub")]
    lateinit var subreddit: Subreddit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subreddit)

        setSupportActionBar(appBarLayout)

        val actionBar = supportActionBar

        actionBar?.setHomeAsUpIndicator(resources.getDrawable(R.drawable.ic_arrow_back_white_24px, null))
        actionBar?.setDisplayHomeAsUpEnabled(true)

        if (subreddit != null) {
            actionBar?.title = subreddit?.displayName
        } else {
            actionBar?.title = null
        }


        var subredditFragment: SubredditFragment? = supportFragmentManager
                .findFragmentById(R.id.contentFrame) as? SubredditFragment

        if (subreddit == null) {
            initFragment(ProgressFragment.newInstance())
            handleIntent(intent)
        } else if (subredditFragment == null) {
            Timber.d(subreddit.toString())
            subredditFragment = SubredditFragment.newInstance(subreddit)
            loadBannerImage(subreddit?.bannerUrl, subreddit?.keyColor)
            initFragment(subredditFragment)
        }

    }

    private fun searchForReddit(subredditName: String) {
        Timber.d("Searching for " + subredditName)
        val call = mRedditApi.get().getSubredditInfo(subredditName)

        call.enqueue(object : Callback<SubredditChildren> {
            override fun onResponse(call: Call<SubredditChildren>, response: Response<SubredditChildren>) {

                if (response.isSuccessful) {
                    Timber.d("Got " + response.body()!!.toString())
                    val subreddit = TextHelper.formatSubreddit(Observable.fromArray(response.body())).blockingSingle()

                    supportActionBar?.title = subreddit.displayName

                    loadBannerImage(subreddit.bannerUrl, subreddit.keyColor)
                    initFragment(SubredditFragment.newInstance(subreddit))
                }
            }

            override fun onFailure(call: Call<SubredditChildren>, t: Throwable) {
                Timber.e(t)
                Toast.makeText(applicationContext, "SubredditResponse not found", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val subredditName = intent.getStringExtra(EXTRA_SUBREDDITNAME)
            if (subredditName != null) {
                searchForReddit(subredditName)
            } else {
                val appLinkData = intent.data
                Timber.d("Got uri " + appLinkData)
                searchForReddit("/r/" + appLinkData.lastPathSegment)
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)

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
        val EXTRA_SUBREDDITNAME = "subredditname"
    }
}
