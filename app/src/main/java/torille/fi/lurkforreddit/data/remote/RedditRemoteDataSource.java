package torille.fi.lurkforreddit.data.remote;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    @Inject
    public RedditRemoteDataSource(@NonNull RedditService.Reddit api, @NonNull Store store) {
        mRedditApi = api;
        mSettingsStore = store;
    }

    private Observable<List<Subreddit>> getUserMultireddits() {
        return mRedditApi.getUserMultireddits()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<MultiredditListing[], List<Subreddit>>() {
                    @Override
                    public List<Subreddit> apply(@io.reactivex.annotations.NonNull MultiredditListing[] multireddits) throws Exception {
                        List<Subreddit> multies = new ArrayList<Subreddit>();
                        for (MultiredditListing multireddit : multireddits) {
                            Timber.d(multireddit.toString());
                            multies.add(TextHelper.formatSubreddit(multireddit.multireddit()));
                        }
                        return multies;
                    }
                });
    }

    private Observable<List<Subreddit>> getUserSubreddits() {
        return mRedditApi.getMySubreddits(200)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(new Function<SubredditListing, List<Subreddit>>() {
                    @Override
                    public List<Subreddit> apply(@io.reactivex.annotations.NonNull SubredditListing subredditListing) throws Exception {
                        return TextHelper.formatSubreddits(subredditListing.data().children());
                    }
                });
    }

    @Override
    public Observable<List<Subreddit>> getSubreddits() {
        Timber.d("Fetching subs!");


        if (mSettingsStore.isLoggedIn()) {
            Timber.d("Was logged in, getting personal subreddits");
            return Observable.zip(getUserSubreddits(), getUserMultireddits(),
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
            return mRedditApi.getDefaultSubreddits(100)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .map(new Function<SubredditListing, List<Subreddit>>() {
                        @Override
                        public List<Subreddit> apply(@io.reactivex.annotations.NonNull SubredditListing subredditListing) throws Exception {
                            return TextHelper.formatSubreddits(subredditListing.data().children());
                        }
                    });
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

                            List<CommentListing> commentListings = CommentsStreamingParser
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

                            List<CommentChild> additionalComments = CommentsStreamingParser
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
