package certh.hit.cmobile.utils
import certh.hit.cmobile.model.Osm
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Created by anmpout on 06/03/2019
 */
interface GetData {

    @GET("api/0.6/way/{id}")
    fun listRepos(@Path("id") id: String): Call<Osm>

}