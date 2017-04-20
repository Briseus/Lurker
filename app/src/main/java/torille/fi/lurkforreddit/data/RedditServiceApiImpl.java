package torille.fi.lurkforreddit.data;

import android.text.TextUtils;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.PostListing;
import torille.fi.lurkforreddit.data.models.RedditToken;
import torille.fi.lurkforreddit.data.models.SubredditChildren;
import torille.fi.lurkforreddit.data.models.SubredditListing;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.utils.CommentsStreamingParser;
import torille.fi.lurkforreddit.utils.NetworkHelper;
import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Actual implementation of the Api
 */

public class RedditServiceApiImpl implements RedditServiceApi {

    @Override
    public void getSubreddits(final ServiceCallback<List<SubredditChildren>> callback,
                              final RedditRepository.ErrorCallback errorCallback) {
        String token = SharedPreferencesHelper.getToken();

        Call<SubredditListing> call;
        if (SharedPreferencesHelper.isLoggedIn()) {
            Timber.d("Was logged in, getting personal subreddits");
            call = RedditService.getInstance(token).getMySubreddits(100);
        } else {
            Timber.d("Was not logged in, getting default subreddits");
            call = RedditService.getInstance(token).getDefaultSubreddits(100);
        }

        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                List<SubredditChildren> subreddits = response.body().getData().getChildren();
                if (response.isSuccessful() && subreddits != null) {
                    // sort subreddits by name before callback
                    Collections.sort(subreddits, new Comparator<SubredditChildren>() {
                        @Override
                        public int compare(SubredditChildren o1, SubredditChildren o2) {
                            return o1.getSubreddit()
                                    .getDisplay_name()
                                    .compareToIgnoreCase(o2.getSubreddit().getDisplay_name());
                        }
                    });
                    callback.onLoaded(subreddits);

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
                                  final ServiceCallbackWithNextpage<List<Post>> callback,
                                  final RedditRepository.ErrorCallback errorCallback) {
        String token = SharedPreferencesHelper.getToken();
        if (token == null) {
            authenticateApp(subredditId, callback, errorCallback);
        } else {
            Call<PostListing> call = RedditService.getInstance(token).getSubreddit(subredditId);
            call.enqueue(new Callback<PostListing>() {
                @Override
                public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                    List<Post> posts = response.body().getData().getPosts();
                    if (response.isSuccessful() && posts != null) {

                        List<Post> formattedPosts = getFormattedPosts(posts);
                        String nextpage = response.body().getData().getNextPage();

                        Timber.d("Got " + formattedPosts.size() + " posts");

                        callback.onLoaded(formattedPosts, nextpage);
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
    }

    private static List<Post> getFormattedPosts(List<Post> posts) {
        return TextHelper.formatPosts(posts);
    }

    @Override
    public void getMorePosts(String subredditUrl,
                             String nextpageId,
                             final ServiceCallbackWithNextpage<List<Post>> callback,
                             final RedditRepository.ErrorCallback errorCallback) {
        String token = SharedPreferencesHelper.getToken();
        Call<PostListing> call = RedditService
                .getInstance(token)
                .getSubredditNextPage(subredditUrl, nextpageId);
        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                if (response.isSuccessful()) {
                    List<Post> posts = response.body().getData().getPosts();
                    List<Post> formattedPosts = getFormattedPosts(posts);
                    String nextpage = response.body().getData().getNextPage();

                    callback.onLoaded(formattedPosts, nextpage);
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
        String token = SharedPreferencesHelper.getToken();

        Call<ResponseBody> call = RedditService.getInstance(token).getComments(permaLinkUrl);

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
                                .getCommentData()
                                .getCommentChildren();

                        List<CommentChild> commentChildFlatList = TextHelper
                                .flatten(commentChildList, 0);

                        callback.onLoaded(commentChildFlatList);
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
    public void getMorePostComments(final CommentChild parentComment,
                                    String linkId,
                                    final int position,
                                    final CommentsServiceCallback<List<CommentChild>> callback,
                                    final RedditRepository.ErrorCallback errorCallback) {
        String token = SharedPreferencesHelper.getToken();

        Call<ResponseBody> call = RedditService
                .getInstance(token)
                .getMoreComments(
                        linkId,
                        TextUtils.join(",", parentComment.getData().getChildren()),
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

                        List<CommentChild> additionalFlattenedComments = TextHelper
                                .flattenAdditionalComments(additionalComments, parentComment.getType());

                        callback.onMoreLoaded(additionalFlattenedComments, position);
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
        String token = SharedPreferencesHelper.getToken();
        Call<SubredditListing> call = RedditService
                .getInstance(token)
                .searchSubreddits(query, "relevance");
        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().getData().getChildren();
                    String after = response.body().getData().getAfter();
                    callback.onLoaded(TextHelper.formatSubreddits(results), after);
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
        String token = SharedPreferencesHelper.getToken();
        Call<SubredditListing> call = RedditService
                .getInstance(token)
                .searchSubredditsNextPage(query, "relevance", after);
        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().getData().getChildren();
                    String after = response.body().getData().getAfter();
                    callback.onLoaded(TextHelper.formatSubreddits(results), after);
                }
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Timber.e("Failed to load more seach results " + t.toString());
                errorCallback.onError(t.toString());
            }
        });

    }

    private void authenticateApp(final String subredditId,
                                 final ServiceCallbackWithNextpage<List<Post>> callback,
                                 final RedditRepository.ErrorCallback errorCallback) {
        Call<RedditToken> call = NetworkHelper.createAuthCall();
        call.enqueue(new Callback<RedditToken>() {
            @Override
            public void onResponse(Call<RedditToken> call, Response<RedditToken> response) {
                String accessToken = response.body().getAccess_token();
                if (response.isSuccessful() && accessToken != null) {
                    SharedPreferencesHelper.setToken(accessToken);
                    getSubredditPosts(subredditId, callback, errorCallback);
                }
            }

            @Override
            public void onFailure(Call<RedditToken> call, Throwable t) {
                Timber.e(t.toString());
                errorCallback.onError(t.toString());
            }
        });
    }

}

