package torille.fi.lurkforreddit.data.models;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;


/**
 * Model containg the source image
 */
@Parcel
class ImageSource {

    @SerializedName("url")
    String url;
    @SerializedName("width")
    int width;
    @SerializedName("height")
    int height;

    ImageSource() {
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public int getWidth() {
        return width;
    }


    public void setWidth(int width) {
        this.width = width;
    }


    public int getHeight() {
        return height;
    }


    public void setHeight(int height) {
        this.height = height;
    }

}