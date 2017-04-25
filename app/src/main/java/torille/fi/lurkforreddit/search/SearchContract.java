package torille.fi.lurkforreddit.search;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.BasePresenter;
import torille.fi.lurkforreddit.BaseView;
import torille.fi.lurkforreddit.data.models.view.SearchResult;

/**
 * Created by eva on 3/20/17.
 */

public interface SearchContract {

    interface View extends BaseView {

        void showResults(@NonNull List<SearchResult> results);

        void showProgressbar();

        void clearResults();

        void showError(String errorText);

    }

    interface Presenter<T extends BaseView> extends BasePresenter<SearchContract.View> {

        void searchSubreddits(@NonNull String query);

        void searchMoreSubreddits();

    }

}
