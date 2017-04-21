package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Model containing Reddit API auth token
 */


@AutoValue
public abstract class RedditToken {
    @SerializedName("access_token")
    public abstract String access_token();

    @SerializedName("token_type")
    public abstract String token_type();

    @SerializedName("scope")
    public abstract String scope();

    @Nullable
    @SerializedName("refresh_token")
    public abstract String refresh_token();

    @SerializedName("expires_in")
    public abstract long expires_in();

    public static TypeAdapter<RedditToken> typeAdapter(Gson gson) {
        return new AutoValue_RedditToken.GsonTypeAdapter(gson);

    }
}
