package torille.fi.lurkforreddit.data;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.parceler.ParcelConstructor;

/**
 * Model containing the details of each post
 */
@org.parceler.Parcel
public class PostDetails {

    public String subreddit;
    public String selftext;
    @Nullable
    @SerializedName("selftext_html")
    public String selftextHtml;
    public int likes;
    public String author;
    public String name;
    public int score;
    public String thumbnail;
    @SerializedName("subreddit_id")
    public String subredditId;
    public String url;
    public String title;
    @Nullable
    @SerializedName("post_hint")
    public String postHint;
    public String domain;
    public String id;
    @SerializedName("is_self")
    public Boolean isSelf;
    @SerializedName("created")
    public long created;
    @SerializedName("created_utc")
    public long createdUtc;
    public boolean stickied;
    @Nullable
    public String distinguished;
    public String permalink;
    @SerializedName("num_comments")
    public int numberOfComments;
    @Nullable
    @SerializedName("preview")
    public ImagePreview images;
    @Nullable
    public String previewImage;
    @Nullable
    public String previewText;

    public PostDetails() {
    }

    @ParcelConstructor
    public PostDetails(String subreddit, String selftext, @Nullable String selftextHtml, int likes, String author, String name, int score, @Nullable String thumbnail, String subredditId, String url, String title, @Nullable String postHint, String domain, String id, Boolean isSelf, long created, long createdUtc, boolean stickied, @Nullable String distinguished, String permalink, int numberOfComments, ImagePreview images, @Nullable String previewImage, @Nullable String previewText) {
        this.subreddit = subreddit;
        this.selftext = selftext;
        this.selftextHtml = selftextHtml;
        this.likes = likes;
        this.author = author;
        this.name = name;
        this.score = score;
        this.thumbnail = thumbnail;
        this.subredditId = subredditId;
        this.url = url;
        this.title = title;
        this.postHint = postHint;
        this.domain = domain;
        this.id = id;
        this.isSelf = isSelf;
        this.created = created;
        this.createdUtc = createdUtc;
        this.stickied = stickied;
        this.distinguished = distinguished;
        this.permalink = permalink;
        this.numberOfComments = numberOfComments;
        this.images = images;
        this.previewImage = previewImage;
        this.previewText = previewText;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getSelftext() {
        return selftext;
    }

    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSubredditId() {
        return subredditId;
    }

    public void setSubredditId(String subredditId) {
        this.subredditId = subredditId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getSelf() {
        return isSelf;
    }

    public void setSelf(Boolean self) {
        isSelf = self;
    }

    public String getPostHint() {
        return postHint;
    }

    public void setPostHint(String postHint) {
        this.postHint = postHint;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public ImagePreview getImages() {
        return images;
    }

    public void setImages(ImagePreview images) {
        this.images = images;
    }

    public String getSelftextHtml() {
        return selftextHtml;
    }

    public void setSelftextHtml(String selftextHtml) {
        this.selftextHtml = selftextHtml;
    }

    public boolean isStickied() {
        return stickied;
    }

    public void setStickied(boolean stickied) {
        this.stickied = stickied;
    }

    public String getDistinguished() {
        return distinguished;
    }

    public void setDistinguished(String distinguished) {
        this.distinguished = distinguished;
    }

    public long getCreatedUtc() {
        return createdUtc;
    }

    public void setCreatedUtc(long createdUtc) {
        this.createdUtc = createdUtc;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }


}
