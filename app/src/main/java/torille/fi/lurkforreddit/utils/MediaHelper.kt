package torille.fi.lurkforreddit.utils

import android.content.Context
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsSession
import android.support.v4.content.ContextCompat
import torille.fi.lurkforreddit.R

/**
 * Helper class for parsing urls and media
 */

object MediaHelper {

    fun isContentMedia(url: String?, domain: String?): Boolean {

        if (url == null || url.length < 4) {
            return false
        }

        return when (TextHelper.getLastFourChars(url)) {
            ".jpg", ".png", "jpeg", ".gif", ".mp4", "gifv" -> true
            else -> checkDomainForMedia(domain)
        }
    }

    private fun checkDomainForMedia(domain: String?): Boolean {
        if (domain.isNullOrEmpty()) {
            return false
        }

        return when (domain) {
            "gfycat.com", "i.reddituploads.com", "streamable.com", "v.redd.it" -> true
            else -> false
        }
    }

    fun launchCustomActivity(domain: String?): Boolean {

        if (domain == null || domain.isEmpty()) {
            return false
        }
        return when (domain) {
            "youtube.com", "reddit.com", "youtu.be" -> true
            else -> false
        }
    }

    fun createCustomTabIntent(context: Context,
                              session: CustomTabsSession): CustomTabsIntent {

        return io.reactivex.Observable.fromArray(session)
                .map { CustomTabsIntent.Builder(it) }
                .map { intentBuilder ->
                    val toolbarColor = ContextCompat.getColor(context, R.color.colorPrimary)
                    val secondaryColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
                    intentBuilder.setToolbarColor(toolbarColor)
                    intentBuilder.setSecondaryToolbarColor(secondaryColor)

                    intentBuilder.addDefaultShareMenuItem()
                    intentBuilder.enableUrlBarHiding()
                    intentBuilder.setShowTitle(true)

                    intentBuilder.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                    intentBuilder.setExitAnimations(context, android.R.anim.slide_in_left,
                            android.R.anim.slide_out_right)
                    intentBuilder.build()
                }.blockingFirst()

    }
}
