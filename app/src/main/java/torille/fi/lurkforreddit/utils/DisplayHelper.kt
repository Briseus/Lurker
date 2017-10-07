package torille.fi.lurkforreddit.utils

import android.content.Context
import io.reactivex.rxkotlin.toObservable
import timber.log.Timber
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails

/**
 * Helper to get dpi and best picture compared to width
 */

object DisplayHelper {

    private var displayDPI: Int = 0

    /**
     * @param postDetails model containing data
     * *
     * @return "" or url of the picture if the picture is not too small or large
     */
    fun getBestPreviewPicture(postDetails: PostDetails): String {
        val imagesPreviews = postDetails.images
        return if (imagesPreviews != null) {
            imagesPreviews.images[0].resolutions.toObservable()
                    .filter({ imageResolution ->
                        val pictureWidth = imageResolution.width
                        val result = compareWidth(displayDPI, pictureWidth)
                        Timber.d("Result is $result for ${imageResolution.url}")
                        (result in 0.7..1.3)
                    })
                    .map {
                        Timber.d("Chose ${it.width}")
                        it.url.orEmpty()
                    }
                    .last("").blockingGet()
        } else {
            ""
        }
    }

    private fun compareWidth(displayWidth: Int, pictureWidth: Int): Double {
        return pictureWidth.toDouble() / displayWidth.toDouble()
    }

    private fun getDisplayDPI(context: Context): Int {
        val dm = context.resources.displayMetrics
        return (dm.widthPixels / dm.density).toInt()

    }

    fun init(context: Context) {
        displayDPI = getDisplayDPI(context.applicationContext)
    }
}
