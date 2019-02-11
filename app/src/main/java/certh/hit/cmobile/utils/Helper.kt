package certh.hit.cmobile.utils


import android.os.Build
import android.util.Base64
import android.util.Log
import certh.hit.cmobile.BuildConfig
import certh.hit.cmobile.model.IVIUserMessage
import certh.hit.cmobile.model.Topic
import certh.hit.cmobile.model.VIVIUserMessage
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey



/**
 * Created by anmpout on 21/01/2019
 */
object Helper {
    private val TAG: String = Helper::class.java.canonicalName  as String
    val mapsKey = BuildConfig.MAP_BOX_API_KEY
    val ZOOM_LEVEL = 14


    fun createUID(){
        val keyGen = KeyPairGenerator.getInstance("EC")
        val random = SecureRandom.getInstance("SHA1PRNG")
        keyGen.initialize(256, random)
        val pair = keyGen.generateKeyPair()
        val priv = pair.getPrivate() as ECPrivateKey
        val pub = pair.getPublic() as ECPublicKey
        val encodedPublicKey = pub.getEncoded()

        val b64PublicKey = Base64.encodeToString(encodedPublicKey,Base64.DEFAULT)
        val identity = b64PublicKey.substring(37, 37 + 20)
        Log.d(TAG,"Identity = $identity")
        Log.d(TAG,"Public key = $b64PublicKey")

    }

     fun calculateQuadTree(latitude:Double, longitude:Double, zoom :Int):String{
         var quadTreeCalculator  = GlobalMercator()
        val googleTile1 = quadTreeCalculator.GoogleTile(latitude, longitude, zoom)
        val tmsTile = quadTreeCalculator.TMSTileFromGoogleTile(googleTile1[0], googleTile1[1], zoom)
        val quadtree = quadTreeCalculator.QuadTree(tmsTile[0], tmsTile[1], zoom)

        return insertPeriodically(quadtree,"/",1)
    }

    private fun insertPeriodically(
        text: String, insert: String, period: Int
    ): String {
        val builder = StringBuilder(
            text.length + insert.length * (text.length / period) + 1
        )

        var index = 0
        var prefix = ""
        while (index < text.length) {
            // Don't put the insert in the very first iteration.
            // This is easier than appending it *after* each substring
            builder.append(prefix)
            prefix = insert
            builder.append(
                text.substring(
                    index,
                    Math.min(index + period, text.length)
                )
            )
            index += period
        }
        return builder.toString()
    }

    fun parseIVIUserMessage(jsonString:String,topic:Topic):IVIUserMessage{
        var message = IVIUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        var ivigeneralivicontainer = rootJsonObject.optJSONObject("ivigeneralivicontainer")
        if (ivigeneralivicontainer != null){
            var iviglcpartroadsigncode = ivigeneralivicontainer.optJSONObject("iviglcpartroadsigncode")
            if(iviglcpartroadsigncode!=null){
                message.name = iviglcpartroadsigncode.optString("Name","")
                message.trafficSingDescription = iviglcpartroadsigncode.optString("Trafficsigndescription","")
                message.ServiceCategoryCode = iviglcpartroadsigncode.optInt("ServiceCategoryCode",0)
                message.serviceCategory = iviglcpartroadsigncode.optString("serviceCategory","")

            }
        }
        message.topic =topic
        return message
    }

    fun parseVIVIUserMessage(jsonString: String,topic: Topic):VIVIUserMessage{
        var message = VIVIUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        var ivigeneralivicontainer = rootJsonObject.optJSONObject("ivigeneralivicontainer")
        if (ivigeneralivicontainer != null){
            var iviglcpartroadsigncode = ivigeneralivicontainer.optJSONObject("iviglcpartroadsigncode")
            if(iviglcpartroadsigncode!=null){
                message.route = iviglcpartroadsigncode.optString("Route","")
                message.eta = iviglcpartroadsigncode.optString("ETA","")

            }
        }
        message.topic =topic
        return message

    }

    fun parseTopic(topicString:String): Topic{
        var topic = Topic()
       var topicSplit = topicString.split("/")
        topic.basePath =topicSplit.get(0)
        topic.type = topicSplit.get(1)
        topic.data = topicSplit.get(topicSplit.size-1)
        return topic

    }

    fun toKmPerHour(speed: Float): Int {
        return (speed *3.6).toInt()
    }
}
