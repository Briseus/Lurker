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
            case "i.imgur.com":
            case "imgur.com":
            default:
                checkType(post.getPostDetails().getUrl());
                break;
        }
    }

    private void checkType(String urlString) {

        switch (TextHelper.getLastFourChars(urlString)) {
            case ".gif":
            case "webm":
            case ".png":
            case ".jpg":
            case "jpeg": {
                mFullscreenView.showImage(urlString);
                break;
            }
            case "gifv": {
                mFullscreenView.showVideo(urlString.substring(0, urlString.length() - 4) + "mp4");
                break;
            }
            case ".mp4": {
                mFullscreenView.showVideo(urlString);
                break;
            }
            default:
                Log.d("Image", "Got with end " + urlString);
                mFullscreenView.showImage(urlString + ".jpg");
                break;
        }
    }
}
