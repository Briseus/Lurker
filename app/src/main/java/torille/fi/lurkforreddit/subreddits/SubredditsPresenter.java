package torille.fi.lurkforreddit.subreddits;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;

/**
 * Created by eva on 2/8/17.
 */

public class SubredditsPresenter implements SubredditsContract.Presenter<SubredditsContract.View> {

    private final RedditRepository mRedditRepository;
    private SubredditsContract.View mSubredditsView;

    @Inject
    public SubredditsPresenter(@NonNull RedditRepository redditRepository) {
        mRedditRepository = redditRepository;
    }

    private final RedditDataSource.ErrorCallback errorCallback = new RedditDataSource.ErrorCallback() {
        @Override
        public void onError(String errorText) {
            mSubredditsView.setProgressIndicator(false);
            mSubredditsView.onError(errorText);
        }
    };

    @Override
    public void loadSubreddits(boolean forceUpdate) {
        Timber.d("Going to fetch subs!");
        mSubredditsView.setProgressIndicator(true);
        if (forceUpdate) {
            mRedditRepository.refreshData();
        }
        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mRedditRepository.getSubreddits(new RedditDataSource.LoadSubredditsCallback() {
            @Override
            public void onSubredditsLoaded(List<Subreddit> subreddits) {
                EspressoIdlingResource.decrement(); // Set app as idle.
                mSubredditsView.setProgressIndicator(false);
                mSubredditsView.showSubreddits(subreddits);
            }
        }, errorCallback);
    }

    @Override
    public void openSubreddit(Subreddit subreddit) {
        mSubredditsView.loadSelectedSubreddit(subreddit);
    }

    @Override
    public void setView(SubredditsContract.View view) {
        mSubredditsView = view;
    }

    @Override
    public void start() {
        loadSubreddits(false);
    }
}
