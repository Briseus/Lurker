package torille.fi.lurkforreddit.data.remote;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Comment;

/**
 * Created by eva on 25.4.2017.
 */

public class FakeRedditRepository implements RedditRepository {

    @Inject
    public FakeRedditRepository() {
    }

    @Override
    public void getSubreddits(@NonNull LoadSubredditsCallback callback, @NonNull ErrorCallback errorCallback) {

    }

    @Override
    public void getSubredditPosts(@NonNull String subredditUrl, @NonNull LoadSubredditPostsCallback callback, @NonNull ErrorCallback errorCallback) {

    }

    @Override
    public void getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId, @NonNull LoadSubredditPostsCallback callback, @NonNull ErrorCallback errorCallback) {

    }

    @Override
    public void refreshData() {

    }

    @Override
    public void getCommentsForPost(@NonNull String permaLinkUrl, @NonNull LoadPostCommentsCallback callback, @NonNull ErrorCallback errorCallback) {

    }

    @Override
    public void getMoreCommentsForPostAt(@NonNull Comment parentComment, @NonNull String linkId, int position, @NonNull LoadPostCommentsCallback callback, @NonNull ErrorCallback errorCallback) {

    }

    @Override
    public void getSearchResults(@NonNull String query, @NonNull LoadCommentsCallback callback, @NonNull ErrorCallback errorCallback) {

    }

    @Override
    public void getMoreSearchResults(@NonNull String query, @NonNull String after, @NonNull LoadCommentsCallback callback, @NonNull ErrorCallback errorCallback) {

    }
}
