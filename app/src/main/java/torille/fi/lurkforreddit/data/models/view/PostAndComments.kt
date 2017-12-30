package torille.fi.lurkforreddit.data.models.view

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Class that holds holds comments and its post
 */
@Parcelize
data class PostAndComments(
        val originalPost: Post,
        val comments: List<Comment>
) : Parcelable
