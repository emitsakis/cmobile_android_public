package certh.hit.cmobile.utils

import android.content.Context
import android.util.Log
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


/**
 * Created by anmpout on 21/01/2019
 */
class MqttHelper(context: Context) {
    var mqttAndroidClient: MqttAndroidClient

    internal val serverUri = "tcp://mqttcits.imet.gr:1883"

    internal val clientId = "js-utility-SY0NY"
    //internal val subscriptionTopic = "hit_certh/ivi_hit/1/2/2/1/0/0/0/0/0/3/2/1/1/3/2/1/1/0/666"
    //internal val subscriptionTopic = "hit_certh/v-ivi_hit/1/2/2/1/0/0/0/0/0/3/0/3/0/2/3/2/3/0/v.olgas-ymca"
    //internal val subscriptionTopic = "hit_certh/spat_hit/1002"
    internal val username = "cmobile"
    internal val password = "antonis"

    init {
        mqttAndroidClient = MqttAndroidClient(context, serverUri, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w("mqtt", s)
            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Mqtt", mqttMessage.toString())
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {

            }

        })
        connect()
    }
    fun setCallback(callback: MqttCallbackExtended) {
        mqttAndroidClient.setCallback(callback)
    }

    public fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.userName = username
        mqttConnectOptions.password = password.toCharArray()

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {

                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 500
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                  //  subscribeToTopic(subscriptionTopic :String,qos:Int)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString())
                }
            })


        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }


    public fun subscribeToTopic(subscriptionTopic :String,qos:Int,callback:IMqttActionListener) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, qos, null, callback)
        } catch (ex: MqttException) {
            System.err.println("Exceptionst subscribing")
            ex.printStackTrace()
        }

    }

    public fun subscribeToTopics(topics :Array<String>, qos:IntArray) {
        try {
            mqttAndroidClient.subscribe(topics, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.w("Mqtt", "Subscribed!")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Subscribed fail!")
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exceptionst subscribing")
            ex.printStackTrace()
        }

    }

    public fun unsubscribeToTopic(subscriptionTopic :String,callback: IMqttActionListener) {
        try {
            mqttAndroidClient.unsubscribe(subscriptionTopic,  null,callback )
        } catch (ex: MqttException) {
            System.err.println("Exceptionst subscribing")
            ex.printStackTrace()
        }

    }

    public fun unsubscribeToTopics(topics :Array<String>) {
        try {
            mqttAndroidClient.unsubscribe(topics,  null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.w("Mqtt", "UnSubscribed!")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.w("Mqtt", "Subscribed fail!")
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exceptionst subscribing")
            ex.printStackTrace()
        }

    }

    public fun disconnect(){
        mqttAndroidClient.unregisterResources()
        mqttAndroidClient.disconnect(null,object:IMqttActionListener{
            override fun onSuccess(asyncActionToken: IMqttToken?) {
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            }

        })

    }

    fun isConnected():Boolean{
        return mqttAndroidClient.isConnected
    }
}
