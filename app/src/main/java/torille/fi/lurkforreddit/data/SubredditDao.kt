package torille.fi.lurkforreddit.data

import android.arch.persistence.room.*
import io.reactivex.Flowable
import org.intellij.lang.annotations.Language
import torille.fi.lurkforreddit.data.models.view.Subreddit

@Dao
interface SubredditDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subreddit: Subreddit)

    @Update
    fun update(subreddit: Subreddit)

    @Delete
    fun delete(subreddit: Subreddit)

    @Query("DELETE FROM subreddits")
    fun deleteAllSubreddits()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(subreddit: List<Subreddit>)

    @Query("SELECT * FROM subreddits")
    fun getSubreddits(): Flowable<List<Subreddit>>
}
