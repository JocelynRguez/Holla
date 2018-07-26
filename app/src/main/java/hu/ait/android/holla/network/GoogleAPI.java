package hu.ait.android.holla.network;

import android.media.Image;

import hu.ait.android.holla.data.Place;
import hu.ait.android.holla.data.PlaceResult;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleAPI {

    @GET("nearbysearch/json?")
    Call<PlaceResult> getNearbyCafe(@Query("location") String location,
                                    @Query("radius") int radius,
                                    @Query("types") String type,
                                    @Query("keyword") String keyword,
                                    @Query("key") String apikey);

    @GET("photo?")
    Call<Image> getImage(@Query("photoreference") String ref,
                         @Query("maxwidth") String width,
                         @Query("key") String key );

}
