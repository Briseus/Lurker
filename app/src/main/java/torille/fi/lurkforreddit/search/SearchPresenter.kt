package torille.fi.lurkforreddit.search

import android.support.v4.util.Pair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.SearchResult
import javax.inject.Inject

class SearchPresenter @Inject
internal constructor(private val mRedditRepository: RedditRepository) : SearchContract.Presenter<SearchContract.View> {
    private var mSearchView: SearchContract.View? = null
    private var searchAfter: String? = null
    private var searchQuery: String? = null
    private val disposables = CompositeDisposable()

    override fun searchSubreddits(query: String) {
        this.searchQuery = query
        mSearchView!!.clearResults()
        mSearchView!!.showProgressbar()
        disposables.add(mRedditRepository.getSearchResults(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Pair<String, List<SearchResult>>>() {
                    override fun onNext(@io.reactivex.annotations.NonNull resultPair: Pair<String, List<SearchResult>>) {
                        searchAfter = resultPair.first
                        mSearchView!!.showResults(resultPair.second)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e(e)
                        mSearchView!!.showError(e.toString())
                    }

                    override fun onComplete() {

                    }
                }))
    }

    override fun searchMoreSubreddits() {
        mSearchView!!.showProgressbar()
        disposables.add(mRedditRepository.getMoreSearchResults(searchQuery!!, searchAfter!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Pair<String, List<SearchResult>>>() {
                    override fun onNext(@io.reactivex.annotations.NonNull resultPair: Pair<String, List<SearchResult>>) {
                        searchAfter = resultPair.first
                        mSearchView!!.showResults(resultPair.second)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e(e)
                        mSearchView!!.showError(e.toString())
                    }

                    override fun onComplete() {

                    }
                }))
    }

    override fun setView(view: SearchContract.View) {
        mSearchView = view
    }

    override fun start() {

    }

    override fun dispose() {
        disposables.dispose()
    }
}
