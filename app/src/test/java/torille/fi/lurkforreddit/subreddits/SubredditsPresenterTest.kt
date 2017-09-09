package torille.fi.lurkforreddit.subreddits

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

import java.util.Arrays

import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.models.view.Subreddit

import org.mockito.Matchers.any
import org.mockito.Mockito.verify

/**
 * Unit tests for the implementation of [SubredditsPresenter]
 */

class SubredditsPresenterTest {

    @Mock
    private val mRedditRepository: RedditRepository = null

    @Mock
    private val mSubredditsView: SubredditsContract.View? = null

    private var mSubredditsPresenter: SubredditsPresenter? = null

    @Before
    fun setupSubredditsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        mSubredditsPresenter = SubredditsPresenter(mRedditRepository!!)
        mSubredditsPresenter!!.setView(mSubredditsView!!)
    }

    @Test
    fun loadSubredditsFromRepositoryAndLoadIntoView() {
        mSubredditsPresenter!!.loadSubreddits(true)

        verify<RedditRepository>(mRedditRepository).getSubreddits(loadSubredditsCallbackArgumentCaptor.capture(), loadErrorCallbackArgumentCaptor.capture())
        loadSubredditsCallbackArgumentCaptor.getValue().onSubredditsLoaded(SUBREDDITS)

        verify<SubredditsContract.View>(mSubredditsView).setProgressIndicator(false)
        verify<SubredditsContract.View>(mSubredditsView).showSubreddits(SUBREDDITS)

    }

    @Test
    fun clickOnSubreddit_ShowsSubredditUi() {

        mSubredditsPresenter!!.openSubreddit(SUBREDDIT_PICS)

        verify<SubredditsContract.View>(mSubredditsView).loadSelectedSubreddit(any(Subreddit::class.java))

    }

    companion object {

        private val SUBREDDIT_PICS = Subreddit()

        private val SUBREDDIT_WORLDNEWS = Subreddit()

        private val SUBREDDITS = Arrays.asList<Subreddit>(SUBREDDIT_PICS, SUBREDDIT_WORLDNEWS)
    }

}
