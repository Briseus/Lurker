package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Created by eva on 26.4.2017.
 */
@AutoValue
public abstract class MultiredditListing {

    @SerializedName("data")
    public abstract Multireddit multireddit();

    public static TypeAdapter<MultiredditListing> typeAdapter(Gson gson) {
        return new AutoValue_MultiredditListing.GsonTypeAdapter(gson);
    }

    @AutoValue
    public abstract static class Multireddit {

        @SerializedName("display_name")
        public abstract String displayName();

        @SerializedName("name")
        public abstract String name();

        @Nullable
        @SerializedName("icon_url")
        public abstract Object iconUrl();

        @SerializedName("created_utc")
        public abstract Float createdUtc();

        @SerializedName("key_color")
        public abstract String keyColor();

        @SerializedName("path")
        public abstract String pathUrl();

        public static TypeAdapter<Multireddit> typeAdapter(Gson gson) {
            return new AutoValue_MultiredditListing_Multireddit.GsonTypeAdapter(gson);
        }
    }


}
