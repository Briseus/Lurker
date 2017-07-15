package torille.fi.lurkforreddit.media

interface FullscreenContract {

    interface View {
        fun showImage(url: String, previewImageUrl: String?)

        fun showVideo(url: String)

        fun showStreamableVideo(identifier: String)

        fun checkDomain(url: String)
    }

    interface Presenter {
        fun checkType(url: String,
                      previewImageUrl: String?)
    }

}
