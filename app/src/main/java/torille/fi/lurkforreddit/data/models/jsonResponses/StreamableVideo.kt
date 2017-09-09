package torille.fi.lurkforreddit.data.models.jsonResponses

import com.google.gson.annotations.SerializedName

/**
 * Class that holds streamable response
 */
data class Videos(
        @SerializedName("mp4-mobile")
        val mobileVideo: ImageResolution?,
        @SerializedName("mp4")
        val video: ImageResolution
)

data class StreamableVideo(
        @SerializedName("title")
        val title: String,
        @SerializedName("files")
        val videos: Videos
)