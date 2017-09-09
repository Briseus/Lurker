package torille.fi.lurkforreddit.data.models.view

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Data class for showing search result
 */
@PaperParcel
data class SearchResult(
        val title: CharSequence = "",
        val subscriptionInfo: String = "",
        val infoText: String = "",
        val description: CharSequence = "",
        val subreddit: Subreddit = Subreddit()
) : Parcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelSearchResult.CREATOR
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelSearchResult.writeToParcel(this, dest, flags)
    }
}




   
