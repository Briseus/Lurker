package torille.fi.lurkforreddit.data;

import dagger.Component;
import torille.fi.lurkforreddit.RedditRepositoryModule;
import torille.fi.lurkforreddit.di.components.NetComponent;
import torille.fi.lurkforreddit.di.scope.RedditScope;
import torille.fi.lurkforreddit.subreddits.SubredditsActivity;
import torille.fi.lurkforreddit.utils.Store;

/**
 * Created by eva on 23.4.2017.
 */
@RedditScope
@Component(modules = RedditRepositoryModule.class, dependencies = NetComponent.class)
public interface RedditRepositoryComponent {

    RedditRepository provideRedditRepository();

    Store provideSettingsStore();

    RedditService.Reddit redditApi();

    void inject(SubredditsActivity activity);
}
