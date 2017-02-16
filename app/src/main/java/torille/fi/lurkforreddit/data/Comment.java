
package torille.fi.lurkforreddit.data;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Comment {
    @SerializedName("subreddit_id")
    public String subredditId;
    @SerializedName("link_id")
    public String linkId;
    @SerializedName("replies")
    public  CommentListing replies;
    @SerializedName("saved")
    public boolean saved;
    @SerializedName("id")
    public String id;
    @SerializedName("gilded")
    public int gilded;
    @SerializedName("archived")
    public boolean archived;
    @SerializedName("author")
    public String author;
    @SerializedName("parent_id")
    public String parentId;
    @SerializedName("score")
    public int score;
    @SerializedName("controversiality")
    public int controversiality;
    @SerializedName("body")
    public String body;
    @SerializedName("downs")
    public int downs;
    @SerializedName("body_html")
    public String bodyHtml;
    @SerializedName("stickied")
    public boolean stickied;
    @SerializedName("subreddit")
    public String subreddit;
    @SerializedName("score_hidden")
    public boolean scoreHidden;
    @SerializedName("name")
    public String name;
    @SerializedName("created")
    public long created;
    @SerializedName("author_flair_text")
    public String authorFlairText;
    @SerializedName("created_utc")
    public long createdUtc;
    @SerializedName("ups")
    public int ups;
    public int count;
    @SerializedName("children")
    public List<String> children;
    private transient CharSequence formattedComment;
    private transient CharSequence formatAuthor;
    private transient String formatTime;

    public Comment() {
    }

    /**
     * Comment that indicates that it has more comments
     * @param count
     * @param children
     * @param name
     * @param id
     * @param parentId
     */
    public Comment(int count, List<String> children, String name, String id, String parentId) {
        this.count = count;
        this.children = children;
        this.name = name;
        this.id = id;
        this.parentId = parentId;
    }

    /**
     * Normal comment
     * @param subredditId
     * @param linkId
     * @param replies
     * @param saved
     * @param id
     * @param gilded
     * @param archived
     * @param author
     * @param parentId
     * @param score
     * @param controversiality
     * @param body
     * @param downs
     * @param bodyHtml
     * @param stickied
     * @param subreddit
     * @param scoreHidden
     * @param name
     * @param createdUtc
     * @param authorFlairText
     * @param ups
     * @param count
     * @param children
     */
    public Comment(String subredditId, String linkId, CommentListing replies, Boolean saved, String id, Integer gilded, Boolean archived, String author, String parentId, Integer score, Integer controversiality, String body, Integer downs, String bodyHtml, Boolean stickied, String subreddit, Boolean scoreHidden, String name, long createdUtc, String authorFlairText, Integer ups, int count, List<String> children) {
        this.subredditId = subredditId;
        this.linkId = linkId;
        this.replies = replies;
        this.saved = saved;
        this.id = id;
        this.gilded = gilded;
        this.archived = archived;
        this.author = author;
        this.parentId = parentId;
        this.score = score;
        this.controversiality = controversiality;
        this.body = body;
        this.downs = downs;
        this.bodyHtml = bodyHtml;
        this.stickied = stickied;
        this.subreddit = subreddit;
        this.scoreHidden = scoreHidden;
        this.name = name;
        this.createdUtc = createdUtc;
        this.authorFlairText = authorFlairText;
        this.ups = ups;
        this.count = count;
        this.children = children;
    }

    public String getSubredditId() {
        return subredditId;
    }

    public void setSubredditId(String subredditId) {
        this.subredditId = subredditId;
    }


    public String getLinkId() {
        return linkId;
    }


    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public CommentListing getReplies() {
        return replies;
    }


    public void setReplies(CommentListing replies) {
        this.replies = replies;
    }

    public boolean getSaved() {
        return saved;
    }


    public void setSaved(boolean saved) {
        this.saved = saved;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public int getGilded() {
        return gilded;
    }


    public void setGilded(int gilded) {
        this.gilded = gilded;
    }


    public boolean getArchived() {
        return archived;
    }


    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getAuthor() {
        return author;
    }


    public void setAuthor(String author) {
        this.author = author;
    }


    public String getParentId() {
        return parentId;
    }


    public void setParentId(String parentId) {
        this.parentId = parentId;
    }


    public int getScore() {
        return score;
    }


    public void setScore(int score) {
        this.score = score;
    }

    public int getControversiality() {
        return controversiality;
    }


    public void setControversiality(int controversiality) {
        this.controversiality = controversiality;
    }


    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }

    public int getDowns() {
        return downs;
    }


    public void setDowns(int downs) {
        this.downs = downs;
    }


    public String getBodyHtml() {
        return bodyHtml;
    }


    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }


    public boolean getStickied() {
        return stickied;
    }

    public void setStickied(boolean stickied) {
        this.stickied = stickied;
    }

    public String getSubreddit() {
        return subreddit;
    }


    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }


    public boolean getScoreHidden() {
        return scoreHidden;
    }


    public void setScoreHidden(boolean scoreHidden) {
        this.scoreHidden = scoreHidden;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public long getCreated() {
        return created;
    }


    public void setCreated(long created) {
        this.created = created;
    }


    public String getAuthorFlairText() {
        return authorFlairText;
    }


    public void setAuthorFlairText(String authorFlairText) {
        this.authorFlairText = authorFlairText;
    }


    public long getCreatedUtc() {
        return createdUtc;
    }


    public void setCreatedUtc(long createdUtc) {
        this.createdUtc = createdUtc;
    }


    public int getUps() {
        return ups;
    }


    public void setUps(int ups) {
        this.ups = ups;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public CharSequence getFormattedComment() {
        return formattedComment;
    }

    public CharSequence getFormatAuthor() {
        return formatAuthor;
    }

    public void setFormatAuthor(CharSequence formatAuthor) {
        this.formatAuthor = formatAuthor;
    }

    public String getFormatTime() {
        return formatTime;
    }

    public void setFormatTime(String formatTime) {
        this.formatTime = formatTime;
    }

    public void setFormattedComment(CharSequence formattedComment) {
        this.formattedComment = formattedComment;
    }

}
