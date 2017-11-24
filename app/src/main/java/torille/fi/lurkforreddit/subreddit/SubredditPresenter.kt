package torille.fi.lurkforreddit.subreddit

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
internal constructor(private val redditRepository: RedditRepository,
                     private val subreddit: Subreddit) : SubredditContract.Presenter {

    private var subredditsView: SubredditContract.View? = null
    private var nextPageId: String? = null
    private var firstLoad = true

    private val disposables = CompositeDisposable()

    override fun openComments(clickedPost: Post) {
        subredditsView?.showCommentsUI(clickedPost)
    }

    override fun openCustomTabs(url: String) {
        subredditsView?.showCustomTabsUI(url)
    }

    override fun openMedia(post: Post) {
        val domain = post.domain
        val url = post.url
        when {
            MediaHelper.isContentMedia(url, domain) -> subredditsView?.showMedia(post)
            MediaHelper.launchCustomActivity(post.domain) -> subredditsView?.launchCustomActivity(post)
            post.isSelf -> openComments(post)
            else -> subredditsView?.showCustomTabsUI(url)
        }
    }

    override fun loadPosts() {
        subredditsView?.setProgressIndicator(true)

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment() // App is busy until further notice
        disposables.add(redditRepository.getSubredditPosts(subreddit.url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<kotlin.Pair<String, List<Post>>>() {
                    override fun onNext(@io.reactivex.annotations.NonNull posts: kotlin.Pair<String, List<Post>>) {
                        Timber.d("Got posts to presenter")
                        nextPageId = posts.first
                        subredditsView?.showPosts(posts.second, posts.first)
                    }

                    override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                        Timber.e("Got error")
                        Timber.e(e)
                        subredditsView?.onError(e.toString())
                        subredditsView?.setProgressIndicator(false)
                    }

                    override fun onComplete() {
                        Timber.d("Completed")
                        EspressoIdlingResource.decrement()
                        subredditsView?.setProgressIndicator(false)
                    }
                })
        )

    }

    override fun loadMorePosts() {
        subredditsView?.setListProgressIndicator(true)

        EspressoIdlingResource.increment()
        if (nextPageId.isNullOrEmpty()) {
            Timber.d("No more posts")
            // TODO show no more posts
        } else {
            Timber.d("Fetching more posts at ${subreddit.url} subId $nextPageId")
            disposables.add(redditRepository.getMoreSubredditPosts(subreddit.url, nextPageId!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableObserver<kotlin.Pair<String, List<Post>>>() {
                        override fun onNext(@io.reactivex.annotations.NonNull postsPair: kotlin.Pair<String, List<Post>>) {
                            nextPageId = postsPair.first
                            subredditsView?.setListProgressIndicator(false)
                            subredditsView?.addMorePosts(postsPair.second, postsPair.first)

                        }

                        override fun onError(@io.reactivex.annotations.NonNull e: Throwable) {
                            subredditsView?.onError(e.toString())
                            subredditsView?.setListErrorButton(true)
                        }

                        override fun onComplete() {
                            EspressoIdlingResource.decrement()
                        }
                    }))
        }

    }

    override fun retry() {
        subredditsView?.setListErrorButton(false)
        loadMorePosts()
    }

    override fun takeView(view: SubredditContract.View) {
        subredditsView = view
        if (firstLoad) {
            loadPosts()
            firstLoad = false
        }
    }

    override fun dropView() {
        disposables.dispose()
        subredditsView = null
    }

}
