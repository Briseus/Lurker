package torille.fi.lurkforreddit.data.models.jsonResponses

import com.google.gson.annotations.SerializedName

/**
 * Root pojo for getting subreddits
 */
data class SubredditListing(
    val kind: String,
    val data: SubredditData
)

/**
 * Model containing subreddits with next/last page
 */
data class SubredditData(
    val children: List<SubredditChildren>,
    val after: String,
    val before: String
)

/**
 * Model containing the listing that includes a single subreddit
 */
data class SubredditChildren(
    val kind: String,
    @SerializedName("data")
    val subreddit: SubredditResponse
)

/**
 * Model containing subreddit info
 */
data class SubredditResponse(
    val id: String? = "",
    val title: String = "",
    val url: String = "",
    val name: String = "",
    @SerializedName("key_color")
    val keyColor: String = "",
    @SerializedName("display_name")
    val displayName: String = "",
    @SerializedName("public_description_html")
    val descriptionHtml: String? = "",
    val over18: Boolean,
    val subscribers: Int,
    @SerializedName("user_is_subscriber")
    val subscribed: Boolean,
    @SerializedName("created_utc")
    val createdUtc: Long,
    @SerializedName("icon_img")
    val icon: String = "",
    @SerializedName("banner_img")
    val banner: String = ""
)