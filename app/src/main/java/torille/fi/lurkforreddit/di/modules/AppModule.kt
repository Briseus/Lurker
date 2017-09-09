package torille.fi.lurkforreddit.di.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger 2 module providing Application
 */
@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    internal fun bindContext(): Context {
        return application.applicationContext
    }
}
