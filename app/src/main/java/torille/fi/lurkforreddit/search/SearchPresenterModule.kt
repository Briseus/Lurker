package torille.fi.lurkforreddit.search

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.RedditRepository

@Module
class SearchPresenterModule {

    @Provides
    fun provideSearchPresenter(redditRepository: RedditRepository): SearchContract.Presenter<SearchContract.View> {
        return SearchPresenter(redditRepository)
    }

}
