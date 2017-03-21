
package torille.fi.lurkforreddit.data;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Comment {
    @SerializedName("subreddit_id")
    String subredditId;
    @SerializedName("link_id")
    String linkId;
    @SerializedName("replies")
     CommentListing replies;
    @SerializedName("saved")
    boolean saved;
    @SerializedName("id")
    String id;
    @SerializedName("gilded")
    int gilded;
    @SerializedName("archived")
    boolean archived;
    @SerializedName("author")
    String author;
    @SerializedName("parent_id")
    String parentId;
    @SerializedName("score")
    int score;
    @SerializedName("controversiality")
    int controversiality;
    @SerializedName("body")
    String body;
    @SerializedName("downs")
    int downs;
    @SerializedName("body_html")
    String bodyHtml;
    @SerializedName("stickied")
    boolean stickied;
    @SerializedName("subreddit")
    String subreddit;
    @SerializedName("score_hidden")
    boolean scoreHidden;
    @SerializedName("name")
    String name;
    @SerializedName("created")
    long created;
    @SerializedName("author_flair_text")
    String authorFlairText;
    @SerializedName("created_utc")
    long createdUtc;
    @SerializedName("ups")
    int ups;
    int count;
    @SerializedName("children")
    List<String> children;
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

    public boolean isSaved() {
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

    public boolean isArchived() {
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

    public boolean isStickied() {
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

    public boolean isScoreHidden() {
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

    public void setFormattedComment(CharSequence formattedComment) {
        this.formattedComment = formattedComment;
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

    @Override
    public String toString() {
        return "Comment{" +
                "subredditId='" + subredditId + '\'' +
                ", linkId='" + linkId + '\'' +
                ", replies=" + replies +
                ", saved=" + saved +
                ", id='" + id + '\'' +
                ", gilded=" + gilded +
                ", archived=" + archived +
                ", author='" + author + '\'' +
                ", parentId='" + parentId + '\'' +
                ", score=" + score +
                ", controversiality=" + controversiality +
                ", body='" + body + '\'' +
                ", downs=" + downs +
                ", bodyHtml='" + bodyHtml + '\'' +
                ", stickied=" + stickied +
                ", subreddit='" + subreddit + '\'' +
                ", scoreHidden=" + scoreHidden +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", authorFlairText='" + authorFlairText + '\'' +
                ", createdUtc=" + createdUtc +
                ", ups=" + ups +
                ", count=" + count +
                ", children=" + children +
                ", formattedComment=" + formattedComment +
                ", formatAuthor=" + formatAuthor +
                ", formatTime='" + formatTime + '\'' +
                '}';
    }
}
