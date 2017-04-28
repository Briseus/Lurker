package torille.fi.lurkforreddit.subreddit;

import android.util.Pair;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditResponse;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link SubredditPresenter}
 */

public class SubredditPresenterTest {
    private static final String AFTER = "t3_5tkppe";

    private static final SubredditResponse SUBREDDIT_WORLDNEWS = SubredditResponse.builder()
            .setId("2qh13")
            .setTitle("world news")
            .setUrl("/r/worldnews")
            .setOver18(false)
            .setSubscribed(false)
            .setSubscribers(1000000)
            .setCreatedUtc(0)
            .build();

    private static final Post POST_SELFPOST = Post.builder()
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
            .build();

    private static final Post POST_CUSTOMURL = Post.builder()
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
            .build();

    private static final Post POST_MEDIA = Post.builder()
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
            .build();

    private static final Post POST_LAUNCH_ACTIVITY = Post.builder()
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
            .build();


    private List<Post> POSTS = new ArrayList<>();

    @Mock
    private Post clickedPost;

    @Mock
    private PostDetails DETAILS;

    @Mock
    private Subreddit subreddit;

    @Mock
    private RedditRepository mRedditRepository;

    @Mock
    private SubredditContract.View mSubredditView;

    private SubredditPresenter mSubredditPresenter;

    @Before
    public void setupSubredditPresenter() {
        MockitoAnnotations.initMocks(this);
        mSubredditPresenter = new SubredditPresenter(mRedditRepository, subreddit);
        mSubredditPresenter.setView(mSubredditView);
    }


    @Test
    public void loadPostsFromRepositoryAndLoadIntoView() {


        Pair<String, List<Post>> posts = new Pair<String, List<Post>>(AFTER, POSTS);
        //when(mRedditRepository.getSubredditPosts(anyString())).thenReturn(posts);
        mSubredditPresenter.loadPosts(SUBREDDIT_WORLDNEWS.url());
        verify(mSubredditView).setProgressIndicator(false);
        verify(mSubredditView).showPosts(POSTS, AFTER);
    }

    @Test
    public void loadMorePostsFromRepositoryAndLoadIntoView() {
        mSubredditPresenter.loadMorePosts(SUBREDDIT_WORLDNEWS.url(), AFTER);

        // verify(mRedditRepository).getMoreSubredditPosts(any(String.class),
        //        any(String.class),
        //        mLoadPostsCallbackArgumentCaptor.capture(),
        //        loadErrorCallbackArgumentCaptor.capture());
        // mLoadPostsCallbackArgumentCaptor.getValue().onPostsLoaded(POSTS, AFTER);

        verify(mSubredditView).setListProgressIndicator(false);
        verify(mSubredditView).addMorePosts(POSTS, AFTER);
    }

    @Test
    public void clickOnBrowser_ShowCustomTabsUi() {
        mSubredditPresenter.openCustomTabs(DETAILS.url());
        verify(mSubredditView).showCustomTabsUI(any(String.class));
    }

    @Test
    public void clickOnComments_ShowCommentsUi() {
        mSubredditPresenter.openComments(clickedPost);
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
        verify(mSubredditView).showCustomTabsUI(POST_CUSTOMURL.url());
    }

    @Test
    public void clickOnView_ShowActivity() {
        mSubredditPresenter.openMedia(POST_LAUNCH_ACTIVITY);
        verify(mSubredditView).launchCustomActivity(POST_LAUNCH_ACTIVITY);
    }

    @Test
    public void clickOnView_ShowMedia() {
        mSubredditPresenter.openMedia(POST_MEDIA);
        verify(mSubredditView).showMedia(POST_MEDIA);
    }

}
