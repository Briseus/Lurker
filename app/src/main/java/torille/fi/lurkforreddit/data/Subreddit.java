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
    @SerializedName("icon_img")
    @Nullable
    public String icon;
    @SerializedName("banner_img")
    @Nullable
    public String banner;
    private transient CharSequence formattedTitle;
    private transient String formattedSubscription;
    private transient String formattedInfo;
    private transient CharSequence formattedDescription;

    public Subreddit() {
    }

    public Subreddit(String id, String title, String url, String name, String key_color, String display_name, @Nullable String descriptionHtml, boolean over18, int subscribers, boolean subscribed, long createdUtc, String icon, String banner) {
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
        this.icon = icon;
        this.banner = banner;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public CharSequence getFormattedTitle() {
        return formattedTitle;
    }

    public void setFormattedTitle(CharSequence formattedTitle) {
        this.formattedTitle = formattedTitle;
    }

    public String getFormattedSubscription() {
        return formattedSubscription;
    }

    public void setFormattedSubscription(String formattedSubscription) {
        this.formattedSubscription = formattedSubscription;
    }

    public String getFormattedInfo() {
        return formattedInfo;
    }

    public void setFormattedInfo(String formattedInfo) {
        this.formattedInfo = formattedInfo;
    }

    public CharSequence getFormattedDescription() {
        return formattedDescription;
    }

    public void setFormattedDescription(CharSequence formattedDescription) {
        this.formattedDescription = formattedDescription;
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
                ", descriptionHtml='" + descriptionHtml + '\'' +
                ", over18=" + over18 +
                ", subscribers=" + subscribers +
                ", subscribed=" + subscribed +
                ", createdUtc=" + createdUtc +
                ", icon='" + icon + '\'' +
                ", banner='" + banner + '\'' +
                '}';
    }
}
