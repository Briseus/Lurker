package torille.fi.lurkforreddit.data.models.view;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * Created by eva on 20.4.2017.
 */
@AutoValue
public abstract class Subreddit implements Parcelable {

    public abstract String id();

    public abstract String url();

    public abstract String displayName();

    @Nullable
    public abstract String bannerUrl();

    @Nullable
    public abstract String keyColor();

    public static Builder builder() {
        return new AutoValue_Subreddit.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setDisplayName(String name);

        public abstract Builder setUrl(String url);

        public abstract Builder setBannerUrl(String bannerUrl);

        public abstract Builder setKeyColor(String hexColor);

        public abstract Subreddit build();
    }
}
