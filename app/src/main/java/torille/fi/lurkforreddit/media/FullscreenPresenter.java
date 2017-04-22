package torille.fi.lurkforreddit.media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Created by eva on 3/3/17.
 */

public class FullscreenPresenter implements FullscreenContract.UserActionsListener {

    private final FullscreenContract.View mFullscreenView;

    FullscreenPresenter(@NonNull FullscreenContract.View fullscreenView) {
        mFullscreenView = fullscreenView;
    }

    public void checkType(@NonNull String urlString, @Nullable String previewImageUrl) {
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
                mFullscreenView.checkDomain(urlString);
                break;
        }
    }
}
