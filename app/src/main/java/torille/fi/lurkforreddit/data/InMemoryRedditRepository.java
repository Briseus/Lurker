package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.List;

import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostResponse;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * In memory cache
 */

class InMemoryRedditRepository implements RedditRepository {

    private final RedditServiceApi mRedditServiceApi;

    @VisibleForTesting
    private List<Subreddit> mCachedSubreddits;

    InMemoryRedditRepository(@NonNull RedditServiceApi redditServiceApi) {
        mRedditServiceApi = redditServiceApi;
    }


    @Override
    public void getSubreddits(@NonNull final LoadSubredditsCallback callback,
                              @NonNull ErrorCallback errorCallback) {
        if (mCachedSubreddits == null) {
            mRedditServiceApi.getSubreddits(new RedditServiceApi.ServiceCallback<List<SubredditChildren>>() {
                @Override
                public void onLoaded(List<SubredditChildren> subreddits) {
                    mCachedSubreddits = TextHelper.formatSubreddits(subreddits);
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
        mRedditServiceApi.getSubredditPosts(subredditId, new RedditServiceApi.ServiceCallbackWithNextpage<List<PostResponse>>() {
            @Override
            public void onLoaded(List<PostResponse> posts, String nextpage) {

                callback.onPostsLoaded(TextHelper.formatPosts(posts), nextpage);
            }
        }, errorCallback);
    }

    @Override
    public void getMoreSubredditPosts(@NonNull String subredditUrl,
                                      @NonNull String nextpageId,
                                      @NonNull final LoadSubredditPostsCallback callback,
                                      @NonNull ErrorCallback errorCallback) {
        mRedditServiceApi.getMorePosts(subredditUrl, nextpageId, new RedditServiceApi.ServiceCallbackWithNextpage<List<PostResponse>>() {
            @Override
            public void onLoaded(List<PostResponse> posts, String nextpage) {
                callback.onPostsLoaded(TextHelper.formatPosts(posts), nextpage);
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
                List<Comment> commentChildFlatList = TextHelper
                        .flatten(comments, 0);
                callback.onCommentsLoaded(commentChildFlatList);
            }

            @Override
            public void onMoreLoaded(List<CommentChild> comments, int position) {

            }
        }, errorCallback);
    }

    @Override
    public void getMoreCommentsForPostAt(@NonNull final Comment parentComment,
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
                List<Comment> additionalFlattenedComments = TextHelper
                        .flattenAdditionalComments(comments, parentComment.commentLevel());
                callback.onMoreCommentsLoaded(additionalFlattenedComments, position);
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
                callback.onSearchLoaded(TextHelper.formatSearchResults(result), after);
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
                callback.onSearchLoaded(TextHelper.formatSearchResults(result), after);
            }
        }, errorCallback);
    }
}
