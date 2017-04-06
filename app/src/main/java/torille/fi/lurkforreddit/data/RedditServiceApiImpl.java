package torille.fi.lurkforreddit.data;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

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
import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.PostListing;
import torille.fi.lurkforreddit.data.models.RedditToken;
import torille.fi.lurkforreddit.data.models.Subreddit;
import torille.fi.lurkforreddit.data.models.SubredditChildren;
import torille.fi.lurkforreddit.data.models.SubredditListing;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.utils.CommentsStreamingParser;
import torille.fi.lurkforreddit.utils.DisplayHelper;
import torille.fi.lurkforreddit.utils.NetworkHelper;
import torille.fi.lurkforreddit.utils.SharedPreferencesHelper;
import torille.fi.lurkforreddit.utils.TextHelper;

/**
 * Actual implementation of the Api
 */

public class RedditServiceApiImpl implements RedditServiceApi {

    @Override
    public void getSubreddits(final SubredditsServiceCallback<List<SubredditChildren>> callback) {
        String token = SharedPreferencesHelper.getToken();

        Call<SubredditListing> call;
        if (SharedPreferencesHelper.isLoggedIn()) {
            Log.d("LoginStatus", "Was logged in, getting personal subreddits");
            call = RedditService.getInstance(token).getMySubreddits(100);
        } else {
            Log.d("LoginStatus", "Was not logged in, getting default subreddits");
            call = RedditService.getInstance(token).getDefaultSubreddits(100);
        }

        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {

                if (response.isSuccessful() && response.body().getData().getChildren() != null) {
                    List<SubredditChildren> subreddits = response.body().getData().getChildren();
                    // sort subreddits by name before callback
                    Collections.sort(subreddits, new Comparator<SubredditChildren>() {
                        @Override
                        public int compare(SubredditChildren o1, SubredditChildren o2) {
                            return o1.getSubreddit().getDisplay_name().compareToIgnoreCase(o2.getSubreddit().getDisplay_name());
                        }
                    });
                    callback.onLoaded(subreddits);

                }
            }


            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Log.e("Subreddits", "Failed to get subreddits " + t.toString());
            }
        });

    }

    @Override
    public void getSubredditPosts(String subredditId, final PostsServiceCallback<List<Post>> callback) {
        String token = SharedPreferencesHelper.getToken();
        if (token == null) {
            authenticateApp(subredditId, callback);
        } else {
            Call<PostListing> call = RedditService.getInstance(token).getSubreddit(subredditId);
            call.enqueue(new Callback<PostListing>() {
                @Override
                public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                    if (response.isSuccessful() && response.body().getData().getPosts() != null) {

                        List<Post> posts = response.body().getData().getPosts();
                        String nextpage = response.body().getData().getNextPage();

                        Log.d("Test", "Got " + posts.size() + " posts");

                        callback.onLoaded(TextHelper.formatPosts(posts), nextpage);
                    }
                }

                // TODO show error message callback
                @Override
                public void onFailure(Call<PostListing> call, Throwable t) {
                    Log.e("Subreddits", "Failed to get subreddit posts " + t.toString());
                }
            });

        }
    }

    @Override
    public void getMorePosts(String subredditUrl, String nextpageId, final PostsServiceCallback<List<Post>> callback) {
        String token = SharedPreferencesHelper.getToken();
        Call<PostListing> call = RedditService.getInstance(token).getSubredditNextPage(subredditUrl, nextpageId);
        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                if (response.isSuccessful() && response.body().getData().getPosts() != null) {
                    List<Post> posts = response.body().getData().getPosts();
                    String nextpage = response.body().getData().getNextPage();

                    callback.onLoaded(TextHelper.formatPosts(posts), nextpage);
                }
            }

            @Override
            public void onFailure(Call<PostListing> call, Throwable t) {
                Log.e("Subreddits", "Failed to load more posts " + t.toString());
            }
        });
    }


    @Override
    public void getPostComments(String permaLinkUrl, final CommentsServiceCallback<List<CommentChild>> callback) {
        String token = SharedPreferencesHelper.getToken();

        Call<ResponseBody> call = RedditService.getInstance(token).getComments(permaLinkUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (InputStream stream = response.body().source().inputStream();
                         InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                         JsonReader reader = new JsonReader(in)) {

                        List<CommentChild> commentChildList = CommentsStreamingParser.readCommentListingArray(reader).get(1).getCommentData().getCommentChildren();
                        List<CommentChild> commentChildFlatList = TextHelper.flatten(commentChildList, 0);

                        callback.onLoaded(commentChildFlatList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Comments", "Something went wrong fetching comments " + t.toString());
            }
        });
    }

    @Override
    public void getMorePostComments(final CommentChild parentComment, String linkId, final int position, final CommentsServiceCallback<List<CommentChild>> callback) {
        String token = SharedPreferencesHelper.getToken();

        Call<ResponseBody> call = RedditService.getInstance(token).getMoreComments(linkId, TextUtils.join(",", parentComment.getData().getChildren()), "json");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (InputStream stream = response.body().source().inputStream();
                         InputStreamReader in = new InputStreamReader(stream, "UTF-8");
                         JsonReader reader = new JsonReader(in)) {

                        List<CommentChild> additionalComments = CommentsStreamingParser.readMoreComments(reader);
                        List<CommentChild> additionalFlattenedComments = TextHelper.flattenAdditionalComments(additionalComments, parentComment.getType());

                        callback.onMoreLoaded(additionalFlattenedComments, position);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    @Override
    public void getSearchResults(String query,
                                 final SearchServiceCallback<List<SubredditChildren>> callback) {
        String token = SharedPreferencesHelper.getToken();
        Call<SubredditListing> call = RedditService.getInstance(token).searchSubreddits(query, "relevance");
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
                Log.e("API", "Failed to load seach results " + t.toString());
            }
        });
    }

    @Override
    public void getMoreSearchResults(String query, String after,
                                     final SearchServiceCallback<List<SubredditChildren>> callback) {
        String token = SharedPreferencesHelper.getToken();
        Call<SubredditListing> call = RedditService.getInstance(token).searchSubredditsNextPage(query, "relevance", after);
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
                Log.e("API", "Failed to load more seach results " + t.toString());
            }
        });

    }

    private void authenticateApp(final String subredditId, final PostsServiceCallback<List<Post>> callback) {
        Call<RedditToken> call = NetworkHelper.createAuthCall();
        call.enqueue(new Callback<RedditToken>() {
            @Override
            public void onResponse(Call<RedditToken> call, Response<RedditToken> response) {
                if (response.isSuccessful() && response.body().getAccess_token() != null) {
                    SharedPreferencesHelper.setToken(response.body().getAccess_token());
                    getSubredditPosts(subredditId, callback);
                }
            }

            @Override
            public void onFailure(Call<RedditToken> call, Throwable t) {
                Log.e("Error", t.toString());
            }
        });
    }

}

