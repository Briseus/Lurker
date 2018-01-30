package torille.fi.lurkforreddit.data.models.jsonResponses


import com.google.gson.annotations.SerializedName

/**
 * Model containing a single images resolutions and source image
 */
data class Image(
    @SerializedName("source")
    val source: ImageSource,
    @SerializedName("resolutions")
    val resolutions: List<ImageResolution>,
    @SerializedName("subId")
    val id: String
)

/**
 * Model containg the source image
 */
data class ImageSource(
    @SerializedName("url")
    val url: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)

/**
 * Model containing information about image resolution
 */
data class ImageResolution(
    @SerializedName("url")
    val url: String?,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)