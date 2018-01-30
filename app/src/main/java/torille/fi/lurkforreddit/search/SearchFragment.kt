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
    internal lateinit var actionsListener: SearchContract.Presenter

    private lateinit var adapter: SearchViewAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var loading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = SearchViewAdapter(searchClickListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        searchRecyclerView.layoutManager = layoutManager
        searchRecyclerView.setHasFixedSize(true)
        searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            internal var pastVisiblesItems: Int = 0
            internal var visibleItemCount: Int = 0
            internal var totalItemCount: Int = 0

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()

                    if (!loading && visibleItemCount + pastVisiblesItems >= totalItemCount) {
                        loading = true
                        Timber.d("Last item reached, getting more!")
                        recyclerView!!.post { actionsListener.searchMoreSubreddits() }

                    }

                }
            }
        })
        searchRecyclerView.adapter = adapter

        searchView.queryHint = "Find subreddits..."
        searchView.isIconified = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (!query.isEmpty()) {
                    Timber.d("Going to search for " + query)
                    searchView.clearFocus()
                    actionsListener.searchSubreddits(query)
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
        actionsListener.dropView()
        Timber.d("Destroyed")
    }

    override fun onResume() {
        super.onResume()
        actionsListener.takeView(this)
    }

    override fun showResults(results: List<SearchResult>) {
        loading = false
        adapter.addResults(results)
    }

    override fun showProgressbar() {
        adapter.addProgressBar()
    }

    override fun clearResults() {
        adapter.clear()
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
}
