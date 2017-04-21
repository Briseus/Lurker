
package torille.fi.lurkforreddit.data.models.jsonResponses;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model of a listing containing comment
 */
@AutoValue
public abstract class CommentChild {

    public abstract String kind();

    @SerializedName("data")
    public abstract CommentResponse data();

    public static CommentChild create(String kind, CommentResponse response) {
        return new AutoValue_CommentChild(kind, response);
    }

    public static TypeAdapter<CommentChild> typeAdapter(Gson gson) {
        return new AutoValue_CommentChild.GsonTypeAdapter(gson);
    }
}
