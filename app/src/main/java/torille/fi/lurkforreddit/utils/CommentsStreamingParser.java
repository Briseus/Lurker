package torille.fi.lurkforreddit.utils;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentResponse;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentData;
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentListing;
import torille.fi.lurkforreddit.data.models.jsonResponses.MyAdapterFactory;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails;
import torille.fi.lurkforreddit.data.models.jsonResponses.PostResponse;
import torille.fi.lurkforreddit.di.scope.RedditScope;

/**
 * Utility classes to stream parse {@link CommentListing} Json and nested models
 */
@RedditScope
public final class CommentsStreamingParser {

    private static Gson gson;

    @Inject
    public CommentsStreamingParser(@NonNull Gson gson) {
        CommentsStreamingParser.gson = gson;
    }

    /**
     * End point to read more comment api response
     *
     * @param reader needed for parsing
     * @return a list of {@link CommentChild}
     * @throws IOException
     */
    public static List<CommentChild> readMoreComments(JsonReader reader) throws IOException {

        List<CommentChild> comments = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("json")) {
                comments = readMore(reader, "data");
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return comments;
    }

    /**
     * Reads and parses more comments api response
     *
     * @param reader    used for parsing
     * @param searchFor used to travel the response
     * @return at the end returns a list of  {@link CommentChild}
     * @throws IOException
     */
    private static List<CommentChild> readMore(JsonReader reader, String searchFor) throws IOException {
        List<CommentChild> comments = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals(searchFor)) {
                switch (searchFor) {
                    case "json":
                        return readMore(reader, "data");
                    case "data":
                        return readMore(reader, "things");
                    case "things":
                        return readCommentChildren(reader);
                    default:
                        reader.skipValue();
                }
            } else {
                reader.skipValue();
            }

        }
        reader.endObject();
        return comments;
    }

    public List<CommentListing> readCommentListingArray(JsonReader reader) throws IOException {
        List<CommentListing> list = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            list.add(readCommentListing(reader));
        }
        reader.endArray();

        return list;
    }

    private static CommentListing readCommentListing(JsonReader reader) throws IOException {
        String kind = null;
        CommentData commentData = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("kind")) {
                kind = reader.nextString();

            } else if (name.equals("data") && reader.peek() != JsonToken.NULL) {
                commentData = readCommentData(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return CommentListing.create(kind, commentData);
    }

    private static CommentData readCommentData(JsonReader reader) throws IOException {
        List<CommentChild> commentChildren = new ArrayList<>();
        String after = null;
        String before = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("children") && reader.peek() != JsonToken.NULL) {
                commentChildren = readCommentChildren(reader);
            } else if (name.equals("after") && reader.peek() != JsonToken.NULL) {
                after = reader.nextString();
            } else if (name.equals("before") && reader.peek() != JsonToken.NULL) {
                before = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return CommentData.create(commentChildren, after, before);

    }

    private static List<CommentChild> readCommentChildren(JsonReader reader) throws IOException {

        List<CommentChild> commentChildren = new ArrayList<>();
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                commentChildren.add(readCommentChild(reader));
            }
            reader.endObject();
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                commentChildren.add(readCommentChild(reader));
            }
            reader.endArray();
        }
        return commentChildren;

    }

    private static CommentChild readCommentChild(JsonReader reader) throws IOException {
        String kind = null;
        CommentResponse comment = null;
        PostDetails postDetails = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "kind":
                    kind = reader.nextString();
                    break;
                case "data":
                    if (kind != null && kind.equals("t3")) {
                        postDetails = gson.fromJson(reader, PostDetails.class);
                    } else {
                        comment = readComment(reader);
                    }
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return CommentChild.create(kind, comment, postDetails);

    }

    private static CommentResponse readComment(JsonReader reader) throws IOException {
        String subredditId = null;
        String linkId = null;
        CommentListing replies = null;
        boolean saved = false;
        String id = null;
        int gilded = 0;
        boolean archived = false;
        String author = null;
        String parendId = null;
        int score = 0;
        int controversiality = 0;
        String bodyHtml = null;
        boolean stickied = false;
        String subreddit = null;
        boolean scoreHidden = false;
        String name = null;
        long createdUtc = 0;
        String authorFlairText = null;
        int ups = 0;
        int count = 0;
        List<String> childrenIds = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String jsonName = reader.nextName();
            switch (jsonName) {
                case "subreddit_id":
                    subredditId = reader.nextString();
                    break;
                case "link_id":
                    linkId = reader.nextString();
                    break;
                case "replies":
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        replies = readCommentListing(reader);
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "saved":
                    saved = reader.nextBoolean();
                    break;
                case "id":
                    id = reader.nextString();
                    break;
                case "gilded":
                    gilded = reader.nextInt();
                    break;
                case "archived":
                    archived = reader.nextBoolean();
                    break;
                case "author":
                    author = reader.nextString();
                    break;
                case "parent_id":
                    parendId = reader.nextString();
                    break;
                case "score":
                    score = reader.nextInt();
                    break;
                case "controversiality":
                    controversiality = reader.nextInt();
                    break;
                case "body_html":
                    bodyHtml = reader.nextString();
                    break;
                case "stickied":
                    stickied = reader.nextBoolean();
                    break;
                case "subreddit":
                    subreddit = reader.nextString();
                    break;
                case "score_hidden":
                    scoreHidden = reader.nextBoolean();
                    break;
                case "name":
                    name = reader.nextString();
                    break;
                case "created_utc":
                    createdUtc = reader.nextLong();
                    break;
                case "author_flair_text":
                    if (reader.peek() != JsonToken.NULL) {
                        authorFlairText = reader.nextString();
                    } else {
                        reader.skipValue();
                    }
                    break;
                case "ups":
                    ups = reader.nextInt();
                    break;
                case "count":
                    count = reader.nextInt();
                    break;
                case "children":
                    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                        childrenIds = readStringList(reader);
                        break;
                    } else {
                        reader.skipValue();
                        break;
                    }
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return CommentResponse.builder()
                .setLinkId(linkId)
                .setReplies(replies)
                .setSaved(saved)
                .setId(id)
                .setGilded(gilded)
                .setArchived(archived)
                .setAuthor(author)
                .setCount(count)
                .setParentId(parendId)
                .setScore(score)
                .setControversiality(controversiality)
                .setBodyHtml(bodyHtml)
                .setStickied(stickied)
                .setSubreddit(subreddit)
                .setScoreHidden(scoreHidden)
                .setName(name)
                .setAuthorFlairText(authorFlairText)
                .setCreatedUtc(createdUtc)
                .setUps(ups)
                .setChildren(childrenIds)
                .build();

    }

    private static List<String> readStringList(JsonReader reader) throws IOException {
        List<String> childrenIds = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            childrenIds.add(reader.nextString());
        }
        reader.endArray();
        return childrenIds;

    }
}
