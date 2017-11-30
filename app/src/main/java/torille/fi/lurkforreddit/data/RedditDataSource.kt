package torille.fi.lurkforreddit.data

import io.reactivex.Flowable
import io.reactivex.Observable
import torille.fi.lurkforreddit.data.models.view.*

/**
 * Main entry point for accessing data.
 */
interface RedditDataSource {

    fun getSubreddits(): Flowable<List<Subreddit>>

    fun saveSubreddit(subreddit: Subreddit)

    fun saveSubreddits(subreddits: List<Subreddit>)

    fun getSubredditPosts(subredditUrl: String): Observable<kotlin.Pair<String, List<Post>>>

    fun getMoreSubredditPosts(subredditUrl: String,
                              nextpageId: String): Observable<kotlin.Pair<String, List<Post>>>

    fun refreshData()

    fun getCommentsForPost(permaLinkUrl: String, isSingleCommentThread: Boolean): Observable<PostAndComments>

    fun getMoreCommentsForPostAt(childCommentIds: List<String>,
                                 linkId: String,
                                 commentLevel: Int): Observable<List<Comment>>

    fun getSearchResults(query: String): Flowable<kotlin.Pair<String, List<SearchResult>>>

    fun getMoreSearchResults(query: String,
                             after: String): Flowable<kotlin.Pair<String, List<SearchResult>>>
}
