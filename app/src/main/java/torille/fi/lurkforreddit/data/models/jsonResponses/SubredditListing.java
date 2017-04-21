package torille.fi.lurkforreddit.data.models.jsonResponses;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * Root pojo for getting subreddits
 */
@AutoValue
public abstract class SubredditListing {
    public abstract String kind();

    public abstract SubredditData data();

    public static TypeAdapter<SubredditListing> typeAdapter(Gson gson) {
        return new AutoValue_SubredditListing.GsonTypeAdapter(gson);

    }
}
