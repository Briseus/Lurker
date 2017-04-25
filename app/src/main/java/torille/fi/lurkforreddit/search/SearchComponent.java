package torille.fi.lurkforreddit.search;

import dagger.Component;
import torille.fi.lurkforreddit.data.RedditRepositoryComponent;
import torille.fi.lurkforreddit.di.scope.FragmentScoped;

/**
 * Created by eva on 25.4.2017.
 */
@FragmentScoped
@Component(dependencies = RedditRepositoryComponent.class, modules = SearchPresenterModule.class)
public interface SearchComponent {

    void inject(SearchFragment searchFragment);

    SearchPresenter searchPresenter();

}
