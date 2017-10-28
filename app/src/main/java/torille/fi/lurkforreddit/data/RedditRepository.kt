package torille.fi.lurkforreddit.data

import io.reactivex.Observable
import torille.fi.lurkforreddit.data.models.view.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject
constructor(@Remote private val redditRemoteApi: RedditDataSource) : RedditDataSource {

    private var cachedSubreddits: List<Subreddit> = emptyList()

    override fun getSubreddits(): Observable<List<Subreddit>> {
        return if (cachedSubreddits.isEmpty()) {
            redditRemoteApi.getSubreddits()
                    .flatMap { subreddits ->
                        cachedSubreddits = subreddits
                        Observable.fromArray(subreddits)
                                .doOnNext { list -> cachedSubreddits = list }
                    }
        } else {
            Observable.fromArray(cachedSubreddits)
        }
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<kotlin.Pair<String, List<Post>>> {
        return redditRemoteApi.getSubredditPosts(subredditUrl)
    }

    override fun getMoreSubredditPosts(subredditUrl: String, nextpageId: String): Observable<kotlin.Pair<String, List<Post>>> {
        return redditRemoteApi.getMoreSubredditPosts(subredditUrl, nextpageId)
    }

    override fun refreshData() {
        cachedSubreddits = emptyList()
    }

    override fun getCommentsForPost(permaLinkUrl: String, isSingleCommentThread: Boolean): Observable<PostAndComments> {
        return redditRemoteApi.getCommentsForPost(permaLinkUrl, isSingleCommentThread)
    }

    override fun getMoreCommentsForPostAt(childCommentIds: List<String>,
                                          linkId: String,
                                          commentLevel: Int): Observable<List<Comment>> {
        return redditRemoteApi.getMoreCommentsForPostAt(childCommentIds, linkId, commentLevel)
    }

    override fun getSearchResults(query: String): Observable<kotlin.Pair<String, List<SearchResult>>> {
        return redditRemoteApi.getSearchResults(query)
    }

    override fun getMoreSearchResults(query: String, after: String): Observable<kotlin.Pair<String, List<SearchResult>>> {
        return redditRemoteApi.getMoreSearchResults(query, after)
    }
}
