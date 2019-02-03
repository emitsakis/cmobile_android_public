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
import org.eclipse.paho.client.mqttv3.*
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

    private val subscribedTopics :ArrayList<Topic> = ArrayList()



    override fun onInactive() = unregisterReceiver()

    private fun unregisterReceiver() {
        fusedLocationClient.removeLocationUpdates(locationCallback)    }

    override fun onActive() {
        startMqtt()
        registerReceiver()
        checkGpsAndReact()

    }

    private fun startMqtt() {
        mqttHelper = MqttHelper(context)
        mqttHelper.connect()
        mqttHelper.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {

            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.d("Debug", mqttMessage.toString())
                postValue(Location("GPS"))
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
            var quadTree =  Helper.calculateQuadTree(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude,22)
            checkLocationAndSubscribe(locationResult,quadTree)
            checkLocationAndUnsubscribe(locationResult,quadTree)
        }
    }

    private fun checkLocationAndUnsubscribe(
        locationResult: LocationResult,
        quadTree: String
    ) {
        for (topic in availableTopics){
            var upperTopicQuadTree = topic.quadTree?.substring(0,15)
            Timber.d("upperQuadTree : %s",upperTopicQuadTree)
            Timber.d("caluclated quadTree : %s",quadTree.substring(0,15))
            if(upperTopicQuadTree?.compareTo(quadTree.substring(0,15)) == 0 &&
                !subscribedTopics.any { topic1 -> topic1.typeId ==topic.typeId }) {
                if (mqttHelper.isConnected()) {
                    mqttHelper.subscribeToTopic(topic.toString(), 0, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Log.w("Mqtt", "Subscribed!")
                            subscribedTopics.add(topic)
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            Log.w("Mqtt", "Subscribed fail!")
                        }
                    })
                }
            }


        }
    }

    private fun checkLocationAndSubscribe(
        locationResult: LocationResult,
        quadTree: String
    ) {
        for (topic in subscribedTopics) {
            var upperTopicQuadTree = topic.quadTree?.substring(0,15)
            Timber.d("upperQuadTree : %s",upperTopicQuadTree)
            Timber.d("caluclated quadTree : %s",quadTree.substring(0,15))
            if(upperTopicQuadTree?.compareTo(quadTree.substring(0,15)) != 0){
                if (mqttHelper.isConnected()) {
                    mqttHelper.unsubscribeToTopic(topic.toString(), object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            Log.w("Mqtt", "UnSubscribed!")
                            subscribedTopics.remove(topic)
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            Log.w("Mqtt", "UnSubscribed fail!")
                        }
                    })
                }


            }

        }
    }

}