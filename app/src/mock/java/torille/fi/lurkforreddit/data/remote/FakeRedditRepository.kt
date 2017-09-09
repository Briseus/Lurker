package torille.fi.lurkforreddit.data.remote

import android.support.v4.util.Pair
import io.reactivex.Observable
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.models.view.*
import torille.fi.lurkforreddit.di.scope.RedditScope
import javax.inject.Inject

/**
 * Created by eva on 25.4.2017.
 */
@RedditScope
class FakeRedditRepository @Inject
internal constructor() : RedditDataSource {

    override fun getCommentsForPost(permaLinkUrl: String, isSingleCommentThread: Boolean): Observable<PostAndComments> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSubreddits(): Observable<List<Subreddit>> {
        return Observable.fromArray(emptyList())
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<Pair<String, List<Post>>> {
        return Observable.fromArray(Pair("first", emptyList()))
    }

    override fun getMoreSubredditPosts(subredditUrl: String, nextpageId: String): Observable<Pair<String, List<Post>>> {
        return Observable.fromArray(Pair("first", emptyList()))
    }

    override fun refreshData() {

    }

    fun getCommentsForPost(permaLinkUrl: String): Observable<List<Comment>>? {
        return null
    }

    override fun getMoreCommentsForPostAt(childCommentIds: List<String>, linkId: String, commentLevel: Int): Observable<List<Comment>> {
        return Observable.fromArray(emptyList())
    }

    override fun getSearchResults(query: String): Observable<Pair<String, List<SearchResult>>> {
        return Observable.fromArray(Pair("first", emptyList()))
    }

    override fun getMoreSearchResults(query: String, after: String): Observable<Pair<String, List<SearchResult>>> {
        return Observable.fromArray(Pair("first", emptyList()))
    }
}
