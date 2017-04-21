package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Class that holds streamable response
 */
@AutoValue
public abstract class StreamableVideo {

    @SerializedName("title")
    public abstract String title();

    @SerializedName("files")
    public abstract Videos videos();


    public static TypeAdapter<StreamableVideo> typeAdapter(Gson gson) {
        return new AutoValue_StreamableVideo.GsonTypeAdapter(gson);

    }

    @AutoValue
    public abstract static class Videos {

        @Nullable
        @SerializedName("mp4-mobile")
        public abstract ImageResolution mobileVideo();

        @SerializedName("mp4")
        public abstract ImageResolution video();

        public static TypeAdapter<Videos> typeAdapter(Gson gson) {
            return new AutoValue_StreamableVideo_Videos.GsonTypeAdapter(gson);

        }
    }

}
