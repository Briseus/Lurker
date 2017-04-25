package torille.fi.lurkforreddit.subreddit;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.BasePresenter;
import torille.fi.lurkforreddit.BaseView;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * This specifies the contract between the view and the presenter.
 */

public interface SubredditContract {

    interface View extends BaseView {

        void setProgressIndicator(boolean active);

        void showPosts(List<Post> posts, String nextpage);

        void setListProgressIndicator(boolean active);

        void addMorePosts(List<Post> posts, String nextpage);

        void showCustomTabsUI(String url);

        void showMedia(Post post);

        void showCommentsUI(Post clickedPost);

        void launchCustomActivity(Post clickedPost);

        void onError(String errorText);
    }

    interface Presenter<T extends BaseView> extends BasePresenter<View> {

        void openComments(@NonNull Post clickedPost);

        void openCustomTabs(@NonNull String url);

        void openMedia(@NonNull Post post);

        void loadPosts(@NonNull String subredditUrl);

        void loadMorePosts(@NonNull String subredditUrl, @NonNull String nextpage);
    }
}
