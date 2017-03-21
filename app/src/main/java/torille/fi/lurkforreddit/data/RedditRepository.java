package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.SubredditChildren;

/**
 * Main entry point for accessing data.
 */
public interface RedditRepository {
    interface LoadSubredditsCallback {
        void onSubredditsLoaded(List<SubredditChildren> subreddits);
    }

    interface LoadSubredditPostsCallback {
        void onPostsLoaded(List<Post> posts, String nextpage);
    }

    interface LoadPostCommentsCallback {
        void onCommentsLoaded(List<CommentChild> commentChildren);
        void onMoreCommentsLoaded(List<CommentChild> comments, int position);
    }

    interface LoadCommentsCallback {
        void onSearchLoaded(List<SubredditChildren> subredditChildrens, String after);
    }

    void getSubreddits(@NonNull LoadSubredditsCallback callback);

    void getSubredditPosts(@NonNull String subredditUrl, @NonNull LoadSubredditPostsCallback callback);

    void getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId, @NonNull LoadSubredditPostsCallback callback);

    void refreshData();

    void getCommentsForPost(@NonNull String permaLinkUrl, @NonNull LoadPostCommentsCallback callback);

    void getMoreCommentsForPostAt(@NonNull CommentChild parentComment, @NonNull String linkId, int position, @NonNull LoadPostCommentsCallback callback);

    void getSearchResults(@NonNull String query, @NonNull LoadCommentsCallback callback);

    void getMoreSearchResults(@NonNull String query,@NonNull String after, @NonNull LoadCommentsCallback callback);
}
