package torille.fi.lurkforreddit.utils

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild
import torille.fi.lurkforreddit.data.models.jsonResponses.PostDetails
import torille.fi.lurkforreddit.data.models.jsonResponses.SubredditChildren
import torille.fi.lurkforreddit.data.models.view.*
import java.util.*

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

    fun flatten(list: List<CommentChild>, level: Int): Observable<List<Comment>> {
        val comments = ArrayList<Comment>(20)
        return Observable.fromArray(list)
                .subscribeOn(Schedulers.computation())
                .concatMapIterable { it }
                .concatMap { formatCommentData(Observable.fromArray(it), level) }
                .collectInto(comments) { _, newComments ->
                    comments.addAll(newComments)
                }.flattenAsObservable { listOf(it) }

    }

    fun flattenAdditionalComments(list: List<CommentChild>, level: Int): List<Comment> {

        val additionalComments: MutableList<Comment> = flatten(list, level).blockingSingle().toMutableList()

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

    private fun formatCommentData(commentChildObservable: Observable<CommentChild>, level: Int): Observable<List<Comment>> {
        return commentChildObservable
                .map { it.data!! }
                .map {
                    var commentText: CharSequence = ""
                    var author: CharSequence = ""
                    var kind: kind = kind.MORE

                    when {
                        it.bodyHtml.isNotEmpty() -> {
                            kind = torille.fi.lurkforreddit.data.models.view.kind.DEFAULT
                            commentText = formatTextToHtml(it.bodyHtml)
                        }
                        it.id == "_" -> commentText = "Continue this thread"
                        it.children.isNotEmpty() -> commentText = "Load more comments (" + it.count + ")"
                    }

                    val responseAuthor = it.author
                    if (responseAuthor.isNotEmpty() && it.stickied) {
                        author = "<font color='#64FFDA'> Sticky post </font>" + responseAuthor
                    } else if (responseAuthor.isNotEmpty()) {
                        author = responseAuthor
                    }

                    author = fromHtml(author.toString() + " " + DateUtils.getRelativeTimeSpanString(it.createdUtc * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))

                    if (it.edited) {
                        author = author.toString() + " (edited)"
                    }

                    val formatScore = formatScore(it.score)

                    val formattedComment = Comment(
                            id = it.id,
                            parentId = it.parentId,
                            name = it.name,
                            kind = kind,
                            author = author,
                            childCommentIds = it.children,
                            commentLevel = level,
                            commentLinkId = it.linkId,
                            commentText = commentText,
                            formattedScore = formatScore,
                            formattedTime = "",
                            replies = null
                    )

                    if (it.replies != null) {
                        val commentChildList = it.replies.commentData.commentChildren
                        val commentList = ArrayList<Comment>(commentChildList.size)
                        commentList.add(formattedComment)

                        Observable.fromArray(commentChildList)
                                .map { commentChildren -> Observable.fromIterable(commentChildren) }
                                .concatMap { commentChildObservable2 -> formatCommentData(commentChildObservable2, level + 1) }
                                .collectInto<List<Comment>>(commentList) { _, newComments -> commentList.addAll(newComments) }
                                .blockingGet()
                    } else {
                        listOf(formattedComment)
                    }
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


    fun formatSubreddit(subredditChildren: Observable<SubredditChildren>): Observable<Subreddit> {

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
                                id = subredditResponse.id!!,
                                keyColor = subredditResponse.keyColor,
                                displayName = subredditResponse.displayName
                        )
                    }

                }
    }

    fun formatSearchResult(observable: Observable<SubredditChildren>): Observable<SearchResult> {
        return observable.flatMap { subredditChildren ->
            val subredditChildObservable = Observable.fromArray(subredditChildren)
            Observable.zip<Subreddit, SubredditChildren, SearchResult>(
                    formatSubreddit(subredditChildObservable),
                    subredditChildObservable,
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
