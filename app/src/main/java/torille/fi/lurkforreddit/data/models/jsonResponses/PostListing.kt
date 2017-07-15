package torille.fi.lurkforreddit.data.models.jsonResponses

import com.google.gson.annotations.SerializedName

/**
 * Root object for Reddit posts
 */
data class PostListing(
        val kind: String,
        val data: PostData
)

/**
 * Model containing all response listings and next/last page
 */
data class PostData(
        @SerializedName("children")
        val Posts: List<PostResponse>,
        @SerializedName("after")
        val nextPage: String?,
        @SerializedName("before")
        val lastPage: String?
)

/**
 * Model containing a single posts details
 */
data class PostResponse(
        val kind: String?,
        @SerializedName("data")
        val postDetails: PostDetails
)

/**
 * Model containing the details of each post
 */
data class PostDetails(
        val subreddit: String,
        @SerializedName("selftext_html")
        val selftextHtml: String?,
        val author: String,
        val name: String,
        val score: Int,
        val thumbnail: String,
        @SerializedName("subreddit_id")
        val subredditId: String,
        val url: String,
        val title: String,
        @SerializedName("post_hint")
        val postHint: String?,
        val domain: String,
        val id: String,
        @SerializedName("is_self")
        val isSelf: Boolean,
        @SerializedName("created_utc")
        val createdUtc: Long,
        val stickied: Boolean,
        val permalink: String,
        @SerializedName("over_18")
        val isOver18: Boolean,
        @SerializedName("num_comments")
        val numberOfComments: Int,
        @SerializedName("link_flair_text")
        val linkFlairText: String?,
        @SerializedName("preview")
        val images: ImagePreview?
)

/**
 * Model containing preview images
 */
data class ImagePreview(
        @SerializedName("images")
        val images: List<Image>
)