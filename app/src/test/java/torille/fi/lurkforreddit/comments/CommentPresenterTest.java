package torille.fi.lurkforreddit.comments;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.RedditDataSource;
import torille.fi.lurkforreddit.data.models.view.Comment;
import torille.fi.lurkforreddit.data.models.view.Post;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link CommentPresenter}
 */
public class CommentPresenterTest {

    private static final String mockLinkId = "linkid";
    private static final int mockPosition = 5;

    @Mock
    private List<Comment> mockComments;

    @Mock
    private Post clickedPost;

    @Mock
    private Comment mockParentComment;

    @Mock
    private RedditRepository mRedditRepository;

    @Mock
    private CommentContract.View mCommentView;

    @Captor
    private ArgumentCaptor<RedditDataSource.LoadPostCommentsCallback> loadPostCommentsCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<RedditDataSource.ErrorCallback> loadErrorCallbackArgumentCaptor;

    private CommentPresenter mCommentPresenter;

    @Before
    public void setupCommentPresenter() {
        MockitoAnnotations.initMocks(this);

        mCommentPresenter = new CommentPresenter(mRedditRepository, clickedPost);
        mCommentPresenter.setView(mCommentView);
    }

    @Test
    public void loadCommentsFromRepositoryAndLoadIntoView() {
        mCommentPresenter.loadComments(mockLinkId);
        verify(mCommentView).setProgressIndicator(true);
        verify(mRedditRepository).getCommentsForPost(any(String.class),
                loadPostCommentsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture());
        loadPostCommentsCallbackArgumentCaptor.getValue().onCommentsLoaded(mockComments);
        verify(mCommentView).setProgressIndicator(false);
        verify(mCommentView).showComments(mockComments);
    }

    @Test
    public void loadMoreCommentsFromRepositoryAndLoadIntoView() {
        mCommentPresenter.loadMoreCommentsAt(mockParentComment, mockLinkId, mockPosition);
        verify(mRedditRepository).getMoreCommentsForPostAt(any(Comment.class),
                any(String.class),
                any(int.class),
                loadPostCommentsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture());
        loadPostCommentsCallbackArgumentCaptor.getValue().onMoreCommentsLoaded(mockComments, mockPosition);
        verify(mCommentView).hideProgressbarAt(mockPosition);
        verify(mCommentView).addCommentsAt(mockComments, mockPosition);
    }

}
