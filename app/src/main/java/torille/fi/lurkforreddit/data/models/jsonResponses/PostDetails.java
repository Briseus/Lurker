package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model containing the details of each post
 */
@AutoValue
public abstract class PostDetails {

    public abstract String subreddit();

    @Nullable
    @SerializedName("selftext_html")
    public abstract String selftextHtml();

    public abstract String author();

    public abstract String name();

    public abstract int score();

    public abstract String thumbnail();

    @SerializedName("subreddit_id")
    public abstract String subredditId();

    public abstract String url();

    public abstract String title();

    @Nullable
    @SerializedName("post_hint")
    public abstract String postHint();

    public abstract String domain();

    public abstract String id();

    @SerializedName("is_self")
    public abstract boolean isSelf();

    @SerializedName("created_utc")
    public abstract long createdUtc();

    public abstract boolean stickied();

    public abstract String permalink();

    @SerializedName("over_18")
    public abstract boolean isOver18();

    @SerializedName("num_comments")
    public abstract int numberOfComments();

    @Nullable
    @SerializedName("link_flair_text")
    public abstract String linkFlairText();

    @Nullable
    @SerializedName("preview")
    public abstract ImagePreview images();

    public static TypeAdapter<PostDetails> typeAdapter(Gson gson) {
        return new AutoValue_PostDetails.GsonTypeAdapter(gson);

    }
}
