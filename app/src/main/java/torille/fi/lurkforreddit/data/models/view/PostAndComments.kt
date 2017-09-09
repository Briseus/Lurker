package torille.fi.lurkforreddit.data.models.view

import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Class that holds holds comments and its post
 */
@PaperParcel
data class PostAndComments(
        val originalPost: Post,
        val comments: List<Comment>
) : Parcelable {
    companion object {
        @JvmField val CREATOR = PaperParcelPostAndComments.CREATOR
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelPostAndComments.writeToParcel(this, dest, flags)
    }
}

