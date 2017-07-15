package torille.fi.lurkforreddit.comments

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
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Comment
import torille.fi.lurkforreddit.data.models.view.Post
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * Use the [CommentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommentFragment : Fragment(), CommentContract.View {

    private lateinit var commentComponent: CommentComponent
    private lateinit var mCommentAdapter: CommentRecyclerViewAdapter
    private lateinit var mPost: Post
    private var mIsSingleCommentThread: Boolean = false

    @Inject internal lateinit var mActionsListener: CommentContract.Presenter<CommentContract.View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPost = arguments.getParcelable<Post>(ARGUMENT_CLICKED_POST)
        mIsSingleCommentThread = arguments.getBoolean(ARGUMENT_IS_SINGLE_COMMENT_THREAD, false)

        commentComponent = DaggerCommentComponent.builder()
                .redditRepositoryComponent((activity.application as MyApplication).getmRedditRepositoryComponent())
                .commentPresenterModule(CommentPresenterModule(mPost, mIsSingleCommentThread))
                .build()

        mCommentAdapter = CommentRecyclerViewAdapter(setupList(mPost), mClickListener)
    }

    private fun setupList(post: Post): MutableList<Any> {
        val list = ArrayList<Any>()
        list.add(post)
        return mutableListOf(post)
    }

    override fun onResume() {
        super.onResume()
        loadIfEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionsListener.dispose()
    }

    private fun loadIfEmpty() {
        if (mCommentAdapter.itemCount == 1) {
            mActionsListener.start()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater!!.inflate(R.layout.fragment_comments, container, false)
        commentComponent.inject(this)
        mActionsListener.setView(this)
        val context = context

        val recyclerView = root.findViewById(R.id.comments_list) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(CommentsItemDecoration(ContextCompat.getDrawable(context, R.drawable.comment_item_decorator)))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mCommentAdapter

        val mSwipeRefreshLayout = root.findViewById(R.id.refresh_layout) as SwipeRefreshLayout

        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))

        mSwipeRefreshLayout.setOnRefreshListener { mActionsListener.loadComments(mPost.permaLink, mIsSingleCommentThread) }
        return root
    }

    override fun showComments(comments: List<Any>) {
        mCommentAdapter.replaceData(comments)
    }

    override fun showProgressbarAt(position: Int, level: Int) {
        mCommentAdapter.addProgressbar(position, level)
    }

    override fun hideProgressbarAt(position: Int) {
        mCommentAdapter.removeAt(position)
    }

    override fun addCommentsAt(comments: List<Comment>, position: Int) {
        if (!comments.isEmpty()) {
            mCommentAdapter.addAllCommentsTo(position, comments)
        }
    }

    override fun showError(errorText: String) {
        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
    }

    override fun showErrorAt(position: Int) {
        mCommentAdapter.changeToErrorAt(position)
    }

    override fun setProgressIndicator(active: Boolean) {
        if (view == null) {
            return
        }
        val srl = view!!.findViewById(R.id.refresh_layout) as SwipeRefreshLayout
        srl.post { srl.isRefreshing = active }

    }

    /**
     * Listener for clicks on notes in the RecyclerView.
     */
    private val mClickListener = object : CommentClickListener {
        override fun onClick(parentComment: Comment, linkId: String, position: Int) {
            mActionsListener.loadMoreCommentsAt(parentComment, linkId, position)
        }

        override fun onContinueThreadClick(permaLinkurl: String) {

            val intent = Intent(context, CommentActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(permaLinkurl)
            startActivity(intent)
        }
    }


    internal interface CommentClickListener {
        fun onClick(parentComment: Comment, linkId: String, position: Int)

        fun onContinueThreadClick(permaLinkurl: String)
    }

    companion object {

        private val ARGUMENT_CLICKED_POST = "post"
        private val ARGUMENT_IS_SINGLE_COMMENT_THREAD = "single"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param clickedPost the PostResponse which comments are to be loaded
         * *
         * @return A new instance of fragment CommentFragment.
         */
        fun newInstance(clickedPost: Post, isSingleCommentThread: Boolean): CommentFragment {
            val fragment = CommentFragment()
            val args = Bundle()
            args.putParcelable(ARGUMENT_CLICKED_POST, clickedPost)
            args.putBoolean(ARGUMENT_IS_SINGLE_COMMENT_THREAD, isSingleCommentThread)
            fragment.arguments = args
            return fragment
        }
    }

}
