package torille.fi.lurkforreddit.data;

import android.support.annotation.NonNull;

/**
 * Created by eva on 2/8/17.
 */

public class RedditRepositories {

    private RedditRepositories() {

    }

    private static RedditRepository repository = null;

    public synchronized static RedditRepository getInMemoryRepoInstance(@NonNull RedditServiceApi redditServiceApi) {

        if (null == repository) {
            repository = new InMemoryRedditRepository(redditServiceApi);
        }
        return repository;
    }
}
