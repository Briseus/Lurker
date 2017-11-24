package torille.fi.lurkforreddit.di.scope

import javax.inject.Qualifier

/**
 * Scope for injecting Reddit API
 */
@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Remote
