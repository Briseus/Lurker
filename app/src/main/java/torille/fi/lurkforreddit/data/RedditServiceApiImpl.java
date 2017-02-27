package torille.fi.lurkforreddit.data;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import torille.fi.lurkforreddit.retrofit.RedditClient;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.retrofit.RedditToken;
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
        if (token == null) {
            authenticateApp(callback);
        } else {
            final RedditClient redditClient = RedditService.createService(RedditClient.class, token);
            Call<SubredditListing> call;
            if (SharedPreferencesHelper.isLoggedIn()) {
                Log.d("LoginStatus", "Was logged in, getting personal subreddits");
                call = redditClient.getMySubreddits();
            } else {
                Log.d("LoginStatus", "Was not logged in, getting default subreddits");
                call = redditClient.getDefaultSubreddits(100);
            }

            call.enqueue(new Callback<SubredditListing>() {
                @Override
                public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {

                    if (response.isSuccessful() && response.body().getData().getChildren() != null) {
                        Log.d("Subreddits", response.body().toString());
                        List<SubredditChildren> subreddits = response.body().getData().getChildren();
                        Log.d("Subreddits", "Got " + subreddits.size() + " Subreddits");
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
    }

    @Override
    public void getSubredditPosts(String subredditId, final PostsServiceCallback<List<Post>, String> callback) {
        String token = SharedPreferencesHelper.getToken();
        if (token == null) {
            Log.d("PostFragment", "Token was null. Going to refresh");
        } else {
            final RedditClient client = RedditService.createService(RedditClient.class, token);
            Call<PostListing> call = client.getSubreddit(subredditId);
            call.enqueue(new Callback<PostListing>() {
                @Override
                public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                    if (response.isSuccessful() && response.body().getData().getPosts() != null) {

                        List<Post> posts = response.body().getData().getPosts();
                        String nextpage = response.body().getData().getNextPage();

                        Log.d("Test", "Got " + posts.size() + " posts");

                        callback.onLoaded(formatPosts(posts), nextpage);
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
    public void getMorePosts(String subredditUrl, String nextpageId, final PostsServiceCallback<List<Post>, String> callback) {
        String token = SharedPreferencesHelper.getToken();
        final RedditClient client = RedditService.createService(RedditClient.class, token);
        Call<PostListing> call = client.getSubredditNextPage(subredditUrl, nextpageId);
        call.enqueue(new Callback<PostListing>() {
            @Override
            public void onResponse(Call<PostListing> call, Response<PostListing> response) {
                if (response.isSuccessful() && response.body().getData().getPosts() != null) {
                    List<Post> posts = response.body().getData().getPosts();
                    String nextpage = response.body().getData().getNextPage();

                    callback.onLoaded(formatPosts(posts), nextpage);
                }
            }

            @Override
            public void onFailure(Call<PostListing> call, Throwable t) {
                Log.e("Subreddits", "Failed to load more posts " + t.toString());
            }
        });
    }

    private static List<Post> formatPosts(List<Post> posts) {
        for (Post post : posts) {
            post.getPostDetails().setPreviewImage(DisplayHelper.getBestPreviewPicture(post.getPostDetails()));
            final String text = DateUtils.getRelativeTimeSpanString(post.getPostDetails().getCreatedUtc() * 1000)
                    + " ● "
                    + "/r/" + post.getPostDetails().getSubreddit()
                    + " ● "
                    + "/u/" + post.getPostDetails().getAuthor();

            post.getPostDetails().setPreviewText(text);
            final int score = post.getPostDetails().getScore();
            final String value = String.valueOf(score);
            if (score < 1000) {
                post.getPostDetails().setPreviewScore(value);
            } else if (score < 10000) {
                post.getPostDetails().setPreviewScore(value.charAt(0) + "." + value.charAt(1) + "k");
            } else if (score < 100000) {
                post.getPostDetails().setPreviewScore(value.charAt(0) + value.charAt(1) + "k");
            } else if (score < 10000000) {
                post.getPostDetails().setPreviewScore(value.charAt(0) + value.charAt(1) + value.charAt(2) + "k");
            }
        }

        return posts;
    }

    @Override
    public void getPostComments(String permaLinkUrl, final CommentsServiceCallback<List<CommentChild>> callback) {
        String token = SharedPreferencesHelper.getToken();
        final RedditClient client = RedditService.createService(RedditClient.class, token);
        Call<ResponseBody> call = client.getComments(permaLinkUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        InputStreamReader in = new InputStreamReader(response.body().source().inputStream(), "UTF-8");
                        JsonReader reader = new JsonReader(in);

                        List<CommentChild> commentChildList = CommentsStreamingParser.readCommentListingArray(reader).get(1).getCommentData().getCommentChildren();
                        reader.close();
                        in.close();
                        response.body().close();
                        callback.onLoaded(TextHelper.flatten(commentChildList));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("commentstest", "SUCCESS");
                    //TODO when no comments callback
                    //List<CommentChild> commentChildList = response.body().get(1).getCommentData().getCommentChildren();

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
        final RedditClient client = RedditService.createService(RedditClient.class, token);
        Call<ResponseBody> call = client.getMoreComments(linkId, TextUtils.join(",", parentComment.getData().getChildren()), "json");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        InputStreamReader in = new InputStreamReader(response.body().source().inputStream(), "UTF-8");
                        JsonReader reader = new JsonReader(in);
                        List<CommentChild> additionalComments = CommentsStreamingParser.readMoreComments(reader);
                        reader.close();
                        in.close();
                        response.body().close();
                        for (int i = 0; i < additionalComments.size(); i++) {
                            /*
                            If type is "continue this thread ->"
                             */
                            if (additionalComments.get(i).getData().getId().equals("_")) {
                                additionalComments.get(i).setKind("more");
                                additionalComments.get(i).setType(parentComment.getType());
                            } else {
                                additionalComments.get(i).setKind("t3");
                                additionalComments.get(i).setType(parentComment.getType());
                            }
                            TextHelper.formatCommentData(additionalComments.get(i));
                            for (int j = 0; j < additionalComments.size(); j++) {
                                if (additionalComments.get(i).getData().getName().equals(additionalComments.get(j).getData().getParentId())) {
                                    additionalComments.get(j).setType(additionalComments.get(i).getType() + 1);
                                }
                            }
                        }
                        callback.onMoreLoaded(additionalComments, position);
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

    private void authenticateApp(final SubredditsServiceCallback<List<SubredditChildren>> callback) {
        Call<RedditToken> call = NetworkHelper.createAuthCall();
        call.enqueue(new Callback<RedditToken>() {
            @Override
            public void onResponse(Call<RedditToken> call, Response<RedditToken> response) {
                if (response.isSuccessful()) {
                    Log.d("Jee", "Got new token " + response.body().getAccess_token());
                    SharedPreferencesHelper.setToken(response.body().getAccess_token());
                    getSubreddits(callback);
                }
            }

            @Override
            public void onFailure(Call<RedditToken> call, Throwable t) {
                Log.e("Error", t.toString());
            }
        });
    }

}

