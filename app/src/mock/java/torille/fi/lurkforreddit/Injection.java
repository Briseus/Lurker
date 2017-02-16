package torille.fi.lurkforreddit;


import torille.fi.lurkforreddit.data.FakeRedditServiceApiImpl;
import torille.fi.lurkforreddit.data.RedditRepositories;
import torille.fi.lurkforreddit.data.RedditRepository;

/**
 * Enables injection of mock implementation
 * {@link torille.fi.lurkforreddit.data.RedditRepository} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {

    public static RedditRepository provideRedditRepository() {
        return RedditRepositories.getInMemoryRepoInstance(new FakeRedditServiceApiImpl());
    }

}
