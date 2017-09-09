package torille.fi.lurkforreddit.comments

import android.content.Intent
import android.net.Uri
import android.text.style.URLSpan
import android.view.View
import timber.log.Timber
import torille.fi.lurkforreddit.data.models.view.Post
import torille.fi.lurkforreddit.media.FullscreenActivity
import torille.fi.lurkforreddit.subreddit.SubredditActivity
import torille.fi.lurkforreddit.utils.AppLinkActivity
import torille.fi.lurkforreddit.utils.MediaHelper
import java.util.regex.Pattern

/**
 * Custom [URLSpan] to modify how to open clicked links in text
 */

internal class CustomUrlSpan(url: String) : URLSpan(url) {

    override fun onClick(widget: View) {
        val url = url
        val uri = Uri.parse(url)
        val domain = uri.host
        Timber.d("Got url $url")

        val intent: Intent
        val context = widget.context
        when {
            MediaHelper.isContentMedia(url, domain) -> {
                intent = Intent(context, FullscreenActivity::class.java)
                intent.putExtra(FullscreenActivity.EXTRA_POST, Post(url = url))
                intent.putExtra(FullscreenActivity.EXTRA_URL, url)
                context.startActivity(intent)
            }
            checkForReddit(url) -> {
                Timber.d("Going to checkout subreddit $url")
                intent = Intent(context, AppLinkActivity::class.java)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            }
            else -> super.onClick(widget)
        }

    }

    private fun checkForReddit(redditUrl: String): Boolean {
        Timber.d("Checking url $redditUrl")
        val p = Pattern.compile("(/r/.*)")
        val m = p.matcher(redditUrl)
        return m.matches()
    }
}
