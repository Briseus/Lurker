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

    interface ServiceCallback<T> {
        void onLoaded(T subreddits);
    }

    interface ServiceCallbackWithNextpage<T> {
        void onLoaded(T subreddits, String nextPageId);
    }

    interface CommentsServiceCallback<T> {
        void onLoaded(T comments);
        void onMoreLoaded(T comments, int position);
    }

    void getSubreddits(ServiceCallback<List<SubredditChildren>> callback, RedditRepository.ErrorCallback errorCallback);

    void getSubredditPosts(String subredditId, ServiceCallbackWithNextpage<List<Post>> callback, RedditRepository.ErrorCallback errorCallback);

    void getMorePosts(String subredditId, String nextPageId, ServiceCallbackWithNextpage<List<Post>> callback, RedditRepository.ErrorCallback errorCallback);

    void getPostComments(String permaLinkUrl, CommentsServiceCallback<List<CommentChild>> callback, RedditRepository.ErrorCallback errorCallback);

    void getMorePostComments(CommentChild parentComment, String linkId, int position, CommentsServiceCallback<List<CommentChild>> callback, RedditRepository.ErrorCallback errorCallback);

    void getSearchResults(String query, ServiceCallbackWithNextpage<List<SubredditChildren>> callback, RedditRepository.ErrorCallback errorCallback);

    void getMoreSearchResults(String query, String after, ServiceCallbackWithNextpage<List<SubredditChildren>> callback, RedditRepository.ErrorCallback errorCallback);
}
