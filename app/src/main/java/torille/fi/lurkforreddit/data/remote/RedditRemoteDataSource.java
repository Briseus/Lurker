package torille.fi.lurkforreddit.data.remote;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostResponse;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditListing;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.RedditService;
import torille.fi.lurkforreddit.di.scope.RedditScope;
import torille.fi.lurkforreddit.utils.CommentsStreamingParser;
import torille.fi.lurkforreddit.utils.Store;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Created by eva on 23.4.2017.
 */
@RedditScope
public class RedditRemoteDataSource implements RedditDataSource {

    private final RedditService.Reddit mRedditApi;

    private final Store mSettingsStore;

    @Inject
    public RedditRemoteDataSource(@NonNull RedditService.Reddit api, @NonNull Store store) {
        mRedditApi = api;
        mSettingsStore = store;
    }

    @Override
    public void getSubreddits(@NonNull final LoadSubredditsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Timber.d("Fetching subs!");
        Call<SubredditListing> call;

        if (mSettingsStore.isLoggedIn()) {
            Timber.d("Was logged in, getting personal subreddits");
            call = mRedditApi.getMySubreddits(100);
        } else {
            Timber.d("Was not logged in, getting default subreddits");
            call = mRedditApi.getDefaultSubreddits(100);
        }

        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                List<SubredditChildren> responseSubreddits = response.body().data().children();
                if (response.isSuccessful() && responseSubreddits != null) {
                    callback.onSubredditsLoaded(TextHelper.formatSubreddits(responseSubreddits));

                }
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Timber.e("Failed to get subreddits " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void getSubredditPosts(@NonNull String subredditUrl, @NonNull final LoadSubredditPostsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Call<PostListing> call = mRedditApi.getSubreddit(subredditUrl);
        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                List<PostResponse> posts = response.body().data().Posts();
                if (response.isSuccessful() && posts != null) {
                    String nextpage = response.body().data().nextPage();
                    Timber.d("Got " + posts.size() + " posts");

                    callback.onPostsLoaded(TextHelper.formatPosts(posts), nextpage);
                }
            }

            @Override
            public void onFailure(Call<PostListing> call, Throwable t) {
                Timber.e("Failed to get subreddit posts " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void getMoreSubredditPosts(@NonNull String subredditUrl, @NonNull String nextpageId, @NonNull final LoadSubredditPostsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Timber.d("Getting more posts!");
        Call<PostListing> call = mRedditApi.getSubredditNextPage(subredditUrl, nextpageId);

        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                List<PostResponse> posts = response.body().data().Posts();
                if (response.isSuccessful() && posts != null) {
                    String nextpage = response.body().data().nextPage();
                    Timber.d("Got " + posts.size() + " posts");

                    callback.onPostsLoaded(TextHelper.formatPosts(posts), nextpage);
                }
            }

            @Override
            public void onFailure(Call<PostListing> call, Throwable t) {
                Timber.e("Failed to get subreddit posts " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void refreshData() {
        //empty
    }

    @Override
    public void getCommentsForPost(@NonNull String permaLinkUrl, @NonNull final LoadPostCommentsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Call<ResponseBody> call = mRedditApi.getComments(permaLinkUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (InputStream stream = response.body().byteStream();
                         InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                         JsonReader reader = new JsonReader(in)) {

                        List<CommentChild> commentChildList = CommentsStreamingParser
                                .readCommentListingArray(reader)
                                .get(1)
                                .commentData()
                                .commentChildren();
                        List<Comment> commentChildFlatList = TextHelper
                                .flatten(commentChildList, 0);
                        callback.onCommentsLoaded(commentChildFlatList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Timber.e("Something went wrong fetching comments " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void getMoreCommentsForPostAt(@NonNull final Comment parentComment, @NonNull String linkId, final int position, @NonNull final LoadPostCommentsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Call<ResponseBody> call = mRedditApi.getMoreComments(linkId,
                TextUtils.join(",", parentComment.childCommentIds()),
                "json");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try (InputStream stream = response.body().byteStream();
                     InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                     JsonReader reader = new JsonReader(in)) {

                    List<CommentChild> additionalComments = CommentsStreamingParser
                            .readMoreComments(reader);
                    List<Comment> additionalFlattenedComments = TextHelper
                            .flattenAdditionalComments(additionalComments, parentComment.commentLevel());
                    callback.onMoreCommentsLoaded(additionalFlattenedComments, position);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void getSearchResults(@NonNull String query, @NonNull final LoadCommentsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Call<SubredditListing> call = mRedditApi.searchSubreddits(query, "relevance");
        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                List<SubredditChildren> results = response.body().data().children();
                String after = response.body().data().after();
                callback.onSearchLoaded(TextHelper.formatSearchResults(results), after);
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Timber.e("Failed to load seach results " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void getMoreSearchResults(@NonNull String query, @NonNull String after, @NonNull final LoadCommentsCallback callback, @NonNull final ErrorCallback errorCallback) {
        Call<SubredditListing> call = mRedditApi.searchSubredditsNextPage(query,
                "relevance",
                after);
        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().data().children();
                    String after = response.body().data().after();
                    callback.onSearchLoaded(TextHelper.formatSearchResults(results), after);
                }
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Timber.e("Failed to load more seach results " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }
}
