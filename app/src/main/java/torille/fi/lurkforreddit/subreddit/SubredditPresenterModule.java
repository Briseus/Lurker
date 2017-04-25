package torille.fi.lurkforreddit.subreddit;

import dagger.Module;
import dagger.Provides;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link SubredditPresenter}.
 */
@Module
public class SubredditPresenterModule {


    private final Subreddit mSubreddit;

    public SubredditPresenterModule(Subreddit subreddit) {
        mSubreddit = subreddit;
    }

    @Provides
    public SubredditContract.Presenter<SubredditContract.View> provideSubredditPresenter(RedditRepository redditRepository,
                                                                                         Subreddit subreddit) {
        return new SubredditPresenter(redditRepository, subreddit);
    }

    @Provides
    Subreddit provideSubreddit() {
        return mSubreddit;
    }

}
