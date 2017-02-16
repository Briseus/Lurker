package torille.fi.lurkforreddit.data;

import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

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
