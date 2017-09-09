package torille.fi.lurkforreddit

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.Remote
import torille.fi.lurkforreddit.data.remote.RedditRemoteDataSource
import torille.fi.lurkforreddit.utils.CommentsStreamingParser
import torille.fi.lurkforreddit.utils.Store
import javax.inject.Singleton

/**
 * This is used by Dagger to inject the required arguments
 */
@Module
class RedditRepositoryModule {

    @Singleton
    @Provides
    @Remote
    internal fun provideRemoteDataSource(commentsStreamingParser: CommentsStreamingParser,
                                         redditApi: RedditService.Reddit,
                                         settingsStore: Store): RedditDataSource {
        return RedditRemoteDataSource(redditApi, settingsStore, commentsStreamingParser)
    }

}
