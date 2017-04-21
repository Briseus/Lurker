
package torille.fi.lurkforreddit.data.models.jsonResponses;

import com.google.auto.value.AutoValue;
import com.google.gson.annotations.SerializedName;

/**
 * Root model of comments
 */
@AutoValue
public abstract class CommentListing {

    @SerializedName("kind")
    public abstract String kind();

    @SerializedName("data")
    public abstract CommentData commentData();

    public static CommentListing create(String kind, CommentData commentData) {
        return new AutoValue_CommentListing(kind, commentData);
    }
}
