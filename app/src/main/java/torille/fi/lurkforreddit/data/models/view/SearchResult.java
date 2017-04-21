package torille.fi.lurkforreddit.data.models.view;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

/**
 * Created by eva on 20.4.2017.
 */

@AutoValue
public abstract class SearchResult implements Parcelable {

    public abstract CharSequence title();

    public abstract String subscriptionInfo();

    public abstract String infoText();

    public abstract CharSequence description();

    public abstract Subreddit subreddit();

    public static Builder builder() {
        return new AutoValue_SearchResult.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setTitle(CharSequence formattedTitle);

        public abstract Builder setSubscriptionInfo(String formattedSubscription);

        public abstract Builder setInfoText(String formattedInfo);

        public abstract Builder setDescription(CharSequence formattedDescription);

        public abstract Builder setSubreddit(Subreddit subreddit);

        public abstract SearchResult build();
    }
}
