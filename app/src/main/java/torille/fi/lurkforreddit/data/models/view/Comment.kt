package torille.fi.lurkforreddit.data.models.view

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Data class for comments
 */
@PaperParcel
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
) : Parcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelComment.CREATOR
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelComment.writeToParcel(this, dest, flags)
    }
}

