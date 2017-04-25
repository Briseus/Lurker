package torille.fi.lurkforreddit.subreddits;

import dagger.Module;
import dagger.Provides;
import torille.fi.lurkforreddit.data.RedditRepository;

/**
 * Created by eva on 24.4.2017.
 */
@Module
public class SubredditsPresenterModule {

    public SubredditsPresenterModule() {
    }

    @Provides
    public SubredditsContract.Presenter<SubredditsContract.View> provideSubredditsPresenter(RedditRepository redditRepository) {
        return new SubredditsPresenter(redditRepository);
    }

}
