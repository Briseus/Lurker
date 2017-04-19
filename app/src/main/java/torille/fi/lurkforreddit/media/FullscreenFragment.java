package torille.fi.lurkforreddit.media;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import org.parceler.Parcels;

import me.relex.photodraweeview.PhotoDraweeView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.StreamableVideo;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.retrofit.StreamableService;

import static android.view.View.GONE;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenFragment extends Fragment implements FullscreenContract.View {

    private static final String EXTRA_POST = "post";

    private PhotoDraweeView mImageView;
    private SimpleExoPlayerView mSimpleExpoPlayerView;
    private ProgressBar mProgressBar;
    private FullscreenPresenter mActionsListener;
    private View mView;
    private SimpleExoPlayer player;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param clickedPost Chosen {@link Post}.
     * @return A new instance of fragment SubredditFragment.
     */
    public static FullscreenFragment newInstance(Post clickedPost) {
        FullscreenFragment fullscreenFragment = new FullscreenFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POST, Parcels.wrap(clickedPost));
        fullscreenFragment.setArguments(args);
        return fullscreenFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionsListener = new FullscreenPresenter(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.d("Stopping");
        releaseVideoPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("Pausing");
        releaseVideoPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("Destroying");
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("Resuming");
        startStuff();
    }

    private void releaseVideoPlayer() {
        Timber.d("Destroying player");
        if (player != null) {
            player.release();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fullscreen, container, false);
        mImageView = (PhotoDraweeView) root.findViewById(R.id.fullscreen_image);
        mSimpleExpoPlayerView = (SimpleExoPlayerView) root.findViewById(R.id.fullscreen_video);
        mView = root;
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBarHorizontal);

        return root;
    }

    private void startStuff() {
        Post post = Parcels.unwrap(getArguments().getParcelable(EXTRA_POST));
        Timber.d("Fragment got " + post.toString());
        mActionsListener.checkDomain(post);
    }

    @Override
    public void showImage(String url, String previewImageUrl) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);

        GenericDraweeHierarchy hierarchy = mImageView.getHierarchy();
        hierarchy.setProgressBarImage(mProgressBar.getProgressDrawable());

        if (previewImageUrl == null) {
            previewImageUrl = "";
        }

        ImageRequest lowResRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(previewImageUrl))
                .setProgressiveRenderingEnabled(true)
                .build();

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setProgressiveRenderingEnabled(true)
                .build();

        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setUri(url)
                .setImageRequest(request)
                .setLowResImageRequest(lowResRequest)
                .setOldController(mImageView.getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                        super.onIntermediateImageSet(id, imageInfo);
                        if (imageInfo == null || mImageView == null) {
                            return;
                        }
                        mImageView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo == null || mImageView == null) {
                            return;
                        }
                        removeMarginTop();
                        mImageView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });

        mImageView.setController(controller.build());
    }

    @Override
    public void showVideo(final String url) {
        mSimpleExpoPlayerView.setVisibility(View.INVISIBLE);
        Context context = getContext();

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        String userAgent = Util.getUserAgent(context, getString(R.string.app_name));
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new OkHttpDataSourceFactory(RedditService.getClient(),
                userAgent, bandwidthMeter);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, null, null);


        LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);

        LoadControl loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);

        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    mProgressBar.setProgress(0);
                    mProgressBar.setVisibility(GONE);
                    removeMarginTop();
                    mSimpleExpoPlayerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Timber.e(error);
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPositionDiscontinuity() {

            }
        });
        mSimpleExpoPlayerView.setPlayer(player);

        // Prepare the player with the source.
        player.prepare(loopingSource);
        player.setPlayWhenReady(true);

    }

    private void removeMarginTop() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mView.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
    }

    @Override
    public void showGfycatVideo(String url) {
        showVideo(url);
    }


    @Override
    public void showStreamableVideo(String identifier) {
        Call<StreamableVideo> call = StreamableService.getInstance().getVideo(identifier);

        call.enqueue(new Callback<StreamableVideo>() {
            @Override
            public void onResponse(Call<StreamableVideo> call, Response<StreamableVideo> response) {
                if (response.isSuccessful()) {
                    // comes in
                    Timber.d(response.toString());
                    String videoUrl = response.body().getMobileVideoUrl();
                    if (!videoUrl.isEmpty()) {
                        showVideo(videoUrl);
                    } else {
                        onFailure(call, new Throwable("No video found"));
                    }

                }
            }

            @Override
            public void onFailure(Call<StreamableVideo> call, Throwable t) {
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
