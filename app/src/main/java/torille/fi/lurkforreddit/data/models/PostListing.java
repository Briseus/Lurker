package torille.fi.lurkforreddit.data.models;

/**
 * Root object for Reddit posts
 */

public class PostListing {

    String kind;
    PostData data;

    public PostListing() {
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public PostData getData() {
        return data;
    }

    public void setData(PostData data) {
        this.data = data;
    }
}

