package torille.fi.lurkforreddit.data;

import java.util.List;

import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.SubredditChildren;

/**
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */

interface RedditServiceApi {

    interface SubredditsServiceCallback<T> {
        void onLoaded(T subreddits);
    }

    interface PostsServiceCallback<T, String> {
        void onLoaded(T subreddits, String nextpage);
    }

    interface CommentsServiceCallback<T> {
        void onLoaded(T comments);
        void onMoreLoaded(T comments, int position);
    }

    interface SearchServiceCallback<T> {
        void onLoaded(T result, String after);
    }

    void getSubreddits(SubredditsServiceCallback<List<SubredditChildren>> callback);

    void getSubredditPosts(String subredditId, PostsServiceCallback<List<Post>, String> callback);

    void getMorePosts(String subredditId, String nextpage, PostsServiceCallback<List<Post>, String> callback);

    void getPostComments(String permaLinkUrl, CommentsServiceCallback<List<CommentChild>> callback);

    void getMorePostComments(CommentChild parentComment, String linkId, int position, CommentsServiceCallback<List<CommentChild>> callback);

    void getSearchResults(String query, SearchServiceCallback<List<SubredditChildren>> callback);

    void getMoreSearchResults(String query, String after, SearchServiceCallback<List<SubredditChildren>> callback);
}
