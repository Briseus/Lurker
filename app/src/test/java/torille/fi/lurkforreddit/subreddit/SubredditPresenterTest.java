package torille.fi.lurkforreddit.subreddit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.PostDetails;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.Subreddit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link SubredditPresenter}
 */

public class SubredditPresenterTest {
    private static String AFTER = "t3_5tkppe";

    private static Subreddit SUBREDDIT_WORLDNEWS = new Subreddit("2qh13", "World News", "/r/worldnews", "t5_2qh13", "", "worldnews", "", false, 15325795, true, (long) 1201231119, null, null);

    private static PostDetails POSTDETAILS_1_WORLDNEWS = new PostDetails(
            "worldnews",
            "",
            null,
            0,
            "askanier",
            "t3_5tich5",
            11970,
            "",
            "t5_2qh13",
            "http://www.bbc.com/news/world-asia-38947451?ns_mchannel=social&amp;ns_campaign=bbc_breaking&amp;ns_source=twitter&amp;ns_linkname=news_central",
            "Awesome title",
            null,
            "bbc.com",
            "5tich5",
            false,
            (long) 1486885305,
            (long) 1486856505,
            false,
            null,
            "/r/worldnews/comments/5tich5/north_korea_test_fires_ballistic_missile/",
            1874,
            null,
            "",
            "",
            "");

    private static PostDetails POSTDETAILS_2_WORLDNEWS = new PostDetails(
            "worldnews",
            "",
            null,
            0,
            "vich523",
            "t3_5tkad1",
            34,
            "",
            "t5_2qh13",
            "http://www.forbes.com/sites/panosmourdoukoutas/2017/02/11/china-tells-india-to-stay-off-its-indian-ocean-colony-sri-lanka/#7e8d62105f3a",
            "China Tells India To Stay Off Its Indian Ocean 'Colony,' Sri Lanka",
            null,
            "forbes.com",
            "5tkad1",
            false,
            (long) 1486913738,
            (long) 1486884938,
            false,
            null,
            "/r/worldnews/comments/5tkad1/china_tells_india_to_stay_off_its_indian_ocean/",
            25,
            null,
            null,
            null,
            null);

    private static Post POST_1_WORLDNEWS = new Post("t3", POSTDETAILS_1_WORLDNEWS);
    private static Post POST_2_WORLDNEWS = new Post("t3", POSTDETAILS_2_WORLDNEWS);

    private static Post POST_SELFPOST = new Post();
    private static PostDetails POSTDETAILS_SELFPOST = new PostDetails();

    private static Post POST_CUSTOMURL = new Post();
    private static PostDetails POSTDETAILS_CUSTOMURL = new PostDetails();

    private static Post POST_MEDIA = new Post();
    private static PostDetails POSTDETAILS_MEDIA = new PostDetails();

    private static Post POST_LAUNCH_ACTIVITY = new Post();
    private static PostDetails POSTDETAILS_LAUNCH_ACTIVITY = new PostDetails();

    private static List<Post> POSTS = Arrays.asList(POST_1_WORLDNEWS, POST_2_WORLDNEWS);

    @Mock
    private RedditRepository mRedditRepository;

    @Mock
    private SubredditContract.View mSubredditView;

    @Captor
    private ArgumentCaptor<RedditRepository.LoadSubredditPostsCallback> mLoadPostsCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<RedditRepository.ErrorCallback> loadErrorCallbackArgumentCaptor;
    
    private SubredditPresenter mSubredditPresenter;

    @Before
    public void setupSubredditPresenter() {
        MockitoAnnotations.initMocks(this);
        mSubredditPresenter = new SubredditPresenter(mRedditRepository, mSubredditView);
    }

    @Before
    public void setupPosts() {
        POSTDETAILS_SELFPOST.setSelf(true);
        POSTDETAILS_SELFPOST.setUrl("www.reddit.com/r/comments");
        POSTDETAILS_SELFPOST.setDomain("reddit.com");
        POST_SELFPOST.setPostDetails(POSTDETAILS_SELFPOST);

        POSTDETAILS_CUSTOMURL.setSelf(false);
        POSTDETAILS_CUSTOMURL.setDomain("reddit.com");
        POSTDETAILS_CUSTOMURL.setUrl("reddit.com/test");
        POST_CUSTOMURL.setPostDetails(POSTDETAILS_CUSTOMURL);

        POSTDETAILS_LAUNCH_ACTIVITY.setDomain("youtube.com");
        POSTDETAILS_LAUNCH_ACTIVITY.setUrl("youtube.com/video");
        POST_LAUNCH_ACTIVITY.setPostDetails(POSTDETAILS_LAUNCH_ACTIVITY);

        POSTDETAILS_MEDIA.setUrl("imagesomewhere.com/cats.jpg");
        POSTDETAILS_MEDIA.setDomain("imagur");
        POST_MEDIA.setPostDetails(POSTDETAILS_MEDIA);
    }

    @Test
    public void loadPostsFromRepositoryAndLoadIntoView() {
        mSubredditPresenter.loadPosts(SUBREDDIT_WORLDNEWS.getUrl());

        verify(mRedditRepository).getSubredditPosts(any(String.class),
                mLoadPostsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture());
        mLoadPostsCallbackArgumentCaptor.getValue().onPostsLoaded(POSTS, AFTER);

        verify(mSubredditView).setProgressIndicator(false);
        verify(mSubredditView).showPosts(POSTS, AFTER);
    }

    @Test
    public void loadMorePostsFromRepositoryAndLoadIntoView() {
        mSubredditPresenter.loadMorePosts(SUBREDDIT_WORLDNEWS.getUrl(), AFTER);

        verify(mRedditRepository).getMoreSubredditPosts(any(String.class),
                any(String.class),
                mLoadPostsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture());
        mLoadPostsCallbackArgumentCaptor.getValue().onPostsLoaded(POSTS, AFTER);

        verify(mSubredditView).setListProgressIndicator(false);
        verify(mSubredditView).addMorePosts(POSTS, AFTER);
    }

    @Test
    public void clickOnBrowser_ShowCustomTabsUi() {
        mSubredditPresenter.openCustomTabs(POSTDETAILS_1_WORLDNEWS.getUrl());
        verify(mSubredditView).showCustomTabsUI(any(String.class));
    }

    @Test
    public void clickOnComments_ShowCommentsUi() {
        mSubredditPresenter.openComments(POST_1_WORLDNEWS);
        verify(mSubredditView).showCommentsUI(any(Post.class));
    }

    @Test
    public void clickOnView_ShowCommentsUi() {
        mSubredditPresenter.openMedia(POST_SELFPOST);
        verify(mSubredditView).showCommentsUI(any(Post.class));
    }

    @Test
    public void clickOnView_ShowCustomTabsUi() {
        mSubredditPresenter.openMedia(POST_CUSTOMURL);
        verify(mSubredditView).showCustomTabsUI(POST_CUSTOMURL.getPostDetails().getUrl());
    }

    @Test
    public void clickOnView_ShowActivity() {
        mSubredditPresenter.openMedia(POST_LAUNCH_ACTIVITY);
        verify(mSubredditView).launchCustomActivity(any(Post.class));
    }

    @Test
    public void clickOnView_ShowMedia() {
        mSubredditPresenter.openMedia(POST_MEDIA);
        verify(mSubredditView).showMedia(POST_MEDIA);
    }

}
