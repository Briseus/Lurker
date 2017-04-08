package torille.fi.lurkforreddit.comments;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.text.style.URLSpan;
import android.view.View;

import org.parceler.Parcels;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;
import torille.fi.lurkforreddit.data.models.Post;
import torille.fi.lurkforreddit.data.models.PostDetails;
import torille.fi.lurkforreddit.media.FullscreenActivity;
import torille.fi.lurkforreddit.subreddit.SubredditActivity;
import torille.fi.lurkforreddit.utils.MediaHelper;

/**
 * Custom {@link URLSpan} to modify how to open clicked links in text
 */

class CustomUrlSpan extends URLSpan {

    CustomUrlSpan(String url) {
        super(url);
    }

    public CustomUrlSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Timber.d("got url " + url);
        Intent intent;
        if (MediaHelper.isContentMedia(url)) {
            Post post = new Post("t5", new PostDetails());
            post.getPostDetails().setUrl(url);
            intent = new Intent(widget.getContext(), FullscreenActivity.class);
            intent.putExtra(FullscreenActivity.EXTRA_POST, Parcels.wrap(post));
            widget.getContext().startActivity(intent);
        } else if (checkForReddit(url)) {
            Uri redditUri = Uri.parse("https://www.reddit.com" + url);
            intent = new Intent(widget.getContext(), SubredditActivity.class);
            intent.setData(redditUri);
            Timber.d("Going to launch " + redditUri);
            widget.getContext().startActivity(intent);
        } else {
            super.onClick(widget);
        }


    }

    private boolean checkForReddit(String redditUrl) {
        Timber.d("Checking url " + redditUrl);
        Pattern p = Pattern.compile("(\\/r\\/.*)");
        Matcher m = p.matcher(redditUrl);
        return m.matches();
    }


}
