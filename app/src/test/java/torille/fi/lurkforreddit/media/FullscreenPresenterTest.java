package torille.fi.lurkforreddit.media;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import torille.fi.lurkforreddit.data.models.Post;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link FullscreenPresenter}
 */

public class FullscreenPresenterTest {


    private static Post mImagePost = new Post();
    private static Post videoPost = new Post();
    private static Post gfycatPost = new Post();
    private static Post imgurVideoPost = new Post();

    @Mock
    private FullscreenContract.View mFullscreenView;

    private FullscreenPresenter mFullscreenPresenter;

    @Before
    public void setupFullscreenPresenter() {
        MockitoAnnotations.initMocks(this);
        mFullscreenPresenter = new FullscreenPresenter(mFullscreenView);
    }

    @Before
    public void setupPosts() {
        mImagePost.getPostDetails().setDomain("i.imgur.com");
        mImagePost.getPostDetails().setUrl("https://i.imgur.com/dogs.jpg");

        videoPost.getPostDetails().setDomain("somedomain");
        videoPost.getPostDetails().setUrl("https://wwww.somegif.com/dogs.mp4");

        gfycatPost.getPostDetails().setDomain("gfycat.com");
        gfycatPost.getPostDetails().setUrl("https://wwww.gfycat.com/HorseBananaTest");

        imgurVideoPost.getPostDetails().setDomain("i.imgur.com");
        imgurVideoPost.getPostDetails().setUrl("https://i.imgur.com/cats.gifv");
    }

    @Test
    public void loadImageIntoView() {
        mFullscreenPresenter.checkDomain(mImagePost);
        verify(mFullscreenView).showImage(anyString());
    }

    @Test
    public void loadGfycatIntoView() {
        mFullscreenPresenter.checkDomain(gfycatPost);
        verify(mFullscreenView).showGfycatVideo(anyString());
    }

    @Test
    public void loadVideoIntoView() {
        mFullscreenPresenter.checkDomain(videoPost);
        verify(mFullscreenView).showVideo(anyString());
    }

    @Test
    public void loadImgurVideoIntoView() {
        mFullscreenPresenter.checkDomain(imgurVideoPost);
        verify(mFullscreenView).showVideo(anyString());
    }

}
