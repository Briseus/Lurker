
package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model containing all the commentListings
 */
@AutoValue
public abstract class CommentData {

    @SerializedName("children")
    public abstract List<CommentChild> commentChildren();

    @Nullable
    @SerializedName("after")
    public abstract String after();

    @Nullable
    @SerializedName("before")
    public abstract String before();

    public static CommentData create(List<CommentChild> commentChildren,
                                     String after,
                                     String before) {
        return new AutoValue_CommentData(commentChildren,
                after,
                before);
    }

    public static TypeAdapter<CommentData> typeAdapter(Gson gson) {
        return new AutoValue_CommentData.GsonTypeAdapter(gson);
    }
}
