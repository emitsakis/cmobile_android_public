package certh.hit.cmobile.location

import android.arch.lifecycle.LiveData
import android.content.Context
import android.location.Location
import android.os.Looper
import android.text.format.DateUtils
import android.util.Log
import certh.hit.cmobile.model.DataFactory
import certh.hit.cmobile.model.Topic
import certh.hit.cmobile.utils.Helper
import certh.hit.cmobile.utils.MqttHelper
import com.google.android.gms.location.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber

/**
 * Created by anmpout on 02/02/2019
 */
class LocationUpdateListener(private val context: Context) : LiveData<Location>() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private lateinit var mqttHelper: MqttHelper

    private var isTrackingRunning :Boolean = true

    private var availableTopics  :List<Topic> = DataFactory().getAllTopics()

    private var subscribedTopics :List<Topic> = ArrayList()



    override fun onInactive() = unregisterReceiver()

    private fun unregisterReceiver() {
        fusedLocationClient.removeLocationUpdates(locationCallback)    }

    override fun onActive() {
        registerReceiver()
        checkGpsAndReact()
        startMqtt()
    }

    private fun startMqtt() {
        mqttHelper = MqttHelper(context)
        mqttHelper.connect()
        mqttHelper?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {

            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.d("Debug", mqttMessage.toString())
              //  postValue(Location("GPS"))
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {

            }
        })
    }
    private fun checkGpsAndReact() {
        isTrackingRunning = try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper())
            true
        } catch (unlikely: SecurityException) {
            Timber.e("Error when registerLocationUpdates()")
            error("Error when registerLocationUpdates()")
        } }

    private fun registerReceiver() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationRequest = LocationRequest.create().apply {
            interval = 5 * DateUtils.SECOND_IN_MILLIS
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            //Decide how to use/store location coordinates
            Timber.d("New Coordinates Received: %s", locationResult.locations.toString())
            checkLocationAndSubscribe(locationResult)
            checkLocationAndUnsubscribe(locationResult)
        }
    }

    private fun checkLocationAndUnsubscribe(locationResult: LocationResult) {
      var quadTree =  Helper.calculateQuadTree(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude,22)
        postValue(Location(quadTree))
    }

    private fun checkLocationAndSubscribe(locationResult: LocationResult) {

    }


}