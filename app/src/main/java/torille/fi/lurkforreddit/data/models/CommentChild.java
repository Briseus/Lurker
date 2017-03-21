
package torille.fi.lurkforreddit.data.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

/**
 * Model of a listing containing comment
 */

@Parcel
public class CommentChild {

    private transient int type;
    @SerializedName("kind")
    String kind;
    @SerializedName("data")
    Comment data;

    public CommentChild() {
    }

    @ParcelConstructor
    public CommentChild(String kind, Comment data) {
        this.kind = kind;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Comment getData() {
        return data;
    }

    public void setData(Comment data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommentChild{" +
                "type=" + type +
                ", kind='" + kind + '\'' +
                ", data=" + data +
                '}';
    }
}
