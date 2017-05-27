package torille.fi.lurkforreddit.utils;

import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentResponse;
import torille.fi.lurkforreddit.data.models.jsonResponses.MultiredditListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostResponse;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren;
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditResponse;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.SearchResult;
import torille.fi.lurkforreddit.data.models.view.Subreddit;

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
    private static Spanned fromHtml(final String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    private static String formatScore(int score) {
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

    public static List<Comment> flatten(List<CommentChild> list, int level) {
        List<Comment> flatComments = new ArrayList<>(list.size());
        flatComments = flattenHelper(list, flatComments, level);
        return flatComments;
    }

    public static List<Comment> flattenAdditionalComments(List<CommentChild> list, int level) {
        List<Comment> additionalComments = flatten(list, level);
        for (int i = 0, size = additionalComments.size(); i < size; i++) {
            for (int j = 0, nestedSize = additionalComments.size(); j < nestedSize; j++) {
                if (additionalComments.get(i).name().equals(additionalComments.get(j).parentId())) {
                    additionalComments.set(j, additionalComments.get(j).withLevel(level + 1));
                }
            }
        }
        return additionalComments;
    }

    private static Comment formatCommentData(CommentResponse commentResponse, int level) {
        CharSequence commentText = "";
        CharSequence author = "";
        Comment.kind kind = Comment.kind.MORE;

        if (commentResponse.bodyHtml() != null) {
            kind = Comment.kind.DEFAULT;
            commentText = formatTextToHtml(commentResponse.bodyHtml());
        } else if (commentResponse.id().equals("_")) {
            commentText = "Continue this thread";
        } else if (commentResponse.children() != null) {
            commentText = "Load more comments (" + commentResponse.count() + ")";
        }

        String responseAuthor = commentResponse.author();
        if (responseAuthor != null && commentResponse.stickied()) {
            author = "<font color='#64FFDA'> Sticky post </font>" + responseAuthor;
        } else if (responseAuthor != null) {
            author = responseAuthor;
        }

        author = fromHtml(author + " " + DateUtils.getRelativeTimeSpanString(commentResponse.createdUtc() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));

        if (commentResponse.edited()) {
            author = author + " (edited)";
        }

        String formatScore = formatScore(commentResponse.score());


        return Comment.builder()
                .setId(commentResponse.id())
                .setParentId(commentResponse.parentId())
                .setName(commentResponse.name())
                .setKind(kind)
                .setAuthor(author)
                .setChildCommentIds(commentResponse.children())
                .setCommentLevel(level)
                .setCommentLinkId(commentResponse.linkId())
                .setCommentText(commentText)
                .setFormattedTime("")
                .setFormattedScore(formatScore)
                .setReplies(null)
                .build();
    }

    private static List<Comment> flattenHelper(List<CommentChild> nestedList, List<Comment> flatList, int level) {

        for (int i = 0, size = nestedList.size(); i < size; i++) {

            CommentResponse commentResponse = nestedList.get(i).data();

            Comment comment = formatCommentData(commentResponse, level);
            //check if its a normal comment or "load more comments" comment
            flatList.add(comment);
            if (commentResponse != null) {
                CommentListing commentListing = commentResponse.replies();
                if (commentListing != null && commentListing.commentData() != null) {
                    List<Comment> replyList = flattenHelper(commentListing.commentData().commentChildren(), flatList, level + 1);
                    comment.withReplies(replyList);
                }
            }

        }
        return flatList;
    }


    private static Function<PostDetails, Post> funcFormatPost = new Function<PostDetails, Post>() {
        @Override
        public Post apply(@NonNull PostDetails postDetails) throws Exception {
            String formatScore = TextHelper.formatScore(postDetails.score());

            CharSequence title = TextHelper.fromHtml(postDetails.title());

            // sometimes formatting title can result in empty if it has <----- at start
            // etc
            if (title != null &&
                    title.length() == 0) {
                title = String.valueOf(postDetails.title());
            }

            CharSequence selfText = null;
            if (postDetails.selftextHtml() != null) {
                selfText = formatTextToHtml(postDetails.selftextHtml());
            }

            CharSequence flair = "";
            if (postDetails.stickied()) {
                flair = TextHelper.fromHtml("Stickied");
            }
            if (postDetails.linkFlairText() != null) {
                flair = TextHelper.fromHtml(flair + " " + postDetails.linkFlairText());
            }
            String previewImageUrl;
            String thumbnail = postDetails.thumbnail();
            switch (thumbnail) {
                case "default":
                case "self":
                case "":
                case "spoiler":
                case "image":
                    previewImageUrl = "";
                    break;
                case "nsfw":
                    flair = TextHelper.fromHtml("<font color='#FF1744'>NSFW </font>" + flair);
                    previewImageUrl = "";
                    break;
                default:
                    previewImageUrl = DisplayHelper.getBestPreviewPicture(postDetails);
            }


        /*if (postDetails.isOver18()) {
            title = TextHelper.fromHtml("<font color='#FF1744'> NSFW </font>" + postDetails.title());
        }*/

            String numberOfComments = String.valueOf(postDetails.numberOfComments());

            return Post.builder()
                    .setId(postDetails.name())
                    .setThumbnail(thumbnail)
                    .setDomain(postDetails.domain())
                    .setUrl(postDetails.url())
                    .setScore(formatScore)
                    .setFlairText(flair)
                    .setSelfText(selfText)
                    .setIsSelf(postDetails.isSelf())
                    .setNumberOfComments(numberOfComments)
                    .setTitle(title)
                    .setPreviewImage(previewImageUrl)
                    .setPermaLink(postDetails.permalink())
                    .setAuthor(postDetails.author())
                    .setCreatedUtc(postDetails.createdUtc())
                    .build();
        }
    };

    public static Observable<Post> formatPost(Observable<PostResponse> postDetails) {
        return postDetails
                .map(PostResponse::postDetails)
                .map(funcFormatPost);

    }

    public static Observable<Post> formatPostDetails(Observable<PostDetails> postDetails) {
        return postDetails
                .map(funcFormatPost);

    }

    public static Observable<Subreddit> formatMultiToSubreddit(Observable<MultiredditListing> multireddit) {

        return multireddit.map(multiredditListing -> {
            MultiredditListing.Multireddit multireddit1 = multiredditListing.multireddit();
            return Subreddit.builder()
                    .setUrl(multireddit1.pathUrl())
                    .setBannerUrl(null)
                    .setId(multireddit1.name())
                    .setKeyColor(multireddit1.keyColor())
                    .setDisplayName(multireddit1.displayName())
                    .build();
        });

    }

    public static Observable<Subreddit> formatSubreddit(Observable<SubredditChildren> subredditChildren) {

        return subredditChildren
                .map(SubredditChildren::subreddit)
                .map(subredditResponse -> Subreddit.builder()
                        .setUrl(subredditResponse.url())
                        .setBannerUrl(subredditResponse.banner())
                        .setId(subredditResponse.id())
                        .setKeyColor(subredditResponse.keyColor())
                        .setDisplayName(subredditResponse.displayName())
                        .build());
    }

    public static Observable<SearchResult> formatSearchResult(Observable<SubredditChildren> observable) {
        return observable.map(subredditChildren -> {
            Observable<SubredditChildren> subredditChildObservable = Observable.<SubredditChildren>fromArray(subredditChildren);
            return Observable.zip(formatSubreddit(subredditChildObservable),
                    subredditChildObservable,
                    funcFormatSearchResult)
                    .blockingSingle();

        });
    }

    private static BiFunction<Subreddit, SubredditChildren, SearchResult> funcFormatSearchResult = (subreddit, subredditChildren) -> {
        SubredditResponse subredditResponse = subredditChildren.subreddit();

        CharSequence formattedTitle;
        String formattedSubscription;
        CharSequence formattedDescription;

        final String infoText = subredditResponse.subscribers() + " subscribers, Community since "
                + DateUtils.getRelativeTimeSpanString(subredditResponse.createdUtc() * 1000);

        if (subredditResponse.over18()) {
            formattedTitle = TextHelper.fromHtml(subredditResponse.url()
                    + "<font color='#FF1744'> NSFW</font>");
        } else {
            formattedTitle = subredditResponse.url();
        }

        if (subredditResponse.subscribed()) {
            formattedSubscription = "Subscribed";
        } else {
            formattedSubscription = "Not subscribed";
        }
        String descriptionHtml = subredditResponse.descriptionHtml();
        if (descriptionHtml != null && !descriptionHtml.isEmpty()) {
            formattedDescription = formatTextToHtml(descriptionHtml);
        } else {
            formattedDescription = "No description";
        }

        return SearchResult.builder()
                .setTitle(formattedTitle)
                .setDescription(formattedDescription)
                .setInfoText(infoText)
                .setSubscriptionInfo(formattedSubscription)
                .setSubreddit(subreddit)
                .build();
    };

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

    private static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1);
    }

    private static CharSequence formatTextToHtml(String bodyText) {
        if (bodyText == null) {
            return "";
        }
        Spanned htmlText = fromHtml(bodyText);
        return trimTrailingWhitespace(htmlText);
    }
}
