package torille.fi.lurkforreddit;

/**
 * Created by eva on 24.4.2017.
 */

public interface BasePresenter<T extends BaseView> {

    void setView(T view);

    void start();
}
