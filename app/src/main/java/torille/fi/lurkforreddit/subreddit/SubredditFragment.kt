package torille.fi.lurkforreddit.subreddit


import android.content.Intent
import android.net.Uri
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
import com.facebook.drawee.backends.pipeline.Fresco
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_subreddit.*
import timber.log.Timber
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.comments.CommentActivity
import torille.fi.lurkforreddit.customTabs.CustomTabActivityHelper
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.media.FullscreenActivity
import torille.fi.lurkforreddit.utils.DisplayHelper
import torille.fi.lurkforreddit.utils.MediaHelper
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [SubredditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubredditFragment @Inject constructor() : DaggerFragment(), SubredditContract.View {

    private val mCustomTabActivityHelper: CustomTabActivityHelper = CustomTabActivityHelper()
    private var refreshing: Boolean = false

    private lateinit var mListAdapter: PostsAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    @Inject
    lateinit var mActionsListener: SubredditContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mListAdapter = PostsAdapter(mClickListener,
                Fresco.getImagePipeline())
        DisplayHelper.init(context)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subreddit, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLayoutManager = LinearLayoutManager(context)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL

        postRecyclerView.layoutManager = mLayoutManager
        postRecyclerView.setHasFixedSize(true)
        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val PREFETCH_SIZE = 2
            internal var pastVisiblesItems: Int = 0
            internal var visibleItemCount: Int = 0
            internal var totalItemCount: Int = 0
            internal var lastFetch = 0
            internal var scrolledItems: Int = 0

            override fun onScrolled(recyclerV: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerV, dx, dy)
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                    scrolledItems = visibleItemCount + pastVisiblesItems

                    if (lastFetch == 0) {
                        mListAdapter.prefetchImages(2, PREFETCH_SIZE, totalItemCount)
                    }

                    if (!refreshing && scrolledItems > lastFetch) {
                        lastFetch = scrolledItems + (PREFETCH_SIZE - 1)
                        mListAdapter.prefetchImages(lastFetch, PREFETCH_SIZE, totalItemCount)
                    }

                    if (!refreshing && scrolledItems >= totalItemCount) {
                        refreshing = true
                        Timber.d("Last item reached, getting more!")
                        recyclerV?.post { mActionsListener.loadMorePosts() }

                    }
                }
            }
        })
        postRecyclerView.adapter = mListAdapter

        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))
        refreshLayout.setOnRefreshListener {
            mListAdapter.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        mActionsListener.takeView(this)
    }

    override fun onStart() {
        super.onStart()
        mCustomTabActivityHelper.bindCustomTabsService(activity)
    }

    override fun onStop() {
        super.onStop()
        setProgressIndicator(false)
        mCustomTabActivityHelper.unbindCustomTabsService(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionsListener.dropView()
    }

    /**
     * Listeners for clicks in the Recyclerview
     */
    private val mClickListener = object : postClickListener {
        override fun onButtonClick(url: String) {
            mActionsListener.openCustomTabs(url)
        }

        override fun onPostClick(clickedPost: Post) {
            mActionsListener.openComments(clickedPost)
        }

        override fun onMediaClick(post: Post) {
            mActionsListener.openMedia(post)
        }

        override fun onRetryClick() {
            mActionsListener.retry()
        }
    }

    override fun setProgressIndicator(active: Boolean) {
        view?.apply {
            // Make sure setRefreshing() is called after the layout is done with everything else.
            refreshLayout.isRefreshing = active
        }
    }

    override fun showPosts(posts: List<Post>, nextpage: String) {
        mListAdapter.addAll(posts)
    }

    override fun setListProgressIndicator(active: Boolean) {
        mListAdapter.setRefreshing(active)
    }

    override fun addMorePosts(posts: List<Post>, nextpage: String) {
        mListAdapter.addMorePosts(posts)
        refreshing = false
    }


    override fun showCustomTabsUI(url: String) {
        CustomTabActivityHelper.openCustomTab(activity,
                MediaHelper.createCustomTabIntent(activity,
                        mCustomTabActivityHelper.session),
                url
        ) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }

    override fun showMedia(post: Post) {
        val intent = Intent(context, FullscreenActivity::class.java)
        intent.putExtra(FullscreenActivity.EXTRA_POST, post)
        intent.putExtra(FullscreenActivity.EXTRA_URL, post.previewImage)
        startActivity(intent)
    }

    override fun showCommentsUI(clickedPost: Post) {
        val intent = Intent(context, CommentActivity::class.java)
        intent.putExtra(CommentActivity.EXTRA_CLICKED_POST, clickedPost)
        startActivity(intent)
    }

    override fun launchCustomActivity(clickedPost: Post) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickedPost.url))
        startActivity(intent)
    }

    override fun onError(errorText: String) {
        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
    }

    override fun setListErrorButton(active: Boolean) {
        mListAdapter.setListLoadingError(active)
    }

    internal interface postClickListener {

        fun onButtonClick(url: String)

        fun onPostClick(clickedPost: Post)

        fun onMediaClick(post: Post)

        fun onRetryClick()

    }

    companion object {
        val ARGUMENT_SUBREDDIT = "subreddit"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param subreddit Chosen subreddit.
         * *
         * @return A new instance of fragment SubredditFragment.
         */
        fun newInstance(subreddit: Subreddit): SubredditFragment {
            val fragment = SubredditFragment()
            val args = Bundle()
            args.putParcelable(ARGUMENT_SUBREDDIT, subreddit)
            fragment.arguments = args
            return fragment
        }
    }
}
