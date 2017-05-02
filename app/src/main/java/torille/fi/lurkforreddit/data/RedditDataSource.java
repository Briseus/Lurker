package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

import io.reactivex.Observable;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.PostAndComments;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

/**
 * Main entry point for accessing data.
 */
public interface RedditDataSource {

    Observable<List<Subreddit>> getSubreddits();

    Observable<Pair<String, List<Post>>> getSubredditPosts(@NonNull String subredditUrl);

    Observable<Pair<String, List<Post>>> getMoreSubredditPosts(@NonNull String subredditUrl,
                                                               @NonNull String nextpageId);

    void refreshData();

    Observable<PostAndComments> getCommentsForPost(@NonNull String permaLinkUrl, boolean isSingleCommentThread);

    Observable<List<Comment>> getMoreCommentsForPostAt(@NonNull List<String> childCommentIds,
                                                       @NonNull String linkId,
                                                       int commentLevel);

    Observable<Pair<String, List<SearchResult>>> getSearchResults(@NonNull String query);

    Observable<Pair<String, List<SearchResult>>> getMoreSearchResults(@NonNull String query,
                                                                      @NonNull String after);
}
