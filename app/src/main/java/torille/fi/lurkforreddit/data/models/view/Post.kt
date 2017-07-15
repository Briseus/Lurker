package torille.fi.lurkforreddit.data.models.view

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Model for showing subreddit posts
 */
@PaperParcel
data class Post(
        val id: String = "",
        val author: String = "",
        val createdUtc: Long = 0,
        val permaLink: String = "",
        val domain: String = "",
        val url: String = "",
        val score: String = "",
        val flairText: CharSequence = "",
        val selfText: CharSequence = "",
        val previewImage: String = "",
        val thumbnail: String = "",
        val title: CharSequence = "",
        val numberOfComments: String = "",
        val isSelf: Boolean = false
) : Parcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelPost.CREATOR
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelPost.writeToParcel(this, dest, flags)
    }
}

