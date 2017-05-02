package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model containing a single posts details
 */
@AutoValue
public abstract class PostResponse {

    @Nullable
    public abstract String kind();

    @SerializedName("data")
    public abstract PostDetails postDetails();

    public static PostResponse create(String kind, PostDetails postDetails) {
        return new AutoValue_PostResponse(kind, postDetails);
    }

    public static TypeAdapter<PostResponse> typeAdapter(Gson gson) {
        return new AutoValue_PostResponse.GsonTypeAdapter(gson);

    }
}
