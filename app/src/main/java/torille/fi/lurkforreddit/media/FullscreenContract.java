package torille.fi.lurkforreddit.media;

import android.support.annotation.NonNull;

import torille.fi.lurkforreddit.data.Post;

/**
 * Created by eva on 3/3/17.
 */

public interface FullscreenContract {

    interface View {
        void showImage(String url);

        void showVideo(String url);

        void showGfycatVideo(String url);
    }

    interface UserActionsListener {
        void checkDomain(@NonNull Post post);
    }

}