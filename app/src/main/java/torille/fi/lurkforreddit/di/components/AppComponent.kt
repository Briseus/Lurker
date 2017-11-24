package torille.fi.lurkforreddit.di.components

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjectionModule
import torille.fi.lurkforreddit.MyApplication
import torille.fi.lurkforreddit.RedditRepositoryModule
import torille.fi.lurkforreddit.data.RedditRepository
import torille.fi.lurkforreddit.di.modules.*
import javax.inject.Singleton


/**
 * Provides network related component
 */

@Singleton
@Component(modules = arrayOf(ActivityBindingModule::class,
        AppModule::class,
        DatabaseModule::class,
        NetModule::class,
        RedditAuthModule::class,
        RedditModule::class,
        StreamableModule::class,
        RedditRepositoryModule::class,
        AndroidSupportInjectionModule::class))
interface AppComponent : AndroidInjector<DaggerApplication> {

    fun inject(application: MyApplication)

    fun redditRepository(): RedditRepository


    override fun inject(instance: DaggerApplication)

    // Gives us syntactic sugar. we can then do DaggerAppComponent.builder().application(this).build().inject(this);
    // never having to instantiate any modules or say which module we are passing the application to.
    // Application will just be provided into our app graph now.
    /*@Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): AppComponent.Builder

        fun build(): AppComponent
    }*/

}
