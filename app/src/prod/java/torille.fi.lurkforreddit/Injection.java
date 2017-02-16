
package torille.fi.lurkforreddit;

import torille.fi.lurkforreddit.data.RedditRepositories;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.RedditServiceApiImpl;

/**
 * Enables injection of production implementations for
 * {@link RedditRepository} at compile time.
 */
public class Injection {

    public static RedditRepository provideRedditRepository() {
        return RedditRepositories.getInMemoryRepoInstance(new RedditServiceApiImpl());
    }
}
