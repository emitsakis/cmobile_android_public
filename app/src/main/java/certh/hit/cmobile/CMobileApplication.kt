package certh.hit.cmobile

import android.app.Application
import certh.hit.cmobile.utils.Helper
import com.mapbox.mapboxsdk.Mapbox

/**
 * Created by anmpout on 21/01/2019
 */
class CMobileApplication : Application(){

    override fun onCreate() {
        super.onCreate()

// Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), Helper.mapsKey);
    }
}