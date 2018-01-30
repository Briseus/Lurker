package torille.fi.lurkforreddit.search

import torille.fi.lurkforreddit.BasePresenter
import torille.fi.lurkforreddit.BaseView
import torille.fi.lurkforreddit.data.models.view.SearchResult

interface SearchContract {

    interface View : BaseView<Presenter> {

        fun showResults(results: List<SearchResult>)

        fun showProgressbar()

        fun clearResults()

        fun showError(errorText: String)

    }

    interface Presenter : BasePresenter<View> {

        fun searchSubreddits(query: String)

        fun searchMoreSubreddits()

    }

}
