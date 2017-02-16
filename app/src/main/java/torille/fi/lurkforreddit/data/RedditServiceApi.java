package torille.fi.lurkforreddit.data;

import java.util.List;

/**
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */

public interface RedditServiceApi {

    interface SubredditsServiceCallback<T> {

        void onLoaded(T subreddits);

    }

    interface PostsServiceCallback<T, String> {

        void onLoaded(T subreddits, String nextpage);

    }

    interface CommentsServiceCallback<T> {

        void onLoaded(T comments);
    }

    void getSubreddits(SubredditsServiceCallback<List<SubredditChildren>> callback);

    void getSubredditPosts(String subredditId, PostsServiceCallback<List<Post>, String> callback);

    void getMorePosts(String subredditId, String nextpage, PostsServiceCallback<List<Post>, String> callback);

    void getPostComments(String permaLinkUrl, CommentsServiceCallback<List<CommentChild>> callback);
}
