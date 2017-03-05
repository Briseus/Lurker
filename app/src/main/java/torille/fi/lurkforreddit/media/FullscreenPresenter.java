package torille.fi.lurkforreddit.media;

import android.support.annotation.NonNull;
import android.util.Log;

import torille.fi.lurkforreddit.data.Post;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenPresenter implements FullscreenContract.UserActionsListener {
    private FullscreenContract.View mFullscreenView;

    public FullscreenPresenter(@NonNull FullscreenContract.View fullscreenView) {
        mFullscreenView = fullscreenView;
    }

    @Override
    public void checkDomain(@NonNull Post post) {
        switch (post.getPostDetails().getDomain()) {
            case "gfycat.com":
                final String[] gfy = post.getPostDetails().getUrl().split("g", 2);
                final String gfyUri = "https://thumbs.g" + gfy[1] + "-mobile.mp4";
                mFullscreenView.showGfycatVideo(gfyUri);
                break;
            case "media.giphy.com":
                mFullscreenView.showGif(post.getPostDetails().getUrl());
                break;
            case "i.imgur.com":
            case "imgur.com":
            default:
                checkType(post.getPostDetails().getUrl());
                break;
        }
    }

    private void checkType(final String urlString) {

        switch (TextHelper.getLastFourChars(urlString)) {
            case ".gif":
            case "webm": {
                Log.d("Image", "Got gif " + urlString);
                mFullscreenView.showGif(urlString);
                break;
            }
            case ".png":
            case ".jpg":
            case "jpeg": {
                Log.d("Image", "Got imgur photo " + urlString);
                mFullscreenView.showImage(urlString);
                break;
            }
            case "gifv": {
                Log.d("Video", "Got a gifv vid " + urlString);
                mFullscreenView.showVideo(urlString.substring(0, urlString.length() - 4) + "mp4");
                break;
            }
            case ".mp4": {
                Log.d("Video", "Got mp4 vid " + urlString);
                mFullscreenView.showVideo(urlString);
                break;
            }
            default:
                mFullscreenView.showImage(urlString + ".jpg");
                break;
        }
    }
}
