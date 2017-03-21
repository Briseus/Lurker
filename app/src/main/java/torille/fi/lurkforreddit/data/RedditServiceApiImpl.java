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
import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.PostListing;
import torille.fi.lurkforreddit.data.models.Subreddit;
import torille.fi.lurkforreddit.data.models.SubredditChildren;
import torille.fi.lurkforreddit.data.models.SubredditListing;
import torille.fi.lurkforreddit.retrofit.RedditService;
import torille.fi.lurkforreddit.data.models.RedditToken;
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

    @Override
    public void getSubredditPosts(String subredditId, final PostsServiceCallback<List<Post>, String> callback) {
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
        Call<PostListing> call = RedditService.getInstance(token).getSubredditNextPage(subredditUrl, nextpageId);
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

            post.getPostDetails().setPreviewScore(formatScore(post.getPostDetails().getScore()));

            if (post.getPostDetails().isStickied()) {
                post.getPostDetails().setPreviewTitle(TextHelper.fromHtml(post.getPostDetails().getTitle() + "<font color='#64FFDA'> Stickied </font>"));
            } else {
                post.getPostDetails().setPreviewTitle(TextHelper.fromHtml(post.getPostDetails().getTitle()));
            }
            switch (post.getPostDetails().getThumbnail()) {
                case "default":
                case "self":
                case "":
                case "image":
                    post.getPostDetails().setPreviewImage("");
                    break;
                case "nsfw":
                    post.getPostDetails().setPreviewTitle(TextHelper.fromHtml(post.getPostDetails().getTitle() + "<font color='#FF1744'> NSFW</font>"));
                    post.getPostDetails().setPreviewImage("");
                    break;
                default:
                    post.getPostDetails().setPreviewImage(DisplayHelper.getBestPreviewPicture(post.getPostDetails()));
            }

        }

        return posts;
    }

    private static String formatScore(int score) {
        final String value = String.valueOf(score);
        if (score < 1000) {
            return value;
        } else if (score < 10000) {
            return value.charAt(0) + "." + value.charAt(1) + "k";
        } else if (score < 100000) {
            return value.charAt(0) + value.charAt(1) + "k";
        } else if (score < 10000000) {
            return value.charAt(0) + value.charAt(1) + value.charAt(2) + "k";
        } else {
            return value;
        }
    }

    @Override
    public void getPostComments(String permaLinkUrl, final CommentsServiceCallback<List<CommentChild>> callback) {
        String token = SharedPreferencesHelper.getToken();

        Call<ResponseBody> call = RedditService.getInstance(token).getComments(permaLinkUrl);

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

        Call<ResponseBody> call = RedditService.getInstance(token).getMoreComments(linkId, TextUtils.join(",", parentComment.getData().getChildren()), "json");
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

                        }
                        for (int i = 0; i < additionalComments.size(); i++) {
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

    @Override
    public void getSearchResults(String query, final SearchServiceCallback<List<SubredditChildren>> callback) {
        String token = SharedPreferencesHelper.getToken();
        Call<SubredditListing> call = RedditService.getInstance(token).searchSubreddits(query, "relevance");
        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().getData().getChildren();
                    String after = response.body().getData().getAfter();
                    callback.onLoaded(formatSubreddits(results), after);
                }
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Log.e("API", "Failed to load seach results " + t.toString());
            }
        });
    }

    @Override
    public void getMoreSearchResults(String query, String after, final SearchServiceCallback<List<SubredditChildren>> callback) {
        String token = SharedPreferencesHelper.getToken();
        Call<SubredditListing> call = RedditService.getInstance(token).searchSubredditsNextPage(query, "relevance", after);
        call.enqueue(new Callback<SubredditListing>() {
            @Override
            public void onResponse(Call<SubredditListing> call, Response<SubredditListing> response) {
                if (response.isSuccessful()) {
                    List<SubredditChildren> results = response.body().getData().getChildren();
                    String after = response.body().getData().getAfter();
                    callback.onLoaded(formatSubreddits(results), after);
                }
            }

            @Override
            public void onFailure(Call<SubredditListing> call, Throwable t) {
                Log.e("API", "Failed to load more seach results " + t.toString());
            }
        });

    }

    private static List<SubredditChildren> formatSubreddits(List<SubredditChildren> childrens) {
        for (SubredditChildren result : childrens) {
            Subreddit subreddit = result.getSubreddit();

            final String info = subreddit.getSubscribers() + " subscribers, Community since " + DateUtils.getRelativeTimeSpanString(subreddit.getCreatedUtc() * 1000);
            subreddit.setFormattedInfo(info);

            if (subreddit.isOver18()) {
                subreddit.setFormattedTitle(TextHelper.fromHtml(subreddit.getUrl() + "<font color='#FF1744'> NSFW</font>"));
            } else {
                subreddit.setFormattedTitle(subreddit.getUrl());
            }

            if (subreddit.isSubscribed()) {
                subreddit.setFormattedSubscription("Subscribed");
            } else {
                subreddit.setFormattedSubscription("Not subscribed");
            }

            if (subreddit.getDescriptionHtml() != null && !subreddit.getDescriptionHtml().isEmpty()) {
                subreddit.setFormattedDescription(TextHelper.trimTrailingWhitespace(TextHelper.fromHtml(subreddit.getDescriptionHtml())));
            } else {
                subreddit.setFormattedDescription("No description");
            }
        }
        return childrens;
    }

    private void authenticateApp(final String subredditId, final PostsServiceCallback<List<Post>, String> callback) {
        Call<RedditToken> call = NetworkHelper.createAuthCall();
        call.enqueue(new Callback<RedditToken>() {
            @Override
            public void onResponse(Call<RedditToken> call, Response<RedditToken> response) {
                if (response.isSuccessful()) {
                    Log.d("Jee", "Got new token " + response.body().getAccess_token());
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

