package torille.fi.lurkforreddit.data.models.jsonResponses;


import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;


/**
 * Model containg the source image
 */
@AutoValue
public abstract class ImageSource {

    @SerializedName("url")
    public abstract String url();

    @SerializedName("width")
    public abstract int width();

    @SerializedName("height")
    public abstract int height();

    public static TypeAdapter<ImageSource> typeAdapter(Gson gson) {
        return new AutoValue_ImageSource.GsonTypeAdapter(gson);

    }
}