package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

/**
 * Main entry point for accessing data.
 */
public interface RedditDataSource {

    interface ErrorCallback {
        void onError(String errorText);
    }

    interface LoadSubredditsCallback {
        void onSubredditsLoaded(List<Subreddit> subreddits);
    }

    interface LoadSubredditPostsCallback {
        void onPostsLoaded(List<Post> posts, String nextpage);
    }

    interface LoadPostCommentsCallback {
        void onCommentsLoaded(List<Comment> comments);
        void onMoreCommentsLoaded(List<Comment> comments, int position);
    }

    interface LoadCommentsCallback {
        void onSearchLoaded(List<SearchResult> subredditChildrens, String after);
    }

    void getSubreddits(@NonNull LoadSubredditsCallback callback,
                       @NonNull ErrorCallback errorCallback);

    void getSubredditPosts(@NonNull String subredditUrl,
                           @NonNull LoadSubredditPostsCallback callback,
                           @NonNull ErrorCallback errorCallback);

    void getMoreSubredditPosts(@NonNull String subredditUrl,
                               @NonNull String nextpageId,
                               @NonNull LoadSubredditPostsCallback callback,
                               @NonNull ErrorCallback errorCallback);

    void refreshData();

    void getCommentsForPost(@NonNull String permaLinkUrl,
                            @NonNull LoadPostCommentsCallback callback,
                            @NonNull ErrorCallback errorCallback);

    void getMoreCommentsForPostAt(@NonNull Comment parentComment,
                                  @NonNull String linkId,
                                  int position,
                                  @NonNull LoadPostCommentsCallback callback,
                                  @NonNull ErrorCallback errorCallback);

    void getSearchResults(@NonNull String query,
                          @NonNull LoadCommentsCallback callback,
                          @NonNull ErrorCallback errorCallback);

    void getMoreSearchResults(@NonNull String query,
                              @NonNull String after,
                              @NonNull LoadCommentsCallback callback,
                              @NonNull ErrorCallback errorCallback);
}
