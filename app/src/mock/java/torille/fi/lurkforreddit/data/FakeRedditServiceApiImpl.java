package torille.fi.lurkforreddit.data;

import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.SubredditChildren;

/**
 * Created by eva on 2/12/17.
 */

public class FakeRedditServiceApiImpl implements RedditServiceApi {

    private static final List<Post> POSTS_SERVICE_DATA = new ArrayList<>();

    private static final List<SubredditChildren> SUBREDDIT_SERVICE_DATA = new ArrayList<>();

    private static String AFTER = "";

    @Override
    public void getSubreddits(SubredditsServiceCallback<List<SubredditChildren>> callback) {
        callback.onLoaded(SUBREDDIT_SERVICE_DATA);
    }

    @Override
    public void getSubredditPosts(String subredditId, PostsServiceCallback<List<Post>, String> callback) {
        callback.onLoaded(POSTS_SERVICE_DATA, AFTER);
    }

    @Override
    public void getMorePosts(String subredditId, String nextpage, PostsServiceCallback<List<Post>, String> callback) {
        callback.onLoaded(POSTS_SERVICE_DATA, AFTER);
    }

    @Override
    public void getPostComments(String permaLinkUrl, CommentsServiceCallback<List<CommentChild>> callback) {

    }

    @Override
    public void getMorePostComments(CommentChild parentComment, String linkId, int position, CommentsServiceCallback<List<CommentChild>> callback) {

    }

    @Override
    public void getSearchResults(String query, SearchServiceCallback<List<SubredditChildren>> callback) {

    }

    @Override
    public void getMoreSearchResults(String query, String after, SearchServiceCallback<List<SubredditChildren>> callback) {

    }

    @VisibleForTesting
    public static void addPosts(Post... posts) {
        for (Post post: posts) {
            POSTS_SERVICE_DATA.add(post);
        }
    }

    @VisibleForTesting
    public static void addSubreddits(SubredditChildren... childrens) {
        for (SubredditChildren child: childrens) {
            SUBREDDIT_SERVICE_DATA.add(child);
        }
    }

    @VisibleForTesting
    public static void addAfterString(String after) {
        AFTER = after;
    }
}
