package torille.fi.lurkforreddit.search

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search.*
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.SearchResult
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import javax.inject.Inject

class SearchFragment @Inject constructor() : DaggerFragment(), SearchContract.View {

    @Inject
    internal lateinit var mActionsListener: SearchContract.Presenter

    private lateinit var mAdapter: SearchViewAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var loading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = SearchViewAdapter(searchClickListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLayoutManager = LinearLayoutManager(context)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL

        searchRecyclerView.layoutManager = mLayoutManager
        searchRecyclerView.setHasFixedSize(true)
        searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            internal var pastVisiblesItems: Int = 0
            internal var visibleItemCount: Int = 0
            internal var totalItemCount: Int = 0

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()

                    if (!loading && visibleItemCount + pastVisiblesItems >= totalItemCount) {
                        loading = true
                        Timber.d("Last item reached, getting more!")
                        recyclerView!!.post { mActionsListener.searchMoreSubreddits() }

                    }

                }
            }
        })
        searchRecyclerView.adapter = mAdapter

        searchView.queryHint = "Find subreddits..."
        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (!query.isEmpty()) {
                    Timber.d("Going to search for " + query)
                    searchView.clearFocus()
                    mActionsListener.searchSubreddits(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionsListener.dropView()
    }

    override fun onResume() {
        super.onResume()
        mActionsListener.takeView(this)
    }

    override fun showResults(results: List<SearchResult>) {
        loading = false
        mAdapter.addResults(results)
    }

    override fun showProgressbar() {
        mAdapter.addProgressBar()
    }

    override fun clearResults() {
        mAdapter.clear()
    }

    override fun showError(errorText: String) {
        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
    }

    private val searchClickListener = object : SearchClickListener {
        override fun onSearchClick(subreddit: Subreddit) {
            val intent = Intent(context, SubredditActivity::class.java)
            intent.putExtra(SubredditActivity.EXTRA_SUBREDDIT, subreddit)
            startActivity(intent)
        }
    }

    internal interface SearchClickListener {
        fun onSearchClick(subreddit: Subreddit)
    }

    companion object {

        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}
