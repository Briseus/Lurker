package torille.fi.lurkforreddit.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import torille.fi.lurkforreddit.data.models.jsonResponses.StreamableVideo;

/**
 * Created by eva on 9.4.2017.
 */


public interface StreamableService {

    @GET("videos/{identifier}")
    Call<StreamableVideo> getVideo(@Path("identifier") String identifier);


}

