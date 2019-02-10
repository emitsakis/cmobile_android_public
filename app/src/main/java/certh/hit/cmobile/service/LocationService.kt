package certh.hit.cmobile.service

import android.Manifest
import android.app.Service
import android.arch.lifecycle.LifecycleService
import android.content.Intent
import android.os.*
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

/**
 * Created by anmpout on 04/02/2019
 */
class LocationService:Service(), LocationServiceInterface {
    private val TAG: String = LocationService::class.java.canonicalName  as String
    private var mPlaybackInfoListener: LocationServiceCallback? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private var gpsIsEnabled = true

    private var permissionIsGranted = true
    private var isTrackingRunning = true
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    private val mBinder = AudioBinder()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            //Decide how to use/store location coordinates
         mPlaybackInfoListener!!.onPositionChanged(locationResult.describeContents())
        }
    }
    override fun setupNotification(noLecture: String, lectureTitle: String) {

        Log.d(TAG, "setupNotification")
    }


    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Service onBind")
        registerForLocationTracking()
        return mBinder
    }
    private fun registerForLocationTracking() {
        if (permissionIsGranted && gpsIsEnabled) {

            isTrackingRunning = try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.myLooper())
                true
            } catch (unlikely: SecurityException) {

                error("Error when registerLocationUpdates()")
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 5 * DateUtils.SECOND_IN_MILLIS
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service onStartCommand " + startId)

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
        Log.d(TAG, "Service onDestroy")
    }

    fun setPlaybackInfoListener(listener: LocationServiceCallback) {
        mPlaybackInfoListener = listener
        mPlaybackInfoListener!!.onPositionChanged(1000)

    }

    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {

            val data = msg.data
            val dataString = data.getString("MyString")
            Toast.makeText(applicationContext,
                dataString, Toast.LENGTH_SHORT).show()
        }
    }

    inner class AudioBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
}