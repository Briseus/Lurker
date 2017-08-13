package torille.fi.lurkforreddit.comments

import android.graphics.Rect
import android.text.Spannable
import android.text.method.TransformationMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView

/**
 * Transforms links with the help of [CustomUrlSpan]
 */

internal class CustomLinkTransformationMethod : TransformationMethod {
    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        if (view is TextView) {
            val textView = view
            //LinkifyCompat.addLinks(textView, Linkify.WEB_URLS);
            if (textView.text == null || textView.text !is Spannable) {
                return source
            }
            val text = textView.text as Spannable
            val spans = text.getSpans(0, textView.length(), URLSpan::class.java)
            for (i in spans.indices.reversed()) {
                val oldSpan = spans[i]
                val start = text.getSpanStart(oldSpan)
                val end = text.getSpanEnd(oldSpan)
                val flags = text.getSpanFlags(oldSpan)
                val url = oldSpan.url
                text.removeSpan(oldSpan)
                text.setSpan(CustomUrlSpan(url), start, end, flags)
            }
            return text
        }
        return source
    }

    override fun onFocusChanged(view: View, sourceText: CharSequence, focused: Boolean, direction: Int, previouslyFocusedRect: Rect) {}
}