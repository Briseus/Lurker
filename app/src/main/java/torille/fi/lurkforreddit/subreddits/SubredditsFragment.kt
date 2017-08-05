package torille.fi.lurkforreddit.subreddits

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import javax.inject.Inject

/**
 * Fragment for
 */

class SubredditsFragment : Fragment(), SubredditsContract.View {

    private lateinit var mSubredditsComponent: SubredditsComponent

    @Inject
    internal lateinit var mActionsListener: SubredditsContract.Presenter<SubredditsContract.View>

    private lateinit var mListAdapter: SubredditsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSubredditsComponent = DaggerSubredditsComponent.builder()
                .redditRepositoryComponent((activity.application as MyApplication).getmRedditRepositoryComponent())
                .build()
        mSubredditsComponent.inject(this)
        mActionsListener.setView(this)
        mListAdapter = SubredditsAdapter(mItemListener, ContextCompat.getColor(context, R.color.colorAccent))
    }

    override fun onResume() {
        super.onResume()
        mActionsListener.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionsListener.dispose()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val context = context
        val root = inflater!!.inflate(R.layout.fragment_subreddits, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.subreddits_list)
        recyclerView.adapter = mListAdapter
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val swipeRefreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))
        swipeRefreshLayout.setOnRefreshListener { mActionsListener.loadSubreddits(true) }
        return root
    }

    /**
     * Listener for clicks on notes in the RecyclerView.
     */
    private val mItemListener = object : SubredditItemListener {
        override fun onSubredditClick(subreddit: Subreddit) {
            mActionsListener.openSubreddit(subreddit)
        }
    }

    override fun setProgressIndicator(active: Boolean) {
        view?.apply {
            val srl = this.findViewById<SwipeRefreshLayout>(R.id.refresh_layout)
            // Make sure setRefreshing() is called after the layout is done with everything else.
            srl.post { srl.isRefreshing = active }
        }
    }

    override fun showSubreddits(subreddits: List<Subreddit>) {
        mListAdapter.replaceData(subreddits)
    }

    override fun loadSelectedSubreddit(subreddit: Subreddit) {
        val intent = Intent(context, SubredditActivity::class.java)
        intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit)
        startActivity(intent)
    }

    override fun onError(errorText: String) {
        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
    }

    internal interface SubredditItemListener {
        fun onSubredditClick(subreddit: Subreddit)
    }

    companion object {
        fun newInstance(): SubredditsFragment {
            return SubredditsFragment()
        }
    }
}
