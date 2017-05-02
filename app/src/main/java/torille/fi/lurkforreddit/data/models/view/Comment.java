package torille.fi.lurkforreddit.data.models.view;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * Created by eva on 21.4.2017.
 */
@AutoValue
public abstract class Comment {

    public enum kind {
        MORE,
        PROGRESSBAR,
        SINGLECOMMENTTOP,
        DEFAULT
    }

    public abstract String id();

    public abstract String name();

    public abstract String parentId();

    public abstract kind kind();

    public abstract int commentLevel();

    @Nullable
    public abstract String commentLinkId();

    public abstract CharSequence commentText();

    public abstract CharSequence author();

    public abstract String formattedTime();

    public abstract String formattedScore();

    @Nullable
    public abstract List<String> childCommentIds();

    @Nullable
    public abstract List<Comment> replies();

    abstract Builder toBuilder();

    public Comment withLevel(int level) {
        return toBuilder().setCommentLevel(level).build();
    }

    public Comment withReplies(List<Comment> replies) {
        return toBuilder().setReplies(replies).build();
    }

    public Comment withKindIdTextAndLevel(Comment.kind kind, String id, String text, int level) {
        return toBuilder().setKind(kind).setId(id).setCommentText(text).setCommentLevel(level).build();
    }

    public Comment withKind(Comment.kind kind) {
        return toBuilder().setKind(kind).build();
    }

    public static Builder builder() {
        return new AutoValue_Comment.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setName(String name);

        public abstract Builder setParentId(String id);

        public abstract Builder setKind(Comment.kind kind);

        public abstract Builder setCommentLevel(int deepness);

        public abstract Builder setCommentLinkId(String linkId);

        public abstract Builder setCommentText(CharSequence commentText);

        public abstract Builder setAuthor(CharSequence author);

        public abstract Builder setFormattedTime(String formattedTime);

        public abstract Builder setFormattedScore(String score);

        public abstract Builder setChildCommentIds(List<String> commentIds);

        public abstract Builder setReplies(List<Comment> replies);

        public abstract Comment build();
    }
}
