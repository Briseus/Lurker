package torille.fi.lurkforreddit.data.remote

import android.text.TextUtils
import com.google.gson.stream.JsonReader
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import torille.fi.lurkforreddit.data.RedditDataSource
import torille.fi.lurkforreddit.data.RedditService
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentListing
import torille.fi.lurkforreddit.data.models.jsonResponses.PostListing
import torille.fi.lurkforreddit.data.models.jsonResponses.PostResponse
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditListing
import torille.fi.lurkforreddit.data.models.view.*
import torille.fi.lurkforreddit.di.scope.Remote
import torille.fi.lurkforreddit.utils.CommentsStreamingParser
import torille.fi.lurkforreddit.utils.Store
import torille.fi.lurkforreddit.utils.TextHelper
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Implementation of Reddit API
 */
@Remote
class RedditRemoteDataSource @Inject
internal constructor(
    private val redditApi: RedditService.Reddit,
    private val store: Store,
    private val commentsStreamingParser: CommentsStreamingParser
) : RedditDataSource {

    override fun saveSubreddits(subreddit: List<Subreddit>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveSubreddit(subreddit: Subreddit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSubreddits(): Flowable<List<Subreddit>> {
        Timber.d("Fetching subs!")
        val subreddits: Flowable<SubredditListing>
        if (store.isLoggedIn) {
            Timber.d("Was logged in, getting personal subreddits")
            subreddits = redditApi.getMySubreddits(200)

            return Flowable.zip(fetchSubreddits(subreddits), getUserMultireddits(),
                BiFunction({ s: List<Subreddit>, m: List<Subreddit> ->
                    s.plus(m)
                })
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            Timber.d("Was not logged in, getting default subreddits")
            subreddits = redditApi.getDefaultSubreddits(100)

            return fetchSubreddits(subreddits)
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun getSubredditPosts(subredditUrl: String): Observable<kotlin.Pair<String, List<Post>>> {
        return redditApi
            .getSubreddit(subredditUrl)
            .observeOn(Schedulers.computation())
            .flatMap(funcFormatPostData)
    }

    override fun getMoreSubredditPosts(
        subredditUrl: String,
        nextpageId: String
    ): Observable<kotlin.Pair<String, List<Post>>> {
        return redditApi.getSubredditNextPage(subredditUrl, nextpageId)
            .observeOn(Schedulers.computation())
            .flatMap(funcFormatPostData)
    }

    override fun refreshData() {
        //empty
    }

    override fun getCommentsForPost(
        permaLinkUrl: String,
        isSingleCommentThread: Boolean
    ): Observable<PostAndComments> {
        return redditApi.getComments(permaLinkUrl)
            .map { responseBody ->
                responseBody.byteStream().use { stream ->
                    InputStreamReader(stream, "UTF-8").use { input ->
                        JsonReader(input).use { reader ->
                            commentsStreamingParser.readCommentListingArray(reader)
                        }
                    }
                }
            }
            .observeOn(Schedulers.computation())
            .concatMap { commentListings ->
                val commentListingsObservable = Observable.fromArray(commentListings)
                Observable.zip<Post, List<Comment>, PostAndComments>(
                    getPost(commentListingsObservable),
                    getFormattedComments(commentListingsObservable, isSingleCommentThread),
                    BiFunction { post, comments -> PostAndComments(post, comments) })
            }
    }

    override fun getMoreCommentsForPostAt(
        childCommentIds: List<String>,
        linkId: String,
        commentLevel: Int
    ): Observable<List<Comment>> {
        return redditApi.getMoreComments(
            linkId,
            TextUtils.join(
                ",",
                childCommentIds
            ),
            "json"
        )
            .observeOn(Schedulers.computation())
            .map { responseBody ->
                responseBody.byteStream().use { stream ->
                    InputStreamReader(stream, "UTF-8").use { input ->
                        JsonReader(input).use { reader ->
                            commentsStreamingParser.readMoreComments(reader)
                        }
                    }
                }
            }
            .map { commentChildren ->
                TextHelper.flattenAdditionalComments(
                    commentChildren,
                    commentLevel
                )
            }
    }

    override fun getSearchResults(query: String): Flowable<kotlin.Pair<String, List<SearchResult>>> {

        return redditApi.searchSubreddits(query, "relevance")
            .observeOn(Schedulers.computation())
            .concatMap(formatSearchData)
    }

    override fun getMoreSearchResults(
        query: String,
        after: String
    ): Flowable<kotlin.Pair<String, List<SearchResult>>> {
        return redditApi.searchSubredditsNextPage(query, "relevance", after)
            .observeOn(Schedulers.computation())
            .concatMap(formatSearchData)
    }

    private fun getUserMultireddits(): Flowable<List<Subreddit>> {
        return redditApi.getUserMultireddits()
            .observeOn(Schedulers.computation())
            .map { multiredditListingArray ->
                multiredditListingArray.map { (multireddit) ->
                    Subreddit(
                        subId = multireddit.name,
                        url = multireddit.pathUrl,
                        keyColor = multireddit.keyColor,
                        displayName = multireddit.displayName
                    )
                }
            }
    }


    private fun fetchSubreddits(listingFlowable: Flowable<SubredditListing>): Flowable<List<Subreddit>> {
        return listingFlowable
            .subscribeOn(Schedulers.computation())
            .map { subredditListing -> Flowable.fromIterable(subredditListing.data.children) }
            .flatMap { TextHelper.formatSubreddit(it) }
            .toSortedList { subreddit1, subreddit2 ->
                subreddit1.displayName.compareTo(
                    subreddit2.displayName,
                    ignoreCase = true
                )
            }
            .toFlowable()
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

    private fun getFormattedComments(
        observable: Observable<List<CommentListing>>,
        isSingleCommentThread: Boolean
    ): Observable<List<Comment>> {

        return observable
            .subscribeOn(Schedulers.computation())
            .map { commentListings ->
                commentListings[1]
                    .commentData
                    .commentChildren
            }
            .map { TextHelper.flatten(it, 0) }
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

        Observable.zip(Observable.fromArray<String>(postListing.data.nextPage),
            getAndFormatPosts(Observable.fromArray(postListing)),
            BiFunction<String, List<Post>, kotlin.Pair<String, List<Post>>> { first, second ->
                kotlin.Pair(first, second)
            })
    }

    private fun getAndFormatPosts(postListingObservable: Observable<PostListing>): Observable<List<Post>> {
        return postListingObservable
            .map { postListing -> postListing.data.Posts }
            .map { Observable.fromIterable(it) }
            .flatMap({ postResponseObservable: Observable<PostResponse> ->
                postResponseObservable
                    .map({ it.postDetails })
                    .map(TextHelper.funcFormatPost)
            })
            .toList().toObservable()
    }

    private val formatSearchData = { subredditListing: SubredditListing ->
        val subredditListingFlowable = Flowable.fromArray<SubredditListing>(subredditListing)
        Flowable.zip(Flowable.fromArray(subredditListing.data.after),
            getAndFormatSearchResults(subredditListingFlowable),
            BiFunction<String, List<SearchResult>, kotlin.Pair<String, List<SearchResult>>> { first, second ->
                kotlin.Pair(first, second)
            })
    }

    private fun getAndFormatSearchResults(subredditListing: Flowable<SubredditListing>): Flowable<List<SearchResult>> {
        return subredditListing.map { subredditListing1 -> Flowable.fromIterable(subredditListing1.data.children) }
            .flatMap { TextHelper.formatSearchResult(it) }
            .toList().toFlowable()
    }
}
