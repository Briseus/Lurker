package torille.fi.lurkforreddit.data.models.view;

import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * Created by eva on 2.5.2017.
 */
@AutoValue
public abstract class PostAndComments {

    public abstract Post originalPost();

    public abstract List<Comment> comments();

    public static PostAndComments create(Post post, List<Comment> comments) {
        return new AutoValue_PostAndComments(post, comments);
    }

}
