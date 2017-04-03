package torille.fi.lurkforreddit.utils;

import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;

import torille.fi.lurkforreddit.data.models.Comment;
import torille.fi.lurkforreddit.data.models.CommentChild;

/**
 * Contains utility classes for parsing text
 */

public class TextHelper {
    private TextHelper() {
    }

    public static String getLastFourChars(String url) {
        return url.substring(url.length() - 4);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(final String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static String formatScore(int score) {
        final String value = String.valueOf(score);
        if (score < 1000) {
            return value;
        } else if (score < 10000) {
            return value.charAt(0) + "." + value.charAt(1) + "k";
        } else if (score < 100000) {
            return value.charAt(0) + value.charAt(1) + "k";
        } else if (score < 10000000) {
            return value.charAt(0) + value.charAt(1) + value.charAt(2) + "k";
        } else {
            return value;
        }
    }

    public static List<CommentChild> flatten(List<CommentChild> list) {
        List<CommentChild> ret = new ArrayList<>();
        ret = flattenHelper(list, ret, 0);
        return ret;
    }

    public static Comment formatCommentData(Comment comment) {

        if (comment.getBodyHtml() != null) {
            comment.setFormattedComment(trimTrailingWhitespace(fromHtml((comment.getBodyHtml()))));
        }

        String author = "";
        if (comment.getAuthor() != null && comment.isStickied()) {
            author = "<font color='#64FFDA'> Sticky post<font> &nbsp &nbsp";
        } else if (comment.getAuthor() != null) {
            author = comment.getAuthor();
        }

        comment.setFormatAuthor(fromHtml(author + " " + DateUtils.getRelativeTimeSpanString(comment.getCreatedUtc() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)));

        comment.setFormatScore(formatScore(comment.getScore()));
        return comment;
    }

    private static List<CommentChild> flattenHelper(List<CommentChild> nestedList, List<CommentChild> flatList, int level) {
        for (int i = 0; i < nestedList.size(); i++) {
            CommentChild commentChild = nestedList.get(i);
            commentChild.setType(level);
            commentChild.setData(formatCommentData(commentChild.getData()));
            flatList.add(commentChild);
            if (commentChild.getData().getReplies() != null) {
                List<CommentChild> replyList = flattenHelper(commentChild.getData().getReplies().getCommentData().getCommentChildren(), flatList, level + 1);
                commentChild.getData().getReplies().getCommentData().setCommentChildren(replyList);
            }
        }
        return flatList;
    }

    /**
     * Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     *
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1);
    }

}
