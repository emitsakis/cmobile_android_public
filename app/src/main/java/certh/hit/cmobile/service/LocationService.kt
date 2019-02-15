package certh.hit.cmobile.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.text.format.DateUtils
import android.util.Log
import certh.hit.cmobile.model.DataFactory
import certh.hit.cmobile.model.MAPUserMessage
import certh.hit.cmobile.model.Topic
import certh.hit.cmobile.utils.Helper
import certh.hit.cmobile.utils.MqttHelper
import com.google.android.gms.location.*
import org.eclipse.paho.client.mqttv3.*
import timber.log.Timber

/**
 * Created by anmpout on 04/02/2019
 */
class LocationService:Service(), LocationServiceInterface {
    private val TAG: String = LocationService::class.java.canonicalName  as String
    private var mPlaybackInfoListener: LocationServiceCallback? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationRequest: LocationRequest

    private var gpsIsEnabled = true
    private  var bearing: Float = 0F
    private var permissionIsGranted = true
    private var isTrackingRunning = true
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    private lateinit var mqttHelper: MqttHelper
    private  var currentQuadTree :String  = ""

    private var availableTopics  :List<Topic> = DataFactory().getAllTopics()

    private val subscribedTopics :ArrayList<Topic> = ArrayList()
    private val mapMessages :ArrayList<MAPUserMessage> = ArrayList()
    private val mBinder = LocationBinder()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
            //Decide how to use/store location coordinates
//            Log.d(TAG, "Bearing")
//            Log.d(TAG, locationResult.lastLocation.bearing.toString())
//            bearing = bearing +2
//            locationResult.lastLocation.bearing = bearing
//            Log.d(TAG, locationResult.lastLocation.bearing.toString())
            Helper.appendLog("onLocationResult! :"+locationResult.lastLocation.latitude.toString()+","+locationResult.lastLocation.longitude.toString()+","+locationResult.lastLocation.bearing.toString()+","+locationResult.lastLocation.speed.toString()+","+locationResult.lastLocation.altitude.toString()+","+locationResult.lastLocation.time.toString()+","+locationResult.lastLocation.accuracy.toString()+",","locations")
         mPlaybackInfoListener!!.onPositionChanged(locationResult.lastLocation)
            var quadTree =  Helper.calculateQuadTree(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude,Helper.ZOOM_LEVEL)
            checkLocationAndSubscribe2(locationResult,quadTree)

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
        Helper.appendLog("Service onCreate","activity")
        Log.d(TAG, "Service onCreate")
        startMqtt()
        registerReceiver()
        checkGpsAndReact()
    }



    override fun onDestroy() {
        stopMqtt();
        Helper.appendLog("Service onDestroy","activity")
        Log.d(TAG, "Service onDestroy")
    }

    private fun stopMqtt() {
        mqttHelper.disconnect()

    }

    fun setPlaybackInfoListener(listener: LocationServiceCallback) {
        mPlaybackInfoListener = listener

    }

    private fun startMqtt() {
        mqttHelper = MqttHelper(this)
        mqttHelper.connect()
        mqttHelper.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Helper.appendLog("connectComplete! ","mqtt")
            }

            override fun connectionLost(throwable: Throwable) {
                Log.d("Debug", "connectionLost")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                var tmpTopic = Helper.parseTopic(topic);
                Helper.appendLog("messageArrived! :"+tmpTopic,"mqtt")
                Helper.appendLog("MqttMessage! :"+mqttMessage.toString(),"mqtt")
                if(topic.contains(Topic.VIVI)){
                    handleVIVIMessage(mqttMessage,tmpTopic)
                }else if(topic.contains(Topic.IVI)){
                    handleIVIMessage(mqttMessage,tmpTopic)
                }else if(topic.contains(Topic.SPAT))
                {
                    handleSPATMessage(mqttMessage,tmpTopic)
                }else if (topic.contains(Topic.MAP)){
                    handleMAPMessage(mqttMessage,tmpTopic)
                }
                Log.d("Debug", topic)
                Log.d("Debug", mqttMessage.toString())

            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.d("Debug", iMqttDeliveryToken.toString())
            }
        })
    }

    private fun handleMAPMessage(
        mqttMessage: MqttMessage,
        tmpTopic: Topic
    ) {
        var  mapMessage = Helper.parseMAPMessage(mqttMessage.toString(),tmpTopic)
        mapMessages.add(mapMessage)
        if(mapMessage.indexNumber==1003) {
            createSPATTopicAndSubscribe(mapMessage)
        }

    }

    private fun createSPATTopicAndSubscribe(mapMessage:MAPUserMessage) {
        var topicSpat = Topic.createSPAT(mapMessage.indexNumber.toString())
        mqttHelper.subscribeToTopic(topicSpat.toStringSpat(), 0, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.w("Mqtt", "Subscribed!")
                Helper.appendLog("Subscribed! :"+topicSpat.toString(),"mqtt")
                subscribedTopics.add(topicSpat)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Log.w("Mqtt", "Subscribed fail!")
            }
        })
    }

    private fun handleSPATMessage(
        mqttMessage: MqttMessage,
        tmpTopic: Topic
    ) {
        var spatUserMessage = Helper.parseSPATMessage(mqttMessage.toString(),tmpTopic)
         var mapMessage = mapMessages.find { msg -> msg.indexNumber==spatUserMessage.indexNumber }
        spatUserMessage.mapMessage = mapMessage
        mPlaybackInfoListener!!.onSPATUserMessage(spatUserMessage)

    }

    private fun handleIVIMessage(mqttMessage: MqttMessage, topic:Topic) {
        var iviUserMessage = Helper.parseIVIUserMessage(mqttMessage.toString(),topic)
        mPlaybackInfoListener!!.onIVIMessageReceived(iviUserMessage)
    }

    private fun handleVIVIMessage(
        mqttMessage: MqttMessage,
        tmpTopic: Topic
    ) {
        var viviUserMessage = Helper.parseVIVIUserMessage(mqttMessage.toString(),tmpTopic)
        mPlaybackInfoListener!!.onVIVIUserMessage(viviUserMessage)

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 5 * DateUtils.SECOND_IN_MILLIS
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
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

    private fun checkLocationAndSubscribe2(result: LocationResult, quadTree: String) {
        if(!currentQuadTree.contentEquals(quadTree)){
            if (mqttHelper.isConnected()) {
                currentQuadTree = quadTree
                var topicViv = Topic.createIVI(quadTree)
                var topicVivI = Topic.createVIVI(quadTree)
                var topicMAP = Topic.createMAP(quadTree)
                mqttHelper.subscribeToTopic(topicViv.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "Subscribed!")
                        Helper.appendLog("Subscribed! :"+topicViv.toString(),"mqtt")
                        subscribedTopics.add(topicViv)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
                mqttHelper.subscribeToTopic(topicVivI.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "Subscribed!")
                        Helper.appendLog("Subscribed! :"+topicVivI.toString(),"mqtt")
                        subscribedTopics.add(topicVivI)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
                mqttHelper.subscribeToTopic(topicMAP.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Helper.appendLog("Subscribed! :"+topicMAP.toString(),"mqtt")

                        Log.w("Mqtt", "Subscribed!")
                        subscribedTopics.add(topicMAP)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            }


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


    inner class LocationBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
}