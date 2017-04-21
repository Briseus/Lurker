package torille.fi.lurkforreddit.data.models.view;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * Created by eva on 21.4.2017.
 */

@AutoValue
public abstract class Post implements Parcelable {

    public abstract String id();

    public abstract String author();

    public abstract long createdUtc();

    public abstract String permaLink();

    public abstract String domain();

    public abstract String url();

    public abstract String score();

    @Nullable
    public abstract CharSequence selfText();

    public abstract String previewImage();

    public abstract String thumbnail();

    public abstract CharSequence title();

    public abstract String numberOfComments();

    public abstract boolean isSelf();

    public static Builder builder() {
        return new AutoValue_Post.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setAuthor(String author);

        public abstract Builder setCreatedUtc(long createdUtc);

        public abstract Builder setPermaLink(String permaLinkUrl);

        public abstract Builder setDomain(String domain);

        public abstract Builder setUrl(String url);

        public abstract Builder setScore(String score);

        public abstract Builder setSelfText(CharSequence selfText);

        public abstract Builder setPreviewImage(String url);

        public abstract Builder setThumbnail(String url);

        public abstract Builder setTitle(CharSequence title);

        public abstract Builder setNumberOfComments(String numberOfComments);

        public abstract Builder setIsSelf(boolean isSelf);

        public abstract Post build();

    }

}

