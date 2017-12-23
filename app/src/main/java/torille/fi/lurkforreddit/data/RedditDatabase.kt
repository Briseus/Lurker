package torille.fi.lurkforreddit.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import torille.fi.lurkforreddit.data.models.view.Subreddit
import javax.inject.Singleton

@Singleton
@Database(entities = [(Subreddit::class)],
        version = 1)
abstract class RedditDatabase : RoomDatabase() {

    abstract fun getSubredditDao(): SubredditDao
}

