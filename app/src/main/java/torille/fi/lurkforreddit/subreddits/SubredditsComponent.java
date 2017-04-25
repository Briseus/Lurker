package torille.fi.lurkforreddit.subreddits;

import dagger.Component;
import torille.fi.lurkforreddit.data.RedditRepositoryComponent;
import torille.fi.lurkforreddit.di.scope.FragmentScoped;

/**
 * Created by eva on 24.4.2017.
 */
@FragmentScoped
@Component(dependencies = RedditRepositoryComponent.class, modules = SubredditsPresenterModule.class)
public interface SubredditsComponent {

    void inject(SubredditsFragment fragment);

    SubredditsPresenter subredditsPresenter();

}
