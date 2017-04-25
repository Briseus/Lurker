package torille.fi.lurkforreddit;

import dagger.Binds;
import dagger.Module;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.Remote;
import torille.fi.lurkforreddit.data.remote.FakeRedditRepository;
import torille.fi.lurkforreddit.di.scope.RedditScope;

/**
 * Created by eva on 25.4.2017.
 */

@Module
abstract public class RedditRepositoryModule {

    @RedditScope
    @Binds
    @Remote
    abstract RedditRepository provideRemoteDataSource(FakeRedditRepository dataSource);

}
