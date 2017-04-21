package torille.fi.lurkforreddit.data.models.jsonResponses;


import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model containing information about image resolution
 */
@AutoValue
public abstract class ImageResolution {

    @Nullable
    @SerializedName("url")
    public abstract String url();
    @SerializedName("width")
    public abstract int width();
    @SerializedName("height")
    public abstract int height();

    public static TypeAdapter<ImageResolution> typeAdapter(Gson gson) {
        return new AutoValue_ImageResolution.GsonTypeAdapter(gson);

    }
}