package torille.fi.lurkforreddit.media

import dagger.Lazy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.VideositeService
import torille.fi.lurkforreddit.data.models.jsonResponses.StreamableVideo
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.utils.TextHelper
import javax.inject.Inject

class FullscreenPresenter @Inject
internal constructor(val post: Post,
                     val previewUrl: String?) : FullscreenContract.Presenter {

    @Inject lateinit var streamableApi: Lazy<VideositeService.Streamable>
    private val disposables = CompositeDisposable()
    private var fullscreenView: FullscreenContract.View? = null

    override fun checkType() {
        Timber.d("Presenter got ${post.url} $previewUrl")
        when (TextHelper.getLastFourChars(post.url)) {
            ".png", ".jpg", "jpeg" -> {
                fullscreenView?.showImage(post.url, previewUrl)
            }
            ".gif", "webp" -> {
                fullscreenView?.showGif(post.url, previewUrl)
            }
            "gifv" -> {
                fullscreenView?.showVideo(post.url.substring(0, post.url.length - 4) + "mp4")
            }
            "webm", ".mp4" -> {
                fullscreenView?.showVideo(post.url)
            }
            ".mpd" -> {
                fullscreenView?.showVideo(post.url, true)
            }
            else -> fullscreenView?.checkDomain(post.url)
        }
    }

    override fun checkStreamableVideo(id: String) {
        disposables.add(streamableApi.get().getVideo(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<StreamableVideo>() {
                    override fun onNext(@io.reactivex.annotations.NonNull streamableVideo: StreamableVideo) {
                        var videoUrl = "https:"
                        val mobileVideo = streamableVideo.videos.mobileVideo
                        videoUrl += if (mobileVideo != null) {
                            mobileVideo.url!!
                        } else {
                            streamableVideo.videos.video.url!!
                        }
                        Timber.d("Got streamable with url " + videoUrl)
                        if (videoUrl == "https:") {
                            onError(Throwable("No video found"))
                        } else {
                            fullscreenView?.showVideo(videoUrl)
                        }
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        fullscreenView?.showNoVideoFound()
                    }

                    override fun onComplete() {

                    }
                }))
    }

    override fun takeView(view: FullscreenContract.View) {
        Timber.d("Setting fragment")
        fullscreenView = view
        checkType()
    }

    override fun dropView() {
        disposables.dispose()
        fullscreenView = null
    }
}
