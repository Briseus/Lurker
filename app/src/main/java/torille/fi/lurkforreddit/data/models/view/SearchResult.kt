package torille.fi.lurkforreddit.data.models.view

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data class for showing search result
 */
@Parcelize
data class SearchResult(
        val title: CharSequence = "",
        val subscriptionInfo: String = "",
        val infoText: String = "",
        val description: CharSequence = "",
        val subreddit: Subreddit = Subreddit()
) : Parcelable
