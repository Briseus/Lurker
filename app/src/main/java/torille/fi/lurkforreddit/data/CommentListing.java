
package torille.fi.lurkforreddit.data;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Root model of comments
 */
@Parcel
public class CommentListing {

    @SerializedName("kind")
    public String kind;
    @SerializedName("data")
    public CommentData commentData;

    @ParcelConstructor
    public CommentListing(String kind, CommentData commentData) {
        this.kind = kind;
        this.commentData = commentData;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public CommentData getCommentData() {
        return commentData;
    }

    public void setCommentData(CommentData commentData) {
        this.commentData = commentData;
    }

    @Override
    public String toString() {
        return "CommentListing{" +
                "kind='" + kind + '\'' +
                ", commentData=" + commentData +
                '}';
    }
}
