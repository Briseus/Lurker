package torille.fi.lurkforreddit.search;

import dagger.Module;
import dagger.Provides;
import torille.fi.lurkforreddit.data.RedditRepository;

/**
 * Created by eva on 25.4.2017.
 */
@Module
public class SearchPresenterModule {

    public SearchPresenterModule() {

    }

    @Provides
    public SearchContract.Presenter<SearchContract.View> provideSearchPresenter(RedditRepository redditRepository) {
        return new SearchPresenter(redditRepository);
    }

}
