package torille.fi.lurkforreddit.data;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import torille.fi.lurkforreddit.data.models.jsonResponses.StreamableVideo;

/**
 * Created by eva on 9.4.2017.
 */


public interface VideositeService {

    interface Streamable {
        @GET("videos/{identifier}")
        Observable<StreamableVideo> getVideo(@Path("identifier") String identifier);
    }


}

