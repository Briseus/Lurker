package torille.fi.lurkforreddit.media;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import timber.log.Timber;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenPresenter implements FullscreenContract.UserActionsListener {

    private final FullscreenContract.View mFullscreenView;

    FullscreenPresenter(@NonNull FullscreenContract.View fullscreenView) {
        mFullscreenView = fullscreenView;
    }

    public void checkDomain(@NonNull String url, @Nullable String previewImageUrl) {
        Uri uri = Uri.parse(url);

        switch (uri.getHost()) {
            case "gfycat.com":
                final String[] gfy = url.split("g", 2);
                final String gfyUri = "https://thumbs.g" + gfy[1] + "-mobile.mp4";
                mFullscreenView.showVideo(gfyUri);
                break;
            case "streamable.com":
                String identifier = uri.getLastPathSegment();
                Timber.d("Got identifier " + identifier + " from uri " + uri);
                mFullscreenView.showStreamableVideo(identifier);
                break;
            case "i.imgur.com":
            case "imgur.com":
            default:
                checkType(url, previewImageUrl);
                break;
        }


    }

    @VisibleForTesting
    void checkType(@NonNull String urlString, String previewImageUrl) {
        switch (TextHelper.getLastFourChars(urlString)) {
            case ".gif":
            case "webm":
            case ".png":
            case ".jpg":
            case "jpeg": {
                mFullscreenView.showImage(urlString, previewImageUrl);
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
                mFullscreenView.showImage(urlString + ".jpg", previewImageUrl);
                break;
        }
    }
}
