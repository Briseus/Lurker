package torille.fi.lurkforreddit.data.models.jsonResponses

import com.google.gson.annotations.SerializedName

/**
 * Root model of comments
 */
data class CommentListing(
        @SerializedName("kind")
        val kind: String = "",
        @SerializedName("data")
        val commentData: CommentData = CommentData()
)

/**
 * Model containing all the commentListings
 */
data class CommentData(
        @SerializedName("children")
        val commentChildren: List<CommentChild> = emptyList(),
        @SerializedName("after")
        val after: String = "",
        @SerializedName("before")
        val before: String = ""
)

/**
 * Model of a listing containing comment
 */
data class CommentChild(
        val kind: String,
        @SerializedName("data")
        val data: CommentResponse?,
        val originalPost: PostDetails?
)

data class CommentResponse(
        @SerializedName("subreddit_id")
        val subredditId: String = "",
        @SerializedName("link_id")
        val linkId: String = "",
        @SerializedName("replies")
        val replies: CommentListing? = CommentListing("", CommentData(emptyList(), "", "")),
        @SerializedName("saved")
        val saved: Boolean = false,
        @SerializedName("subId")
        val id: String = "",
        @SerializedName("gilded")
        val gilded: Int = -1,
        @SerializedName("archived")
        val archived: Boolean = false,
        @SerializedName("author")
        val author: String = "",
        @SerializedName("parent_id")
        val parentId: String = "",
        @SerializedName("score")
        val score: Int = -1,
        @SerializedName("controversiality")
        val controversiality: Int = -1,
        @SerializedName("body_html")
        val bodyHtml: String = "",
        @SerializedName("stickied")
        val stickied: Boolean = false,
        @SerializedName("subreddit")
        val subreddit: String = "",
        @SerializedName("score_hidden")
        val scoreHidden: Boolean = false,
        val edited: Boolean = false,
        @SerializedName("name")
        val name: String = "",
        @SerializedName("author_flair_text")
        val authorFlairText: String = "",
        @SerializedName("created_utc")
        val createdUtc: Long = -1,
        @SerializedName("ups")
        val ups: Int = -1,
        val count: Int = -1,
        @SerializedName("children")
        val children: List<String> = emptyList()
)