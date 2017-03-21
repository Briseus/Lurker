package torille.fi.lurkforreddit.utils;

import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import java.util.LinkedList;
import java.util.List;

import torille.fi.lurkforreddit.data.CommentChild;

/**
 * Contains utility classes for parsing text
 */

public class TextHelper {
    private TextHelper() {}

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

    public static List<CommentChild> flatten(List<?> list) {
        List<CommentChild> ret = new LinkedList<>();
        flattenHelper(list, ret, 0);
        return ret;
    }

    public static CommentChild formatCommentData(CommentChild commentChild) {
        if (commentChild.getData().getBodyHtml() != null) {
            commentChild.getData().setFormattedComment(trimTrailingWhitespace(fromHtml((commentChild.getData().getBodyHtml()))));
        }

        String author = "";
        if (commentChild.getData().getAuthor() != null && commentChild.getData().isStickied()) {
            author = "<font color='#64FFDA'> Sticky post<font> &nbsp &nbsp";
        } else if (commentChild.getData().getAuthor() != null) {
            author = commentChild.getData().getAuthor();
        }
        commentChild.getData().setFormatAuthor(fromHtml(author + " " + DateUtils.getRelativeTimeSpanString(commentChild.getData().getCreatedUtc() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)));

        return commentChild;
    }

    private static void flattenHelper(List<?> nestedList, List<CommentChild> flatList, int level) {
        for (Object item : nestedList) {
            if (item instanceof List<?>) {
                flattenHelper((List<?>) item, flatList, level + 1);
            } else if (item instanceof CommentChild) {
                item = formatCommentData(((CommentChild) item));
                ((CommentChild) item).setType(level);
                flatList.add((CommentChild) item);
                if (((CommentChild) item).getData().getReplies() != null) {
                    flattenHelper(((CommentChild) item).getData().getReplies().getCommentData().getCommentChildren(), flatList, level + 1);
                }
            }
        }
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
