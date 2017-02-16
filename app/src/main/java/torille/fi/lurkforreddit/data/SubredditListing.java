package torille.fi.lurkforreddit.data;

/**
 * Root pojo for getting subreddits
 */

public class SubredditListing {
    private String kind;
    private SubredditData data;

    public SubredditListing() {
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public SubredditData getData() {
        return data;
    }

    public void setData(SubredditData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SubredditListing{" +
                "kind='" + kind + '\'' +
                ", data=" + data +
                '}';
    }
}
