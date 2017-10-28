package torille.fi.lurkforreddit.subreddit

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.core.ImagePipeline
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import timber.log.Timber
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Post

internal class PostsAdapter internal constructor(private val mClicklistener: SubredditFragment.postClickListener, private val imagePipeline: ImagePipeline) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val posts: SortedList<Post>

    init {
        posts = SortedList(Post::class.java, object : SortedListAdapterCallback<Post>(this) {
            override fun compare(o1: Post, o2: Post): Int {
                if (o1.id == o2.id) {
                    return 0
                }
                return -1
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(item1: Post, item2: Post): Boolean {
                return item1.id == item2.id
            }
        }, 25)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_ITEM -> PostViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_post_small, parent, false))
            VIEW_ERROR -> ErrorViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_error, parent, false))
            else -> ProgressViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_progressbar, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PostViewHolder) {
            holder.bind(posts.get(position))
        } else if (holder is ProgressViewHolder) {
            holder.progressBar.isIndeterminate = true
        }

    }

    /**
     * Prefetch to bitmap cache in fresco to make
     * images appear without fading or waiting

     * @param fromIndex     tells the position in posts
     * *
     * @param prefetchCount how much to cache ahead
     * *
     * @param listMaxSize   the maxsize which you cant go over
     */
    internal fun prefetchImages(fromIndex: Int, prefetchCount: Int, listMaxSize: Int) {

        val amount = fromIndex + prefetchCount
        /*
            make toIndex value of "to" only if its not bigger than maxsize
            so you dont get indexOutOfBounds errors
             */
        val toIndex = if (amount > listMaxSize) listMaxSize else amount

        (fromIndex until toIndex)
                .map { posts.get(it).previewImage }
                .forEach { imagePipeline.prefetchToBitmapCache(ImageRequest.fromUri(it), null) }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            posts.get(position).id == PROGRESSBAR -> VIEW_PROGRESS
            posts.get(position).id == ERROR -> VIEW_ERROR
            else -> VIEW_ITEM
        }

    }

    internal fun addAll(list: List<Post>) {
        posts.beginBatchedUpdates()
        list.map { posts.add(it) }
        posts.endBatchedUpdates()
    }

    internal fun addMorePosts(newPosts: List<Post>) {
        addAll(newPosts)
    }

    internal fun clear() {
        posts.clear()

    }

    /**
     * To use setStableIds and get viewtype,
     * add identifiers to a dummy post
     * that is used in the progressbar
     */
    internal fun setRefreshing(active: Boolean) {
        if (active) {
            posts.add(createPost(PROGRESSBAR))
        } else {
            posts.removeItemAt(getIndex())
        }

    }

    internal fun setListLoadingError(active: Boolean) {
        val index = getIndex()
        if (active) {
            posts.updateItemAt(index, createPost(ERROR))
        } else {
            posts.removeItemAt(index)
        }

    }

    private fun getIndex(): Int {
        return posts.size() - 1
    }

    private fun createPost(id: String): Post {
        return Post(id)
    }

    override fun getItemCount(): Int {
        return posts.size()
    }

    internal fun getItem(position: Int): Post {
        return posts.get(position)
    }


    internal inner class PostViewHolder(postView: View) : RecyclerView.ViewHolder(postView) {

        private val title: TextView = postView.findViewById(R.id.post_title)
        private val domain: TextView = postView.findViewById(R.id.post_domain)
        private val flair: TextView = postView.findViewById(R.id.post_flair)
        private val comments: Button = postView.findViewById(R.id.post_messages)
        private val openBrowser: Button = postView.findViewById(R.id.post_open_browser)
        private val image: SimpleDraweeView = postView.findViewById(R.id.post_image)
        private val baseControllerListener: BaseControllerListener<ImageInfo>
        private val onClickListener: View.OnClickListener = View.OnClickListener { mClicklistener.onMediaClick(getItem(adapterPosition)) }

        init {
            postView.setOnClickListener(onClickListener)
            image.setOnClickListener(onClickListener)
            openBrowser.setOnClickListener { mClicklistener.onButtonClick(getItem(adapterPosition).url) }
            comments.setOnClickListener { mClicklistener.onPostClick(getItem(adapterPosition)) }
            baseControllerListener = object : BaseControllerListener<ImageInfo>() {
                override fun onFailure(id: String?, throwable: Throwable?) {
                    super.onFailure(id, throwable)
                    Timber.e("Failed to load image id: " + id + " error " + throwable!!.localizedMessage)
                    image.setImageURI(getItem(adapterPosition).thumbnail)
                }
            }
        }

        fun bind(postDetails: Post) {
            val previewImage = postDetails.previewImage
            val flairText = postDetails.flairText
            val infoText = postDetails.score + " |  " + postDetails.domain

            title.text = postDetails.title
            domain.text = infoText
            //score.setText(postDetails.score());
            comments.text = postDetails.numberOfComments

            if (flairText.isEmpty()) {
                flair.visibility = View.GONE
            } else {
                flair.visibility = View.VISIBLE
                flair.text = flairText
            }

            if (previewImage.isEmpty()) {
                image.visibility = View.GONE

            } else {
                image.visibility = View.VISIBLE

                val draweeController = Fresco.newDraweeControllerBuilder()
                        .setControllerListener(baseControllerListener)
                        .setImageRequest(ImageRequest.fromUri(previewImage))
                        .setOldController(image.controller)
                        .build()

                image.controller = draweeController

            }
        }
    }

    private inner class ErrorViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val retryButton: Button = v.findViewById(R.id.button)

        init {
            retryButton.setOnClickListener { mClicklistener.onRetryClick() }
        }
    }

    private class ProgressViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val progressBar: ProgressBar = v.findViewById(R.id.progressBar)
    }

    companion object {
        private val VIEW_ITEM = 1
        private val VIEW_PROGRESS = 0
        private val VIEW_ERROR = 2

        private val PROGRESSBAR = "progressbar"
        private val ERROR = "error"
    }
}