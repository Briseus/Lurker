package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.List;

/**
 * Model containing subreddits with next/last page
 */
@AutoValue
public abstract class SubredditData {
    public abstract List<SubredditChildren> children();

    public abstract String after();

    public abstract String before();

    public static TypeAdapter<SubredditData> typeAdapter(Gson gson) {
        return new AutoValue_SubredditData.GsonTypeAdapter(gson)
                .setDefaultAfter("")
                .setDefaultBefore("");

    }
}
