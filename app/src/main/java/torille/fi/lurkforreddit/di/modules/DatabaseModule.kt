package torille.fi.lurkforreddit.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.RedditDatabase
import torille.fi.lurkforreddit.data.SubredditDao
import javax.inject.Singleton


@Module
class DatabaseModule {

    @Provides
    @Singleton
    internal fun provideDatabase(context: Context): RedditDatabase {
        return Room.databaseBuilder(context, RedditDatabase::class.java, "reddit").build()
    }

    @Provides
    @Singleton
    internal fun provideSubredditDao(redditDatabase: RedditDatabase): SubredditDao {
        return redditDatabase.getSubredditDao()
    }

}