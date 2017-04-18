package torille.fi.lurkforreddit.data.models;

import android.support.annotation.NonNull;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Created by eva on 17.4.2017.
 */
@Parcel
public class PreviewImage {
    float aspectRatio;
    String url;
    int height;
    int width;

    @ParcelConstructor
    public PreviewImage(float aspectRatio, String url, int height) {
        this.aspectRatio = aspectRatio;
        this.url = url;
        this.height = height;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public int getHeight() {
        return height;
    }
}
