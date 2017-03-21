package torille.fi.lurkforreddit.search;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.SubredditChildren;

/**
 * Created by eva on 3/20/17.
 */

public interface SearchContract {

    interface View {

        void showResults(@NonNull List<SubredditChildren> subredditChildrens);

        void showProgressbar();

        void clearResults();

    }

    interface UserActionsListener {

        void searchSubreddits(@NonNull String query);

        void searchMoreSubreddits();

    }

}
