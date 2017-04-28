package torille.fi.lurkforreddit.media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import torille.fi.lurkforreddit.BasePresenter;
import torille.fi.lurkforreddit.BaseView;

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

    interface Presenter {
        void checkType(@NonNull String url,
                       @Nullable String previewImageUrl);
    }

}
