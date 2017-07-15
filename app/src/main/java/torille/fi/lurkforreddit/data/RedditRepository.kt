package torille.fi.lurkforreddit.data

import android.support.v4.util.Pair
import io.reactivex.Observable
import torille.fi.lurkforreddit.data.models.view.*
import torille.fi.lurkforreddit.di.scope.RedditScope
import javax.inject.Inject

@RedditScope
class RedditRepository @Inject
constructor(@param:Remote private val mRedditRemoteApi: RedditDataSource) : RedditDataSource {

    private var mCachedSubreddits: List<Subreddit> = emptyList()

    override fun getSubreddits(): Observable<List<Subreddit>> {
        if (mCachedSubreddits.isEmpty()) {
            return mRedditRemoteApi.getSubreddits()
                    .flatMap { subreddits ->
                        Observable.fromArray(subreddits)
                                .doOnNext { subreddits1 -> mCachedSubreddits = subreddits1 }
                    }
        } else {
            return Observable.fromArray(mCachedSubreddits)
        }
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<Pair<String, List<Post>>> {
        return mRedditRemoteApi.getSubredditPosts(subredditUrl)
    }

    override fun getMoreSubredditPosts(subredditUrl: String, nextpageId: String): Observable<Pair<String, List<Post>>> {
        return mRedditRemoteApi.getMoreSubredditPosts(subredditUrl, nextpageId)
    }

    override fun refreshData() {
        mCachedSubreddits = emptyList()
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
