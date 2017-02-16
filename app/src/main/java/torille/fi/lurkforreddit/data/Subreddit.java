package torille.fi.lurkforreddit.data;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Model containing subreddit info
 */
@org.parceler.Parcel
public class Subreddit {
    public String id;
    public String title;
    public String url;
    public String name;
    public String key_color;
    public String display_name;
    @SerializedName("public_description_html")
    public String descriptionHtml;
    public boolean over18;
    public int subscribers;
    @SerializedName("user_is_subscriber")
    public boolean subscribed;
    @SerializedName("created_utc")
    public long createdUtc;

    public Subreddit() {
    }

    public Subreddit(String id, String title, String url, String name, String key_color, String display_name, @Nullable String descriptionHtml, boolean over18, int subscribers, boolean subscribed, long createdUtc) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.name = name;
        this.key_color = key_color;
        this.display_name = display_name;
        this.descriptionHtml = descriptionHtml;
        this.over18 = over18;
        this.subscribers = subscribers;
        this.subscribed = subscribed;
        this.createdUtc = createdUtc;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey_color() {
        return key_color;
    }

    public void setKey_color(String key_color) {
        this.key_color = key_color;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public long getCreatedUtc() {
        return createdUtc;
    }

    public void setCreatedUtc(long createdUtc) {
        this.createdUtc = createdUtc;
    }

    @Override
    public String toString() {
        return "Subreddit{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", key_color='" + key_color + '\'' +
                ", display_name='" + display_name + '\'' +
                '}';
    }
}
