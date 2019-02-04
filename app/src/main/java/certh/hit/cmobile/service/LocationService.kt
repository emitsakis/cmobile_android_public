package certh.hit.cmobile.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.widget.Toast

/**
 * Created by anmpout on 04/02/2019
 */
class LocationService:Service() {
    private val TAG = "LocationService"
    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Service onBind")
        return null
    }

    override fun onCreate() {
        Log.i(TAG, "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i(TAG, "Service onStartCommand " + startId)

        var i: Int = 0

        while (i <= 3) {

            try {
                Thread.sleep(10000)
                i++
            } catch (e: Exception) {
            }
            Log.i(TAG, "Service running")
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")
    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {

            val data = msg.data
            val dataString = data.getString("MyString")
            Toast.makeText(applicationContext,
                dataString, Toast.LENGTH_SHORT).show()
        }
    }

}