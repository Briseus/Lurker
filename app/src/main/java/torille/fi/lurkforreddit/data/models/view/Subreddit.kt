package torille.fi.lurkforreddit.data.models.view

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Data class for showing a subreddit
 */
@PaperParcel
data class Subreddit(
        val id: String = "",
        val url: String = "",
        val displayName: String = "",
        val bannerUrl: String? = "",
        val keyColor: String? = ""
) : Parcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelSubreddit.CREATOR
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelSubreddit.writeToParcel(this, dest, flags)
    }
}
