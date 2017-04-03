package torille.fi.lurkforreddit.comments;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import torille.fi.lurkforreddit.data.models.CommentChild;

/**
 * Item decoration for {@link CommentChild} in {@link torille.fi.lurkforreddit.comments.CommentFragment.CommentRecyclerViewAdapter}
 * that adds padding to left depending on how deep the level of the comment is and also draws a line to the left side
 * of the comment
 */

class CommentsItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable mDivider;

    CommentsItemDecoration(Drawable divider) {
        this.mDivider = divider;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {

        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            if (parent.getChildViewHolder(child) instanceof CommentFragment.CommentRecyclerViewAdapter.CommentViewHolder) {
                draw(((CommentFragment.CommentRecyclerViewAdapter.CommentViewHolder) parent.getChildViewHolder(child)).mComment, child, canvas);
            } else if (parent.getChildViewHolder(child) instanceof CommentFragment.CommentRecyclerViewAdapter.CommentLoadMoreViewHolder) {
                draw(((CommentFragment.CommentRecyclerViewAdapter.CommentLoadMoreViewHolder) parent.getChildViewHolder(child)).mComment, child, canvas);
            }
        }

    }

    private void draw(CommentChild mItem, View child, Canvas canvas) {
        if (mItem != null && (mItem.getType() > 0)) {
            final int left = child.getLeft();
            final int right = left + mDivider.getIntrinsicHeight();
            final int top = child.getTop();
            final int bottom = child.getBottom();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect,
                               View view,
                               RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        if (parent.getChildViewHolder(view) instanceof CommentFragment.CommentRecyclerViewAdapter.CommentViewHolder) {
            setPadding(((CommentFragment.CommentRecyclerViewAdapter.CommentViewHolder) parent.getChildViewHolder(view)).mComment, outRect);

        } else if (parent.getChildViewHolder(view) instanceof CommentFragment.CommentRecyclerViewAdapter.CommentLoadMoreViewHolder) {
            setPadding(((CommentFragment.CommentRecyclerViewAdapter.CommentLoadMoreViewHolder) parent.getChildViewHolder(view)).mComment, outRect);

        } else if (parent.getChildViewHolder(view) instanceof CommentFragment.CommentRecyclerViewAdapter.ProgressViewHolder) {
            setPadding(((CommentFragment.CommentRecyclerViewAdapter.ProgressViewHolder) parent.getChildViewHolder(view)).mComment, outRect);

        }

    }

    private static void setPadding(CommentChild mComment, Rect outRect) {
        if (mComment != null) {
            outRect.left = (mComment.getType() * 16);
        }
    }

}
