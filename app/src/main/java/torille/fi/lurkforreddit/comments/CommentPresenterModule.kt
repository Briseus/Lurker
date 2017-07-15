package torille.fi.lurkforreddit.comments

import dagger.Module
import dagger.Provides
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.data.models.view.Post

@Module
class CommentPresenterModule(private val mPost: Post, private val mIsSingleCommentThread: Boolean) {

    @Provides
    internal fun provideCommentContractPresenter(repository: RedditRepository,
                                                 post: Post,
                                                 isSingleCommentThread: Boolean): CommentContract.Presenter<CommentContract.View> {
        return CommentPresenter(repository, post, isSingleCommentThread)
    }

    @Provides
    internal fun provideCommentOriginalPost(): Post {
        return mPost
    }

    @Provides
    internal fun provideIsSingleCommentThread(): Boolean {
        return mIsSingleCommentThread
    }
}
