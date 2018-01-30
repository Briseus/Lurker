package torille.fi.lurkforreddit.data

import io.reactivex.Flowable
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import torille.fi.lurkforreddit.data.models.jsonResponses.*

/**
 * API endpoints for Reddit API
 */

interface RedditService {

    interface Auth {
        @FormUrlEncoded
        @POST("access_token")
        fun getAuthToken(@Field("grant_type") grant_type: String, @Field("device_id") UUID: String): Observable<RedditToken>

        @FormUrlEncoded
        @POST("access_token")
        fun getUserAuthToken(
            @Field("grant_type") grant_type: String, @Field("code") code: String, @Field(
                "redirect_uri"
            ) redirectUri: String
        ): Observable<RedditToken>

        @FormUrlEncoded
        @POST("access_token")
        fun refreshUserToken(@Field("grant_type") grant_type: String, @Field("refresh_token") token: String): Observable<RedditToken>

    }

    interface Reddit {
        @GET("subreddits/mine/subscriber")
        fun getMySubreddits(@Query("limit") count: Int): Flowable<SubredditListing>

        @GET("subreddits/default")
        fun getDefaultSubreddits(@Query("limit") count: Int): Flowable<SubredditListing>

        @GET("/api/multi/mine")
        fun getUserMultireddits(): Flowable<Array<MultiredditListing>>

        @GET("me/m/{multipath}")
        fun getMultiRedditData(
            @Path(
                value = "multipath",
                encoded = true
            ) multiPath: String
        ): Call<ResponseBody>

        @GET("me/m/{multipath}")
        fun getMultiRedditDataNextPage(
            @Path(
                value = "multipath",
                encoded = true
            ) multiPath: String, @Query("after") afterPage: String
        ): Call<ResponseBody>

        @GET("{subreddit}")
        fun getSubreddit(
            @Path(
                value = "subreddit",
                encoded = true
            ) subredditUrl: String
        ): Observable<PostListing>

        @GET("{subreddit}")
        fun getSubredditNextPage(
            @Path(
                value = "subreddit",
                encoded = true
            ) subredditUrl: String, @Query("after") afterPageId: String
        ): Observable<PostListing>

        @GET("{comments}")
        fun getComments(
            @Path(
                value = "comments",
                encoded = true
            ) commentUrl: String
        ): Observable<ResponseBody>

        @GET("api/morechildren")
        fun getMoreComments(
            @Query(value = "link_id") parentId: String, @Query(value = "children") childId: String, @Query(
                value = "api_type"
            ) json: String
        ): Observable<ResponseBody>

        @GET("subreddits/search")
        fun searchSubreddits(@Query(value = "q") searchQuery: String, @Query(value = "sort") sortBy: String): Flowable<SubredditListing>

        @GET("subreddits/search")
        fun searchSubredditsNextPage(
            @Query(value = "q") searchQuery: String, @Query(value = "sort") sortBy: String, @Query(
                value = "after"
            ) after: String
        ): Flowable<SubredditListing>

        @GET("{subreddit}/about")
        fun getSubredditInfo(
            @Path(
                value = "subreddit",
                encoded = true
            ) subredditName: String
        ): Flowable<SubredditChildren>

    }
}
