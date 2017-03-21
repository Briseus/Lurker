
package torille.fi.lurkforreddit.data.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Model containing all the commentListings
 */

@Parcel
public class CommentData {

    @SerializedName("modhash")
    String modhash;
    @SerializedName("children")
    List<CommentChild> commentChildren = new ArrayList<>();
    @SerializedName("after")
    String after;
    @SerializedName("before")
    String before;

    @ParcelConstructor
    public CommentData(String modhash, List<CommentChild> commentChildren, String after, String before) {
        this.modhash = modhash;
        this.commentChildren = commentChildren;
        this.after = after;
        this.before = before;
    }

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public List<CommentChild> getCommentChildren() {
        return commentChildren;
    }

    public void setCommentChildren(List<CommentChild> commentChildren) {
        this.commentChildren = commentChildren;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    @Override
    public String toString() {
        return "CommentData{" +
                "modhash='" + modhash + '\'' +
                ", commentChildren=" + commentChildren +
                ", after=" + after +
                ", before=" + before +
                '}';
    }
}
