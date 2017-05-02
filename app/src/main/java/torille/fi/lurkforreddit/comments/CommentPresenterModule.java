package torille.fi.lurkforreddit.comments;

import dagger.Module;
import dagger.Provides;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link CommentPresenter}.
 */
@Module
public class CommentPresenterModule {

    private final Post mPost;
    private final boolean mIsSingleCommentThread;

    public CommentPresenterModule(Post post, boolean isSingleCommentThread) {
        mPost = post;
        mIsSingleCommentThread = isSingleCommentThread;
    }

    @Provides
    CommentContract.Presenter<CommentContract.View> provideCommentContractPresenter(RedditRepository repository,
                                                                                    Post post,
                                                                                    boolean isSingleCommentThread) {
        return new CommentPresenter(repository, post, isSingleCommentThread);
    }

    @Provides
    Post provideCommentOriginalPost() {
        return mPost;
    }

    @Provides
    boolean provideIsSingleCommentThread() { return mIsSingleCommentThread;}
}
