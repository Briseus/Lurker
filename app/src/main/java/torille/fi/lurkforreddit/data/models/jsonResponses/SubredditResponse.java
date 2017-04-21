package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model containing subreddit info
 */
@AutoValue
public abstract class SubredditResponse implements Parcelable {

    public abstract String id();

    @Nullable
    public abstract String title();

    public abstract String url();

    @Nullable
    public abstract String name();

    @Nullable
    @SerializedName("key_color")
    public abstract String keyColor();

    @Nullable
    @SerializedName("display_name")
    public abstract String displayName();

    @Nullable
    @SerializedName("public_description_html")
    public abstract String descriptionHtml();


    public abstract boolean over18();


    public abstract int subscribers();


    @SerializedName("user_is_subscriber")
    public abstract boolean subscribed();


    @SerializedName("created_utc")
    public abstract long createdUtc();

    @Nullable
    @SerializedName("icon_img")
    public abstract String icon();

    @Nullable
    @SerializedName("banner_img")
    public abstract String banner();

    public static Builder builder() {
        return new AutoValue_SubredditResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setTitle(String title);

        public abstract Builder setUrl(String url);

        public abstract Builder setName(String name);

        public abstract Builder setKeyColor(String colorHex);

        public abstract Builder setDisplayName(String displayName);

        public abstract Builder setDescriptionHtml(String descriptionHtml);

        public abstract Builder setOver18(boolean isOver18);

        public abstract Builder setSubscribers(int subscribers);

        public abstract Builder setSubscribed(boolean subscribed);

        public abstract Builder setCreatedUtc(long timeUtc);

        public abstract Builder setIcon(String iconUrl);

        public abstract Builder setBanner(String bannerUrl);

        public abstract SubredditResponse build();
    }

    public static TypeAdapter<SubredditResponse> typeAdapter(Gson gson) {
        return new AutoValue_SubredditResponse.GsonTypeAdapter(gson);

    }
}
