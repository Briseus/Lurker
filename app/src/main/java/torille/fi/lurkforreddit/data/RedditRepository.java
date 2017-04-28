package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.di.scope.RedditScope;

/**
 * Created by eva on 23.4.2017.
 */
@RedditScope
public class RedditRepository implements RedditDataSource {

    private final RedditDataSource mRedditRemoteApi;

    @Nullable
    private List<Subreddit> mCachedSubreddits;

    @Inject
    public RedditRepository(@Remote RedditDataSource redditApi) {
        mRedditRemoteApi = redditApi;
    }

    @Override
    public Observable<List<Subreddit>> getSubreddits() {
        if (mCachedSubreddits == null) {
            return mRedditRemoteApi.getSubreddits()
                    .flatMap(new Function<List<Subreddit>, Observable<List<Subreddit>>>() {
                        @Override
                        public Observable<List<Subreddit>> apply(@io.reactivex.annotations.NonNull List<Subreddit> subreddits) throws Exception {
                            return Observable.fromArray(subreddits)
                                    .doOnNext(new Consumer<List<Subreddit>>() {
                                        @Override
                                        public void accept(@io.reactivex.annotations.NonNull List<Subreddit> subreddits) throws Exception {
                                            mCachedSubreddits = subreddits;
                                        }
                                    });
                        }
                    });
        } else {
            return  Observable.fromArray(mCachedSubreddits);
        }
    }

    @Override
    public Observable<Pair<String, List<Post>>> getSubredditPosts(@NonNull String subredditUrl) {
        return mRedditRemoteApi.getSubredditPosts(subredditUrl);
    }

    @Override
    public Observable<Pair<String, List<Post>>> getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId) {
        return mRedditRemoteApi.getMoreSubredditPosts(subredditUrl, nextpageId);
    }

    @Override
    public void refreshData() {
        mCachedSubreddits = null;
    }

    @Override
    public Observable<List<Comment>> getCommentsForPost(@NonNull String permaLinkUrl) {
        return mRedditRemoteApi.getCommentsForPost(permaLinkUrl);
    }

    @Override
    public Observable<List<Comment>> getMoreCommentsForPostAt(@NonNull List<String> childCommentIds,
                                                              @NonNull String linkId,
                                                              int commentLevel) {
        return mRedditRemoteApi.getMoreCommentsForPostAt(childCommentIds, linkId, commentLevel);
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getSearchResults(@NonNull String query) {
        return mRedditRemoteApi.getSearchResults(query);
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getMoreSearchResults(@NonNull String query, @NonNull String afterId) {
        return mRedditRemoteApi.getMoreSearchResults(query, afterId);
    }
}
