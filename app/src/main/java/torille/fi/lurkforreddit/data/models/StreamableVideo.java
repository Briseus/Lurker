package torille.fi.lurkforreddit.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Class that holds streamable response
 */

public class StreamableVideo {

    @SerializedName("title")
    private String title;

    @SerializedName("files")
    private Videos videos;

    private class Videos {
        @SerializedName("mp4-mobile")
        private ImageResolution mobileVideo;

        private String getMobileVideoUrl() {
            return mobileVideo.getUrl();
        }

    }

    public String getTitle() {
        return title;
    }

    public String getMobileVideoUrl() {
        return "https:" + videos.getMobileVideoUrl();
    }

}
