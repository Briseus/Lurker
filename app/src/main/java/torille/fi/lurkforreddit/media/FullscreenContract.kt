package torille.fi.lurkforreddit.media

import torille.fi.lurkforreddit.BasePresenter
import torille.fi.lurkforreddit.BaseView

interface FullscreenContract {

    interface View : BaseView<Presenter> {

        fun showImage(url: String, previewImageUrl: String?)

        fun showVideo(url: String, isDash: Boolean = false)

        fun checkDomain(url: String)

        fun showNoVideoFound()
    }

    interface Presenter : BasePresenter<View> {

        fun checkType()

        fun checkStreamableVideo(id: String)
    }

}
