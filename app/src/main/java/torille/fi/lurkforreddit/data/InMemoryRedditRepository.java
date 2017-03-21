package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.List;

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
    public void getSubreddits(@NonNull final LoadSubredditsCallback callback) {
        if (mCachedSubreddits == null) {
            mRedditServiceApi.getSubreddits(new RedditServiceApi.SubredditsServiceCallback<List<SubredditChildren>>() {
                @Override
                public void onLoaded(List<SubredditChildren> subreddits) {
                    mCachedSubreddits = subreddits;
                    callback.onSubredditsLoaded(mCachedSubreddits);
                }
            });
        } else {
            callback.onSubredditsLoaded(mCachedSubreddits);
        }
    }

    @Override
    public void getSubredditPosts(@NonNull String subredditId, @NonNull final LoadSubredditPostsCallback callback) {
        mRedditServiceApi.getSubredditPosts(subredditId, new RedditServiceApi.PostsServiceCallback<List<Post>, String>() {
            @Override
            public void onLoaded(List<Post> posts, String nextpage) {
                callback.onPostsLoaded(posts, nextpage);
            }
        });
    }

    @Override
    public void getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId, @NonNull final LoadSubredditPostsCallback callback) {
        mRedditServiceApi.getMorePosts(subredditUrl, nextpageId, new RedditServiceApi.PostsServiceCallback<List<Post>, String>() {
            @Override
            public void onLoaded(List<Post> posts, String nextpage) {
                callback.onPostsLoaded(posts, nextpage);
            }
        });
    }

    @Override
    public void refreshData() {
        mCachedSubreddits = null;
    }

    @Override
    public void getCommentsForPost(@NonNull String permaLinkUrl, @NonNull final LoadPostCommentsCallback callback) {
        mRedditServiceApi.getPostComments(permaLinkUrl, new RedditServiceApi.CommentsServiceCallback<List<CommentChild>>() {
            @Override
            public void onLoaded(List<CommentChild> comments) {
                callback.onCommentsLoaded(comments);
            }

            @Override
            public void onMoreLoaded(List<CommentChild> comments, int position) {

            }
        });
    }

    @Override
    public void getMoreCommentsForPostAt(@NonNull CommentChild parentComment, @NonNull String linkId, int position, @NonNull final LoadPostCommentsCallback callback) {
        mRedditServiceApi.getMorePostComments(parentComment, linkId, position, new RedditServiceApi.CommentsServiceCallback<List<CommentChild>>() {
            @Override
            public void onLoaded(List<CommentChild> comments) {

            }

            @Override
            public void onMoreLoaded(List<CommentChild> comments, int position) {
                callback.onMoreCommentsLoaded(comments, position);
            }
        });
    }

    @Override
    public void getSearchResults(@NonNull String query, @NonNull final LoadCommentsCallback callback) {
        mRedditServiceApi.getSearchResults(query, new RedditServiceApi.SearchServiceCallback<List<SubredditChildren>>() {
            @Override
            public void onLoaded(List<SubredditChildren> result, String after) {
                callback.onSearchLoaded(result, after);
            }
        });
    }

    @Override
    public void getMoreSearchResults(@NonNull String query, @NonNull String after, @NonNull final LoadCommentsCallback callback) {
        mRedditServiceApi.getMoreSearchResults(query, after, new RedditServiceApi.SearchServiceCallback<List<SubredditChildren>>() {
            @Override
            public void onLoaded(List<SubredditChildren> result, String after) {
                callback.onSearchLoaded(result, after);
            }
        });
    }
}
