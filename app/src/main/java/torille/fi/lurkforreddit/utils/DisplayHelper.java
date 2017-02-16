package torille.fi.lurkforreddit.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import torille.fi.lurkforreddit.data.PostDetails;

/**
 * Helper to get dpi and best picture compared to width
 */

final public class DisplayHelper {
    private static int mDisplayDPI;

    /**
     * @param postDetails model containing data
     * @return "" or url of the picture if the picture is not too small or large
     */
    public static String getBestPreviewPicture(PostDetails postDetails) {

        if (postDetails.getImages() != null) {
            if (postDetails.getImages().getImages().size() >= 1) {
                for (int i = 0; i < postDetails.getImages().getImages().get(0).getResolutions().size(); i++) {
                    final int pictureWidth = postDetails.getImages().getImages().get(0).getResolutions().get(i).getWidth();
                    final double result = compareWidth(mDisplayDPI, pictureWidth);
                    if (result >= 0.7
                            && result <= 1.3) {
                        return postDetails.getImages().getImages().get(0).getResolutions().get(i).getUrl();
                        // if no hits check last resolution again
                    }
                }
                return "";
            }

        }
        return "";

    }

    private static Double compareWidth(int displayWidth, int pictureWidth) {
        return ((double) pictureWidth / (double) displayWidth);
    }

    public static int getDisplayDPI(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (dm.widthPixels / dm.density);

    }

    public static void init(Context context) {
        mDisplayDPI = getDisplayDPI(context.getApplicationContext());
    }
}
