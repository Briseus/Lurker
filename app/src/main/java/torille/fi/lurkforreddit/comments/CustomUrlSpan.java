package torille.fi.lurkforreddit.comments;

import android.content.Context;
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

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Uri uri = Uri.parse(url);
        String domain = uri.getHost();
        Timber.d("got url " + url);

        Intent intent;
        Context context = widget.getContext();
        if (MediaHelper.isContentMedia(url) || MediaHelper.checkDomainForMedia(domain)) {
            Post post = new Post("t5", new PostDetails());
            post.getPostDetails().setUrl(url);
            post.getPostDetails().setDomain(domain);

            intent = new Intent(context, FullscreenActivity.class);
            intent.putExtra(FullscreenActivity.EXTRA_POST, Parcels.wrap(post));

            context.startActivity(intent);
        } else if (checkForReddit(url)) {

            intent = new Intent(context, SubredditActivity.class);
            intent.putExtra(SubredditActivity.EXTRA_SUBREDDITNAME, url);

            Timber.d("Going to checkout subreddit " + url);

            context.startActivity(intent);
        } else {
            super.onClick(widget);
        }


    }

    private boolean checkForReddit(String redditUrl) {
        Timber.d("Checking url " + redditUrl);
        Pattern p = Pattern.compile("(/r/.*)");
        Matcher m = p.matcher(redditUrl);
        return m.matches();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected CustomUrlSpan(Parcel in) {
        super(in);
    }

    public static final Creator<CustomUrlSpan> CREATOR = new Creator<CustomUrlSpan>() {
        @Override
        public CustomUrlSpan createFromParcel(Parcel source) {
            return new CustomUrlSpan(source);
        }

        @Override
        public CustomUrlSpan[] newArray(int size) {
            return new CustomUrlSpan[size];
        }
    };
}
