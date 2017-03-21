package torille.fi.lurkforreddit.data;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model containing a single images resolutions and source image
 */
@Parcel
public class Image {

    @SerializedName("source")
    ImageSource source;
    @SerializedName("resolutions")
    public List<ImageResolution> resolutions = new ArrayList<>();
    @SerializedName("id")
    String id;

    @ParcelConstructor
    Image(ImageSource source, List<ImageResolution> resolutions, String id) {
        this.source = source;
        this.resolutions = resolutions;
        this.id = id;
    }

    public ImageSource getSource() {
        return source;
    }

    public void setSource(ImageSource source) {
        this.source = source;
    }

    public List<ImageResolution> getResolutions() {
        return resolutions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Image{" +
                "source=" + source +
                ", resolutions=" + resolutions +
                ", id='" + id + "}";
    }
}
