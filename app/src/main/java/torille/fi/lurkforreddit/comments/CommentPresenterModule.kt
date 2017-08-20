package torille.fi.lurkforreddit.comments

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.di.scope.FragmentScoped
import javax.annotation.Nullable

@Module
abstract class CommentPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun commentFragment(): CommentFragment

    @ActivityScoped
    @Binds
    internal abstract fun commentPresenter(presenter: CommentPresenter): CommentContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        internal fun provideCommentOriginalPost(commentActivity: CommentActivity): Post {
            return commentActivity.intent.getParcelableExtra(CommentActivity.EXTRA_CLICKED_POST)
        }

        @JvmStatic
        @Provides
        @ActivityScoped
        internal fun provideIsSingleCommentThread(commentActivity: CommentActivity): Boolean {
            return commentActivity.intent.getBooleanExtra(CommentFragment.ARGUMENT_IS_SINGLE_COMMENT_THREAD, false)
        }
    }
}
