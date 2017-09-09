package torille.fi.lurkforreddit.data.models.jsonResponses

import com.google.gson.annotations.SerializedName

/**
 * Holds multireddit json model
 */
data class Multireddit(
        @SerializedName("display_name")
        val displayName: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("icon_url")
        val iconUrl: Any?,
        @SerializedName("created_utc")
        val createdUtc: Float?,
        @SerializedName("key_color")
        val keyColor: String,
        @SerializedName("path")
        val pathUrl: String
)


data class MultiredditListing(
        @SerializedName("data")
        val multireddit: Multireddit
)

