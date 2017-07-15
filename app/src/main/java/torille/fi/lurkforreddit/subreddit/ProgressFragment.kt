package torille.fi.lurkforreddit.subreddit


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import torille.fi.lurkforreddit.R

class ProgressFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_progressbar, container, false)
    }

    companion object {

        fun newInstance(): ProgressFragment {
            val fragment = ProgressFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
