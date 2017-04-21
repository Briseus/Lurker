package torille.fi.lurkforreddit.search;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.SearchResult;

/**
 * Created by eva on 3/20/17.
 */

public class SearchPresenter implements SearchContract.UserActionsListener {

    private final RedditRepository mRedditRepository;
    private final SearchContract.View mSearchView;
    private String searchAfter;
    private String searchQuery;

    SearchPresenter(RedditRepository mRedditRepository, SearchContract.View mSearchView) {
        this.mRedditRepository = mRedditRepository;
        this.mSearchView = mSearchView;
    }

    private final RedditRepository.ErrorCallback errorCallback = new RedditRepository.ErrorCallback() {
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
        mRedditRepository.getSearchResults(searchQuery, new RedditRepository.LoadCommentsCallback() {
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
        mRedditRepository.getMoreSearchResults(searchQuery, searchAfter, new RedditRepository.LoadCommentsCallback() {
            @Override
            public void onSearchLoaded(List<SearchResult> subredditChildrens, String after) {
                mSearchView.showResults(subredditChildrens);
                searchAfter = after;
            }
        }, errorCallback);
    }
}
