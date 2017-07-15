package torille.fi.lurkforreddit

import dagger.Binds
import dagger.Module
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.Remote
import torille.fi.lurkforreddit.data.remote.RedditRemoteDataSource
import torille.fi.lurkforreddit.di.scope.RedditScope

/**
 * Abstract class used to point to the datasource you want
 * Used to mock in test
 */
@Module
abstract class RedditRepositoryModule {

    @RedditScope
    @Binds
    @Remote
    internal abstract fun provideRemoteDataSource(dataSource: RedditRemoteDataSource): RedditDataSource

}
