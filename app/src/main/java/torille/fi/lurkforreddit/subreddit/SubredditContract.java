package torille.fi.lurkforreddit.subreddit;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import torille.fi.lurkforreddit.data.Post;

/**
 * This specifies the contract between the view and the presenter.
 */

public interface SubredditContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showPosts(List<Post> posts, String nextpage);

        void setListProgressIndicator(boolean active);

        void addMorePosts(List<Post> posts, String nextpage);

        void showCustomTabsUI(String url);

        void showMedia(String url, String domain);

        void showCommentsUI(Post clickedPost);
    }

    interface UserActionsListener {

        void openComments(@NonNull Post clickedPost);

        void openCustomTabs(@NonNull String url);

        void openMedia(@NonNull String url, @NonNull String domain);

        void loadPosts(@Nullable String subredditUrl);

        void loadMorePosts(@NonNull String subredditUrl, @NonNull String nextpage);
    }
}
