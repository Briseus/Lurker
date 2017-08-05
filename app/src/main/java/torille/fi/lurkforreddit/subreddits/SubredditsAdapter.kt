package torille.fi.lurkforreddit.subreddits

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.Subreddit
import java.util.ArrayList

/**
 * [RecyclerView.Adapter] for showing subreddits
 */
internal class SubredditsAdapter internal constructor(private val mItemListener: SubredditsFragment.SubredditItemListener, color: Int) : RecyclerView.Adapter<SubredditsAdapter.ViewHolder>() {

    private val mSubreddits: MutableList<Subreddit>
    private val mDefaultColor: ColorStateList = ColorStateList.valueOf(color)

    init {
        mSubreddits = ArrayList<Subreddit>(25)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val subredditsView = inflater.inflate(R.layout.item_subreddit, parent, false)
        return ViewHolder(subredditsView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val (_, _, displayName, _, keyColor) = mSubreddits[position]
        viewHolder.title.text = displayName
        if (keyColor.isNullOrEmpty()) {
            viewHolder.colorButton.backgroundTintList = mDefaultColor
        } else {
            viewHolder.colorButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(keyColor))
        }
    }

    internal fun replaceData(subreddits: List<Subreddit>) {
        mSubreddits.clear()
        mSubreddits.addAll(subreddits)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mSubreddits.size
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val title: TextView = itemView.findViewById<TextView>(R.id.item_subreddit_title)
        val colorButton: Button = itemView.findViewById<Button>(R.id.item_subreddit_circle)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            mItemListener.onSubredditClick(mSubreddits[adapterPosition])

        }
    }

}