package torille.fi.lurkforreddit.comments

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.jetbrains.annotations.TestOnly
import org.junit.Before
import org.junit.Test

import io.reactivex.subscribers.TestSubscriber
import org.mockito.*
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.Comment
import torille.fi.lurkforreddit.data.models.view.Post

import org.mockito.Matchers.any
import org.mockito.Matchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import torille.fi.lurkforreddit.data.models.view.PostAndComments
import torille.fi.lurkforreddit.data.remote.FakeRedditRepository
import java.util.*

/**
 * Unit tests for the implementation of [CommentPresenter]
 */

class CommentPresenterTest {

    private val mCommentTestSubscriber: TestSubscriber<List<Comment>>? = null

    @Mock
    private val mockComments: List<Comment>? = null

    @Mock
    private val clickedPost: Post? = null

    @Mock
    private val postAndComments: PostAndComments = PostAndComments(clickedPost!!, mockComments!!)

    @Mock
    private val mockParentComment: Comment? = null

    @Mock
    private var mRedditRepository: RedditRepository? = null

    @Mock
    private val mCommentView: CommentContract.View? = null

    private lateinit var mCommentPresenter: CommentPresenter

    @Before
    fun setupCommentPresenter() {
        MockitoAnnotations.initMocks(this)
        mRedditRepository = RedditRepository(FakeRedditRepository())
        mCommentPresenter = CommentPresenter(mRedditRepository!!, clickedPost!!, false)
        mCommentPresenter.setView(mCommentView!!)
    }

    @Test
    fun loadCommentsFromRepositoryAndLoadIntoView() {
        mCommentPresenter.loadComments(mockLinkId, false)
        verify<CommentContract.View>(mCommentView).setProgressIndicator(true)

        val testObserver = TestObserver<PostAndComments>()
        mRedditRepository!!
                .getCommentsForPost(mockLinkId, false)
                .subscribe(testObserver)

        verify(mRedditRepository!!).getCommentsForPost(mockLinkId, false)
        verify<CommentContract.View>(mCommentView).setProgressIndicator(false)
        verify<CommentContract.View>(mCommentView).showComments(mockComments!!)
    }

    @Test
    fun loadMoreCommentsFromRepositoryAndLoadIntoView() {
       /* mCommentPresenter.loadMoreCommentsAt(mockParentComment!!, mockLinkId, mockPosition)
        verify<RedditRepository>(mRedditRepository).getMoreCommentsForPostAt(any(Comment::class.java),
                any(String::class.java),
                any(Int::class.javaPrimitiveType),
                loadPostCommentsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture())
        loadPostCommentsCallbackArgumentCaptor.getValue().onMoreCommentsLoaded(mockComments, mockPosition)
        verify<CommentContract.View>(mCommentView).hideProgressbarAt(mockPosition)
        verify<CommentContract.View>(mCommentView).addCommentsAt(mockComments!!, mockPosition)*/
    }

    private fun setComments(redditDataSource: RedditDataSource, mockId: String, isSingleCommentThread: Boolean) {
        `when`(redditDataSource.getCommentsForPost(ArgumentMatchers.eq(mockId), ArgumentMatchers.eq(isSingleCommentThread)))
                .thenReturn(Observable.just(postAndComments))
    }

    companion object {

        private val mockLinkId = "linkid"
        private val mockPosition = 5
    }

}
