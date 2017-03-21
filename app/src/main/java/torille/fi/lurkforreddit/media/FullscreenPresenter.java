package torille.fi.lurkforreddit.media;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenPresenter implements FullscreenContract.UserActionsListener {

    private final FullscreenContract.View mFullscreenView;

    public FullscreenPresenter(@NonNull FullscreenContract.View fullscreenView) {
        mFullscreenView = fullscreenView;
    }

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

    @VisibleForTesting
    void checkType(String urlString) {

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
                mFullscreenView.showImage(urlString + ".jpg");
                break;
        }
    }
}
