package torille.fi.lurkforreddit.data.models.view

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Data class for showing a subreddit
 */

@PaperParcel
@Entity(tableName = "subreddits")
data class Subreddit(
        @PrimaryKey @ColumnInfo(name = "reddit_id") var subId: String = "",
        var url: String = "",
        @ColumnInfo(name = "display_name") var displayName: String = "",
        @ColumnInfo(name = "banner_url") var bannerUrl: String? = "",
        @ColumnInfo(name = "key_color") var keyColor: String? = ""
) : Parcelable {

    companion object {
        @JvmField
        val CREATOR = PaperParcelSubreddit.CREATOR

    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        PaperParcelSubreddit.writeToParcel(this, dest, flags)
    }
}
