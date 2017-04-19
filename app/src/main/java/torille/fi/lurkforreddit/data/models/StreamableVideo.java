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

    private static class Videos {

        @SerializedName("mp4-mobile")
        private ImageResolution mobileVideo;

        @SerializedName("mp4")
        private ImageResolution video;

        public ImageResolution getMobileVideo() {
            return mobileVideo;
        }

        public ImageResolution getVideo() {
            return video;
        }

    }

    public String getTitle() {
        return title;
    }

    public String getMobileVideoUrl() {

        String url = "";

        if (videos.mobileVideo != null) {
            url = videos.mobileVideo.getUrl();
        } else if (videos.video != null) {
            url = videos.getVideo().getUrl();
        }
        if (url.isEmpty()) {
            return "";
        } else {
            return "https:" + url;
        }

    }
}
