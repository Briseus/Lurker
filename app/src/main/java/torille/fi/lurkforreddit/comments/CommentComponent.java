package torille.fi.lurkforreddit.comments;

import dagger.Component;
import torille.fi.lurkforreddit.data.RedditRepositoryComponent;
import torille.fi.lurkforreddit.di.scope.FragmentScoped;

/**
 * Created by eva on 24.4.2017.
 */
@FragmentScoped
@Component(dependencies = RedditRepositoryComponent.class, modules = {CommentPresenterModule.class})
public interface CommentComponent {

    void inject(CommentFragment fragment);

}
