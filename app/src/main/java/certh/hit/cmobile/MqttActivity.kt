package certh.hit.cmobile

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import certh.hit.cmobile.utils.MqttHelper
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Created by anmpout on 22/01/2019
 */
class MqttActivity : AppCompatActivity() {
    var mqttHelper: MqttHelper? = null

    var dataReceived: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mqtt)

        dataReceived = findViewById(R.id.dataReceived)

        startMqtt()
    }

    private fun startMqtt() {
        mqttHelper = MqttHelper(applicationContext)
        mqttHelper?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {

            }

            override fun connectionLost(throwable: Throwable) {

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", mqttMessage.toString())
                dataReceived?.text = mqttMessage.toString()
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {

            }
        })
    }
}
