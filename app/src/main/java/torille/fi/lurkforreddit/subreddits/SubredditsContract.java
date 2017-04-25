package torille.fi.lurkforreddit.subreddits;

import java.util.List;

import torille.fi.lurkforreddit.BasePresenter;
import torille.fi.lurkforreddit.BaseView;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

/**
 * Created by eva on 2/8/17.
 */

public interface SubredditsContract {

    interface View extends BaseView {

        void setProgressIndicator(boolean active);

        void showSubreddits(List<Subreddit> subreddits);

        void loadSelectedSubreddit(Subreddit subreddit);

        void onError(String errorText);
    }

    interface Presenter<T extends BaseView> extends BasePresenter<View> {

        void loadSubreddits(boolean b);

        void openSubreddit(Subreddit subreddit);

    }

}
