package torille.fi.lurkforreddit.search;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.SearchResult;

/**
 * Created by eva on 3/20/17.
 */

public class SearchPresenter implements SearchContract.Presenter<SearchContract.View> {

    private final RedditRepository mRedditRepository;
    private SearchContract.View mSearchView;
    private String searchAfter;
    private String searchQuery;

    @Inject
    SearchPresenter(RedditRepository mRedditRepository) {
        this.mRedditRepository = mRedditRepository;
    }

    private final RedditDataSource.ErrorCallback errorCallback = new RedditDataSource.ErrorCallback() {
        @Override
        public void onError(String errorText) {
            mSearchView.showError(errorText);
        }
    };

    @Override
    public void searchSubreddits(@NonNull String query) {
        this.searchQuery = query;
        mSearchView.clearResults();
        mSearchView.showProgressbar();
        mRedditRepository.getSearchResults(searchQuery, new RedditDataSource.LoadCommentsCallback() {
            @Override
            public void onSearchLoaded(List<SearchResult> subredditChildrens, String after) {
                mSearchView.showResults(subredditChildrens);
                searchAfter = after;
            }
        }, errorCallback);
    }

    @Override
    public void searchMoreSubreddits() {
        mSearchView.showProgressbar();
        mRedditRepository.getMoreSearchResults(searchQuery, searchAfter, new RedditDataSource.LoadCommentsCallback() {
            @Override
            public void onSearchLoaded(List<SearchResult> subredditChildrens, String after) {
                mSearchView.showResults(subredditChildrens);
                searchAfter = after;
            }
        }, errorCallback);
    }

    @Override
    public void setView(@NonNull SearchContract.View view) {
        mSearchView = view;
    }

    @Override
    public void start() {

    }
}
