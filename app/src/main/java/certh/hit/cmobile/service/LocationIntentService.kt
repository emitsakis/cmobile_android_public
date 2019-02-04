package certh.hit.cmobile.service

import android.app.IntentService
import android.content.Intent
import android.util.Log

/**
 * Created by anmpout on 04/02/2019
 */
class LocationIntentService :IntentService("LocationIntentService"){
    private val TAG = "LocationIntentService"
    override fun onHandleIntent(p0: Intent?) {
        Log.i(TAG, "Intent Service started")

    }
}