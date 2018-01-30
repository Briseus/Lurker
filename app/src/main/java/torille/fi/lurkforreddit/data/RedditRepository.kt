package torille.fi.lurkforreddit.data

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import torille.fi.lurkforreddit.data.models.view.*
import torille.fi.lurkforreddit.di.scope.Local
import torille.fi.lurkforreddit.di.scope.Remote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject
constructor(@Remote private val redditRemoteApi: RedditDataSource, @Local private val redditLocalApi: RedditDataSource) :
    RedditDataSource {

    override fun saveSubreddits(subreddits: List<Subreddit>) {
        return redditLocalApi.saveSubreddits(subreddits)
    }

    override fun getSubreddits(): Flowable<List<Subreddit>> {
        return redditLocalApi
            .getSubreddits()
            .map {
                if (it.isNotEmpty()) {
                    Flowable.fromArray(it)
                } else {
                    redditRemoteApi.getSubreddits()
                        .observeOn(Schedulers.io())
                        .doOnNext {
                            redditLocalApi.saveSubreddits(it)
                        }
                }
            }.flatMap { it }

    }

    override fun saveSubreddit(subreddit: Subreddit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<kotlin.Pair<String, List<Post>>> {
        return redditRemoteApi.getSubredditPosts(subredditUrl)
    }

    override fun getMoreSubredditPosts(
        subredditUrl: String,
        nextpageId: String
    ): Observable<kotlin.Pair<String, List<Post>>> {
        return redditRemoteApi.getMoreSubredditPosts(subredditUrl, nextpageId)
    }

    override fun refreshData() {
        redditLocalApi.refreshData()
    }

    override fun getCommentsForPost(
        permaLinkUrl: String,
        isSingleCommentThread: Boolean
    ): Observable<PostAndComments> {
        return redditRemoteApi.getCommentsForPost(permaLinkUrl, isSingleCommentThread)
    }

    override fun getMoreCommentsForPostAt(
        childCommentIds: List<String>,
        linkId: String,
        commentLevel: Int
    ): Observable<List<Comment>> {
        return redditRemoteApi.getMoreCommentsForPostAt(childCommentIds, linkId, commentLevel)
    }

    override fun getSearchResults(query: String): Flowable<kotlin.Pair<String, List<SearchResult>>> {
        return redditRemoteApi.getSearchResults(query)
    }

    override fun getMoreSearchResults(
        query: String,
        after: String
    ): Flowable<kotlin.Pair<String, List<SearchResult>>> {
        return redditRemoteApi.getMoreSearchResults(query, after)
    }
}
