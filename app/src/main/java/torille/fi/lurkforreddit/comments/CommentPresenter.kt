package torille.fi.lurkforreddit.comments

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.Comment
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.PostAndComments
import java.util.*
import javax.inject.Inject

class CommentPresenter @Inject
internal constructor(
    private val redditRepository: RedditRepository,
    val post: Post,
    private val isSingleCommentThread: Boolean
) : CommentContract.Presenter {

    private lateinit var commentsView: CommentContract.View
    private val disposables = CompositeDisposable()
    private var firstLoad = true

    override fun loadComments(permaLinkUrl: String, isSingleCommentThread: Boolean) {
        Timber.d("Loading comments for permalink $permaLinkUrl")
        commentsView.setProgressIndicator(true)
        disposables.add(redditRepository.getCommentsForPost(permaLinkUrl, isSingleCommentThread)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<PostAndComments>() {
                override fun onNext(postAndComments: PostAndComments) {
                    val comments = ArrayList<Any>(postAndComments.comments.size + 1)
                    Timber.d("Hmm ${postAndComments.originalPost}")
                    comments.add(postAndComments.originalPost)
                    comments.addAll(postAndComments.comments)
                    commentsView.showComments(comments)
                }

                override fun onError(e: Throwable) {
                    Timber.e(e)
                    commentsView.setProgressIndicator(false)
                    commentsView.showError(e.toString())
                }

                override fun onComplete() {
                    Timber.d("Fetched comments")
                    commentsView.setProgressIndicator(false)
                }
            })
        )
    }

    override fun loadMoreCommentsAt(parentComment: Comment, linkId: String, position: Int) {
        val level = parentComment.commentLevel
        commentsView.showProgressbarAt(position, level)
        val childCommentIds = parentComment.childCommentIds
        if (childCommentIds != null) {
            disposables.add(redditRepository.getMoreCommentsForPostAt(
                childCommentIds,
                linkId,
                parentComment.commentLevel
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<List<Comment>>() {
                    override fun onNext(comments: List<Comment>) {
                        commentsView.hideProgressbarAt(position)
                        commentsView.addCommentsAt(comments, position)
                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e)
                        commentsView.showErrorAt(position)
                    }

                    override fun onComplete() {
                        Timber.d("Fetching more comments completed")
                    }
                })
            )
        } else {
            commentsView.showError("How did you get here?")
        }
    }

    override fun takeView(view: CommentContract.View) {
        commentsView = view
        if (firstLoad) {
            loadComments(post.permaLink, isSingleCommentThread)
            firstLoad = false
        }
    }

    override fun dropView() {
        disposables.dispose()
    }
}
