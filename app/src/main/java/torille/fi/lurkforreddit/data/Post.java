package torille.fi.lurkforreddit.data;

import com.google.gson.annotations.SerializedName;

import org.parceler.ParcelConstructor;

/**
 * Model containing a single posts details
 */
@org.parceler.Parcel
public class Post {

    String kind;
    @SerializedName("data")
    PostDetails postDetails = new PostDetails();

    public Post() {
    }

    @ParcelConstructor
    public Post(String kind, PostDetails postDetails) {
        this.kind = kind;
        this.postDetails = postDetails;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public PostDetails getPostDetails() {
        return postDetails;
    }

    public void setPostDetails(PostDetails postDetails) {
        this.postDetails = postDetails;
    }

    @Override
    public String toString() {
        return "Post{" +
                "kind='" + kind + '\'' +
                ", postDetails=" + postDetails +
                '}';
    }
}
