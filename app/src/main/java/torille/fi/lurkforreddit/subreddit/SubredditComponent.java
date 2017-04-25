package torille.fi.lurkforreddit.subreddit;

import dagger.Component;
import torille.fi.lurkforreddit.data.RedditRepositoryComponent;
import torille.fi.lurkforreddit.di.scope.FragmentScoped;

/**
 * Created by eva on 24.4.2017.
 */
@FragmentScoped
@Component(dependencies = RedditRepositoryComponent.class, modules = {SubredditPresenterModule.class})
public interface SubredditComponent {

    void inject(SubredditFragment fragment);
}
