package torille.fi.lurkforreddit.data.remote;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.MultiredditListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditListing;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.PostAndComments;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.di.scope.RedditScope;
import torille.fi.lurkforreddit.utils.CommentsStreamingParser;
import torille.fi.lurkforreddit.utils.Store;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Created by eva on 23.4.2017.
 */
@RedditScope
public class RedditRemoteDataSource implements RedditDataSource {

    private final RedditService.Reddit mRedditApi;
    private final Store mSettingsStore;
    private final CommentsStreamingParser mCommentsStreamingParser;

    @Inject
    RedditRemoteDataSource(@NonNull RedditService.Reddit api,
                           @NonNull Store store,
                           @NonNull CommentsStreamingParser commentsStreamingParser) {
        mRedditApi = api;
        mSettingsStore = store;
        mCommentsStreamingParser = commentsStreamingParser;
    }

    @Override
    public Observable<List<Subreddit>> getSubreddits() {
        Timber.d("Fetching subs!");
        Observable<SubredditListing> subreddits;
        if (mSettingsStore.isLoggedIn()) {
            Timber.d("Was logged in, getting personal subreddits");
            subreddits = mRedditApi.getMySubreddits(200);

            return Observable.zip(fetchSubreddits(subreddits), getUserMultireddits(),
                    (subreddits1, subreddits2) -> {
                        Collections.addAll(subreddits1, subreddits2.toArray(new Subreddit[0]));
                        return subreddits1;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            Timber.d("Was not logged in, getting default subreddits");
            subreddits = mRedditApi.getDefaultSubreddits(100);
            return fetchSubreddits(subreddits);

        }
    }

    @Override
    public Observable<Pair<String, List<Post>>> getSubredditPosts(@NonNull String subredditUrl) {

        return mRedditApi
                .getSubreddit(subredditUrl)
                .observeOn(Schedulers.computation())
                .map(funcFormatPostData);
    }

    @Override
    public Observable<Pair<String, List<Post>>> getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId) {
        return mRedditApi.getSubredditNextPage(subredditUrl, nextpageId)
                .observeOn(Schedulers.computation())
                .map(funcFormatPostData);
    }

    @Override
    public void refreshData() {
        //empty
    }

    @Override
    public Observable<PostAndComments> getCommentsForPost(@NonNull String permaLinkUrl,
                                                          final boolean isSingleCommentThread) {

        return mRedditApi.getComments(permaLinkUrl)
                .map(responseBody -> {
                    try (InputStream stream = responseBody.byteStream();
                         InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                         JsonReader reader = new JsonReader(in)) {

                        return mCommentsStreamingParser
                                .readCommentListingArray(reader);
                    }
                })
                .observeOn(Schedulers.computation())
                .map(commentListings -> {
                    Observable<List<CommentListing>> commentListingsObservable = Observable.fromArray(commentListings);
                    return Observable.zip(getPost(commentListingsObservable),
                            getFormattedComments(commentListingsObservable,
                                    isSingleCommentThread),
                            PostAndComments::create).blockingSingle();
                });
    }

    @Override
    public Observable<List<Comment>> getMoreCommentsForPostAt(@NonNull List<String> childCommentIds,
                                                              @NonNull String linkId,
                                                              final int commentLevel) {
        return mRedditApi.getMoreComments(linkId,
                TextUtils.join(",", childCommentIds),
                "json")
                .observeOn(Schedulers.computation())
                .map(responseBody -> {
                    try (InputStream stream = responseBody.byteStream();
                         InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                         JsonReader reader = new JsonReader(in)) {

                        return mCommentsStreamingParser
                                .readMoreComments(reader);
                    }
                })
                .map(commentChildren ->
                        TextHelper.flattenAdditionalComments(commentChildren, commentLevel));
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getSearchResults(@NonNull String query) {

        return mRedditApi.searchSubreddits(query, "relevance")
                .observeOn(Schedulers.computation())
                .map(formatSearchData)
                .doOnError(throwable -> {
                    return ;
                });
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getMoreSearchResults(@NonNull String query, @NonNull String afterId) {
        return mRedditApi.searchSubredditsNextPage(query, "relevance", afterId)
                .observeOn(Schedulers.computation())
                .map(formatSearchData);
    }

    private Observable<List<Subreddit>> getUserMultireddits() {
        return mRedditApi.getUserMultireddits()
                .observeOn(Schedulers.computation())
                .map(Observable::fromArray)
                .flatMap(multiredditListingObservable -> multiredditListingObservable.map(MultiredditListing::multireddit))
                .map(multireddit -> Subreddit.builder()
                        .setUrl(multireddit.pathUrl())
                        .setBannerUrl(null)
                        .setId(multireddit.name())
                        .setKeyColor(multireddit.keyColor())
                        .setDisplayName(multireddit.displayName())
                        .build())
                .toList().toObservable();
    }

    private Observable<List<Subreddit>> fetchSubreddits(Observable<SubredditListing> listingObservable) {
        return listingObservable
                .subscribeOn(Schedulers.computation())
                .map(subredditListing -> Observable.fromIterable(subredditListing.data().children()))
                .flatMap(TextHelper::formatSubreddit)
                .toSortedList((subreddit1, subreddit2) ->
                        subreddit1.displayName().compareToIgnoreCase(subreddit2.displayName())
                ).toObservable();
    }

    private Observable<Post> getPost(final Observable<List<CommentListing>> observable) {
        return observable
                .map(commentListings -> Observable.fromArray(commentListings.get(0)
                        .commentData()
                        .commentChildren()
                        .get(0)
                        .originalPost()))
                .map(TextHelper.funcFormatPostDetails).blockingSingle();
    }

    private Observable<List<Comment>> getFormattedComments(Observable<List<CommentListing>> observable, final boolean isSingleCommentThread) {

        return observable
                .subscribeOn(Schedulers.computation())
                .map(commentListings ->
                        commentListings.get(1)
                                .commentData()
                                .commentChildren())
                .map(commentChildren -> TextHelper.flatten(commentChildren, 0))
                .map(comments -> {
                    if (isSingleCommentThread) {
                        comments.set(0, comments.get(0).withKind(Comment.kind.SINGLECOMMENTTOP));
                    }
                    return comments;
                });
    }

    private Function<PostListing, Pair<String, List<Post>>> funcFormatPostData = postListing -> {
        String nextPageId = postListing.data().nextPage();

        return Observable.zip(Observable.fromArray(nextPageId),
                getAndFormatPosts(Observable.fromArray(postListing)),
                Pair::new)
                .blockingSingle();
    };

    private Observable<List<Post>> getAndFormatPosts(Observable<PostListing> postListingObservable) {
        return postListingObservable
                .map(postListing -> postListing.data().Posts())
                .map(Observable::fromIterable)
                .flatMap(TextHelper.funcFormatPostResponse)
                .toList().toObservable();
    }

    private Function<SubredditListing, Pair<String, List<SearchResult>>> formatSearchData = subredditListing -> {
        Observable<SubredditListing> subredditListingObservable = Observable.fromArray(subredditListing);
        return Observable.zip(Observable.fromArray(subredditListing.data().after()),
                getAndFormatSearchResults(subredditListingObservable),
                Pair::new)
                .blockingSingle();
    };

    private Observable<List<SearchResult>> getAndFormatSearchResults(Observable<SubredditListing> subredditListing) {
        return subredditListing.map(subredditListing1 -> Observable.fromIterable(subredditListing1.data().children()))
                .flatMap(TextHelper::formatSearchResult)
                .toList().toObservable();
    }
}
