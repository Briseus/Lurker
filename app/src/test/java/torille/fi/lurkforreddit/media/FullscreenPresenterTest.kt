package torille.fi.lurkforreddit.media


import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the implementation of [FullscreenPresenter]
 */
class FullscreenPresenterTest {

    @Mock
    private val mFullscreenView: FullscreenContract.View? = null

    private var mFullscreenPresenter: FullscreenPresenter? = null

    @Before
    fun setupFullscreenPresenter() {
        MockitoAnnotations.initMocks(this)
        mFullscreenPresenter = FullscreenPresenter(mFullscreenView!!)
    }

    @Test
    fun loadImageIntoView() {
        mFullscreenPresenter!!.checkType(imageUrl, previewImageUrl)
        Mockito.verify<FullscreenContract.View>(mFullscreenView).showImage(imageUrl, previewImageUrl)
    }

    @Test
    fun loadVideoIntoView() {
        mFullscreenPresenter!!.checkType(mp4link, null)
        Mockito.verify<FullscreenContract.View>(mFullscreenView).showVideo(mp4link)
    }

    @Test
    fun loadImgurVideoIntoView() {
        mFullscreenPresenter!!.checkType(imgurGifv, null)
        Mockito.verify<FullscreenContract.View>(mFullscreenView).showVideo(Matchers.anyString())
    }

    companion object {

        private val imgurGifv = "https://i.imgur.com/Uq5DRSk.gifv"

        private val imageUrl = "https://i.redd.it/qhmesl7h9wsy.jpg"

        private val mp4link = "https://test.com/test.mp4"

        private val previewImageUrl: String? = null
    }


}
