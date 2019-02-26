package certh.hit.cmobile.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.text.format.DateUtils
import android.util.Log
import certh.hit.cmobile.model.*
import certh.hit.cmobile.utils.Helper
import certh.hit.cmobile.utils.MqttHelper
import certh.hit.cmobile.utils.NotificationManager_CMobile
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
    private val receiveTopics: ArrayList<String> = ArrayList()
    private val mapMessages :ArrayList<MAPUserMessage> = ArrayList()
    private val mBinder = LocationBinder()
    private var mMediaNotificationManager: NotificationManager_CMobile? = null

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
            checkLocationAndUnsubscribe(locationResult,quadTree)

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
        stopMqtt()
        mMediaNotificationManager!!.stopNotification()
        Helper.appendLog("Service onDestroy","activity")
        Log.d(TAG, "Service onDestroy")
    }

    private fun stopMqtt() {
        mqttHelper.disconnect()

    }

    fun setPlaybackInfoListener(listener: LocationServiceCallback) {
        mPlaybackInfoListener = listener
        try {
            mMediaNotificationManager = NotificationManager_CMobile(this)
            mMediaNotificationManager!!.startNotification("C- Mobile", "C- Mobile")
        } catch (e: RemoteException) {
            throw IllegalStateException("Could not create a MediaNotificationManager", e)
        }


    }

    private fun startMqtt() {
        mqttHelper = MqttHelper(this)
        mqttHelper.connect()
        mqttHelper.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Helper.appendLog("connectComplete! ","mqtt")
                clearTopics()
            }

            override fun connectionLost(throwable: Throwable) {
                Log.d("Debug", "connectionLost")
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                var tmpTopic = Helper.parseTopic(topic);
                if(!receiveTopics.contains(topic)) {
                    receiveTopics.add(topic)
                }
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
                }else if(topic.contains(Topic.VIVI_EGNATIA)){
                    handleEgnatiaMessage(mqttMessage,tmpTopic)

                }else if(topic.contains(Topic.DENM)){
                    handleDENMMessage(mqttMessage,tmpTopic)
                }
                Log.d("Debug", topic)
                Log.d("Debug", mqttMessage.toString())

            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.d("Debug", iMqttDeliveryToken.toString())
            }
        })
    }

    private fun handleDENMMessage(mqttMessage: MqttMessage, tmpTopic: Topic) {
        mPlaybackInfoListener!!.onDenmUserMessage(DENMUserMessage())

    }

    private fun handleEgnatiaMessage(mqttMessage: MqttMessage, tmpTopic: Topic) {
        var viviUserMessage = Helper.parseVIVIEgnatiaUserMessage(mqttMessage.toString(),tmpTopic)
        mPlaybackInfoListener!!.onEgnatiaUserMessage(viviUserMessage)

    }

    private fun handleMAPMessage(
        mqttMessage: MqttMessage,
        tmpTopic: Topic
    ) {
        var  mapMessage = Helper.parseMAPMessage(mqttMessage.toString(),tmpTopic)
        mapMessages.add(mapMessage)
        //if(mapMessage.indexNumber == 1003) {
            createSPATTopicAndSubscribe(mapMessage)
        //}

    }

    private fun createSPATTopicAndSubscribe(mapMessage:MAPUserMessage) {
        var topicSpat = Topic.createSPAT(mapMessage.indexNumber.toString())
        mqttHelper.subscribeToTopic(topicSpat.toStringSpat(), 0, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.w("Mqtt", "Subscribed!")
                Helper.appendLog("Subscribed! :"+topicSpat.toString(),"mqtt")
                //subscribedTopics.add(topicSpat)
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



    private fun checkLocationAndSubscribe2(result: LocationResult, quadTree: String) {
        if(!currentQuadTree.contentEquals(quadTree)){
            Log.w("c quadtree", currentQuadTree)
            Log.w("n quadtree", quadTree)
            Log.w("quadtree", "------------------------------------------------------------------------")
            if (mqttHelper.isConnected()) {
                currentQuadTree = quadTree
                var topicViv = Topic.createIVI(quadTree)
                var topicVivI = Topic.createVIVI(quadTree)
                var topicMAP = Topic.createMAP(quadTree)
                var topicEgnatia = Topic.createEgnatia(quadTree)
                var topicFr = Topic.createFr(quadTree)
                mqttHelper.subscribeToTopic(topicViv.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Helper.appendLog("Subscribed! :"+topicViv.toString(),"mqtt")
                       // subscribedTopics.add(topicViv)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
                mqttHelper.subscribeToTopic(topicVivI.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Helper.appendLog("Subscribed! :"+topicVivI.toString(),"mqtt")
                        //subscribedTopics.add(topicVivI)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
                mqttHelper.subscribeToTopic(topicMAP.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Helper.appendLog("Subscribed! :"+topicMAP.toString(),"mqtt")

                        //subscribedTopics.add(topicMAP)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
                mqttHelper.subscribeToTopic(topicEgnatia.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Helper.appendLog("Subscribed! :"+topicEgnatia.toString(),"mqtt")
                        //subscribedTopics.add(topicEgnatia)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
                mqttHelper.subscribeToTopic(topicFr.toString(), 0, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Helper.appendLog("Subscribed! :"+topicFr.toString(),"mqtt")
                        Log.w("Mqtt", "Subscribed! :"+topicFr.toString())
                        //subscribedTopics.add(topicFr)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            }else{
                var locationVIVI = Location("vivilocation")
                locationVIVI.latitude =40.615006
                locationVIVI.longitude =22.954195
                var locationIVI = Location("ivilocation")
                locationIVI.latitude =40.5549
                locationIVI.longitude =23.0148
                var locationSPAT = Location("trafficLight")
                locationSPAT.latitude =40.629549
                locationSPAT.longitude =22.947945
                var locationEgnatia = Location("egnatia")
                locationEgnatia.latitude =39.790804323
                locationEgnatia.longitude =21.300095498
                var locationFr = Location("france")
                locationFr.latitude =44.88388823
                locationFr.longitude =-0.5573381
                if(result.lastLocation.distanceTo(locationVIVI)<100){
                    var viviUserMessage = VIVIUserMessage()
                    viviUserMessage.eta = "00:02:00"
                    viviUserMessage.route = "V.OLGAS-YMCA"

                    mPlaybackInfoListener!!.onVIVIUserMessage(viviUserMessage)
                }else if(result.lastLocation.distanceTo(locationIVI)<100){
                    var iviUserMessage = IVIUserMessage()
                    mPlaybackInfoListener!!.onIVIMessageReceived(iviUserMessage)

                }
                else if(result.lastLocation.distanceTo(locationSPAT)<100){
                    var spatUserMessage = SPATUserMessage()
                    spatUserMessage.eventState = "green"
                    mPlaybackInfoListener!!.onSPATUserMessage(spatUserMessage)

                }else if(result.lastLocation.distanceTo(locationEgnatia)<100){
                    var egnatiaUserMessage = EgnatiaUserMessage()
                    egnatiaUserMessage.egantiaMessage = "IN CONGESTION DO NOT BLOCK EMERGENCY LANE "
                    mPlaybackInfoListener!!.onEgnatiaUserMessage(egnatiaUserMessage)

                }
                else if(result.lastLocation.distanceTo(locationFr)<100){
                    var denmUserMessage = DENMUserMessage()
                    mPlaybackInfoListener!!.onDenmUserMessage(denmUserMessage)

                }

            }




        }
    }

    private fun checkLocationAndUnsubscribe(
        locationResult: LocationResult,
        quadTree: String
    ) {
        for(topic in receiveTopics) {
            if (!topic.contains("spat")){
            var tmpTopic = Helper.parseTopic(topic)
                var topicQuadTreeRmvSlash = tmpTopic.quadTree!!.replace("/","")

                var quadTreeRmvSlash = quadTree!!.replace("/","")
                Log.w("quad t",topicQuadTreeRmvSlash.substring(0,Helper.ZOOM_LEVEL))
                Log.w("quad i",quadTreeRmvSlash)

                if(!quadTreeRmvSlash.equals(topicQuadTreeRmvSlash.substring(0,Helper.ZOOM_LEVEL))) {
                    Log.w("quad m","in")
                    if (tmpTopic.type.equals(Topic.MAP)) {
                        mqttHelper.unsubscribeToTopic(topic,object :IMqttActionListener{
                            override fun onSuccess(asyncActionToken: IMqttToken?) {
                                receiveTopics.remove(topic)
                                Log.d(TAG,"map")
                                val SpatTopic = receiveTopics.first { w -> w.endsWith(tmpTopic.data.toString()) }
                                mqttHelper.unsubscribeToTopic(SpatTopic,object :IMqttActionListener{
                                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                                        receiveTopics.remove(SpatTopic)
                                        mPlaybackInfoListener!!.onSPATUnsubscribe()
                                        Log.d(TAG,"remove spat")
                                    }

                                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                                    }
                                })

                            }

                            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            }
                        })

                    } else if (tmpTopic.type.equals(Topic.VIVI_EGNATIA) || tmpTopic.type.equals(Topic.VIVI) || tmpTopic.type.equals(
                            Topic.IVI
                        ) || tmpTopic.type.equals(Topic.DENM)
                    ){
                        mqttHelper.unsubscribeToTopic(topic,object :IMqttActionListener{
                            override fun onSuccess(asyncActionToken: IMqttToken?) {
                                receiveTopics.remove(topic)
                                if(tmpTopic.type.equals( Topic.IVI)) {
                                        mPlaybackInfoListener!!.onIVIUnsubscribe()
                                    }
                                Log.d(TAG,"remove"+tmpTopic.type)
                            }

                            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            }
                        })

                    }
                }


                }
        }



//            var upperTopicQuadTree = topic.quadTree?.substring(0,Helper.ZOOM_LEVEL)
//            Timber.d("upperQuadTree : %s",upperTopicQuadTree)
//            Timber.d("caluclated quadTree : %s",quadTree.substring(0,Helper.ZOOM_LEVEL))
//            if(upperTopicQuadTree?.compareTo(quadTree.substring(0,Helper.ZOOM_LEVEL)) == 0 &&
//                !subscribedTopics.any { topic1 -> topic1.typeId ==topic.typeId }) {
//                if (mqttHelper.isConnected()) {
//                    mqttHelper.subscribeToTopic(topic.toString(), 0, object : IMqttActionListener {
//                        override fun onSuccess(asyncActionToken: IMqttToken) {
//                            Log.w("Mqtt", "Subscribed!")
//                            subscribedTopics.add(topic)
//                        }
//
//                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
//                            Log.w("Mqtt", "Subscribed fail!")
//                        }
//                    })
//                }
//            }


    }
    fun clearTopics(){
        for (i in 1..20) {
            mqttHelper.unsubscribeToTopic("tt/denm/0/3/1/3/3/3/1/1/1/0/2/3/1/2/2/1/0/3/1001_1001",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            mqttHelper.unsubscribeToTopic("egnatia_sa/v-ivi_egnatia/1/2/2/0/1/1/1/3/0/0/1/2/2/3/0/1/1/0/vms1",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            mqttHelper.unsubscribeToTopic("tt/denm/0/3/1/3/3/3/1/1/1/0/2/3/1/2/1/0/3/2/1001_1001",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            mqttHelper.unsubscribeToTopic("hit_certh/spat_hit/1003", object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.w("Mqtt", "UnSubscribed!")

                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Subscribed fail!")
                }
            })
            mqttHelper.unsubscribeToTopic("hit_certh/ivi_hit/1/2/2/1/0/0/0/0/0/3/2/1/1/3/2/1/1/0/666",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            mqttHelper.unsubscribeToTopic("hit_certh/v-ivi_hit/1/2/2/1/0/0/0/0/0/3/0/3/0/2/3/2/3/0/v.olgas-ymca",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            mqttHelper.unsubscribeToTopic("hit_certh/map_hit/1/2/2/1/0/0/0/0/0/3/0/3/0/2/1/0/2/0/1003",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })
            mqttHelper.unsubscribeToTopic("hit_certh/map_hit/1/2/2/1/0/0/0/0/0/3/0/3/0/2/1/0/2/0/1002",
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        Log.w("Mqtt", "UnSubscribed!")

                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        Log.w("Mqtt", "Subscribed fail!")
                    }
                })

            mqttHelper.unsubscribeToTopic("hit_certh/spat_hit/1002", object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.w("Mqtt", "UnSubscribed!")

                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Subscribed fail!")
                }
            })
        }
    }

    inner class LocationBinder : Binder() {
        internal val service: LocationService
            get() = this@LocationService
    }
}