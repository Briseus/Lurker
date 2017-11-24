package torille.fi.lurkforreddit.search

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.SafeObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import io.reactivex.subscribers.ResourceSubscriber
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.SearchResult
import javax.inject.Inject

class SearchPresenter @Inject
internal constructor(private val redditRepository: RedditRepository) : SearchContract.Presenter {

    private var searchView: SearchContract.View? = null
    private var searchAfter: String? = null
    private var searchQuery: String? = null
    private val disposables = CompositeDisposable()

    override fun searchSubreddits(query: String) {
        this.searchQuery = query
        searchView?.clearResults()
        searchView?.showProgressbar()
        disposables.add(redditRepository.getSearchResults(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSubscriber<kotlin.Pair<String, List<SearchResult>>>() {
                    override fun onNext(resultPair: kotlin.Pair<String, List<SearchResult>>) {
                        searchAfter = resultPair.first
                        searchView?.showResults(resultPair.second)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e(e)
                        searchView?.showError(e.toString())
                    }

                    override fun onComplete() {

                    }
                })
        )
    }

    override fun searchMoreSubreddits() {
        searchView?.showProgressbar()
        disposables.add(redditRepository.getMoreSearchResults(searchQuery!!, searchAfter!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSubscriber<kotlin.Pair<String, List<SearchResult>>>() {
                    override fun onNext(resultPair: kotlin.Pair<String, List<SearchResult>>) {
                        searchAfter = resultPair.first
                        searchView?.showResults(resultPair.second)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e(e)
                        searchView?.showError(e.toString())
                    }

                    override fun onComplete() {

                    }
                })
        )
    }

    override fun takeView(view: SearchContract.View) {
        searchView = view
    }

    override fun dropView() {
        disposables.dispose()
        searchView = null
    }


}
