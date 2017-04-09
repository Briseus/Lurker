package torille.fi.lurkforreddit.subreddits;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.Subreddit;
import torille.fi.lurkforreddit.data.models.SubredditChildren;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link SubredditsPresenter}
 */

public class SubredditsPresenterTest {

    private static final Subreddit SUBREDDIT_PICS = new Subreddit("2qh0u", "Reddit Pics", "/r/pics", "t5_2qh0u", "#222222", "pics", "", false, 15295521, true, (long) 1201221069, null, null);
    private static final Subreddit SUBREDDIT_WORLDNEWS = new Subreddit("2qh13", "World News", "/r/worldnews", "t5_2qh13", "", "worldnews", "", false, 15325795, true, (long) 1201231119, null, null);

    private static final SubredditChildren PICS = new SubredditChildren("t5", SUBREDDIT_PICS);
    private static final SubredditChildren WORLDNEWS = new SubredditChildren("t5", SUBREDDIT_WORLDNEWS);

    private static final List<SubredditChildren> SUBREDDITS = Arrays.asList(PICS, WORLDNEWS);

    @Mock
    private RedditRepository mRedditRepository;

    @Mock
    private SubredditsContract.View mSubredditsView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<RedditRepository.LoadSubredditsCallback> loadSubredditsCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<RedditRepository.ErrorCallback> loadErrorCallbackArgumentCaptor;

    private SubredditsPresenter mSubredditsPresenter;

    @Before
    public void setupSubredditsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mSubredditsPresenter = new SubredditsPresenter(mRedditRepository, mSubredditsView);
    }

    @Test
    public void loadSubredditsFromRepositoryAndLoadIntoView() {
        mSubredditsPresenter.loadSubreddits(true);

        verify(mRedditRepository).getSubreddits(loadSubredditsCallbackArgumentCaptor.capture(), loadErrorCallbackArgumentCaptor.capture());
        loadSubredditsCallbackArgumentCaptor.getValue().onSubredditsLoaded(SUBREDDITS);

        verify(mSubredditsView).setProgressIndicator(false);
        verify(mSubredditsView).showSubreddits(SUBREDDITS);

    }

    @Test
    public void clickOnSubreddit_ShowsSubredditUi() {

        mSubredditsPresenter.openSubreddit(SUBREDDIT_PICS);

        verify(mSubredditsView).loadSelectedSubreddit(any(Subreddit.class));

    }

}
