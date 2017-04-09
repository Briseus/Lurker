package torille.fi.lurkforreddit.comments;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import torille.fi.lurkforreddit.data.RedditRepository;
import torille.fi.lurkforreddit.data.models.CommentChild;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link CommentPresenter}
 */
public class CommentPresenterTest {

    private static final String mockLinkId = "linkid";
    private static final int mockPosition = 5;

    @Mock
    private List<CommentChild> mockComments;

    @Mock
    private CommentChild mockParentComment;

    @Mock
    private RedditRepository mRedditRepository;

    @Mock
    private CommentContract.View mCommentView;

    @Captor
    private ArgumentCaptor<RedditRepository.LoadPostCommentsCallback> loadPostCommentsCallbackArgumentCaptor;

    @Captor
    private ArgumentCaptor<RedditRepository.ErrorCallback> loadErrorCallbackArgumentCaptor;

    private CommentPresenter mCommentPresenter;

    @Before
    public void setupCommentPresenter() {
        MockitoAnnotations.initMocks(this);

        mCommentPresenter = new CommentPresenter(mRedditRepository, mCommentView);
    }

    @Test
    public void loadCommentsFromRepositoryAndLoadIntoView() {
        mCommentPresenter.loadComments(mockLinkId);
        verify(mRedditRepository).getCommentsForPost(any(String.class),
                loadPostCommentsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture());
        loadPostCommentsCallbackArgumentCaptor.getValue().onCommentsLoaded(mockComments);
        verify(mCommentView).hideProgressbarAt(1);
        verify(mCommentView).showComments(mockComments);
    }

    @Test
    public void loadMoreCommentsFromRepositoryAndLoadIntoView() {
        mCommentPresenter.loadMoreCommentsAt(mockParentComment, mockLinkId, mockPosition);
        verify(mRedditRepository).getMoreCommentsForPostAt(any(CommentChild.class),
                any(String.class),
                any(int.class),
                loadPostCommentsCallbackArgumentCaptor.capture(),
                loadErrorCallbackArgumentCaptor.capture());
        loadPostCommentsCallbackArgumentCaptor.getValue().onMoreCommentsLoaded(mockComments, mockPosition);
        verify(mCommentView).hideProgressbarAt(mockPosition);
        verify(mCommentView).addCommentsAt(mockComments, mockPosition);
    }

}
