package torille.fi.lurkforreddit.subreddits

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import timber.log.Timber
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.models.jsonResponses.RedditToken
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.search.SearchFragment
import torille.fi.lurkforreddit.subreddit.SubredditFragment
import torille.fi.lurkforreddit.utils.MediaHelper
import torille.fi.lurkforreddit.utils.NetworkHelper
import torille.fi.lurkforreddit.utils.Store
import javax.inject.Inject

/**
 * The apps main activity that starts with the app
 */

class SubredditsActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val helper = CustomTabActivityHelper()

    @Inject internal lateinit var store: Store
    @Inject internal lateinit var mRedditAuthApi: RedditService.Auth
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        (application as MyApplication).getmRedditRepositoryComponent().inject(this)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById(R.id.appBarLayout) as Toolbar
        setSupportActionBar(toolbar)

        val bottomNavigationView = findViewById(R.id.bottom_navigation) as BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            getFrontPageFragment()
        }

    }

    private fun initFragment(subredditsFragment: Fragment) {
        // Add the NotesFragment to the layout
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.content, subredditsFragment)
        transaction.commit()
    }

    private fun getFrontPageFragment() {
        val frontpage = Subreddit(id = "frontpage", displayName = "Popular")
        initFragment(SubredditFragment.newInstance(frontpage))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onNewIntent(intent: Intent) {
        checkIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        helper.bindCustomTabsService(this)
        checkIntent(intent)
    }

    override fun onStop() {
        super.onStop()
        helper.unbindCustomTabsService(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_login)
        if (store.isLoggedIn) {
            item.setTitle(R.string.menu_item_login_logout)
        } else {
            item.setTitle(R.string.menu_item_login)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun checkIntent(intent: Intent?) {
        if (intent != null && intent.data != null) {
            val uri = intent.data
            intent.data = null
            Timber.d("Uri is " + uri)
            val state = uri.getQueryParameter("state")
            // check that state matches
            if (state == STATE) {
                val code = uri.getQueryParameter("code")
                if (!code.isNullOrEmpty()) {
                    Timber.d("Code was " + code)
                    getToken(code)
                } else if (uri.getQueryParameter("error") != null) {
                    // show an error message here
                    Timber.e("Got error " + uri.getQueryParameter("error"))
                    Toast.makeText(this, "Got error " + uri.getQueryParameter("error"), Toast.LENGTH_LONG).show()
                }
            } else {
                Timber.e(state + " does not match " + STATE)
                Toast.makeText(this, state + " does not match " + STATE, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getToken(code: String) {
        val grant_type = "authorization_code"
        disposables.add(mRedditAuthApi.getUserAuthToken(grant_type, code, REDIRECT_URI)
                .map { redditToken -> redditToken }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<RedditToken>() {
                    override fun onNext(redditToken: RedditToken) {
                        store.token = redditToken.accessToken
                        store.refreshToken = redditToken.refreshToken!!
                        store.loggedIn(true)

                    }

                    override fun onError(throwable: Throwable) {
                        Timber.e(throwable)
                        Toast.makeText(this@SubredditsActivity, R.string.toast_login_failed, Toast.LENGTH_LONG).show()
                    }

                    override fun onComplete() {
                        Toast.makeText(this@SubredditsActivity, R.string.toast_login_success, Toast.LENGTH_LONG).show()
                    }
                }))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_frontpage -> {
                getFrontPageFragment()
                return true
            }
            R.id.action_subreddits -> {
                initFragment(SubredditsFragment.newInstance())
                return true
            }
            R.id.action_search -> {
                initFragment(SearchFragment.newInstance())
                return true
            }
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_login -> {
                if (store.isLoggedIn) {
                    logOut()
                } else {
                    logIn()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logOut() {
        store.loggedIn(false)
        store.token = ""
        store.refreshToken = ""
        Toast.makeText(this, R.string.toast_logout_success, Toast.LENGTH_SHORT).show()
    }

    private fun logIn() {
        val CLIENT_ID = resources.getString(R.string.client_id)
        //TODO Switch to constant
        val url = "https://www.reddit.com/api/v1/"
                .plus("authorize.compact?client_id=" + CLIENT_ID)
                .plus("&response_type=" + RESPONSE_TYPE)
                .plus("&state=" + STATE)
                .plus("&redirect_uri=" + REDIRECT_URI)
                .plus("&duration=" + DURATION)
                .plus("&scope=" + SCOPE)

        CustomTabActivityHelper.openCustomTab(this,
                MediaHelper.createCustomTabIntent(this,
                        helper.session),
                url
        ) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    companion object {
        private val STATE = NetworkHelper.nextStateId()
        private val REDIRECT_URI = "lurk://redirecturi"
        private val RESPONSE_TYPE = "code"
        private val DURATION = "permanent"
        private val SCOPE = "identity,mysubreddits,read,account"
    }
}
