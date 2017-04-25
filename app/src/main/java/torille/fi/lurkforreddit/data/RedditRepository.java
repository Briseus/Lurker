package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

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

    private List<Subreddit> mCachedSubreddits;

    @Inject
    public RedditRepository(@Remote RedditDataSource redditApi) {
        mRedditRemoteApi = redditApi;
    }

    @Override
    public void getSubreddits(@NonNull final LoadSubredditsCallback callback, @NonNull ErrorCallback errorCallback) {
        if (mCachedSubreddits == null) {
            mRedditRemoteApi.getSubreddits(new LoadSubredditsCallback() {
                @Override
                public void onSubredditsLoaded(List<Subreddit> subreddits) {
                    mCachedSubreddits = subreddits;
                    callback.onSubredditsLoaded(mCachedSubreddits);
                }
            }, errorCallback);
        } else {
            callback.onSubredditsLoaded(mCachedSubreddits);
        }
    }

    @Override
    public void getSubredditPosts(@NonNull String subredditUrl, @NonNull final LoadSubredditPostsCallback callback, @NonNull final ErrorCallback errorCallback) {
        mRedditRemoteApi.getSubredditPosts(subredditUrl, new LoadSubredditPostsCallback() {
            @Override
            public void onPostsLoaded(List<Post> posts, String nextpage) {
                callback.onPostsLoaded(posts, nextpage);
            }
        }, errorCallback);
    }

    @Override
    public void getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId, @NonNull final LoadSubredditPostsCallback callback, @NonNull ErrorCallback errorCallback) {
        mRedditRemoteApi.getMoreSubredditPosts(subredditUrl, nextpageId, new LoadSubredditPostsCallback() {
            @Override
            public void onPostsLoaded(List<Post> posts, String nextpage) {
                callback.onPostsLoaded(posts, nextpage);
            }
        }, errorCallback);
    }

    @Override
    public void refreshData() {
        mCachedSubreddits = null;
    }

    @Override
    public void getCommentsForPost(@NonNull String permaLinkUrl, @NonNull final LoadPostCommentsCallback callback, @NonNull ErrorCallback errorCallback) {
        mRedditRemoteApi.getCommentsForPost(permaLinkUrl, new LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
                callback.onCommentsLoaded(comments);
            }

            @Override
            public void onMoreCommentsLoaded(List<Comment> comments, int position) {

            }
        }, errorCallback);
    }

    @Override
    public void getMoreCommentsForPostAt(@NonNull Comment parentComment, @NonNull String linkId, int position, @NonNull final LoadPostCommentsCallback callback, @NonNull ErrorCallback errorCallback) {
        mRedditRemoteApi.getMoreCommentsForPostAt(parentComment, linkId, position, new LoadPostCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {

            }

            @Override
            public void onMoreCommentsLoaded(List<Comment> comments, int position) {
                callback.onMoreCommentsLoaded(comments, position);
            }
        }, errorCallback);
    }

    @Override
    public void getSearchResults(@NonNull String query, @NonNull final LoadCommentsCallback callback, @NonNull ErrorCallback errorCallback) {
        mRedditRemoteApi.getSearchResults(query, new LoadCommentsCallback() {
            @Override
            public void onSearchLoaded(List<SearchResult> results, String after) {
                callback.onSearchLoaded(results, after);
            }
        }, errorCallback);
    }

    @Override
    public void getMoreSearchResults(@NonNull String query, @NonNull String after, @NonNull final LoadCommentsCallback callback, @NonNull ErrorCallback errorCallback) {
        mRedditRemoteApi.getMoreSearchResults(query, after, new LoadCommentsCallback() {
            @Override
            public void onSearchLoaded(List<SearchResult> results, String after) {
                callback.onSearchLoaded(results, after);
            }
        }, errorCallback);
    }
}
