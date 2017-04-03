package torille.fi.lurkforreddit.media;

import android.graphics.drawable.Animatable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.parceler.Parcels;

import java.io.IOException;

import me.relex.photodraweeview.PhotoDraweeView;
import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.Post;

import static android.view.View.GONE;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenFragment extends Fragment implements FullscreenContract.View, MediaController.MediaPlayerControl {

    private static final String EXTRA_POST = "post";

    private PhotoDraweeView mImageView;
    private SurfaceView mVideoView;
    private ProgressBar mProgressBar;
    private MediaPlayer mMediaPlayer;
    private MediaController mMediaController;
    private FullscreenPresenter mActionsListener;
    private int mBufferPercent;
    private View mView;

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
        Log.d("Video", "Stopping");
        releaseVideoPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Video", "Pausing");
        releaseVideoPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Video", "Resuming");
        startStuff();
    }

    private void releaseVideoPlayer() {
        Log.d("Video", "Destroying");
        if (mMediaPlayer != null) {
            if (mMediaController.isShowing()) {
                mMediaController.hide();
            }
            mMediaPlayer.release();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fullscreen, container, false);
        mImageView = (PhotoDraweeView) root.findViewById(R.id.fullscreen_image);
        mVideoView = (SurfaceView) root.findViewById(R.id.fullscreen_video);
        mView = root;
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBarHorizontal);

        return root;
    }

    private void startStuff() {
        Post post = Parcels.unwrap(getArguments().getParcelable(EXTRA_POST));
        Log.d("fullscreen", "fragment got " + post.toString());
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
        Log.d("Video", "Got url " + url);
        final FrameLayout.LayoutParams svLayoutParams = new FrameLayout.LayoutParams(0, 0);
        mMediaPlayer = new MediaPlayer();
        mMediaController = new MediaController(getContext());
        mMediaController.setMediaPlayer(this);
        mMediaController.setAnchorView(mView);
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mMediaController != null && mMediaPlayer.isPlaying()) {
                    /*
                      the MediaController will hide after 3 seconds - tap the screen to
                      make it appear again
                     */
                    mMediaController.show();

                }
                return false;
            }
        });
        mVideoView.setVisibility(View.VISIBLE);
        final SurfaceHolder surfaceHolder = mVideoView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMediaPlayer.setDisplay(surfaceHolder);
                mMediaPlayer.prepareAsync();


                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mp) {
                        Log.d("Video", "Video is " + mp.getVideoWidth() + " and " + mp.getVideoHeight());
                        if (mp.getVideoHeight() != 0 && mp.getVideoWidth() != 0) {
                            Log.d("Video", "Video is over 0 0");
                            configureSurfaceForVideo(mp.getVideoWidth(), mp.getVideoHeight());
                        } else {
                            Log.d("Video", "Video is 0");
                            playMedia();
                        }

                    }
                });
                mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {

                        mBufferPercent = percent;
                        mProgressBar.setIndeterminate(false);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            mProgressBar.setProgress(mBufferPercent, true);
                        } else {
                            mProgressBar.setProgress(mBufferPercent);
                        }
                        if (percent == 100 && !mp.isPlaying()) {
                            removeMarginTop();
                            Log.d("Video", "Buffering percent over 40! " + mBufferPercent);
                            mProgressBar.setProgress(0);
                            mProgressBar.setVisibility(GONE);
                            playMedia();
                        }
                    }
                });
            }

            private void playMedia() {
                Log.d("Video", "Checking if playing");
                if (!mMediaPlayer.isPlaying()) {
                    Log.d("Video", "GOING TO PLAY");
                    mMediaController.setEnabled(true);
                    mMediaPlayer.start();
                    mMediaPlayer.setLooping(true);
                }
            }

            private void configureSurfaceForVideo(int videoWidth, int videoHeight) {

                int screenHeight = mView.getHeight();
                int screenWidth = mView.getWidth();

                double aspectRatio = ((double) videoHeight / (double) videoWidth);

                if (screenHeight > (int) (screenWidth * aspectRatio)) {
                    // limited by narrow width; restrict height
                    svLayoutParams.width = screenWidth;
                    svLayoutParams.height = (screenWidth * videoHeight) / videoWidth;
                } else {
                    // limited by short height; restrict width
                    svLayoutParams.width = (screenHeight * videoWidth) / videoHeight;
                    svLayoutParams.height = screenHeight;
                }

                svLayoutParams.gravity = Gravity.CENTER;

                mVideoView.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoView.setLayoutParams(svLayoutParams);
                    }
                });

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("Video", "Surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("Video", "Surface destroyed");
            }
        });
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
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mBufferPercent != 0) {
            return mBufferPercent;
        } else {
            return 0;
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }
}
