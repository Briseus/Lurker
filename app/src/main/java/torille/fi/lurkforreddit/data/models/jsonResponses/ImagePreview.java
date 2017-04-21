package torille.fi.lurkforreddit.data.models.jsonResponses;


import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model containing preview images
 */
@AutoValue
public abstract class ImagePreview {

    @SerializedName("images")
    public abstract List<Image> images();

    public static TypeAdapter<ImagePreview> typeAdapter(Gson gson) {
        return new AutoValue_ImagePreview.GsonTypeAdapter(gson);

    }
}
