package torille.fi.lurkforreddit;

import dagger.Binds;
import dagger.Module;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.Remote;
import torille.fi.lurkforreddit.data.remote.RedditRemoteDataSource;
import torille.fi.lurkforreddit.di.scope.RedditScope;

/**
 * Created by eva on 23.4.2017.
 */
@Module
abstract public class RedditRepositoryModule {

    @RedditScope
    @Binds
    @Remote
    abstract RedditDataSource provideRemoteDataSource(RedditRemoteDataSource dataSource);

}
