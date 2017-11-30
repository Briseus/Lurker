package torille.fi.lurkforreddit.data.local

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.SubredditDao
import torille.fi.lurkforreddit.data.models.view.*
import torille.fi.lurkforreddit.di.scope.Local
import javax.inject.Inject

@Local
class RedditLocalDataSource @Inject
internal constructor(private val subredditDao: SubredditDao) : RedditDataSource {

    override fun saveSubreddits(subreddits: List<Subreddit>) {
        subredditDao.insertAll(subreddits)
    }

    override fun getSubreddits(): Flowable<List<Subreddit>> {
        return subredditDao.getSubreddits()
    }

    override fun saveSubreddit(subreddit: Subreddit) {
        Timber.d("Saving subreddit")
        subredditDao.insert(subreddit)
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<Pair<String, List<Post>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMoreSubredditPosts(subredditUrl: String, nextpageId: String): Observable<Pair<String, List<Post>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refreshData() {
        Observable.fromCallable({ subredditDao.deleteAllSubreddits() }).subscribeOn(Schedulers.io()).subscribe()
    }

    override fun getCommentsForPost(permaLinkUrl: String, isSingleCommentThread: Boolean): Observable<PostAndComments> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMoreCommentsForPostAt(childCommentIds: List<String>, linkId: String, commentLevel: Int): Observable<List<Comment>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSearchResults(query: String): Flowable<Pair<String, List<SearchResult>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMoreSearchResults(query: String, after: String): Flowable<Pair<String, List<SearchResult>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
