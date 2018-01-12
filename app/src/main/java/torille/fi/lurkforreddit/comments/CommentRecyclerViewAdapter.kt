package torille.fi.lurkforreddit.comments

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Comment
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.kind

internal class CommentRecyclerViewAdapter(private var mComments: MutableList<Any>, private val mClickListener: CommentFragment.CommentClickListener, private val quoteColor: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            COMMENT_ORIGINAL -> PostViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_clicked_post, parent, false))
            COMMENT_LOAD_MORE -> CommentLoadMoreViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_loadmore, parent, false))
            COMMENT_PROGRESSBAR -> ProgressViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_progressbar, parent, false))
            COMMENT_TOP -> TopSingleCommentThreadViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_comment_top_comment, parent, false))
        //COMMENT_NORMAL,
            else -> CommentViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_constraint, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CommentViewHolder -> holder.bind(mComments[position] as Comment)
            is CommentLoadMoreViewHolder -> holder.bind(mComments[position] as Comment)
            is PostViewHolder -> holder.bind(mComments[position] as Post)
            is ProgressViewHolder -> {
                holder.progressBar.isIndeterminate = true
                holder.bind(mComments[position] as Comment)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        if (position == 0) {
            return COMMENT_ORIGINAL
        } else if (mComments[position] is Comment) {
            return when ((mComments[position] as Comment).kind) {
                kind.SINGLECOMMENTTOP -> COMMENT_TOP
                kind.MORE -> COMMENT_LOAD_MORE
                kind.PROGRESSBAR -> COMMENT_PROGRESSBAR
                else -> COMMENT_NORMAL
            }
        }
        return COMMENT_NORMAL

    }

    override fun getItemCount(): Int {
        return mComments.size
    }

    fun replaceData(comments: List<Any>) {
        launch(UI) {
            kotlin.run {
                mComments.clear()
                mComments.addAll(comments)
            }
            notifyDataSetChanged()
        }


    }

    /**
     * Creates a progressbar with certain padding

     * @param level deepness of the comment
     * *              the bigger the level the bigger the left padding
     * *
     * @return progressbar with padding info
     */
    private fun createProgressbar(level: Int): Comment {
        return Comment(kind = kind.PROGRESSBAR, commentLevel = level)
    }

    fun addProgressbar(position: Int, level: Int) {
        Timber.d("Adding to " + position)
        if (position > 1) {
            mComments[position] = createProgressbar(level)
            notifyItemChanged(position)
        } else {
            mComments.add(position, createProgressbar(level))
            notifyItemInserted(position)

        }

    }

    fun changeToErrorAt(position: Int) {
        val error = mComments[position] as Comment
        val errorComment = error.copy(
                kind = kind.MORE,
                id = "Retry",
                commentText = "Retry",
                commentLevel = error.commentLevel)
        mComments[position] = errorComment
        notifyItemChanged(position, error)

    }

    fun removeAt(position: Int) {
        mComments.removeAt(position)
        notifyItemRemoved(position)

    }

    fun addAllCommentsTo(position: Int, comments: List<Comment>) {
        mComments.addAll(position, comments)
        notifyItemRangeInserted(position, comments.size)

    }

    internal inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val author: TextView = view.findViewById(R.id.comment_post_author)
        private val selftext: TextView = view.findViewById(R.id.comment_post_selftext)
        private val title: TextView = view.findViewById(R.id.comment_post_title)
        private val flairText: TextView = view.findViewById(R.id.comment_post_flair)

        init {
            selftext.transformationMethod = CustomLinkTransformationMethod(quoteColor)
            selftext.movementMethod = LinkMovementMethod.getInstance()
            selftext.linksClickable = true
        }

        fun bind(mClickedPost: Post) {
            val selftext = mClickedPost.selfText

            if (mClickedPost.title.isEmpty()) {
                author.text = ""
                title.text = ""
            } else {
                val time = DateUtils.getRelativeTimeSpanString(mClickedPost.createdUtc * 1000)
                val author = "${mClickedPost.score} points | Submitted $time by ${mClickedPost.author}"
                this.author.text = author
                title.text = mClickedPost.title
            }
            if (mClickedPost.flairText.isEmpty()) {
                flairText.visibility = View.GONE
            } else {
                flairText.visibility = View.VISIBLE
                flairText.text = mClickedPost.flairText
            }

            if (selfText.isNotEmpty()) {
                selftext.visibility = View.VISIBLE
                selftext.text = selfText
            } else {
                selftext.visibility = View.GONE
            }

        }
    }

    internal inner class TopSingleCommentThreadViewHolder(view: View) : CommentViewHolder(view)

    internal open inner class CommentViewHolder(view: View) : CommentNormalViewHolder(view) {
        private val commentText: TextView = view.findViewById(R.id.comment_text)
        private val commentScore: TextView = view.findViewById(R.id.comment_post_score)
        private val commentAuthor: TextView = view.findViewById(R.id.comment_author)

        init {
            commentText.linksClickable = true
            commentText.transformationMethod = CustomLinkTransformationMethod(quoteColor)
            commentText.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(comment: Comment) {
            mComment = comment
            commentText.text = mComment!!.commentText
            commentAuthor.text = mComment!!.author
            commentScore.text = mComment!!.formattedScore
        }

    }

    internal open inner class CommentNormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mComment: Comment? = null

    }

    internal inner class CommentLoadMoreViewHolder(view: View) : CommentNormalViewHolder(view), View.OnClickListener {
        private val clickMore: TextView = view.findViewById(R.id.comment_loadmore)

        init {
            view.setOnClickListener(this)
        }

        fun bind(commentChild: Comment) {
            mComment = commentChild
            clickMore.text = mComment!!.commentText
        }

        override fun onClick(v: View) {
            val clickedComment = mComment
            Timber.d(clickedComment?.toString())
            val (id, _, _, permaLink) = mComments[0] as Post
            clickedComment?.let {
                if (it.childCommentIds != null && it.childCommentIds.isNotEmpty()) {
                    mClickListener.onClick(mComment!!, id, adapterPosition)
                } else {
                    //remove t1_ from start of subId
                    val parentId = clickedComment.parentId.substring(3)
                    val permalinkToComment = "https://www.reddit.com$permaLink$parentId"
                    Timber.d("Going to open permalink $permalinkToComment")
                    mClickListener.onContinueThreadClick(permalinkToComment)
                }
            }

        }

    }

    internal inner class ProgressViewHolder(view: View) : CommentNormalViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        fun bind(commentChild: Comment) {
            mComment = commentChild
        }
    }

    companion object {
        private val COMMENT_ORIGINAL = -1
        private val COMMENT_PROGRESSBAR = -2
        private val COMMENT_LOAD_MORE = -3
        private val COMMENT_NORMAL = -4
        private val COMMENT_TOP = -5
    }
}