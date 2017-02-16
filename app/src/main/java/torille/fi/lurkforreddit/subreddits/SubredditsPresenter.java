package torille.fi.lurkforreddit.subreddits;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.Subreddit;
import torille.fi.lurkforreddit.data.SubredditChildren;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;

/**
 * Created by eva on 2/8/17.
 */

public class SubredditsPresenter implements SubredditsContract.UserActionsListener {

    private final RedditRepository mRedditRepository;
    private final SubredditsContract.View mSubredditsView;

    public SubredditsPresenter(@NonNull RedditRepository redditRepository, @NonNull SubredditsContract.View subredditsView) {
        mRedditRepository = redditRepository;
        mSubredditsView = subredditsView;
    }

    @Override
    public void loadSubreddits(boolean forceUpdate) {
        mSubredditsView.setProgressIndicator(true);
        if (forceUpdate) {
            mRedditRepository.refreshData();
        }
        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mRedditRepository.getSubreddits(new RedditRepository.LoadSubredditsCallback() {
            @Override
            public void onSubredditsLoaded(List<SubredditChildren> subreddits) {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mSubredditsView.setProgressIndicator(false);
                mSubredditsView.showSubreddits(subreddits);
            }
        });
    }

    @Override
    public void openSubreddit(Subreddit subreddit) {
        mSubredditsView.loadSelectedSubreddit(subreddit);
    }
}
