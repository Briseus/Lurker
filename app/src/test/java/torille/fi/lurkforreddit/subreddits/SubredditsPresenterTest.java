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
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link SubredditsPresenter}
 */

public class SubredditsPresenterTest {

    private static final Subreddit SUBREDDIT_PICS = Subreddit.builder()
            .build();

    private static final Subreddit SUBREDDIT_WORLDNEWS = Subreddit.builder()
            .build();


    private static final List<Subreddit> SUBREDDITS = Arrays.asList(SUBREDDIT_PICS, SUBREDDIT_WORLDNEWS);

    @Mock
    private RedditRepository mRedditRepository;

    @Mock
    private SubredditsContract.View mSubredditsView;

    private SubredditsPresenter mSubredditsPresenter;

    @Before
    public void setupSubredditsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        mSubredditsPresenter = new SubredditsPresenter(mRedditRepository);
        mSubredditsPresenter.setView(mSubredditsView);
    }

    @Test
    public void loadSubredditsFromRepositoryAndLoadIntoView() {
        mSubredditsPresenter.loadSubreddits(true);

        //verify(mRedditRepository).getSubreddits(loadSubredditsCallbackArgumentCaptor.capture(), loadErrorCallbackArgumentCaptor.capture());
        //loadSubredditsCallbackArgumentCaptor.getValue().onSubredditsLoaded(SUBREDDITS);

        verify(mSubredditsView).setProgressIndicator(false);
        verify(mSubredditsView).showSubreddits(SUBREDDITS);

    }

    @Test
    public void clickOnSubreddit_ShowsSubredditUi() {

        mSubredditsPresenter.openSubreddit(SUBREDDIT_PICS);

        verify(mSubredditsView).loadSelectedSubreddit(any(Subreddit.class));

    }

}
