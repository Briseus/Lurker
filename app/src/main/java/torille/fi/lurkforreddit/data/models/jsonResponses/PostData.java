package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model containing all response listings and next/last page
 */
@AutoValue
public abstract class PostData {

    @SerializedName("children")
    public abstract List<PostResponse> Posts();

    @Nullable
    @SerializedName("after")
    public abstract String nextPage();

    @Nullable
    @SerializedName("before")
    public abstract String lastPage();

    public static TypeAdapter<PostData> typeAdapter(Gson gson) {
        return new AutoValue_PostData.GsonTypeAdapter(gson);

    }
}