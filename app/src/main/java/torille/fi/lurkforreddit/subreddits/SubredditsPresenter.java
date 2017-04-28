package torille.fi.lurkforreddit.subreddits;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;

/**
 * Created by eva on 2/8/17.
 */

public class SubredditsPresenter implements SubredditsContract.Presenter<SubredditsContract.View> {

    private final RedditRepository mRedditRepository;
    private SubredditsContract.View mSubredditsView;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SubredditsPresenter(@NonNull RedditRepository redditRepository) {
        mRedditRepository = redditRepository;
    }

    @Override
    public void loadSubreddits(boolean forceUpdate) {
        Timber.d("Going to fetch subs!");
        mSubredditsView.setProgressIndicator(true);
        if (forceUpdate) {
            mRedditRepository.refreshData();
        }
        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice
        disposables.add(mRedditRepository.getSubreddits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Subreddit>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Subreddit> subreddits) {
                        mSubredditsView.showSubreddits(subreddits);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        mSubredditsView.onError(e.toString());
                        mSubredditsView.setProgressIndicator(false);
                    }

                    @Override
                    public void onComplete() {
                        EspressoIdlingResource.decrement(); // Set app as idle.
                        mSubredditsView.setProgressIndicator(false);
                    }
                }));
    }

    @Override
    public void openSubreddit(Subreddit subreddit) {
        mSubredditsView.loadSelectedSubreddit(subreddit);
    }

    @Override
    public void setView(SubredditsContract.View view) {
        mSubredditsView = view;
    }

    @Override
    public void start() {
        loadSubreddits(false);
    }

    @Override
    public void dispose() {
        mSubredditsView.setProgressIndicator(false);
        disposables.dispose();
    }
}
