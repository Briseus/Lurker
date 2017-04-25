package torille.fi.lurkforreddit.subreddit;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditDataSource;
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

        mRedditRepository.getSubredditPosts(subredditUrl,
                new RedditDataSource.LoadSubredditPostsCallback() {
                    @Override
                    public void onPostsLoaded(List<Post> posts, String nextpage) {
                        EspressoIdlingResource.decrement(); // Set app as idle.
                        mSubredditsView.setProgressIndicator(false);
                        mSubredditsView.showPosts(posts, nextpage);
                    }
                }, new RedditDataSource.ErrorCallback() {
                    @Override
                    public void onError(String errorText) {
                        mSubredditsView.setProgressIndicator(false);
                        mSubredditsView.onError(errorText);
                    }
                });

    }

    @Override
    public void loadMorePosts(@NonNull String subredditUrl, @NonNull String nextpage) {
        mSubredditsView.setListProgressIndicator(true);

        EspressoIdlingResource.increment();
        Timber.d("Fetching more posts at " + subredditUrl + " id " + nextpage);
        mRedditRepository.getMoreSubredditPosts(subredditUrl,
                nextpage,
                new RedditDataSource.LoadSubredditPostsCallback() {
                    @Override
                    public void onPostsLoaded(List<Post> posts, String nextpage) {
                        EspressoIdlingResource.decrement();
                        mSubredditsView.setListProgressIndicator(false);
                        mSubredditsView.addMorePosts(posts, nextpage);
                    }

                }, new RedditDataSource.ErrorCallback() {
                    @Override
                    public void onError(String errorText) {
                        mSubredditsView.setListProgressIndicator(false);
                        mSubredditsView.onError(errorText);
                    }
                });
    }

    @Override
    public void setView(SubredditContract.View view) {
        mSubredditsView = view;
    }


    @Override
    public void start() {
        loadPosts(mSubreddit.url());
    }
}
