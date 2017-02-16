package torille.fi.lurkforreddit.data;

import java.util.List;

/**
 * Model containing subreddits with next/last page
 */
public class SubredditData {
    private List<SubredditChildren> children;
    private String after;
    private String before;

    public SubredditData() {
    }

    public List<SubredditChildren> getChildren() {
        return children;
    }

    public void setChildren(List<SubredditChildren> children) {
        this.children = children;
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
        return "SubredditData{" +
                "children=" + children +
                '}';
    }
}
