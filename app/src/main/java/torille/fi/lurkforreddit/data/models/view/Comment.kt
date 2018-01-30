package torille.fi.lurkforreddit.data.models.view

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data class for comments
 */
@Parcelize
data class Comment(
    val id: String = "",
    val name: String = "",
    val parentId: String = "",
    val kind: kind = torille.fi.lurkforreddit.data.models.view.kind.DEFAULT,
    val commentLevel: Int = 0,
    val commentLinkId: String? = null,
    val commentText: CharSequence = "",
    val author: CharSequence = "",
    val formattedTime: String = "",
    val formattedScore: String = "",
    val childCommentIds: List<String>? = null,
    val replies: List<Comment>? = null
) : Parcelable
