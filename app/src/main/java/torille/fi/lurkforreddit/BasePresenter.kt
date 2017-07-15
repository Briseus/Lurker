package torille.fi.lurkforreddit

interface BasePresenter<in T : BaseView> {

    fun setView(view: T)

    fun start()

    fun dispose()
}
