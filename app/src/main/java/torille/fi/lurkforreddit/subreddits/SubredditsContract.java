package torille.fi.lurkforreddit.subreddits;

import java.util.List;

import torille.fi.lurkforreddit.data.Subreddit;
import torille.fi.lurkforreddit.data.SubredditChildren;

/**
 * Created by eva on 2/8/17.
 */

public interface SubredditsContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showSubreddits(List<SubredditChildren> subreddits);

        void loadSelectedSubreddit(Subreddit subreddit);
    }

    interface UserActionsListener {

        void loadSubreddits(boolean b);

        void openSubreddit(Subreddit subreddit);

    }

}
