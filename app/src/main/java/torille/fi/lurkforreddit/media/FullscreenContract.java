package torille.fi.lurkforreddit.media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by eva on 3/3/17.
 */

public interface FullscreenContract {

    interface View {
        void showImage(String url, String previewImageUrl);

        void showVideo(String url);

        void showStreamableVideo(String identifier);

        void checkDomain(String url);
    }

    interface UserActionsListener {
        void checkType(@NonNull String url,
                         @Nullable String previewImageUrl);
    }

}
