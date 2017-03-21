package torille.fi.lurkforreddit.data.models;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Model containing information about image resolution
 */

@Parcel
public class ImageResolution {

    @SerializedName("url")
    String url;
    @SerializedName("width")
    int width;
    @SerializedName("height")
    int height;

    ImageResolution() {
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width The width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return The height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height The height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "ImageResolution{" +
                "url='" + url + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}