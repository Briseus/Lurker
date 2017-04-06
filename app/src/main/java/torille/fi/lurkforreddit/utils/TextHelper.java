package torille.fi.lurkforreddit.utils;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;

import torille.fi.lurkforreddit.data.models.Comment;
import torille.fi.lurkforreddit.data.models.CommentChild;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.Subreddit;
import torille.fi.lurkforreddit.data.models.SubredditChildren;

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

    public static List<CommentChild> flatten(List<CommentChild> list, int level) {
        List<CommentChild> flatComments = new ArrayList<>();
        flatComments = flattenHelper(list, flatComments, level);
        return flatComments;
    }

    public static List<CommentChild> flattenAdditionalComments(List<CommentChild> list, int level) {
        List<CommentChild> additionalComments = flatten(list, level);
        for (int i = 0; i < additionalComments.size(); i++) {
            for (int j = 0; j < additionalComments.size(); j++) {
                if (additionalComments.get(i).getData().getName().equals(additionalComments.get(j).getData().getParentId())) {
                    additionalComments.get(j).setType(additionalComments.get(i).getType() + 1);
                }
            }
        }
        return additionalComments;
    }

    private static Comment formatCommentData(Comment comment) {

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

    private static Comment formatAdditionalCommentData(Comment comment) {
        String text;
        if (comment.getId().equals("_")) {
            text = "Continue this thread ->";
        } else {
            text = "Load more comments (" + comment.getCount() + ")";
        }
        comment.setFormattedComment(text);
        return comment;
    }

    private static List<CommentChild> flattenHelper(List<CommentChild> nestedList, List<CommentChild> flatList, int level) {
        for (int i = 0; i < nestedList.size(); i++) {
            CommentChild commentChild = nestedList.get(i);
            Comment comment = commentChild.getData();
            commentChild.setType(level);

            //check if its a normal comment or "load more comments" comment
            if (commentChild.getData().getId().equals("_") || commentChild.getData().getChildren() != null) {
                commentChild.setKind("more");
                commentChild.setData(formatAdditionalCommentData(comment));
            } else {
                commentChild.setKind("t3");
                commentChild.setData(formatCommentData(comment));
            }

            flatList.add(commentChild);
            if (commentChild.getData().getReplies() != null) {
                List<CommentChild> replyList = flattenHelper(commentChild.getData().getReplies().getCommentData().getCommentChildren(), flatList, level + 1);
                commentChild.getData().getReplies().getCommentData().setCommentChildren(replyList);
            }
        }
        return flatList;
    }

    public static List<Post> formatPosts(List<Post> posts) {

        for (Post post : posts) {

            post.getPostDetails().setPreviewScore(TextHelper.formatScore(post.getPostDetails().getScore()));

            if (post.getPostDetails().isStickied()) {
                post.getPostDetails().setPreviewTitle(TextHelper.fromHtml(post.getPostDetails().getTitle() + "<font color='#64FFDA'> Stickied </font>"));
            } else {
                post.getPostDetails().setPreviewTitle(TextHelper.fromHtml(post.getPostDetails().getTitle()));
            }
            // sometimes formatting title can result in empty if it has <----- at start
            // etc
            if (post.getPostDetails().getPreviewTitle() != null &&
                    post.getPostDetails().getPreviewTitle().length() == 0) {
                SpannableString titleWithoutFormatting = SpannableString.valueOf(post.getPostDetails().getTitle());
                post.getPostDetails().setPreviewTitle(titleWithoutFormatting);
            }
            switch (post.getPostDetails().getThumbnail()) {
                case "default":
                case "self":
                case "":
                case "image":
                    post.getPostDetails().setPreviewImage("");
                    break;
                case "nsfw":
                    post.getPostDetails().setPreviewTitle(TextHelper.fromHtml(post.getPostDetails().getTitle() + "<font color='#FF1744'> NSFW</font>"));
                    post.getPostDetails().setPreviewImage("");
                    break;
                default:
                    post.getPostDetails().setPreviewImage(DisplayHelper.getBestPreviewPicture(post.getPostDetails()));
            }

        }

        return posts;
    }

    public static List<SubredditChildren> formatSubreddits (List<SubredditChildren> childrens) {
        for (SubredditChildren result : childrens) {
            Subreddit subreddit = result.getSubreddit();

            final String info = subreddit.getSubscribers() + " subscribers, Community since "
                    + DateUtils.getRelativeTimeSpanString(subreddit.getCreatedUtc() * 1000);
            subreddit.setFormattedInfo(info);

            if (subreddit.isOver18()) {
                subreddit.setFormattedTitle(TextHelper.fromHtml(subreddit.getUrl()
                        + "<font color='#FF1744'> NSFW</font>"));
            } else {
                subreddit.setFormattedTitle(subreddit.getUrl());
            }

            if (subreddit.isSubscribed()) {
                subreddit.setFormattedSubscription("Subscribed");
            } else {
                subreddit.setFormattedSubscription("Not subscribed");
            }

            if (subreddit.getDescriptionHtml() != null && !subreddit.getDescriptionHtml().isEmpty()) {
                subreddit.setFormattedDescription(TextHelper.trimTrailingWhitespace(TextHelper.fromHtml(subreddit.getDescriptionHtml())));
            } else {
                subreddit.setFormattedDescription("No description");
            }
        }
        return childrens;
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
