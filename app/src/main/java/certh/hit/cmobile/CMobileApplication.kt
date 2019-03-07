package certh.hit.cmobile

import android.app.Application
import certh.hit.cmobile.utils.GetData
import certh.hit.cmobile.utils.Helper
import com.mapbox.mapboxsdk.Mapbox
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import timber.log.Timber
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory


/**
 * Created by anmpout on 21/01/2019
 */
class CMobileApplication : Application(){



    override fun onCreate() {
        super.onCreate()
            instance = this;
        retrofit = Retrofit.Builder()
            .baseUrl("https://www.openstreetmap.org/")
            .client(OkHttpClient())
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(
                     Persister(
                         AnnotationStrategy() // important part!
                            ))
                )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
         service = retrofit!!.create(GetData::class.java!!)
// Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), Helper.mapsKey);
    }
    companion object {
        private var instance: CMobileApplication? = null
        fun getInstance(): CMobileApplication? {
            return instance
        }
        private var retrofit:Retrofit? =null
        fun getRetrofit():Retrofit?{
            return retrofit
        }
        private var service:GetData? =null
        fun getService():GetData?{
            return service
        }
    }


}