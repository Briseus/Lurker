package torille.fi.lurkforreddit.data.models.jsonResponses;


import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model containing a single images resolutions and source image
 */
@AutoValue
public abstract class Image {

    @SerializedName("source")
    public abstract ImageSource source();

    @SerializedName("resolutions")
    public abstract List<ImageResolution> resolutions();

    @SerializedName("id")
    public abstract String id();

    public static TypeAdapter<Image> typeAdapter(Gson gson) {
        return new AutoValue_Image.GsonTypeAdapter(gson);

    }
}
