package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.List;

import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.SubredditChildren;

/**
 * In memory cache
 */

public class InMemoryRedditRepository implements RedditRepository {

    private final RedditServiceApi mRedditServiceApi;

    @VisibleForTesting
    List<SubredditChildren> mCachedSubreddits;

    public InMemoryRedditRepository(@NonNull RedditServiceApi redditServiceApi) {
        mRedditServiceApi = redditServiceApi;
    }



    @Override
    public void getSubreddits(@NonNull final LoadSubredditsCallback callback,
                              @NonNull ErrorCallback errorCallback) {
        if (mCachedSubreddits == null) {
            mRedditServiceApi.getSubreddits(new RedditServiceApi.ServiceCallback<List<SubredditChildren>>() {
                @Override
                public void onLoaded(List<SubredditChildren> subreddits) {
                    mCachedSubreddits = subreddits;
                    callback.onSubredditsLoaded(mCachedSubreddits);
                }
            }, errorCallback);
        } else {
            callback.onSubredditsLoaded(mCachedSubreddits);
        }
    }

    @Override
    public void getSubredditPosts(@NonNull String subredditId,
                                  @NonNull final LoadSubredditPostsCallback callback,
                                  @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getSubredditPosts(subredditId, new RedditServiceApi.ServiceCallbackWithNextpage<List<Post>>() {
            @Override
            public void onLoaded(List<Post> posts, String nextpage) {
                callback.onPostsLoaded(posts, nextpage);
            }
        }, errorCallback);
    }

    @Override
    public void getMoreSubredditPosts(@NonNull String subredditUrl,
                                      @NonNull String nextpageId,
                                      @NonNull final LoadSubredditPostsCallback callback,
                                      @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getMorePosts(subredditUrl, nextpageId, new RedditServiceApi.ServiceCallbackWithNextpage<List<Post>>() {
            @Override
            public void onLoaded(List<Post> posts, String nextpage) {
                callback.onPostsLoaded(posts, nextpage);
            }
        }, errorCallback);
    }

    @Override
    public void refreshData() {
        mCachedSubreddits = null;
    }

    @Override
    public void getCommentsForPost(@NonNull String permaLinkUrl,
                                   @NonNull final LoadPostCommentsCallback callback,
                                   @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getPostComments(permaLinkUrl, new RedditServiceApi.CommentsServiceCallback<List<CommentChild>>() {
            @Override
            public void onLoaded(List<CommentChild> comments) {
                callback.onCommentsLoaded(comments);
            }

            @Override
            public void onMoreLoaded(List<CommentChild> comments, int position) {

            }
        }, errorCallback);
    }

    @Override
    public void getMoreCommentsForPostAt(@NonNull CommentChild parentComment,
                                         @NonNull String linkId,
                                         int position,
                                         @NonNull final LoadPostCommentsCallback callback,
                                         @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getMorePostComments(parentComment, linkId, position, new RedditServiceApi.CommentsServiceCallback<List<CommentChild>>() {
            @Override
            public void onLoaded(List<CommentChild> comments) {

            }

            @Override
            public void onMoreLoaded(List<CommentChild> comments, int position) {
                callback.onMoreCommentsLoaded(comments, position);
            }
        }, errorCallback);
    }

    @Override
    public void getSearchResults(@NonNull String query,
                                 @NonNull final LoadCommentsCallback callback,
                                 @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getSearchResults(query, new RedditServiceApi.ServiceCallbackWithNextpage<List<SubredditChildren>>() {
            @Override
            public void onLoaded(List<SubredditChildren> result, String after) {
                callback.onSearchLoaded(result, after);
            }
        }, errorCallback);
    }

    @Override
    public void getMoreSearchResults(@NonNull String query,
                                     @NonNull String after,
                                     @NonNull final LoadCommentsCallback callback,
                                     @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getMoreSearchResults(query, after, new RedditServiceApi.ServiceCallbackWithNextpage<List<SubredditChildren>>() {
            @Override
            public void onLoaded(List<SubredditChildren> result, String after) {
                callback.onSearchLoaded(result, after);
            }
        }, errorCallback);
    }
}
