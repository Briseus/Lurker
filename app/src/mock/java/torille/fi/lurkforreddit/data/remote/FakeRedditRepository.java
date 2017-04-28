package torille.fi.lurkforreddit.data.remote;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

/**
 * Created by eva on 25.4.2017.
 */

public class FakeRedditRepository implements RedditDataSource {

    @Inject
    public FakeRedditRepository() {
    }

    @Override
    public Observable<List<Subreddit>> getSubreddits() {
        return null;
    }

    @Override
    public Observable<Pair<String, List<Post>>> getSubredditPosts(@NonNull String subredditUrl) {
        return null;
    }

    @Override
    public Observable<Pair<String, List<Post>>> getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId) {
        return null;
    }

    @Override
    public void refreshData() {

    }

    @Override
    public Observable<List<Comment>> getCommentsForPost(@NonNull String permaLinkUrl) {
        return null;
    }

    @Override
    public Observable<List<Comment>> getMoreCommentsForPostAt(@NonNull List<String> childCommentIds, @NonNull String linkId, int commentLevel) {
        return null;
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getSearchResults(@NonNull String query) {
        return null;
    }

    @Override
    public Observable<Pair<String, List<SearchResult>>> getMoreSearchResults(@NonNull String query, @NonNull String after) {
        return null;
    }
}
