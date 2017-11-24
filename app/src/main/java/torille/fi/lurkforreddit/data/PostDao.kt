package torille.fi.lurkforreddit.data

import android.arch.persistence.room.*
import io.reactivex.Observable
import torille.fi.lurkforreddit.data.models.view.Post

@Dao
interface PostDao {

    @Insert
    fun insert(post: Post)

    @Update
    fun update(post: Post)

    @Delete
    fun delete(post: Post)

    @Query("SELECT * FROM post WHERE subredditId=:subredditId")
    fun getSubredditPosts(postId: String): Observable<List<Post>>

}