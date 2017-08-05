package torille.fi.lurkforreddit.subreddit

import android.util.Pair

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import java.util.ArrayList

import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditResponse
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.Subreddit

import org.mockito.Matchers.any
import org.mockito.Mockito.verify

/**
 * Unit tests for the implementation of [SubredditPresenter]
 */

class SubredditPresenterTest {


    private val POSTS = ArrayList<Post>()

    @Mock
    private val clickedPost: Post? = null

    @Mock
    private val DETAILS: PostDetails? = null

    @Mock
    private val subreddit: Subreddit? = null

    @Mock
    private val mRedditRepository: RedditRepository? = null

    @Mock
    private val mSubredditView: SubredditContract.View? = null

    private var mSubredditPresenter: SubredditPresenter? = null

    @Before
    fun setupSubredditPresenter() {
        MockitoAnnotations.initMocks(this)
        mSubredditPresenter = SubredditPresenter(mRedditRepository!!, subreddit!!)
        mSubredditPresenter!!.setView(mSubredditView!!)
    }


    @Test
    fun loadPostsFromRepositoryAndLoadIntoView() {


        val posts = Pair<String, List<Post>>(AFTER, POSTS)
        `when`(mRedditRepository!!.getSubredditPosts(anyString())).thenReturn(posts)
        mSubredditPresenter!!.loadPosts(SUBREDDIT_WORLDNEWS.url())
        verify<View>(mSubredditView).setProgressIndicator(false)
        verify<View>(mSubredditView).showPosts(POSTS, AFTER)
    }

    @Test
    fun loadMorePostsFromRepositoryAndLoadIntoView() {
        mSubredditPresenter!!.loadMorePosts(SUBREDDIT_WORLDNEWS.url(), AFTER)

        verify<RedditRepository>(mRedditRepository).getMoreSubredditPosts(any(String::class.java),
                any(String::class.java),
                mLoadPostsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture())
        mLoadPostsCallbackArgumentCaptor.getValue().onPostsLoaded(POSTS, AFTER)

        verify<View>(mSubredditView).setListProgressIndicator(false)
        verify<View>(mSubredditView).addMorePosts(POSTS, AFTER)
    }

    @Test
    fun clickOnBrowser_ShowCustomTabsUi() {
        mSubredditPresenter!!.openCustomTabs(DETAILS!!.url())
        verify<View>(mSubredditView).showCustomTabsUI(any(String::class.java))
    }

    @Test
    fun clickOnComments_ShowCommentsUi() {
        mSubredditPresenter!!.openComments(clickedPost!!)
        verify<View>(mSubredditView).showCommentsUI(any(Post::class.java))
    }

    @Test
    fun clickOnView_ShowCommentsUi() {
        mSubredditPresenter!!.openMedia(POST_SELFPOST)
        verify<View>(mSubredditView).showCommentsUI(any(Post::class.java))
    }

    @Test
    fun clickOnView_ShowCustomTabsUi() {
        mSubredditPresenter!!.openMedia(POST_CUSTOMURL)
        verify<View>(mSubredditView).showCustomTabsUI(POST_CUSTOMURL.url())
    }

    @Test
    fun clickOnView_ShowActivity() {
        mSubredditPresenter!!.openMedia(POST_LAUNCH_ACTIVITY)
        verify<View>(mSubredditView).launchCustomActivity(POST_LAUNCH_ACTIVITY)
    }

    @Test
    fun clickOnView_ShowMedia() {
        mSubredditPresenter!!.openMedia(POST_MEDIA)
        verify<View>(mSubredditView).showMedia(POST_MEDIA)
    }

    companion object {
        private val AFTER = "t3_5tkppe"

        private val SUBREDDIT_WORLDNEWS = SubredditResponse.Companion.builder()
                .setId("2qh13")
                .setTitle("world news")
                .setUrl("/r/worldnews")
                .setOver18(false)
                .setSubscribed(false)
                .setSubscribers(1000000)
                .setCreatedUtc(0)
                .build()

        private val POST_SELFPOST = Post.builder()
                .setIsSelf(true)
                .setUrl("www.reddit.com/r/comments")
                .setDomain("reddit.com")
                .setId("")
                .setThumbnail("")
                .setNumberOfComments("")
                .setSelfText("")
                .setTitle("")
                .setPermaLink("")
                .setScore("")
                .setPreviewImage("")
                .setAuthor("tester")
                .setCreatedUtc(10)
                .build()

        private val POST_CUSTOMURL = Post.builder()
                .setIsSelf(false)
                .setUrl("reddit.com/test")
                .setDomain("reddit.com")
                .setId("")
                .setThumbnail("")
                .setNumberOfComments("")
                .setSelfText("")
                .setTitle("")
                .setPermaLink("")
                .setScore("")
                .setPreviewImage("")
                .setAuthor("tester")
                .setCreatedUtc(10)
                .build()

        private val POST_MEDIA = Post.builder()
                .setIsSelf(false)
                .setUrl("imagesomewhere.com/cats.jpg")
                .setDomain("imagur")
                .setId("")
                .setThumbnail("")
                .setNumberOfComments("")
                .setSelfText("")
                .setTitle("")
                .setPermaLink("")
                .setScore("")
                .setPreviewImage("")
                .setAuthor("tester")
                .setCreatedUtc(10)
                .build()

        private val POST_LAUNCH_ACTIVITY = Post.builder()
                .setIsSelf(false)
                .setUrl("youtube.com/videourl")
                .setDomain("youtube.com")
                .setId("")
                .setThumbnail("")
                .setNumberOfComments("")
                .setSelfText("")
                .setTitle("")
                .setPermaLink("")
                .setScore("")
                .setPreviewImage("")
                .setAuthor("tester")
                .setCreatedUtc(10)
                .build()
    }

}
