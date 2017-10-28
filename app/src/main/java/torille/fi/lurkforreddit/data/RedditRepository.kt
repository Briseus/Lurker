package torille.fi.lurkforreddit.data

import android.support.v4.util.Pair
import io.reactivex.Observable
import torille.fi.lurkforreddit.data.models.view.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject
constructor(@Remote private val mRedditRemoteApi: RedditDataSource) : RedditDataSource {

    private var cachedSubreddits: List<Subreddit> = emptyList()

    override fun getSubreddits(): Observable<List<Subreddit>> {
        return if (cachedSubreddits.isEmpty()) {
            mRedditRemoteApi.getSubreddits()
                    .flatMap { subreddits ->
                        cachedSubreddits = subreddits
                        Observable.fromArray(subreddits)
                                .doOnNext { list -> cachedSubreddits = list }
                    }
        } else {
            Observable.fromArray(cachedSubreddits)
        }
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<Pair<String, List<Post>>> {
        return mRedditRemoteApi.getSubredditPosts(subredditUrl)
    }

    override fun getMoreSubredditPosts(subredditUrl: String, nextpageId: String): Observable<Pair<String, List<Post>>> {
        return mRedditRemoteApi.getMoreSubredditPosts(subredditUrl, nextpageId)
    }

    override fun refreshData() {
        cachedSubreddits = emptyList()
    }

    override fun getCommentsForPost(permaLinkUrl: String, isSingleCommentThread: Boolean): Observable<PostAndComments> {
        return mRedditRemoteApi.getCommentsForPost(permaLinkUrl, isSingleCommentThread)
    }

    override fun getMoreCommentsForPostAt(childCommentIds: List<String>,
                                          linkId: String,
                                          commentLevel: Int): Observable<List<Comment>> {
        return mRedditRemoteApi.getMoreCommentsForPostAt(childCommentIds, linkId, commentLevel)
    }

    override fun getSearchResults(query: String): Observable<Pair<String, List<SearchResult>>> {
        return mRedditRemoteApi.getSearchResults(query)
    }

    override fun getMoreSearchResults(query: String, after: String): Observable<Pair<String, List<SearchResult>>> {
        return mRedditRemoteApi.getMoreSearchResults(query, after)
    }
}
