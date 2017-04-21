package torille.fi.lurkforreddit.utils;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.List;

import torille.fi.lurkforreddit.data.models.jsonResponses.Image;
import torille.fi.lurkforreddit.data.models.jsonResponses.ImagePreview;
import torille.fi.lurkforreddit.data.models.jsonResponses.ImageResolution;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails;

/**
 * Helper to get dpi and best picture compared to width
 */

final public class DisplayHelper {
    private static int mDisplayDPI;

    private DisplayHelper() {
    }


    /**
     * @param postDetails model containing data
     * @return "" or url of the picture if the picture is not too small or large
     */
    public static String getBestPreviewPicture(PostDetails postDetails) {
        ImagePreview imagesPreviews = postDetails.images();
        if (imagesPreviews != null) {
            List<Image> images = imagesPreviews.images();
            if (images != null && images.size() >= 1) {
                List<ImageResolution> imageResolutions = images.get(0).resolutions();
                for (int i = 0, size = imageResolutions.size(); i < size; i++) {
                    final int pictureWidth = imageResolutions.get(i).width();
                    final double result = compareWidth(mDisplayDPI, pictureWidth);
                    if (result >= 0.7
                            && result <= 1.3) {
                        return imageResolutions.get(i).url();

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

    private static int getDisplayDPI(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (int) (dm.widthPixels / dm.density);

    }

    public static void init(Context context) {
        mDisplayDPI = getDisplayDPI(context.getApplicationContext());
    }
}
