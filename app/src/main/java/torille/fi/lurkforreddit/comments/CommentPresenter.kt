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
internal constructor(val mRedditRepository: RedditRepository,
                     val mPost: Post,
                     val mIsSingleCommentThread: Boolean) : CommentContract.Presenter {

    private lateinit var mCommentsView: CommentContract.View

    private val disposables = CompositeDisposable()

    override fun loadComments(permaLinkUrl: String, isSingleCommentThread: Boolean) {
        Timber.d("Loading comments for permalink $permaLinkUrl")
        mCommentsView.setProgressIndicator(true)
        disposables.add(mRedditRepository.getCommentsForPost(permaLinkUrl, isSingleCommentThread)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<PostAndComments>() {
                    override fun onNext(@io.reactivex.annotations.NonNull postAndComments: PostAndComments) {
                        val comments = ArrayList<Any>(postAndComments.comments.size + 1)
                        comments.add(postAndComments.originalPost)
                        comments.addAll(postAndComments.comments)
                        mCommentsView.showComments(comments)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e(e)
                        mCommentsView.setProgressIndicator(false)
                        mCommentsView.showError(e.toString())
                    }

                    override fun onComplete() {
                        Timber.d("Fetched comments")
                        mCommentsView.setProgressIndicator(false)
                    }
                }))
    }

    override fun loadMoreCommentsAt(parentComment: Comment, linkId: String, position: Int) {
        val level = parentComment.commentLevel
        mCommentsView.showProgressbarAt(position, level)
        val childCommentIds = parentComment.childCommentIds
        if (childCommentIds != null) {
            disposables.add(mRedditRepository.getMoreCommentsForPostAt(childCommentIds, linkId, parentComment.commentLevel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableObserver<List<Comment>>() {
                        override fun onNext(@io.reactivex.annotations.NonNull comments: List<Comment>) {
                            mCommentsView.hideProgressbarAt(position)
                            mCommentsView.addCommentsAt(comments, position)
                        }

                        override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                            Timber.e(e)
                            mCommentsView.showErrorAt(position)
                        }

                        override fun onComplete() {
                            Timber.d("Fetching more comments completed")
                        }
                    }))
        } else {
            mCommentsView.showError("How did you get here?")
        }
    }

    override fun takeView(view: CommentContract.View) {
        mCommentsView = view
        loadComments(mPost.permaLink, mIsSingleCommentThread)
    }

    override fun dropView() {
        disposables.dispose()
    }
}
