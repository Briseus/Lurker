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

class CommentFragment @Inject constructor() : DaggerFragment(), CommentContract.View {

    @Inject internal lateinit var post: Post
    @Inject internal lateinit var actionsListener: CommentContract.Presenter
    @Inject @JvmField var singleCommentThread: Boolean = false

    private lateinit var commentAdapter: CommentRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentAdapter = CommentRecyclerViewAdapter(setupList(post), mClickListener)
    }

    private fun setupList(post: Post): MutableList<Any> {
        val list = ArrayList<Any>()
        list.add(post)
        return mutableListOf(post)
    }

    override fun onResume() {
        super.onResume()
        actionsListener.takeView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        actionsListener.dropView()
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
        commentRecyclerView.adapter = commentAdapter


        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(context, R.color.colorPrimary),
                ContextCompat.getColor(context, R.color.colorAccent),
                ContextCompat.getColor(context, R.color.colorPrimaryDark))

        refreshLayout.setOnRefreshListener { actionsListener.loadComments(post.permaLink, singleCommentThread) }
    }

    override fun showComments(comments: List<Any>) {
        commentAdapter.replaceData(comments)
    }

    override fun showProgressbarAt(position: Int, level: Int) {
        commentAdapter.addProgressbar(position, level)
    }

    override fun hideProgressbarAt(position: Int) {
        commentAdapter.removeAt(position)
    }

    override fun addCommentsAt(comments: List<Comment>, position: Int) {
        if (!comments.isEmpty()) {
            commentAdapter.addAllCommentsTo(position, comments)
        }
    }

    override fun showError(errorText: String) {
        Toast.makeText(context, errorText, Toast.LENGTH_SHORT).show()
    }

    override fun showErrorAt(position: Int) {
        commentAdapter.changeToErrorAt(position)
    }

    override fun setProgressIndicator(active: Boolean) {
        refreshLayout.post { refreshLayout.isRefreshing = active }
    }

    /**
     * Listener for clicks on notes in the RecyclerView.
     */
    private val mClickListener = object : CommentClickListener {
        override fun onClick(parentComment: Comment, linkId: String, position: Int) {
            actionsListener.loadMoreCommentsAt(parentComment, linkId, position)
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

}
