package torille.fi.lurkforreddit.utils;

import android.content.Context;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;

import torille.fi.lurkforreddit.R;
import torille.fi.lurkforreddit.data.models.Post;

/**
 * Helper class for parsing urls and media
 */

final public class MediaHelper {

    private MediaHelper() {}

    public static boolean isContentMedia(final String url) {

        if (url == null || url.length() < 4) {
            return false;
        }

        switch (TextHelper.getLastFourChars(url)) {
            case ".jpg":
            case ".png":
            case "jpeg":
            case ".gif":
            case ".mp4":
            case "gifv":
                return true;
            default:
                return false;
        }
    }

    public static boolean checkDomainForMedia(String domain) {
        if (domain == null) {
            return false;
        }

        switch (domain) {
            case "gfycat.com":
            case "i.reddituploads.com":
            case "streamable.com":
                return true;
            default:
                return false;
        }
    }

    public static boolean launchCustomActivity(Post post) {
        String domain = post.getPostDetails().getDomain();
        if (domain == null) {
            return false;
        }
        switch (domain) {
            case "youtube.com":
            case "youtu.be":
                return true;
            default:
                return false;
        }
    }

    public static CustomTabsIntent createCustomTabIntent(Context context, CustomTabsSession session) {
        final CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder(session);

        intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        intentBuilder.enableUrlBarHiding();
        intentBuilder.setShowTitle(true);
        intentBuilder.addDefaultShareMenuItem();

        intentBuilder.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left);
        intentBuilder.setExitAnimations(context, R.anim.slide_in_left,
                R.anim.slide_out_right);
        return intentBuilder.build();
    }
}
