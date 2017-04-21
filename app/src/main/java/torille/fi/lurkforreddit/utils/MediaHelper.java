package torille.fi.lurkforreddit.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;

import torille.fi.lurkforreddit.R;

/**
 * Helper class for parsing urls and media
 */

final public class MediaHelper {

    private MediaHelper() {
    }

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

    public static boolean checkDomainForMedia(@Nullable String domain) {
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

    public static boolean launchCustomActivity(@Nullable String domain) {

        if (domain == null || domain.isEmpty()) {
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

    public static CustomTabsIntent createCustomTabIntent(Context context,
                                                         CustomTabsSession session) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder(session);

        int toolbarColor = ContextCompat.getColor(context, R.color.colorPrimary);
        int secondaryColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        intentBuilder.setToolbarColor(toolbarColor);
        intentBuilder.setSecondaryToolbarColor(secondaryColor);

        intentBuilder.addDefaultShareMenuItem();
        intentBuilder.enableUrlBarHiding();
        intentBuilder.setShowTitle(true);

        intentBuilder.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left);
        intentBuilder.setExitAnimations(context, android.R.anim.slide_in_left,
                android.R.anim.slide_out_right);

        return intentBuilder.build();
    }
}
