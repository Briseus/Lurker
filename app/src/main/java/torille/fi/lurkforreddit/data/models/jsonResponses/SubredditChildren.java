package torille.fi.lurkforreddit.data.models.jsonResponses;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model containing the listing that includes a single subreddit
 */
@AutoValue
public abstract class SubredditChildren {
    public abstract String kind();

    @SerializedName("data")
    public abstract SubredditResponse subreddit();

    public static TypeAdapter<SubredditChildren> typeAdapter(Gson gson) {
        return new AutoValue_SubredditChildren.GsonTypeAdapter(gson);

    }
}
