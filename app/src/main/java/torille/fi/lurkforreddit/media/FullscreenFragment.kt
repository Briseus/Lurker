package torille.fi.lurkforreddit.media

import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Util
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_fullscreen.*
import kotlinx.android.synthetic.main.progress_bar_horizontal.*
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import timber.log.Timber
import torille.fi.lurkforreddit.R
import javax.inject.Inject

class FullscreenFragment @Inject constructor() : DaggerFragment(), FullscreenContract.View {

    @Inject lateinit var actionsListener: FullscreenContract.Presenter

    @Inject lateinit var okHttpClient: OkHttpClient

    private var player: SimpleExoPlayer? = null

    override fun onResume() {
        super.onResume()
        Timber.d("Resuming")
        actionsListener.takeView(this)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Starting")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("Pausing")
    }

    override fun onStop() {
        super.onStop()
        Timber.d("Stopping")
        player?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroying")
        releaseVideoPlayer()
        actionsListener.dropView()
    }

    private fun releaseVideoPlayer() {
        Timber.d("Destroying player")
        player?.release()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fullscreen, container, false)
    }

    override fun showImage(url: String, previewImageUrl: String?) {
        progressBarHorizontal.visibility = View.INVISIBLE
        imageView.visibility = View.VISIBLE
        imageView.hierarchy.setProgressBarImage(progressBarHorizontal.progressDrawable)

        val previewImgUrl = if (previewImageUrl.isNullOrEmpty()) "" else previewImageUrl

        val lowResRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(previewImgUrl))
                .build()

        val imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .build()

        val controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(imageRequest)
                .setLowResImageRequest(lowResRequest)
                .setOldController(imageView.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                        super.onIntermediateImageSet(id, imageInfo)
                        if (imageInfo == null) {
                            return
                        }
                        imageView.update(imageInfo.width, imageInfo.height)
                    }

                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        if (imageInfo == null) {
                            Timber.d("Had an error")
                            return
                        }
                        removeMarginTop()
                        imageView.update(imageInfo.width, imageInfo.height)
                    }
                })

        imageView.controller = controller.build()
    }

    override fun showVideo(url: String, isDash: Boolean) {
        Timber.d("Got url to play $url")
        videoView.visibility = View.INVISIBLE
        val context = context
        val defaultBandwidthMeter = DefaultBandwidthMeter()
        val control = CacheControl.Builder().build()
        val userAgent = Util.getUserAgent(context, getString(R.string.app_name))
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = OkHttpDataSourceFactory(okHttpClient,
                userAgent, defaultBandwidthMeter, control)
        // Produces Extractor instances for parsing the media data.
        val extractorsFactory = DefaultExtractorsFactory()
        // This is the MediaSource representing the media to be played.
        val videoSource = if (isDash)
            DashMediaSource(
                    Uri.parse(url),
                    dataSourceFactory,
                    DefaultDashChunkSource.Factory(dataSourceFactory),
                    null, null)
        else ExtractorMediaSource(
                Uri.parse(url),
                dataSourceFactory,
                extractorsFactory,
                null, null)

        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(defaultBandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        player!!.repeatMode = Player.REPEAT_MODE_ONE
        player!!.addListener(object : Player.EventListener {
            override fun onRepeatModeChanged(repeatMode: Int) {}

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.e(error)
                Toast.makeText(getContext(), "Failed to find video", Toast.LENGTH_SHORT).show()
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    progressBarHorizontal.hide()
                    removeMarginTop()
                    videoView.visibility = View.VISIBLE
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPositionDiscontinuity() {}

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}

        })
        videoView.player = player

        // Prepare the player with the source.
        player!!.prepare(videoSource)
        player!!.playWhenReady = true


    }

    override fun showNoVideoFound() {
        progressBarHorizontal.hide()
        notFound.visibility = View.VISIBLE
    }

    private fun removeMarginTop() {
        val params = container.layoutParams as FrameLayout.LayoutParams
        params.setMargins(0, 0, 0, 0)
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
                actionsListener.checkStreamableVideo(identifier)
            }
            "i.imgur.com", "imgur.com" -> {
                Timber.d("Showing imgur")
                showImage(url + ".jpg", "")
            }
            "v.redd.it" -> {
                Timber.d("Got reddit video domain")
                showVideo(url)
            }
            else -> showImage(url + ".jpg", null)
        }

    }
}
