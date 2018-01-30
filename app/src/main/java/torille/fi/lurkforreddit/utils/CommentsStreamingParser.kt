package torille.fi.lurkforreddit.utils

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import torille.fi.lurkforreddit.data.models.jsonResponses.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility classes to stream parse [CommentListing] Json and nested models
 */
@Singleton
class CommentsStreamingParser @Inject
constructor(val gson: Gson) {

    /**
     * End point to read more comment api response

     * @param reader needed for parsing
     * *
     * @return a list of [CommentChild]
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun readMoreComments(reader: JsonReader): List<CommentChild> {
        var comments: List<CommentChild> = emptyList()

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "json") {
                comments = readMore(reader, "data")
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        return comments
    }

    @Throws(IOException::class)
    fun readCommentListingArray(reader: JsonReader): List<CommentListing> {
        var list: List<CommentListing> = emptyList()

        reader.beginArray()
        while (reader.hasNext()) {
            val listing = readCommentListing(reader)
            list += listing
        }
        reader.endArray()

        return list
    }

    /**
     * Reads and parses more comments api response

     * @param reader    used for parsing
     * *
     * @param searchFor used to travel the response
     * *
     * @return at the end returns a list of  [CommentChild]
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun readMore(reader: JsonReader, searchFor: String): List<CommentChild> {
        val comments: List<CommentChild> = emptyList()

        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == searchFor) {
                when (searchFor) {
                    "json" -> return readMore(reader, "data")
                    "data" -> return readMore(reader, "things")
                    "things" -> return readCommentChildren(reader)
                    else -> reader.skipValue()
                }
            } else {
                reader.skipValue()
            }

        }
        reader.endObject()

        return comments
    }

    @Throws(IOException::class)
    private fun readCommentListing(reader: JsonReader): CommentListing {
        var kind = ""
        var commentData: CommentData = CommentData(emptyList(), "", "")

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "kind") {
                kind = reader.nextString()
            } else if (name == "data" && reader.peek() != JsonToken.NULL) {
                commentData = readCommentData(reader)
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        return CommentListing(kind = kind, commentData = commentData)
    }

    @Throws(IOException::class)
    private fun readCommentData(reader: JsonReader): CommentData {
        var commentChildren: List<CommentChild> = emptyList()
        var after = ""
        var before = ""

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "children" && reader.peek() != JsonToken.NULL) {
                commentChildren += readCommentChildren(reader)
            } else if (name == "after" && reader.peek() != JsonToken.NULL) {
                after = reader.nextString()
            } else if (name == "before" && reader.peek() != JsonToken.NULL) {
                before = reader.nextString()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        return CommentData(commentChildren, after, before)
    }

    @Throws(IOException::class)
    private fun readCommentChildren(reader: JsonReader): List<CommentChild> {
        var commentChildren: List<CommentChild> = emptyList()

        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject()
            while (reader.hasNext()) {
                commentChildren += readCommentChild(reader)
            }
            reader.endObject()
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray()
            while (reader.hasNext()) {
                commentChildren += readCommentChild(reader)
            }
            reader.endArray()
        }

        return commentChildren
    }

    @Throws(IOException::class)
    private fun readCommentChild(reader: JsonReader): CommentChild {
        var kind = ""
        var postDetails: PostDetails? = null
        var comment = CommentResponse()

        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                "kind" -> kind = reader.nextString()
                "data" ->
                    if (kind == "t3") {
                        postDetails = gson.fromJson(reader, PostDetails::class.java)
                    } else {
                        comment = readComment(reader)
                    }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return CommentChild(kind, comment, postDetails)
    }

    @Throws(IOException::class)
    private fun readComment(reader: JsonReader): CommentResponse {
        var subredditId = ""
        var linkId = ""
        var replies: CommentListing = CommentListing()
        var saved = false
        var id = ""
        var gilded = 0
        var archived = false
        var author = ""
        var parendId = ""
        var score = 0
        var controversiality = 0
        var bodyHtml = ""
        var stickied = false
        var subreddit = ""
        var scoreHidden = false
        var edited = false
        var name = ""
        var createdUtc: Long = 0
        var authorFlairText = ""
        var ups = 0
        var count = 0
        var childrenIds: List<String> = emptyList()

        reader.beginObject()
        while (reader.hasNext()) {
            val jsonName = reader.nextName()
            when (jsonName) {
                "subreddit_id" -> subredditId = reader.nextString()
                "link_id" -> linkId = reader.nextString()
                "replies" ->
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        replies = readCommentListing(reader)
                    } else {
                        reader.skipValue()
                    }
                "saved" -> saved = reader.nextBoolean()
                "subId" -> id = reader.nextString()
                "gilded" -> gilded = reader.nextInt()
                "archived" -> archived = reader.nextBoolean()
                "author" -> author = reader.nextString()
                "parent_id" -> parendId = reader.nextString()
                "score" -> score = reader.nextInt()
                "controversiality" -> controversiality = reader.nextInt()
                "body_html" -> bodyHtml = reader.nextString()
                "stickied" -> stickied = reader.nextBoolean()
                "subreddit" -> subreddit = reader.nextString()
                "score_hidden" -> scoreHidden = reader.nextBoolean()
                "edited" ->
                    if (reader.peek() == JsonToken.NUMBER) {
                        edited = true
                        reader.skipValue()
                    } else {
                        edited = reader.nextBoolean()
                    }
                "name" -> name = reader.nextString()
                "created_utc" -> createdUtc = reader.nextLong()
                "author_flair_text" ->
                    if (reader.peek() != JsonToken.NULL) {
                        authorFlairText = reader.nextString()
                    } else {
                        reader.skipValue()
                    }
                "ups" -> ups = reader.nextInt()
                "count" -> count = reader.nextInt()
                "children" -> {
                    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                        childrenIds += readStringList(reader)
                    } else {
                        reader.skipValue()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return CommentResponse(
            linkId = linkId,
            replies = replies,
            saved = saved,
            id = id,
            gilded = gilded,
            archived = archived,
            author = author,
            count = count,
            parentId = parendId,
            score = score,
            controversiality = controversiality,
            bodyHtml = bodyHtml,
            stickied = stickied,
            subreddit = subreddit,
            scoreHidden = scoreHidden,
            edited = edited,
            name = name,
            authorFlairText = authorFlairText,
            createdUtc = createdUtc,
            ups = ups,
            children = childrenIds,
            subredditId = subredditId
        )
    }

    @Throws(IOException::class)
    private fun readStringList(reader: JsonReader): List<String> {
        var childrenIds: List<String> = emptyList()

        reader.beginArray()
        while (reader.hasNext()) {
            childrenIds += (reader.nextString())
        }
        reader.endArray()

        return childrenIds
    }

}
