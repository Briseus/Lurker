package torille.fi.lurkforreddit.subreddit

import android.support.v4.util.Pair
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.Subreddit
import torille.fi.lurkforreddit.utils.MediaHelper
import torille.fi.lurkforreddit.utils.test.EspressoIdlingResource
import javax.inject.Inject

class SubredditPresenter @Inject
internal constructor(private val mRedditRepository: RedditRepository,
                     private val mSubreddit: Subreddit) : SubredditContract.Presenter {

    private var mSubredditsView: SubredditContract.View? = null
    private var nextPageId: String? = null
    private var firstLoad = true

    private val disposables = CompositeDisposable()

    override fun openComments(clickedPost: Post) {
        mSubredditsView?.showCommentsUI(clickedPost)
    }

    override fun openCustomTabs(url: String) {
        mSubredditsView?.showCustomTabsUI(url)
    }

    override fun openMedia(post: Post) {
        val domain = post.domain
        val url = post.url
        if (MediaHelper.isContentMedia(url, domain)) {
            mSubredditsView?.showMedia(post)
        } else if (MediaHelper.launchCustomActivity(post.domain)) {
            mSubredditsView?.launchCustomActivity(post)
        } else if (post.isSelf) {
            openComments(post)
        } else {
            mSubredditsView?.showCustomTabsUI(url)
        }
    }

    override fun loadPosts() {
        mSubredditsView?.setProgressIndicator(true)

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment() // App is busy until further notice
        disposables.add(mRedditRepository.getSubredditPosts(mSubreddit.url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Pair<String, List<Post>>>() {
                    override fun onNext(@io.reactivex.annotations.NonNull posts: Pair<String, List<Post>>) {
                        Timber.d("Got posts to presenter")
                        nextPageId = posts.first
                        mSubredditsView?.showPosts(posts.second!!, posts.first!!)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e("Got error")
                        Timber.e(e)
                        mSubredditsView?.onError(e.toString())
                        mSubredditsView?.setProgressIndicator(false)
                    }

                    override fun onComplete() {
                        Timber.d("Completed")
                        EspressoIdlingResource.decrement()
                        mSubredditsView?.setProgressIndicator(false)
                    }
                })
        )

    }

    override fun loadMorePosts() {
        mSubredditsView?.setListProgressIndicator(true)

        EspressoIdlingResource.increment()
        if(nextPageId.isNullOrEmpty()) {
            Timber.d("No more posts")
            // TODO show no more posts
        } else {
            Timber.d("Fetching more posts at ${mSubreddit.url} id ${nextPageId}")
            disposables.add(mRedditRepository.getMoreSubredditPosts(mSubreddit.url, nextPageId!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableObserver<Pair<String, List<Post>>>() {
                        override fun onNext(@io.reactivex.annotations.NonNull postsPair: Pair<String, List<Post>>) {
                            nextPageId = postsPair.first
                            mSubredditsView?.setListProgressIndicator(false)
                            mSubredditsView?.addMorePosts(postsPair.second!!, postsPair.first!!)

                        }

                        override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                            mSubredditsView?.onError(e.toString())
                            mSubredditsView?.setListErrorButton(true)
                        }

                        override fun onComplete() {
                            EspressoIdlingResource.decrement()
                        }
                    }))
        }

    }

    override fun retry() {
        mSubredditsView?.setListErrorButton(false)
        loadMorePosts()
    }

    override fun takeView(view: SubredditContract.View) {
        mSubredditsView = view
        if (firstLoad) {
            loadPosts()
            firstLoad = false
        }
    }

    override fun dropView() {
        disposables.dispose()
        mSubredditsView = null
    }

}
