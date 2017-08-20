package torille.fi.lurkforreddit.comments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_comments.*
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
class CommentFragment @Inject constructor() : DaggerFragment(), CommentContract.View {

    private lateinit var mCommentAdapter: CommentRecyclerViewAdapter
    @Inject lateinit var mPost: Post
    var mIsSingleCommentThread: Boolean = false

    @Inject internal lateinit var mActionsListener: CommentContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIsSingleCommentThread = arguments.getBoolean(ARGUMENT_IS_SINGLE_COMMENT_THREAD, false)
        mCommentAdapter = CommentRecyclerViewAdapter(setupList(mPost), mClickListener)
    }

    private fun setupList(post: Post): MutableList<Any> {
        val list = ArrayList<Any>()
        list.add(post)
        return mutableListOf(post)
    }

    override fun onResume() {
        super.onResume()
        mActionsListener.takeView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mActionsListener.dropView()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_comments, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context

        commentRecyclerView.setHasFixedSize(true)
        commentRecyclerView.addItemDecoration(CommentsItemDecoration(ContextCompat.getDrawable(context, R.drawable.comment_item_decorator)))
        commentRecyclerView.layoutManager = LinearLayoutManager(context)
        commentRecyclerView.adapter = mCommentAdapter


        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))

        refreshLayout.setOnRefreshListener { mActionsListener.loadComments(mPost.permaLink, mIsSingleCommentThread) }
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
        refreshLayout.post { refreshLayout.isRefreshing = active }

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

        val ARGUMENT_CLICKED_POST = "post"
        val ARGUMENT_IS_SINGLE_COMMENT_THREAD = "single"

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
