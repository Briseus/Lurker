package torille.fi.lurkforreddit.media

import torille.fi.lurkforreddit.utils.TextHelper

class FullscreenPresenter internal constructor(private val mFullscreenView: FullscreenContract.View) : FullscreenContract.Presenter {

    override fun checkType(url: String, previewImageUrl: String?) {
        when (TextHelper.getLastFourChars(url)) {
            ".gif", "webp", ".png", ".jpg", "jpeg" -> {
                mFullscreenView.showImage(url, previewImageUrl)
            }
            "gifv" -> {
                mFullscreenView.showVideo(url.substring(0, url.length - 4) + "mp4")
            }
            "webm", ".mp4" -> {
                mFullscreenView.showVideo(url)
            }
            else -> mFullscreenView.checkDomain(url)
        }
    }
}
