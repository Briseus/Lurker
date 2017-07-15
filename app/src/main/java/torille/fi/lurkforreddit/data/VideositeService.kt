package torille.fi.lurkforreddit.data

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import torille.fi.lurkforreddit.data.models.jsonResponses.StreamableVideo

interface VideositeService {

    interface Streamable {
        @GET("videos/{identifier}")
        fun getVideo(@Path("identifier") identifier: String): Observable<StreamableVideo>
    }

}

