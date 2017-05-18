package torille.fi.lurkforreddit.data.remote;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.MultiredditListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren;
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

    private Observable<List<Subreddit>> getUserMultireddits() {
        return mRedditApi.getUserMultireddits()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<MultiredditListing[], Observable<MultiredditListing>>() {
                    @Override
                    public Observable<MultiredditListing> apply(@io.reactivex.annotations.NonNull MultiredditListing[] multireddits) throws Exception {
                        return Observable.fromArray(multireddits);
                    }
                })
                .flatMap(new Function<Observable<MultiredditListing>, Observable<Subreddit>>() {
                    @Override
                    public Observable<Subreddit> apply(@io.reactivex.annotations.NonNull Observable<MultiredditListing> multiredditListingObservable) throws Exception {
                        return multiredditListingObservable.map(new Function<MultiredditListing, Subreddit>() {
                            @Override
                            public Subreddit apply(@io.reactivex.annotations.NonNull MultiredditListing multiredditListing) throws Exception {
                                return TextHelper.formatSubreddit(multiredditListing.multireddit());
                            }
                        });
                    }
                })
                .toList().toObservable();
    }

    private Observable<List<Subreddit>> fetchSubreddits(Observable<SubredditListing> listingObservable) {
        return listingObservable
                .subscribeOn(Schedulers.computation())
                .map(new Function<SubredditListing, Observable<SubredditChildren>>() {
                    @Override
                    public Observable<SubredditChildren> apply(@io.reactivex.annotations.NonNull SubredditListing subredditListing) throws Exception {
                        return Observable.fromIterable(subredditListing.data().children());
                    }
                })
                .flatMap(new Function<Observable<SubredditChildren>, Observable<Subreddit>>() {
                    @Override
                    public Observable<Subreddit> apply(@io.reactivex.annotations.NonNull Observable<SubredditChildren> subredditChildrenObservable) throws Exception {
                        return subredditChildrenObservable.map(new Function<SubredditChildren, Subreddit>() {
                            @Override
                            public Subreddit apply(@io.reactivex.annotations.NonNull SubredditChildren subredditChildren) throws Exception {
                                return TextHelper.formatSubreddit(subredditChildren);
                            }
                        });
                    }
                })
                .toList().toObservable()
                .map(new Function<List<Subreddit>, List<Subreddit>>() {
                    @Override
                    public List<Subreddit> apply(@io.reactivex.annotations.NonNull List<Subreddit> subreddits) throws Exception {
                        Collections.sort(subreddits, new Comparator<Subreddit>() {
                            @Override
                            public int compare(Subreddit o1, Subreddit o2) {
                                String displayName = o1.displayName();
                                String displayName2 = o2.displayName();
                                if (displayName != null && displayName2 != null) {
                                    return displayName.compareToIgnoreCase(displayName2);
                                }
                                return -1;
                            }
                        });
                        return subreddits;
                    }
                });
    }

    @Override
    public Observable<List<Subreddit>> getSubreddits() {
        Timber.d("Fetching subs!");
        Observable<SubredditListing> subreddits;
        if (mSettingsStore.isLoggedIn()) {
            Timber.d("Was logged in, getting personal subreddits");
            subreddits = mRedditApi.getMySubreddits(200);

            return Observable.zip(fetchSubreddits(subreddits), getUserMultireddits(),
                    new BiFunction<List<Subreddit>, List<Subreddit>, List<Subreddit>>() {
                        @Override
                        public List<Subreddit> apply(@io.reactivex.annotations.NonNull List<Subreddit> subreddits, @io.reactivex.annotations.NonNull List<Subreddit> subreddits2) throws Exception {
                            List<Subreddit> combinedList = new ArrayList<Subreddit>(subreddits.size() + subreddits2.size());
                            combinedList.addAll(subreddits);
                            combinedList.addAll(subreddits2);
                            return combinedList;
                        }
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
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<PostListing, Pair<String, List<Post>>>() {
                    @Override
                    public Pair<String, List<Post>> apply(@io.reactivex.annotations.NonNull PostListing postListing) throws Exception {
                        List<Post> results = TextHelper.formatPosts(postListing.data().Posts());
                        String nextPageId = postListing.data().nextPage();
                        return new Pair<String, List<Post>>(nextPageId, results);
                    }
                });
    }

    @Override
    public Observable<Pair<String, List<Post>>> getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId) {
        return mRedditApi.getSubredditNextPage(subredditUrl, nextpageId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<PostListing, Pair<String, List<Post>>>() {
                    @Override
                    public Pair<String, List<Post>> apply(@io.reactivex.annotations.NonNull PostListing postListing) throws Exception {
                        List<Post> results = TextHelper.formatPosts(postListing.data().Posts());
                        String nextPageId = postListing.data().nextPage();
                        return new Pair<String, List<Post>>(nextPageId, results);
                    }
                });
    }

    @Override
    public void refreshData() {
        //empty
    }

    @Override
    public Observable<PostAndComments> getCommentsForPost(@NonNull String permaLinkUrl,
                                                          final boolean isSingleCommentThread) {
        return mRedditApi.getComments(permaLinkUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<ResponseBody, PostAndComments>() {
                    @Override
                    public PostAndComments apply(@io.reactivex.annotations.NonNull ResponseBody responseBody) throws Exception {
                        try (InputStream stream = responseBody.byteStream();
                             InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                             JsonReader reader = new JsonReader(in)) {

                            List<CommentListing> commentListings = mCommentsStreamingParser
                                    .readCommentListingArray(reader);

                            PostDetails postDetails = commentListings.get(0)
                                    .commentData()
                                    .commentChildren().get(0)
                                    .originalPost();

                            Post post = TextHelper.formatPost(postDetails);

                            List<CommentChild> commentChildList = commentListings
                                    .get(1)
                                    .commentData()
                                    .commentChildren();

                            List<Comment> comments = TextHelper.flatten(commentChildList, 0);
                            if (isSingleCommentThread) {
                                comments.set(0, comments.get(0).withKind(Comment.kind.SINGLECOMMENTTOP));
                            }
                            return PostAndComments.create(post, comments);
                        }
                    }
                });
    }

    @Override
    public Observable<List<Comment>> getMoreCommentsForPostAt(@NonNull List<String> childCommentIds,
                                                              @NonNull String linkId,
                                                              final int commentLevel) {
        return mRedditApi.getMoreComments(linkId,
                TextUtils.join(",", childCommentIds),
                "json")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<ResponseBody, List<Comment>>() {
                    @Override
                    public List<Comment> apply(@io.reactivex.annotations.NonNull ResponseBody responseBody) throws Exception {
                        try (InputStream stream = responseBody.byteStream();
                             InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                             JsonReader reader = new JsonReader(in)) {

                            List<CommentChild> additionalComments = mCommentsStreamingParser
                                    .readMoreComments(reader);
                            return TextHelper
                                    .flattenAdditionalComments(additionalComments, commentLevel);
                        }
                    }
                });
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getSearchResults(@NonNull String query) {
        return mRedditApi.searchSubreddits(query, "relevance")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<SubredditListing, Pair<String, List<SearchResult>>>() {
                    @Override
                    public Pair<String, List<SearchResult>> apply(@io.reactivex.annotations.NonNull SubredditListing subredditListing) throws Exception {
                        List<SubredditChildren> results = subredditListing.data().children();
                        String after = subredditListing.data().after();
                        return new Pair<String, List<SearchResult>>(after, TextHelper.formatSearchResults(results));
                    }
                });
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getMoreSearchResults(@NonNull String query, @NonNull String afterId) {
        return mRedditApi.searchSubredditsNextPage(query, "relevance", afterId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<SubredditListing, Pair<String, List<SearchResult>>>() {
                    @Override
                    public Pair<String, List<SearchResult>> apply(@io.reactivex.annotations.NonNull SubredditListing subredditListing) throws Exception {
                        List<SubredditChildren> results = subredditListing.data().children();
                        String after = subredditListing.data().after();
                        return new Pair<String, List<SearchResult>>(after, TextHelper.formatSearchResults(results));
                    }
                });
    }
}
