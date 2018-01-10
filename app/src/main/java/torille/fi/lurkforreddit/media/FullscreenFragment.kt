package torille.fi.lurkforreddit.media

import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.davemorrissey.labs.subscaleview.ImageSource
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.datasource.BaseDataSubscriber
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.AbstractDraweeController
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
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
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import timber.log.Timber
import torille.fi.lurkforreddit.R
import javax.inject.Inject

class FullscreenFragment @Inject constructor() : DaggerFragment(), FullscreenContract.View {

    @Inject lateinit var actionsListener: FullscreenContract.Presenter

    @Inject lateinit var okHttpClient: OkHttpClient


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
        videoView?.player?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroying")
        videoView?.player?.release()
        actionsListener.dropView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_fullscreen, container, false)
    }

    override fun showImage(url: String, previewImageUrl: String?) {
        imageView.visibility = View.VISIBLE

        val imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .build()

        if (Fresco.getImagePipeline().isInDiskCache(imageRequest).result == true) {
            setImage(imageRequest)
        } else {
            prefetchImage(imageRequest)
        }

    }

    private fun setImage(imageRequest: ImageRequest) {
        progressBarHorizontal?.hide()
        val cacheKey = Fresco.getImagePipeline().cacheKeyFactory.getEncodedCacheKey(imageRequest, this)
        Fresco.getImagePipelineFactory().mainFileCache.getResource(cacheKey)?.let {
            val resource: FileBinaryResource = it as FileBinaryResource
            imageView.setImage(ImageSource.uri(resource.file.absolutePath))
        }

    }

    private fun prefetchImage(imageRequest: ImageRequest) {
        val dataSource = Fresco.getImagePipeline().prefetchToDiskCache(imageRequest, this)
        progressBarHorizontal.isIndeterminate = false
        dataSource.subscribe(object : BaseDataSubscriber<Void>() {
            override fun onNewResultImpl(dataSource: DataSource<Void>?) {
                dataSource?.let {
                    Timber.d("Finished")
                    it.isFinished.let {
                        Handler().postDelayed({
                            setImage(imageRequest)
                        }, 50) // need a delay as the file not always already
                    }
                }
            }

            override fun onFailureImpl(dataSource: DataSource<Void>?) {
                progressBarHorizontal?.hide()
            }

            override fun onProgressUpdate(dataSource: DataSource<Void>?) {
                super.onProgressUpdate(dataSource)
                dataSource?.let {
                    progressBarHorizontal?.progress = (it.progress * 100).toInt()
                }

            }
        }, UiThreadImmediateExecutorService.getInstance())
    }

    override fun showGif(url: String, previewImageUrl: String?) {
        gifView.visibility = View.VISIBLE
        gifView.controller = setupController(url, previewImageUrl)
        setTopMargin(-20)
    }

    private fun setTopMargin(topMargin: Int) {
        val params = container.layoutParams as FrameLayout.LayoutParams
        params.setMargins(0, topMargin, 0, 0)
    }

    private fun setupController(url: String, previewImageUrl: String?): AbstractDraweeController<*, *> {
        progressBarHorizontal.visibility = View.INVISIBLE
        gifView.hierarchy.setProgressBarImage(progressBarHorizontal.progressDrawable)

        val previewImgUrl = if (previewImageUrl.isNullOrEmpty()) "" else previewImageUrl

        val lowResRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(previewImgUrl))
                .build()

        val imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .build()

        return Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(imageRequest)
                .setLowResImageRequest(lowResRequest)
                .setOldController(gifView.controller)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        setTopMargin(0)
                    }
                })
                .build()
    }

    override fun showVideo(url: String, isDash: Boolean) {
        Timber.d("Got url to play $url")
        videoView.visibility = View.INVISIBLE

        launch(UI) {
            val defaultBandwidthMeter = DefaultBandwidthMeter()

            val player = async { setupExoPlayer(defaultBandwidthMeter) }
            val videoSource = async { setupVideoSource(url, isDash, defaultBandwidthMeter) }

            videoView.player = player.await()
            videoView.player.prepare(videoSource.await())
            videoView.player.playWhenReady = true
        }

    }

    private fun setupExoPlayer(defaultBandwidthMeter: DefaultBandwidthMeter): SimpleExoPlayer {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(defaultBandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.addListener(object : Player.EventListener {
            override fun onPositionDiscontinuity(reason: Int) {}

            override fun onSeekProcessed() {}

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

            override fun onRepeatModeChanged(repeatMode: Int) {}

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

            override fun onPlayerError(error: ExoPlaybackException?) {
                Timber.e(error)
                progressBarHorizontal.hide()
                Toast.makeText(context, "Failed to find video", Toast.LENGTH_SHORT).show()
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    progressBarHorizontal.hide()
                    videoView.visibility = View.VISIBLE
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {}

        })
        return exoPlayer
    }

    private fun setupVideoSource(url: String, isDash: Boolean, defaultBandwidthMeter: DefaultBandwidthMeter): MediaSource {
        val control = CacheControl.Builder().build()
        val userAgent = Util.getUserAgent(context, getString(R.string.app_name))
        val dataSourceFactory = OkHttpDataSourceFactory(okHttpClient,
                userAgent, defaultBandwidthMeter, control)
        val extractorsFactory = DefaultExtractorsFactory()
        return if (isDash)
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
    }

    override fun showNoVideoFound() {
        progressBarHorizontal.hide()
        notFound.visibility = View.VISIBLE
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
