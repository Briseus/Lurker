package torille.fi.lurkforreddit.data.models.jsonResponses;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * Root object for Reddit posts
 */
@AutoValue
public abstract class PostListing {

    public abstract String kind();
    public abstract PostData data();

    public static TypeAdapter<PostListing> typeAdapter(Gson gson) {
        return new AutoValue_PostListing.GsonTypeAdapter(gson);

    }
}

