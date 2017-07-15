package torille.fi.lurkforreddit.comments

import android.graphics.drawable.Animatable
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Comment
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.kind

internal class CommentRecyclerViewAdapter(private var mComments: MutableList<Any>, private val mClickListener: CommentFragment.CommentClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            COMMENT_ORIGINAL -> return PostViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_clicked_post, parent, false))
            COMMENT_LOAD_MORE -> return CommentLoadMoreViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_loadmore, parent, false))
            COMMENT_PROGRESSBAR -> return ProgressViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_progressbar, parent, false))
            COMMENT_TOP -> return TopSingleCommentThreadViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_comment_top_comment, parent, false))
        //COMMENT_NORMAL,
            else -> return CommentViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment_constraint, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? CommentViewHolder)?.bind(mComments[position] as Comment) ?: if (holder is ProgressViewHolder) {
            holder.progressBar.isIndeterminate = true
            holder.bind(mComments[position] as Comment)
        } else (holder as? CommentLoadMoreViewHolder)?.bind(mComments[position] as Comment) ?: (holder as? PostViewHolder)?.bind(mComments[position] as Post)
    }

    override fun getItemViewType(position: Int): Int {

        if (position == 0) {
            return COMMENT_ORIGINAL
        } else if (mComments[position] is Comment) {
            when ((mComments[position] as Comment).kind) {
                kind.SINGLECOMMENTTOP -> return COMMENT_TOP
                kind.MORE -> return COMMENT_LOAD_MORE
                kind.PROGRESSBAR -> return COMMENT_PROGRESSBAR
                else -> return COMMENT_NORMAL
            }
        }
        return COMMENT_NORMAL

    }

    override fun getItemCount(): Int {
        return mComments.size
    }

    fun replaceData(comments: List<Any>) {
        mComments.clear()
        mComments.addAll(comments)
        notifyDataSetChanged()

    }

    /**
     * Creates a progressbar with certain padding

     * @param level deepness of the comment
     * *              the bigger the level the bigger the left padding
     * *
     * @return progressbar with padding info
     */
    fun createProgressbar(level: Int): Comment {
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
        val comment = Comment(
                kind = kind.MORE,
                id = "Retry",
                commentText = "Retry",
                commentLevel = error.commentLevel)
        mComments[position] = comment
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
        val mAuthor: TextView = view.findViewById(R.id.comment_post_author) as TextView
        val mSelftext: TextView = view.findViewById(R.id.comment_post_selftext) as TextView
        val mTitle: TextView = view.findViewById(R.id.comment_post_title) as TextView
        val mFlairText: TextView = view.findViewById(R.id.comment_post_flair) as TextView
        val mImage: SimpleDraweeView = view.findViewById(R.id.comment_post_image) as SimpleDraweeView

        init {
            mSelftext.transformationMethod = CustomLinkTransformationMethod()
            mSelftext.movementMethod = LinkMovementMethod.getInstance()
            mSelftext.linksClickable = true
        }

        fun bind(mClickedPost: Post) {

            if (mClickedPost.title.isEmpty()) {
                mAuthor.text = ""
                mTitle.text = ""
            } else {
                val author = mClickedPost.score + " points | " + "Submitted " + DateUtils.getRelativeTimeSpanString(mClickedPost.createdUtc * 1000) + " by " + mClickedPost.author
                mAuthor.text = author
                mTitle.text = mClickedPost.title
            }
            if (mClickedPost.flairText.isEmpty()) {
                mFlairText.visibility = View.GONE
            } else {
                mFlairText.visibility = View.VISIBLE
                mFlairText.text = mClickedPost.flairText
            }

            if (mClickedPost.previewImage.isEmpty()) {
                //TODO if post doesnt have image but preview has the layout breaks
                mImage.visibility = View.GONE
            } else {
                mImage.visibility = View.VISIBLE
                val controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mImage.controller)
                        .setImageRequest(ImageRequest.fromUri(mClickedPost.previewImage))
                        .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                                super.onFinalImageSet(id, imageInfo, animatable)
                                val ap = imageInfo!!.width.toFloat() / imageInfo.height
                                mImage.aspectRatio = ap
                            }
                        })
                        .build()
                mImage.controller = controller
            }
            val selftext = mClickedPost.selfText
            if (selftext.isNotEmpty()) {
                mSelftext.text = selftext
            } else {
                mSelftext.visibility = View.GONE
            }


        }
    }

    internal inner class TopSingleCommentThreadViewHolder(view: View) : CommentViewHolder(view)

    internal open inner class CommentViewHolder(view: View) : CommentNormalViewHolder(view) {
        val mCommentText: TextView = view.findViewById(R.id.comment_text) as TextView
        val mCommentScore: TextView = view.findViewById(R.id.comment_post_score) as TextView
        val mCommentAuthor: TextView = view.findViewById(R.id.comment_author) as TextView

        init {
            mCommentText.linksClickable = true
            mCommentText.transformationMethod = CustomLinkTransformationMethod()
            mCommentText.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(comment: Comment) {
            mComment = comment
            mCommentText.text = mComment!!.commentText
            mCommentAuthor.text = mComment!!.author
            mCommentScore.text = mComment!!.formattedScore
        }

    }

    internal open inner class CommentNormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mComment: Comment? = null

    }

    internal inner class CommentLoadMoreViewHolder(view: View) : CommentNormalViewHolder(view), View.OnClickListener {
        val mClickMore: TextView = view.findViewById(R.id.comment_loadmore) as TextView

        init {
            view.setOnClickListener(this)
        }

        fun bind(commentChild: Comment) {
            mComment = commentChild
            mClickMore.text = mComment!!.commentText
        }

        override fun onClick(v: View) {
            Timber.d(mComment!!.toString())
            val (id, _, _, permaLink) = mComments[0] as Post
            if (mComment!!.id == "_") {

                //remove t1_ from start of id
                val parentId = mComment!!.parentId.substring(3)
                val permalinkToComment = "https://www.reddit.com" + permaLink + parentId
                Timber.d(permalinkToComment)
                mClickListener.onContinueThreadClick(permalinkToComment)
            } else {
                mClickListener.onClick(mComment!!, id, adapterPosition)
            }
        }


    }

    internal inner class ProgressViewHolder(view: View) : CommentNormalViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar) as ProgressBar

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