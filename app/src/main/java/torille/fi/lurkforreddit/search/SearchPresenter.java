package torille.fi.lurkforreddit.search;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
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
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    SearchPresenter(RedditRepository mRedditRepository) {
        this.mRedditRepository = mRedditRepository;
    }

    @Override
    public void searchSubreddits(@NonNull String query) {
        this.searchQuery = query;
        mSearchView.clearResults();
        mSearchView.showProgressbar();
        disposables.add(mRedditRepository.getSearchResults(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Pair<String, List<SearchResult>>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Pair<String, List<SearchResult>> resultPair) {
                        searchAfter = resultPair.first;
                        mSearchView.showResults(resultPair.second);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Timber.e(e);
                        mSearchView.showError(e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void searchMoreSubreddits() {
        mSearchView.showProgressbar();
        disposables.add(mRedditRepository.getMoreSearchResults(searchQuery, searchAfter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Pair<String, List<SearchResult>>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Pair<String, List<SearchResult>> resultPair) {
                        searchAfter = resultPair.first;
                        mSearchView.showResults(resultPair.second);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Timber.e(e);
                        mSearchView.showError(e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void setView(@NonNull SearchContract.View view) {
        mSearchView = view;
    }

    @Override
    public void start() {

    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
