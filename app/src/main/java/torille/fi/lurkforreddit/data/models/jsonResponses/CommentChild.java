
package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

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

    @Nullable
    @SerializedName("data")
    public abstract CommentResponse data();

    @Nullable
    public abstract PostDetails originalPost();


    public static CommentChild create(String kind, CommentResponse response, PostDetails postDetails) {
        return new AutoValue_CommentChild(kind, response, postDetails);
    }

    public static TypeAdapter<CommentChild> typeAdapter(Gson gson) {
        return new AutoValue_CommentChild.GsonTypeAdapter(gson);
    }
}
