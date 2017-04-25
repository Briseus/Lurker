package torille.fi.lurkforreddit.di.components;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;
import torille.fi.lurkforreddit.MyApplication;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.di.modules.AppModule;
import torille.fi.lurkforreddit.di.modules.NetModule;
import torille.fi.lurkforreddit.di.modules.RedditAuthModule;
import torille.fi.lurkforreddit.di.modules.RedditModule;
import torille.fi.lurkforreddit.di.modules.StreamableModule;
import torille.fi.lurkforreddit.media.FullscreenFragment;
import torille.fi.lurkforreddit.data.StreamableService;
import torille.fi.lurkforreddit.subreddit.SubredditActivity;
import torille.fi.lurkforreddit.subreddits.SubredditsActivity;
import torille.fi.lurkforreddit.utils.Store;

/**
 * Created by eva on 22.4.2017.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        NetModule.class,
        RedditAuthModule.class,
        RedditModule.class,
        StreamableModule.class})
public interface NetComponent {

    OkHttpClient okHttpClient();

    Store store();

    RedditService.Auth redditAuthApi();

    RedditService.Reddit redditApi();

    StreamableService streamableApi();

    void inject(MyApplication application);

    void inject(SubredditsActivity activity);

    void inject(SubredditActivity activity);

    void inject(FullscreenFragment fragment);

}
