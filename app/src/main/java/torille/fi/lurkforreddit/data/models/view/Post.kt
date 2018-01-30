package torille.fi.lurkforreddit.data.models.view

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Model for showing subreddit posts
 */
@Entity(
    foreignKeys = (arrayOf(
        ForeignKey(
            entity = Subreddit::class,
            parentColumns = arrayOf("subId"),
            childColumns = arrayOf("postId")
        )
    ))
)
@Parcelize
data class Post(
    @PrimaryKey val id: String = "",
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
) : Parcelable
