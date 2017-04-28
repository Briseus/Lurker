package torille.fi.lurkforreddit.comments;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;

/**
 * Created by eva on 2/13/17.
 */

public class CommentPresenter implements CommentContract.Presenter<CommentContract.View> {

    private final RedditRepository mRedditRepository;

    private CommentContract.View mCommentsView;

    private final Post mPost;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    CommentPresenter(@NonNull RedditRepository redditRepository,
                     @NonNull Post clickedPost) {
        mRedditRepository = redditRepository;
        mPost = clickedPost;
    }

    @Override
    public void loadComments(@NonNull String permaLinkUrl) {
        mCommentsView.setProgressIndicator(true);
        disposables.add(mRedditRepository.getCommentsForPost(permaLinkUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<Comment>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Comment> comments) {
                        mCommentsView.showComments(comments);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Timber.e(e);
                        mCommentsView.showError(e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("Fetched comments");
                        mCommentsView.setProgressIndicator(false);
                    }
                }));
    }

    @Override
    public void loadMoreCommentsAt(Comment parentComment, String linkId, final int position) {
        final int level = parentComment.commentLevel();
        mCommentsView.showProgressbarAt(position, level);
        List<String> childCommentIds = parentComment.childCommentIds();
        if (childCommentIds != null) {
            disposables.add(mRedditRepository.getMoreCommentsForPostAt(childCommentIds, linkId, parentComment.commentLevel())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<Comment>>() {
                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull List<Comment> comments) {
                            mCommentsView.hideProgressbarAt(position);
                            mCommentsView.addCommentsAt(comments, position);
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            Timber.e(e);
                            mCommentsView.showErrorAt(position);
                        }

                        @Override
                        public void onComplete() {
                            Timber.d("Fetching more comments completed");
                        }
                    }));
        } else {
            mCommentsView.showError("How did you get here?");
        }
    }

    @Override
    public void setView(CommentContract.View view) {
        mCommentsView = view;
    }

    @Override
    public void start() {
        loadComments(mPost.permaLink());
    }

    @Override
    public void dispose() {
        disposables.dispose();
    }
}
