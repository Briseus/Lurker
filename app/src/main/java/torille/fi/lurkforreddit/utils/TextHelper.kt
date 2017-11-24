package torille.fi.lurkforreddit.utils

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import io.reactivex.Flowable
import io.reactivex.functions.Function
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentResponse
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren
import torille.fi.lurkforreddit.data.models.view.*

/**
 * Contains utility classes for parsing text
 */

object TextHelper {

    fun getLastFourChars(url: String): String {
        return url.substring(url.length - 4)
    }

    private fun fromHtml(html: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    private fun formatScore(score: Int): String {
        val value = score.toString()
        return when {
            score < 1000 -> value
            score < 10000 -> value[0] + "." + value[1] + "k"
            score < 100000 -> value[0] + "" + value[1] + "k"
            score < 10000000 -> value[0] + "" + value[1] + "" + value[2] + "k"
            else -> value
        }
    }

    fun flatten(list: List<CommentChild>, commentDepth: Int): List<Comment> {
        return list.flatMap { formatCommentData(it.data, commentDepth) }
    }

    fun flattenAdditionalComments(list: List<CommentChild>, level: Int): List<Comment> {

        val additionalComments = flatten(list, level).toMutableList()

        var i = 0
        val size = additionalComments.size
        while (i < size) {
            var j = 0
            val nestedSize = additionalComments.size
            while (j < nestedSize) {
                if (additionalComments[i].name == additionalComments[j].parentId) {
                    additionalComments[j] = additionalComments[j].copy(commentLevel = level + 1)
                }
                j++
            }
            i++
        }

        return additionalComments.toList()
    }

    private fun formatCommentData(commentData: CommentResponse?, commentDepth: Int): List<Comment> {

        return if (commentData != null) {
            var commentText: CharSequence = ""
            var author: CharSequence = ""
            var kind: kind = kind.MORE

            when {
                commentData.bodyHtml.isNotEmpty() -> {
                    kind = torille.fi.lurkforreddit.data.models.view.kind.DEFAULT
                    commentText = formatTextToHtml(commentData.bodyHtml)
                }
                commentData.id == "_" -> commentText = "Continue this thread"
                commentData.children.isNotEmpty() -> commentText = "Load more comments (" + commentData.count + ")"
            }

            val responseAuthor = commentData.author
            if (responseAuthor.isNotEmpty() && commentData.stickied) {
                author = "<font color='#64FFDA'> Sticky post </font>" + responseAuthor
            } else if (responseAuthor.isNotEmpty()) {
                author = responseAuthor
            }

            author = fromHtml(author.toString() + " " + DateUtils.getRelativeTimeSpanString(commentData.createdUtc * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))

            if (commentData.edited) {
                author = author.toString() + " (edited)"
            }

            val formatScore = formatScore(commentData.score)

            val formattedComment = Comment(
                    id = commentData.id,
                    parentId = commentData.parentId,
                    name = commentData.name,
                    kind = kind,
                    author = author,
                    childCommentIds = commentData.children,
                    commentLevel = commentDepth,
                    commentLinkId = commentData.linkId,
                    commentText = commentText,
                    formattedScore = formatScore,
                    formattedTime = "",
                    replies = null
            )

            val listWithOriginalComment = listOf(formattedComment)

            return if (commentData.replies != null) {
                val replies = commentData.replies.commentData.commentChildren
                        .flatMap { formatCommentData(it.data, commentDepth + 1) }

                listWithOriginalComment.plus(replies)
            } else {
                listWithOriginalComment
            }
        } else {
            emptyList()
        }


    }

    val funcFormatPost = Function { postDetails: PostDetails ->
        val formatScore = TextHelper.formatScore(postDetails.score)
        var selfText: CharSequence = ""
        var previewImageUrl = ""
        val thumbnail = postDetails.thumbnail
        var title: CharSequence = TextHelper.fromHtml(postDetails.title)
        var flair: Spanned = SpannableStringBuilder.valueOf("")

        // sometimes formatting title can result in empty if it has <----- at start
        // etc
        if (title.isEmpty()) {
            title = postDetails.title
        }

        if (postDetails.selftextHtml != null) {
            selfText = formatTextToHtml(postDetails.selftextHtml)
        }

        if (postDetails.stickied) {
            flair = TextHelper.fromHtml("Stickied")
        }
        if (postDetails.linkFlairText != null) {
            flair = TextHelper.fromHtml(flair.toString() + " " + postDetails.linkFlairText)
        }

        when (thumbnail) {
        //"default", "self", "", "spoiler", "image" -> previewImageUrl = ""
            "nsfw" -> {
                flair = TextHelper.fromHtml("<font color='#FF1744'>NSFW </font>" + flair)
            }
            else -> previewImageUrl = DisplayHelper.getBestPreviewPicture(postDetails)
        }
        val dashUrl = postDetails.media?.redditVideo?.dashUrl
        val fallbackUrl = postDetails.media?.redditVideo?.fallbackUrl
        val url: String = if (!dashUrl.isNullOrEmpty() && postDetails.media?.redditVideo?.transcodingStatus == "completed") {
            dashUrl!!
        } else if (!fallbackUrl.isNullOrEmpty()) {
            fallbackUrl!!
        } else {
            postDetails.url
        }
        /*if (postDetails.isOver18()) {
        title = TextHelper.fromHtml("<font color='#FF1744'> NSFW </font>" + postDetails.title());
        }*/

        val numberOfComments = postDetails.numberOfComments
        Post(
                id = postDetails.name,
                thumbnail = thumbnail,
                domain = postDetails.domain,
                url = url,
                score = formatScore,
                flairText = flair,
                selfText = selfText,
                isSelf = postDetails.isSelf,
                numberOfComments = numberOfComments.toString(),
                title = title,
                previewImage = previewImageUrl,
                permaLink = postDetails.permalink,
                author = postDetails.author,
                createdUtc = postDetails.createdUtc
        )
    }


    fun formatSubreddit(subredditChildren: Flowable<SubredditChildren>): Flowable<Subreddit> {

        return subredditChildren
                .map { it.subreddit }
                .map { subredditResponse ->
                    // TODO("If subreddit isnt found return something else than empty")
                    if (subredditResponse.id.isNullOrBlank()) {
                        Subreddit()
                    } else {
                        Subreddit(
                                url = subredditResponse.url,
                                bannerUrl = subredditResponse.banner,
                                subId = subredditResponse.id!!,
                                keyColor = subredditResponse.keyColor,
                                displayName = subredditResponse.displayName
                        )
                    }

                }
    }

    fun formatSearchResult(flowable: Flowable<SubredditChildren>): Flowable<SearchResult> {
        return flowable.flatMap { subredditChildren ->
            val subredditChildFlowable = Flowable.fromArray(subredditChildren)
            Flowable.zip<Subreddit, SubredditChildren, SearchResult>(
                    formatSubreddit(subredditChildFlowable),
                    subredditChildFlowable,
                    io.reactivex.functions.BiFunction({ subreddit: Subreddit, subredditChild: SubredditChildren ->

                        val subredditResponse = subredditChild.subreddit
                        val descriptionHtml = subredditResponse.descriptionHtml

                        val formattedTitle: CharSequence
                        val formattedSubscription: String
                        val formattedDescription: CharSequence

                        val infoText = subredditResponse.subscribers.toString() + " subscribers, Community since " + DateUtils.getRelativeTimeSpanString(subredditResponse.createdUtc * 1000)

                        formattedTitle = if (subredditResponse.over18) {
                            TextHelper.fromHtml(subredditResponse.url + "<font color='#FF1744'> NSFW</font>")
                        } else {
                            subredditResponse.url
                        }

                        formattedSubscription = if (subredditResponse.subscribed) {
                            "Subscribed"
                        } else {
                            "Not subscribed"
                        }

                        formattedDescription = if (!descriptionHtml.isNullOrEmpty()) {
                            formatTextToHtml(descriptionHtml)
                        } else {
                            "No description"
                        }

                        SearchResult(
                                title = formattedTitle,
                                description = formattedDescription,
                                infoText = infoText,
                                subscriptionInfo = formattedSubscription,
                                subreddit = subreddit
                        )
                    })
            )

        }
    }

    private fun formatTextToHtml(bodyText: String?): CharSequence {
        if (bodyText == null) {
            return ""
        }
        val htmlText = fromHtml(bodyText)
        return htmlText.trim()
    }
}
