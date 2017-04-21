package torille.fi.lurkforreddit.data;

import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostResponse;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditListing;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.utils.CommentsStreamingParser;
import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;

/**
 * Actual implementation of the Api
 */

public class RedditServiceApiImpl implements RedditServiceApi {

    @Override
    public void getSubreddits(final ServiceCallback<List<SubredditChildren>> callback,
                              final RedditRepository.ErrorCallback errorCallback) {

        Call<SubredditListing> call;

        if (SharedPreferencesHelper.isLoggedIn()) {
            Timber.d("Was logged in, getting personal subreddits");
            call = RedditService.getInstance().getMySubreddits(100);
        } else {
            Timber.d("Was not logged in, getting default subreddits");
            call = RedditService.getInstance().getDefaultSubreddits(100);
        }

        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                List<SubredditChildren> responseSubreddits = response.body().data().children();
                if (response.isSuccessful() && responseSubreddits != null) {
                    callback.onLoaded(responseSubreddits);

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
    public void getSubredditPosts(String subredditId,
                                  final ServiceCallbackWithNextpage<List<PostResponse>> callback,
                                  final RedditRepository.ErrorCallback errorCallback) {

        Call<PostListing> call = RedditService.getInstance().getSubreddit(subredditId);
        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                List<PostResponse> posts = response.body().data().Posts();
                if (response.isSuccessful() && posts != null) {
                    String nextpage = response.body().data().nextPage();
                    Timber.d("Got " + posts.size() + " posts");
                    callback.onLoaded(posts, nextpage);
                }
            }

            // TODO show error message callback
            @Override
            public void onFailure(Call<PostListing> call, Throwable t) {
                Timber.e("Failed to get subreddit posts " + t.toString());
                errorCallback.onError(t.toString());
            }
        });


    }

    @Override
    public void getMorePosts(String subredditUrl,
                             String nextpageId,
                             final ServiceCallbackWithNextpage<List<PostResponse>> callback,
                             final RedditRepository.ErrorCallback errorCallback) {

        Call<PostListing> call = RedditService
                .getInstance()
                .getSubredditNextPage(subredditUrl, nextpageId);

        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                if (response.isSuccessful()) {
                    List<PostResponse> posts = response.body().data().Posts();
                    String nextpage = response.body().data().nextPage();

                    callback.onLoaded(posts, nextpage);
                }
            }

            @Override
            public void onFailure(Call<PostListing> call, Throwable t) {
                Timber.e("Failed to load more posts " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }


    @Override
    public void getPostComments(String permaLinkUrl,
                                final CommentsServiceCallback<List<CommentChild>> callback,
                                final RedditRepository.ErrorCallback errorCallback) {

        Call<ResponseBody> call = RedditService.getInstance().getComments(permaLinkUrl);

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

                        callback.onLoaded(commentChildList);
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
    public void getMorePostComments(final Comment parentComment,
                                    String linkId,
                                    final int position,
                                    final CommentsServiceCallback<List<CommentChild>> callback,
                                    final RedditRepository.ErrorCallback errorCallback) {

        Call<ResponseBody> call = RedditService
                .getInstance()
                .getMoreComments(
                        linkId,
                        TextUtils.join(",", parentComment.childCommentIds()),
                        "json");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (InputStream stream = response.body().byteStream();
                         InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                         JsonReader reader = new JsonReader(in)) {

                        List<CommentChild> additionalComments = CommentsStreamingParser
                                .readMoreComments(reader);

                        callback.onMoreLoaded(additionalComments, position);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                errorCallback.onError(t.toString());
            }
        });

    }

    @Override
    public void getSearchResults(String query,
                                 final ServiceCallbackWithNextpage<List<SubredditChildren>> callback,
                                 final RedditRepository.ErrorCallback errorCallback) {

        Call<SubredditListing> call = RedditService
                .getInstance()
                .searchSubreddits(query, "relevance");

        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().data().children();
                    String after = response.body().data().after();
                    callback.onLoaded(results, after);
                }
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Timber.e("Failed to load seach results " + t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

    @Override
    public void getMoreSearchResults(String query,
                                     String after,
                                     final ServiceCallbackWithNextpage<List<SubredditChildren>> callback,
                                     final RedditRepository.ErrorCallback errorCallback) {

        Call<SubredditListing> call = RedditService
                .getInstance()
                .searchSubredditsNextPage(query, "relevance", after);

        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().data().children();
                    String after = response.body().data().after();
                    callback.onLoaded(results, after);
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

