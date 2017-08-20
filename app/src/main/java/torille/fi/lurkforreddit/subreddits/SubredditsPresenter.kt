package torille.fi.lurkforreddit.subreddits

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.utils.test.EspressoIdlingResource
import javax.inject.Inject

/**
 * Presenter in the MVP model of displaying subreddits
 */
@ActivityScoped
class SubredditsPresenter @Inject
constructor(private val mRedditRepository: RedditRepository) : SubredditsContract.Presenter {

    private lateinit var mSubredditsView: SubredditsContract.View
    private val disposables = CompositeDisposable()

    override fun loadSubreddits(forcedUpdate: Boolean) {
        Timber.d("Going to fetch subs!")
        mSubredditsView.setProgressIndicator(true)
        if (forcedUpdate) {
            mRedditRepository.refreshData()
        }
        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment() // App is busy until further notice
        disposables.add(mRedditRepository.getSubreddits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<List<Subreddit>>() {
                    override fun onNext(@io.reactivex.annotations.NonNull subreddits: List<Subreddit>) {
                        mSubredditsView.showSubreddits(subreddits)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        mSubredditsView.onError(e.toString())
                        mSubredditsView.setProgressIndicator(false)
                    }

                    override fun onComplete() {
                        EspressoIdlingResource.decrement() // Set app as idle.
                        mSubredditsView.setProgressIndicator(false)
                    }
                }))
    }

    override fun openSubreddit(subreddit: Subreddit) {
        mSubredditsView.loadSelectedSubreddit(subreddit)
    }

    override fun dropView() {
        mSubredditsView.setProgressIndicator(false)
        disposables.dispose()
    }

    override fun takeView(view: SubredditsContract.View) {
        mSubredditsView = view
        loadSubreddits(false)
    }

}
