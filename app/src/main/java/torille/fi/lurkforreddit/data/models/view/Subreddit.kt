package torille.fi.lurkforreddit.data.models.view

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Data class for showing a subreddit
 */

@Entity(tableName = "subreddits")
@Parcelize
data class Subreddit(
        @PrimaryKey @ColumnInfo(name = "reddit_id") var subId: String = "",
        var url: String = "",
        @ColumnInfo(name = "display_name") var displayName: String = "",
        @ColumnInfo(name = "banner_url") var bannerUrl: String? = "",
        @ColumnInfo(name = "key_color") var keyColor: String? = ""
) : Parcelable
