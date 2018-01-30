package torille.fi.lurkforreddit.search

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import torille.fi.lurkforreddit.R
import torille.fi.lurkforreddit.data.models.view.SearchResult

internal class SearchViewAdapter internal constructor(private val mClickListener: SearchFragment.SearchClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val results: SortedList<SearchResult>

    init {
        results = SortedList(
            SearchResult::class.java,
            object : SortedListAdapterCallback<SearchResult>(this) {
                override fun compare(o1: SearchResult, o2: SearchResult): Int {
                    if (o1.title == o2.title) {
                        return 0
                    }
                    return -1
                }

                override fun areContentsTheSame(
                    oldItem: SearchResult,
                    newItem: SearchResult
                ): Boolean {
                    return oldItem == newItem
                }

                override fun areItemsTheSame(item1: SearchResult, item2: SearchResult): Boolean {
                    return item1.title == item2.title
                }
            })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_ITEM -> SearchViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search, parent, false)
            )
            else -> ProgressViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_progressbar, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SearchViewHolder) {
            holder.bind(results.get(position))
        } else if (holder is ProgressViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (results.get(position).title == "Progressbar") {
            VIEW_PROGRESS
        } else {
            VIEW_ITEM
        }
    }

    override fun getItemCount(): Int {
        return results.size()
    }

    internal fun addResults(newResults: List<SearchResult>) {
        val position = results.size() - 1
        results.removeItemAt(position)
        addAll(newResults)
    }

    private fun addAll(newResults: List<SearchResult>) {
        results.beginBatchedUpdates()
        newResults.map { results.add(it) }
        results.endBatchedUpdates()
    }

    internal fun addProgressBar() {
        val dummy = SearchResult(title = "Progressbar")
        addAll(listOf(dummy))

    }

    internal fun clear() {
        results.clear()
    }

    internal inner class SearchViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.subreddit_title)
        private val infoText: TextView = v.findViewById(R.id.subreddits_infotext)
        private val description: TextView = v.findViewById(R.id.subreddit_description)
        private val subscribe: Button = v.findViewById(R.id.subreddit_subscribe)

        init {
            description.movementMethod = LinkMovementMethod.getInstance()
            description.linksClickable = true
            v.setOnClickListener { mClickListener.onSearchClick(results.get(adapterPosition).subreddit) }
        }

        fun bind(result: SearchResult) {
            title.text = result.title
            description.text = result.description
            subscribe.text = result.subscriptionInfo
            infoText.text = result.infoText
        }
    }

    private class ProgressViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        internal val progressBar: ProgressBar = v.findViewById(R.id.progressBar)
    }

    companion object {
        private val VIEW_ITEM = 1
        private val VIEW_PROGRESS = 0
    }
}