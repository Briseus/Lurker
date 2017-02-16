package torille.fi.lurkforreddit.data;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;


/**
 * Model containg the source image
 */
@Parcel
public class ImageSource {

    @SerializedName("url")
    public String url;
    @SerializedName("width")
    public Integer width;
    @SerializedName("height")
    public Integer height;

    public ImageSource() {
    }

    public String getUrl() {
        return url;
    }


    public void setUrl(String url) {
        this.url = url;
    }


    public Integer getWidth() {
        return width;
    }


    public void setWidth(Integer width) {
        this.width = width;
    }


    public Integer getHeight() {
        return height;
    }


    public void setHeight(Integer height) {
        this.height = height;
    }

}