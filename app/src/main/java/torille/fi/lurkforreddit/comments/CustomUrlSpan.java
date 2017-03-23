package torille.fi.lurkforreddit.comments;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

import org.parceler.Parcels;

import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.PostDetails;
import torille.fi.lurkforreddit.media.FullscreenActivity;
import torille.fi.lurkforreddit.utils.MediaHelper;

/**
 * Custom {@link URLSpan} to modify how to open clicked links in text
 */

public class CustomUrlSpan extends URLSpan {

    public CustomUrlSpan(String url) {
        super(url);
    }

    public CustomUrlSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Log.d("Test", "got url " + url);
        Intent intent;
        if (MediaHelper.isContentMedia(url)) {
            Post post = new Post("t5", new PostDetails());
            post.getPostDetails().setUrl(url);
            intent = new Intent(widget.getContext(), FullscreenActivity.class);
            intent.putExtra(FullscreenActivity.EXTRA_POST, Parcels.wrap(post));
        } else {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }
        widget.getContext().startActivity(intent);
    }
}
