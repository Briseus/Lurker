package torille.fi.lurkforreddit.subreddit;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Post;
import torille.fi.lurkforreddit.data.models.view.Subreddit;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;
import torille.fi.lurkforreddit.utils.MediaHelper;

/**
 * Created by eva on 2/11/17.
 */

public class SubredditPresenter implements SubredditContract.Presenter<SubredditContract.View> {

    private final RedditRepository mRedditRepository;

    private SubredditContract.View mSubredditsView;

    private final Subreddit mSubreddit;
    private String nextPageId;
    
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    SubredditPresenter(@NonNull RedditRepository redditRepository,
                       @NonNull Subreddit subreddit) {
        mRedditRepository = redditRepository;
        mSubreddit = subreddit;
    }

    @Override
    public void openComments(@NonNull Post clickedPost) {
        mSubredditsView.showCommentsUI(clickedPost);
    }

    @Override
    public void openCustomTabs(@NonNull String url) {
        mSubredditsView.showCustomTabsUI(url);
    }

    @Override
    public void openMedia(@NonNull Post post) {
        String domain = post.domain();
        String url = post.url();
        if (MediaHelper.isContentMedia(url) || MediaHelper.checkDomainForMedia(domain)) {
            mSubredditsView.showMedia(post);
        } else if (MediaHelper.launchCustomActivity(domain)) {
            mSubredditsView.launchCustomActivity(post);
        } else if (post.isSelf()) {
            openComments(post);
        } else {
            mSubredditsView.showCustomTabsUI(url);
        }
    }

    @Override
    public void loadPosts(@NonNull String subredditUrl) {
        mSubredditsView.setProgressIndicator(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice
        disposables.add(mRedditRepository.getSubredditPosts(subredditUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Pair<String, List<Post>>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Pair<String, List<Post>> posts) {
                        Timber.d("Got posts to presenter");
                        nextPageId = posts.first;
                        mSubredditsView.showPosts(posts.second, posts.first);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Timber.d("Got error");
                        mSubredditsView.onError(e.toString());
                        mSubredditsView.setProgressIndicator(false);
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("Completed");
                        EspressoIdlingResource.decrement();
                        mSubredditsView.setProgressIndicator(false);
                    }
                })
        );

    }

    @Override
    public void loadMorePosts(@NonNull String subredditUrl, @NonNull final String nextpage) {
        mSubredditsView.setListProgressIndicator(true);

        EspressoIdlingResource.increment();
        Timber.d("Fetching more posts at " + subredditUrl + " id " + nextpage);
        disposables.add(mRedditRepository.getMoreSubredditPosts(subredditUrl, nextpage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Pair<String, List<Post>>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Pair<String, List<Post>> postsPair) {
                        nextPageId = postsPair.first;
                        mSubredditsView.setListProgressIndicator(false);
                        mSubredditsView.addMorePosts(postsPair.second, postsPair.first);

                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        mSubredditsView.onError(e.toString());
                        mSubredditsView.setListErrorButton(true);
                    }

                    @Override
                    public void onComplete() {
                        EspressoIdlingResource.decrement();
                    }
                }));

    }

    @Override
    public void retry() {
        mSubredditsView.setListErrorButton(false);
        loadMorePosts(mSubreddit.url(), nextPageId);
    }

    @Override
    public void setView(SubredditContract.View view) {
        mSubredditsView = view;
    }


    @Override
    public void start() {
        loadPosts(mSubreddit.url());
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
