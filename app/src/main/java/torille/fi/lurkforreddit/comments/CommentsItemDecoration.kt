package torille.fi.lurkforreddit.comments

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View

import torille.fi.lurkforreddit.data.models.jsonResponses.CommentChild
import torille.fi.lurkforreddit.data.models.view.Comment

/**
 * Item decoration for [CommentChild] in [torille.fi.lurkforreddit.comments.CommentFragment.CommentRecyclerViewAdapter]
 * that adds padding to left depending on how deep the level of the comment is and also draws a line to the left side
 * of the comment
 */

internal class CommentsItemDecoration(private val mDivider: Drawable) :
    RecyclerView.ItemDecoration() {

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {

        (0 until parent.childCount)
            .map { parent.getChildAt(it) }
            .forEach {
                if (parent.getChildViewHolder(it) is CommentRecyclerViewAdapter.CommentViewHolder) {
                    draw(
                        (parent.getChildViewHolder(it) as CommentRecyclerViewAdapter.CommentViewHolder).comment!!,
                        it,
                        canvas
                    )
                } else if (parent.getChildViewHolder(it) is CommentRecyclerViewAdapter.CommentLoadMoreViewHolder) {
                    draw(
                        (parent.getChildViewHolder(it) as CommentRecyclerViewAdapter.CommentLoadMoreViewHolder).comment!!,
                        it,
                        canvas
                    )
                }
            }

    }

    private fun draw(mItem: Comment, child: View, canvas: Canvas) {
        if (mItem.commentLevel > 0) {
            val left = child.left
            val right = left + mDivider.intrinsicHeight
            val top = child.top
            val bottom = child.bottom
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(canvas)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State?
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        if (parent.getChildAdapterPosition(view) == 0) {
            return
        }

        when {
            parent.getChildViewHolder(view) is CommentRecyclerViewAdapter.CommentViewHolder -> setPadding(
                (parent.getChildViewHolder(view) as CommentRecyclerViewAdapter.CommentViewHolder).comment!!,
                outRect
            )
            parent.getChildViewHolder(view) is CommentRecyclerViewAdapter.CommentLoadMoreViewHolder -> setPadding(
                (parent.getChildViewHolder(view) as CommentRecyclerViewAdapter.CommentLoadMoreViewHolder).comment!!,
                outRect
            )
            parent.getChildViewHolder(view) is CommentRecyclerViewAdapter.ProgressViewHolder -> setPadding(
                (parent.getChildViewHolder(view) as CommentRecyclerViewAdapter.ProgressViewHolder).comment!!,
                outRect
            )
        }

    }

    private fun setPadding(mComment: Comment, outRect: Rect) {
        outRect.left = mComment.commentLevel * 16
    }

}
