package torille.fi.lurkforreddit.media

import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import dagger.Lazy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import me.relex.photodraweeview.PhotoDraweeView
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import timber.log.Timber
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.VideositeService
import torille.fi.lurkforreddit.data.models.jsonResponses.StreamableVideo
import javax.inject.Inject

class FullscreenFragment : Fragment(), FullscreenContract.View {

    private lateinit var mImageView: PhotoDraweeView
    private lateinit var mSimpleExpoPlayerView: SimpleExoPlayerView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mActionsListener: FullscreenPresenter
    private lateinit var mView: View
    private lateinit var player: SimpleExoPlayer

    private val disposables = CompositeDisposable()

    @Inject
    internal lateinit var mStreamableApi: Lazy<VideositeService.Streamable>

    @Inject
    internal lateinit var mOkHttpClient: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity.application as MyApplication)
                .getmNetComponent()
                .inject(this)
        mActionsListener = FullscreenPresenter(this)

        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(null)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Starting")
        startStuff()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("Pausing")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("Stopping")
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroying")
        releaseVideoPlayer()
        disposables.dispose()
    }

    private fun releaseVideoPlayer() {
        Timber.d("Destroying player")
        player.release()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_fullscreen, container, false)
        mImageView = root.findViewById<PhotoDraweeView>(R.id.fullscreen_image)
        mSimpleExpoPlayerView = root.findViewById<SimpleExoPlayerView>(R.id.fullscreen_video)
        mView = root
        mProgressBar = root.findViewById<ProgressBar>(R.id.progressBarHorizontal)

        return root
    }

    private fun startStuff() {
        val url = arguments.getString(EXTRA_URL)
        val previewImageUrl = arguments.getString(EXTRA_PREVIEWIMAGE)
        if (url != null) {
            Timber.d("Fragment got $url $previewImageUrl")
            mActionsListener.checkType(url, previewImageUrl)
        }

    }

    override fun showImage(url: String, previewImageUrl: String?) {
        var previewImageUrl = previewImageUrl
        mProgressBar.visibility = View.INVISIBLE
        mImageView.visibility = View.VISIBLE

        val hierarchy = mImageView.hierarchy
        hierarchy.setProgressBarImage(mProgressBar.progressDrawable)

        if (previewImageUrl == null) {
            previewImageUrl = ""
        }

        val lowResRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(previewImageUrl))
                .build()

        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .build()

        val controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(request)
                .setLowResImageRequest(lowResRequest)
                .setOldController(mImageView.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                        super.onIntermediateImageSet(id, imageInfo)
                        if (imageInfo == null) {
                            return
                        }
                        mImageView.update(imageInfo.width, imageInfo.height)
                    }

                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        if (imageInfo == null) {
                            Timber.d("Was error")
                            return
                        }
                        removeMarginTop()
                        mImageView.update(imageInfo.width, imageInfo.height)
                    }
                })

        mImageView.controller = controller.build()
    }

    override fun showVideo(url: String) {
        Timber.d("Got url to play $url")
        mSimpleExpoPlayerView.visibility = View.INVISIBLE
        val context = context

        val control = CacheControl.Builder().build()
        val userAgent = Util.getUserAgent(context, getString(R.string.app_name))
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = OkHttpDataSourceFactory(mOkHttpClient,
                userAgent, DefaultBandwidthMeter(), control)

        // Produces Extractor instances for parsing the media data.
        val extractorsFactory = DefaultExtractorsFactory()
        // This is the MediaSource representing the media to be played.
        val videoSource = ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, null, null)

        val loopingSource = LoopingMediaSource(videoSource)

        player.addListener(object : ExoPlayer.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.e(error)
                Toast.makeText(getContext(), "Failed to find video", Toast.LENGTH_SHORT).show()
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    mProgressBar.progress = 0
                    mProgressBar.visibility = GONE
                    removeMarginTop()
                    mSimpleExpoPlayerView.visibility = View.VISIBLE
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPositionDiscontinuity() {}

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}

        })
        mSimpleExpoPlayerView.player = player

        // Prepare the player with the source.
        player.prepare(loopingSource)
        player.playWhenReady = true


    }

    private fun removeMarginTop() {
        val params = mView.layoutParams as FrameLayout.LayoutParams
        params.setMargins(0, 0, 0, 0)
    }

    override fun showStreamableVideo(identifier: String) {
        disposables.add(mStreamableApi.get().getVideo(identifier)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<StreamableVideo>() {
                    override fun onNext(@io.reactivex.annotations.NonNull streamableVideo: StreamableVideo) {
                        var videoUrl = "https:"
                        val mobileVideo = streamableVideo.videos.mobileVideo
                        if (mobileVideo != null) {
                            videoUrl += mobileVideo.url!!
                        } else {
                            videoUrl += streamableVideo.videos.video.url!!
                        }
                        Timber.d("Got streamable with url " + videoUrl)
                        if (videoUrl == "https:") {
                            onError(Throwable("No video found"))
                        } else {
                            showVideo(videoUrl)
                        }
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                    }

                    override fun onComplete() {

                    }
                }))
    }

    override fun checkDomain(url: String) {
        val uri = Uri.parse(url)
        Timber.d("Uri was $uri")
        when (uri.host) {
            "gfycat.com" -> {
                val gfy = url.split("/").last()
                val gfyUri = "https://thumbs.gfycat.com/$gfy-mobile.mp4"
                Timber.d("Playing gfycat $gfyUri")
                showVideo(gfyUri)
            }
            "streamable.com" -> {
                val identifier = uri.lastPathSegment
                Timber.d("Got identifier $identifier from uri $uri")
                showStreamableVideo(identifier)
            }
            "i.imgur.com", "imgur.com" -> {
                Timber.d("Showing imgur")
                showImage(url + ".jpg", "")
            }
            else -> showImage(url + ".jpg", null)
        }

    }

    companion object {

        private val EXTRA_URL = "url"
        private val EXTRA_PREVIEWIMAGE = "imageUrl"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param url        Link to media
         * *
         * @param previewUrl link to previewUrl
         * *
         * @return A new instance of fragment SubredditFragment.
         */
        fun newInstance(url: String, previewUrl: String?): FullscreenFragment {
            val fullscreenFragment = FullscreenFragment()
            val args = Bundle()
            args.putString(EXTRA_URL, url)
            args.putString(EXTRA_PREVIEWIMAGE, previewUrl)
            fullscreenFragment.arguments = args
            return fullscreenFragment
        }
    }

}
