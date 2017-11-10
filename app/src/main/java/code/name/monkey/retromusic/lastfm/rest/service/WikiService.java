package code.name.monkey.retromusic.lastfm.rest.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author Hemanth S (h4h13).
 */

public interface WikiService {
    @GET("api.php")
    Call<ResponseBody> wikiLyrics(@Query("func") String func,
                                  @Query("fmt") String realjson,
                                  @Query("artist") String artist,
                                  @Query("song") String song);
}
