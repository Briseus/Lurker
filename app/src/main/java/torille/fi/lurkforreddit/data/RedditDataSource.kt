package torille.fi.lurkforreddit.data

import android.support.v4.util.Pair

import io.reactivex.Observable
import torille.fi.lurkforreddit.data.models.view.Comment
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.data.models.view.PostAndComments
import torille.fi.lurkforreddit.data.models.view.SearchResult
import torille.fi.lurkforreddit.data.models.view.Subreddit

/**
 * Main entry point for accessing data.
 */
interface RedditDataSource {

    fun getSubreddits(): Observable<List<Subreddit>>

    fun getSubredditPosts(subredditUrl: String): Observable<Pair<String, List<Post>>>

    fun getMoreSubredditPosts(subredditUrl: String,
                              nextpageId: String): Observable<Pair<String, List<Post>>>

    fun refreshData()

    fun getCommentsForPost(permaLinkUrl: String, isSingleCommentThread: Boolean): Observable<PostAndComments>

    fun getMoreCommentsForPostAt(childCommentIds: List<String>,
                                 linkId: String,
                                 commentLevel: Int): Observable<List<Comment>>

    fun getSearchResults(query: String): Observable<Pair<String, List<SearchResult>>>

    fun getMoreSearchResults(query: String,
                             after: String): Observable<Pair<String, List<SearchResult>>>
}
