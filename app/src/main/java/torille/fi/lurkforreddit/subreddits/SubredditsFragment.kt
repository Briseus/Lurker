package torille.fi.lurkforreddit.subreddits

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_subreddits.*
import kotlinx.android.synthetic.main.fragment_subreddits.view.*
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import javax.inject.Inject

/**
 * Fragment for
 */

class SubredditsFragment : DaggerFragment(), SubredditsContract.View {

    @Inject
    internal lateinit var mActionsListener: SubredditsContract.Presenter

    private lateinit var mListAdapter: SubredditsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListAdapter = SubredditsAdapter(mItemListener, ContextCompat.getColor(context, R.color.colorAccent))
    }

    override fun onResume() {
        super.onResume()
        mActionsListener.takeView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionsListener.dropView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_subreddits, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context
        subRecyclerView.adapter = mListAdapter
        subRecyclerView.setHasFixedSize(true)
        subRecyclerView.layoutManager = LinearLayoutManager(context)

        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))
        refreshLayout.setOnRefreshListener { mActionsListener.loadSubreddits(true) }
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
            // Make sure setRefreshing() is called after the layout is done with everything else.
            refreshLayout.post { refreshLayout.isRefreshing = active }
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
