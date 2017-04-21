
package torille.fi.lurkforreddit.data.models.jsonResponses;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class CommentResponse {
    @Nullable
    @SerializedName("subreddit_id")
    public abstract String subredditId();

    @Nullable
    @SerializedName("link_id")
    public abstract String linkId();

    @Nullable
    @SerializedName("replies")
    public abstract CommentListing replies();

    @SerializedName("saved")
    public abstract boolean saved();

    @SerializedName("id")
    public abstract String id();

    @SerializedName("gilded")
    public abstract int gilded();

    @SerializedName("archived")
    public abstract boolean archived();

    @Nullable
    @SerializedName("author")
    public abstract String author();

    @Nullable
    @SerializedName("parent_id")
    public abstract String parentId();

    @SerializedName("score")
    public abstract int score();

    @SerializedName("controversiality")
    public abstract int controversiality();

    @Nullable
    @SerializedName("body_html")
    public abstract String bodyHtml();

    @SerializedName("stickied")
    public abstract boolean stickied();

    @Nullable
    @SerializedName("subreddit")
    public abstract String subreddit();

    @SerializedName("score_hidden")
    public abstract boolean scoreHidden();

    @SerializedName("name")
    public abstract String name();

    @Nullable
    @SerializedName("author_flair_text")
    public abstract String authorFlairText();

    @SerializedName("created_utc")
    public abstract long createdUtc();

    @SerializedName("ups")
    public abstract int ups();

    public abstract int count();

    @Nullable
    @SerializedName("children")
    public abstract List<String> children();

    public static Builder builder() {
        return new AutoValue_CommentResponse.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setSubredditId(String subredditIdb);

        public abstract Builder setLinkId(String linkId);

        public abstract Builder setReplies(CommentListing replies);

        public abstract Builder setSaved(boolean saved);

        public abstract Builder setId(String id);

        public abstract Builder setGilded(int gildedTimes);

        public abstract Builder setArchived(boolean archived);

        public abstract Builder setAuthor(String author);

        public abstract Builder setCount(int count);

        public abstract Builder setParentId(String parentId);

        public abstract Builder setScore(int score);

        public abstract Builder setControversiality(int controversiality);

        public abstract Builder setBodyHtml(String bodyHtml);

        public abstract Builder setStickied(boolean stickied);

        public abstract Builder setSubreddit(String subreddit);

        public abstract Builder setScoreHidden(boolean scoreHidden);

        public abstract Builder setName(String name);

        public abstract Builder setAuthorFlairText(String flairText);

        public abstract Builder setCreatedUtc(long createdUtc);

        public abstract Builder setUps(int upsAmount);

        public abstract Builder setChildren(List<String> children);

        public abstract CommentResponse build();
    }
}
