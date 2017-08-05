package torille.fi.lurkforreddit.data.remote

import android.support.v4.util.Pair
import android.text.TextUtils
import com.google.gson.stream.JsonReader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentListing
import torille.fi.lurkforreddit.data.models.jsonResponses.PostListing
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditListing
import torille.fi.lurkforreddit.data.models.view.*
import torille.fi.lurkforreddit.di.scope.RedditScope
import torille.fi.lurkforreddit.utils.CommentsStreamingParser
import torille.fi.lurkforreddit.utils.Store
import torille.fi.lurkforreddit.utils.TextHelper
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Implementation of Reddit API
 */
@RedditScope
class RedditRemoteDataSource @Inject
internal constructor(private val mRedditApi: RedditService.Reddit,
                     private val mSettingsStore: Store,
                     private val mCommentsStreamingParser: CommentsStreamingParser) : RedditDataSource {

    override fun getSubreddits(): Observable<List<Subreddit>> {
        Timber.d("Fetching subs!")
        val subreddits: Observable<SubredditListing>
        if (mSettingsStore.isLoggedIn) {
            Timber.d("Was logged in, getting personal subreddits")
            subreddits = mRedditApi.getMySubreddits(200)

            return Observable.zip(fetchSubreddits(subreddits), getUserMultireddits(),
                    BiFunction({
                        s: List<Subreddit>, m: List<Subreddit> ->
                        s.plus(m)
                    }))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        } else {
            Timber.d("Was not logged in, getting default subreddits")
            subreddits = mRedditApi.getDefaultSubreddits(100)

            return fetchSubreddits(subreddits)
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<Pair<String, List<Post>>> {
        return mRedditApi
                .getSubreddit(subredditUrl)
                .observeOn(Schedulers.computation())
                .map(funcFormatPostData)
    }

    override fun getMoreSubredditPosts(subredditUrl: String, nextpageId: String): Observable<Pair<String, List<Post>>> {
        return mRedditApi.getSubredditNextPage(subredditUrl, nextpageId)
                .observeOn(Schedulers.computation())
                .map(funcFormatPostData)
    }

    override fun refreshData() {
        //empty
    }

    override fun getCommentsForPost(permaLinkUrl: String,
                                    isSingleCommentThread: Boolean): Observable<PostAndComments> {
        return mRedditApi.getComments(permaLinkUrl)
                .map { responseBody ->
                    responseBody.byteStream().use { stream ->
                        InputStreamReader(stream, "UTF-8").use { input ->
                            JsonReader(input).use { reader ->
                                mCommentsStreamingParser.readCommentListingArray(reader)
                            }
                        }
                    }
                }
                .observeOn(Schedulers.computation())
                .map { commentListings ->
                    val commentListingsObservable = Observable.fromArray(commentListings)
                    Observable.zip<Post, List<Comment>, PostAndComments>(getPost(commentListingsObservable),
                            getFormattedComments(commentListingsObservable,
                                    isSingleCommentThread),
                            BiFunction<Post, List<Comment>, PostAndComments> { post, comments -> PostAndComments(post, comments) })
                }.map { it.blockingSingle() }
    }

    override fun getMoreCommentsForPostAt(childCommentIds: List<String>,
                                          linkId: String,
                                          commentLevel: Int): Observable<List<Comment>> {
        return mRedditApi.getMoreComments(linkId,
                TextUtils.join(",", childCommentIds),
                "json")
                .observeOn(Schedulers.computation())
                .map { responseBody ->
                    responseBody.byteStream().use { stream ->
                        InputStreamReader(stream, "UTF-8").use { input ->
                            JsonReader(input).use { reader ->
                                mCommentsStreamingParser.readMoreComments(reader)
                            }
                        }
                    }
                }
                .map { commentChildren -> TextHelper.flattenAdditionalComments(commentChildren, commentLevel) }
    }

    override fun getSearchResults(query: String): Observable<Pair<String, List<SearchResult>>> {

        return mRedditApi.searchSubreddits(query, "relevance")
                .observeOn(Schedulers.computation())
                .map(formatSearchData)
    }

    override fun getMoreSearchResults(query: String, after: String): Observable<Pair<String, List<SearchResult>>> {
        return mRedditApi.searchSubredditsNextPage(query, "relevance", after)
                .observeOn(Schedulers.computation())
                .map(formatSearchData)
    }

    fun getUserMultireddits(): Observable<List<Subreddit>> {
        return mRedditApi.getUserMultireddits()
                .observeOn(Schedulers.computation())
                .map { multiredditListingArray ->
                    multiredditListingArray.map { (multireddit) ->
                        Subreddit(
                                id = multireddit.name,
                                url = multireddit.pathUrl,
                                keyColor = multireddit.keyColor,
                                displayName = multireddit.displayName
                        )
                    }
                }
    }


    private fun fetchSubreddits(listingObservable: Observable<SubredditListing>): Observable<List<Subreddit>> {
        return listingObservable
                .subscribeOn(Schedulers.computation())
                .map { subredditListing -> Observable.fromIterable(subredditListing.data.children) }
                .flatMap { TextHelper.formatSubreddit(it) }
                .toSortedList { subreddit1, subreddit2 -> subreddit1.displayName.compareTo(subreddit2.displayName, ignoreCase = true) }.toObservable()
    }

    private fun getPost(observable: Observable<List<CommentListing>>): Observable<Post> {
        return observable
                .map { commentListings ->
                    commentListings[0]
                            .commentData
                            .commentChildren[0]
                            .originalPost!!
                }
                .map(TextHelper.funcFormatPost)
    }

    private fun getFormattedComments(observable: Observable<List<CommentListing>>, isSingleCommentThread: Boolean): Observable<List<Comment>> {

        return observable
                .subscribeOn(Schedulers.computation())
                .map { commentListings ->
                    commentListings[1]
                            .commentData
                            .commentChildren
                }
                .map { commentChildren -> TextHelper.flatten(commentChildren, 0) }
                .map { comments ->
                    if (isSingleCommentThread) {
                        val commentsMutable = comments.toMutableList()
                        commentsMutable[0] = commentsMutable[0].copy(kind = kind.SINGLECOMMENTTOP)
                        commentsMutable.toList()
                    } else {
                        comments
                    }
                }
    }

    private val funcFormatPostData = { postListing: PostListing ->
        val nextPageId = postListing.data.nextPage

        Observable.zip(Observable.fromArray<String>(nextPageId),
                getAndFormatPosts(Observable.fromArray(postListing)),
                BiFunction<String, List<Post>, Pair<String, List<Post>>> { first, second -> Pair(first, second) })
                .blockingSingle()
    }

    private fun getAndFormatPosts(postListingObservable: Observable<PostListing>): Observable<List<Post>> {
        return postListingObservable
                .map { postListing -> postListing.data.Posts }
                .map { Observable.fromIterable(it) }
                .flatMap(TextHelper.funcFormatPostResponse)
                .toList().toObservable()
    }

    private val formatSearchData = { subredditListing: SubredditListing ->
        val subredditListingObservable = Observable.fromArray<SubredditListing>(subredditListing)
        Observable.zip(Observable.fromArray(subredditListing.data.after),
                getAndFormatSearchResults(subredditListingObservable),
                BiFunction<String, List<SearchResult>, Pair<String, List<SearchResult>>> { first, second ->
                    Pair(first, second)
                }).blockingSingle()
    }

    private fun getAndFormatSearchResults(subredditListing: Observable<SubredditListing>): Observable<List<SearchResult>> {
        return subredditListing.map { subredditListing1 -> Observable.fromIterable(subredditListing1.data.children) }
                .flatMap { TextHelper.formatSearchResult(it) }
                .toList().toObservable()
    }
}
