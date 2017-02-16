package torille.fi.lurkforreddit.data;

import com.google.gson.annotations.SerializedName;

/**
 * Model containing the listing that includes a single subreddit
 */
public class SubredditChildren {
    private String kind;
    @SerializedName("data")
    private Subreddit subreddit;

    public SubredditChildren() {
    }

    public SubredditChildren(String kind, Subreddit subreddit) {
        this.kind = kind;
        this.subreddit = subreddit;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Subreddit getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(Subreddit subreddit) {
        this.subreddit = subreddit;
    }

    @Override
    public String toString() {
        return "SubredditChildren{" +
                "kind='" + kind + '\'' +
                ", subreddit=" + subreddit +
                '}';
    }
}
