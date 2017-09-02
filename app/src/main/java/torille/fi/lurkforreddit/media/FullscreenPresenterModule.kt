package torille.fi.lurkforreddit.media

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.di.scope.ActivityScoped
import torille.fi.lurkforreddit.di.scope.FragmentScoped

@Module
abstract class FullscreenPresenterModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun fullscreenFragment(): FullscreenFragment

    @ActivityScoped
    @Binds
    abstract fun fullscreenPresenter(presenter: FullscreenPresenter): FullscreenContract.Presenter

    @Module
    companion object {
        @JvmStatic
        @Provides
        @ActivityScoped
        fun providePreviewImageUrl(fullscreenActivity: FullscreenActivity): Post {
            return fullscreenActivity.intent.getParcelableExtra(FullscreenActivity.EXTRA_POST)
        }

        @JvmStatic
        @Provides
        @ActivityScoped
        fun provideUrl(fullscreenActivity: FullscreenActivity): String? {
            return fullscreenActivity.intent.getStringExtra(FullscreenActivity.EXTRA_URL)
        }
    }
}

