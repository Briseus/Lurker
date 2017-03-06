package torille.fi.lurkforreddit.media;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.parceler.Parcels;

import java.io.IOException;

import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.Post;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static android.view.View.GONE;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenFragment extends Fragment implements FullscreenContract.View, MediaController.MediaPlayerControl {

    public static final String EXTRA_POST = "post";

    private PhotoView mImageView;
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
        mImageView = (PhotoView) root.findViewById(R.id.fullscreen_image);
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
    public void showImage(String url) {
        mImageView.setVisibility(View.INVISIBLE);
        final PhotoViewAttacher attacher = new PhotoViewAttacher(mImageView);
        Glide.with(getActivity())
                .asDrawable()
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("fullscreen", "image ready");
                        mProgressBar.setVisibility(View.GONE);
                        mImageView.setVisibility(View.VISIBLE);
                        attacher.update();
                        return false;
                    }
                })
                .into(mImageView);
    }

    @Override
    public void showVideo(final String url) {
        Log.d("Video", "Got url " + url);
        final SurfaceHolder surfaceHolder = mVideoView.getHolder();
        final FrameLayout.LayoutParams svLayoutParams = new FrameLayout.LayoutParams(0, 0);
        mMediaPlayer = new MediaPlayer();
        mMediaController = new MediaController(getContext());
        mMediaController.setMediaPlayer(this);
        mMediaController.setAnchorView(mView);
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mMediaController != null && mMediaPlayer.isPlaying()) {
                    /**
                     * the MediaController will hide after 3 seconds - tap the screen to
                     * make it appear again
                     */
                    mMediaController.show();

                }
                return false;
            }
        });
        mVideoView.setVisibility(View.VISIBLE);
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
                        if (percent > 40 && !mp.isPlaying()) {
                            Log.d("Video", "Buffering percent over 40! " + percent);
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
                //Get the width of the screen

                DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
                //Get the width of the screen
                /*int screenWidth = dm.widthPixels;
                int screenHeight = dm.heightPixels ;*/
                int screenHeight = mView.getHeight();
                int screenWidth = mView.getRootView().getWidth();
                Log.d("Video", "Height " + screenHeight + " width " + screenHeight);
                Log.d("Video", "mp h " + screenHeight + " width " + screenHeight);

                double aspectRatio = ((double) videoHeight / (double) videoWidth);
                Log.d("Video ", " AP " + aspectRatio);
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
            /*private void configureSurfaceForVideo(final MediaPlayer mp, int videoWidth, int videoHeight) {
                final DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
                //Get the width of the screen
                final int screenWidth = dm.widthPixels;

                final float aspectRatio = ((float) videoWidth / (float) videoHeight);
                Log.d("Video ", " AP " + aspectRatio);

                if (aspectRatio >= 1) {
                    //Set the width of the SurfaceView to the width of the screen
                    svLayoutParams.width = screenWidth;
                    //Set the height of the SurfaceView to match the aspect ratio of the video
                    //be sure to cast these as floats otherwise the calculation will likely be 0
                    svLayoutParams.height = (int) (((float) videoHeight / (float) videoWidth) * dm.widthPixels);
                    Log.d("Video", "Height is " + svLayoutParams.height);
                } else {
                    svLayoutParams.width = (int) (((float) videoWidth / (float) videoHeight) * dm.heightPixels);
                    svLayoutParams.height = mVideoView.getHeight();
                }

                svLayoutParams.gravity = Gravity.CENTER;

                mVideoView.post(new Runnable() {
                    @Override
                    public void run() {
                        mVideoView.setLayoutParams(svLayoutParams);
                    }
                });

            }*/


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("Video", "Surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
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
