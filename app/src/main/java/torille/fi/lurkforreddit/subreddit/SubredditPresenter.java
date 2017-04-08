package torille.fi.lurkforreddit.subreddit;

import android.support.annotation.NonNull;

import java.util.List;

import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.utils.EspressoIdlingResource;
import torille.fi.lurkforreddit.utils.MediaHelper;

/**
 * Created by eva on 2/11/17.
 */

public class SubredditPresenter implements SubredditContract.UserActionsListener {

    private final RedditRepository mRedditRepository;

    private final SubredditContract.View mSubredditsView;

    SubredditPresenter(@NonNull RedditRepository redditRepository,
                              @NonNull SubredditContract.View subredditView) {
        mRedditRepository = redditRepository;
        mSubredditsView = subredditView;
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
        if (MediaHelper.isContentMedia(post.getPostDetails().getUrl()) || MediaHelper.checkDomainForMedia(post.getPostDetails().getDomain())) {
            mSubredditsView.showMedia(post);
        } else if (MediaHelper.launchCustomActivity(post)) {
            mSubredditsView.launchCustomActivity(post);
        } else if (post.getPostDetails().isSelf) {
            openComments(post);
        } else {
            mSubredditsView.showCustomTabsUI(post.getPostDetails().getUrl());
        }
    }

    @Override
    public void loadPosts(@NonNull String subredditUrl) {
        mSubredditsView.setProgressIndicator(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the appm is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mRedditRepository.getSubredditPosts(subredditUrl,
                new RedditRepository.LoadSubredditPostsCallback() {
                    @Override
                    public void onPostsLoaded(List<Post> posts, String nextpage) {
                        EspressoIdlingResource.decrement(); // Set app as idle.
                        mSubredditsView.setProgressIndicator(false);
                        mSubredditsView.showPosts(posts, nextpage);
                    }
                }, new RedditRepository.ErrorCallback() {
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

        mRedditRepository.getMoreSubredditPosts(subredditUrl,
                nextpage,
                new RedditRepository.LoadSubredditPostsCallback() {
                    @Override
                    public void onPostsLoaded(List<Post> posts, String nextpage) {
                        EspressoIdlingResource.decrement();
                        mSubredditsView.setListProgressIndicator(false);
                        mSubredditsView.addMorePosts(posts, nextpage);
                    }

                }, new RedditRepository.ErrorCallback() {
                    @Override
                    public void onError(String errorText) {
                        mSubredditsView.setListProgressIndicator(false);
                        mSubredditsView.onError(errorText);
                    }
                });
    }
}
