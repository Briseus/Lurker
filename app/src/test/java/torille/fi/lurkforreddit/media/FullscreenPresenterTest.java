package torille.fi.lurkforreddit.media;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the implementation of {@link FullscreenPresenter}
 */
public class FullscreenPresenterTest {

    private static final String imgurGifv = "https://i.imgur.com/Uq5DRSk.gifv";

    private static final String imageUrl = "https://i.redd.it/qhmesl7h9wsy.jpg";

    private static final String mp4link = "https://test.com/test.mp4";

    private static final String previewImageUrl = null;

    @Mock
    private FullscreenContract.View mFullscreenView;

    private FullscreenPresenter mFullscreenPresenter;

    @Before
    public void setupFullscreenPresenter() {
        MockitoAnnotations.initMocks(this);
        mFullscreenPresenter = new FullscreenPresenter(mFullscreenView);
    }

    @Test
    public void loadImageIntoView() {
        mFullscreenPresenter.checkType(imageUrl, previewImageUrl);
        Mockito.verify(mFullscreenView).showImage(imageUrl, previewImageUrl);
    }

    @Test
    public void loadVideoIntoView() {
        mFullscreenPresenter.checkType(mp4link, null);
        Mockito.verify(mFullscreenView).showVideo(mp4link);
    }

    @Test
    public void loadImgurVideoIntoView() {
        mFullscreenPresenter.checkType(imgurGifv, null);
        Mockito.verify(mFullscreenView).showVideo(Matchers.anyString());
    }


}
