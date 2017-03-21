package torille.fi.lurkforreddit.data.models;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model containing preview images
 */
@Parcel
public class ImagePreview {

    @SerializedName("images")
    List<Image> images = new ArrayList<>();

    public List<Image> getImages() {
        return images;
    }

    @ParcelConstructor
    ImagePreview(List<Image> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "ImagePreview{" +
                "images=" + images +
                "}";
    }
}
