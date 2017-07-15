package torille.fi.lurkforreddit.data.models.jsonResponses

import com.google.gson.annotations.SerializedName

/**
 * Model containing Reddit API auth token
 */


data class RedditToken(
        @SerializedName("access_token")
        val accessToken: String,
        @SerializedName("token_type")
        val tokenType: String,
        @SerializedName("scope")
        val scope: String,
        @SerializedName("refresh_token")
        val refreshToken: String?,
        @SerializedName("expires_in")
        val expiresIn: Long
)
