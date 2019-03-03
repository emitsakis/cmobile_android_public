package certh.hit.cmobile.utils


import android.os.Environment
import android.util.Base64
import android.util.Log
import certh.hit.cmobile.BuildConfig
import certh.hit.cmobile.model.*
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
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
    val ZOOM_LEVEL = 17


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
        var ivigeographiclocationcontainer = rootJsonObject.optJSONObject("ivigeographiclocationcontainer")
        if (ivigeographiclocationcontainer != null){
            message.latitude = ivigeographiclocationcontainer.optDouble("POINT_X",0.0)
            message.longitude = ivigeographiclocationcontainer.optDouble("POINT_Y",0.0)
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
        var ivigeographiclocationcontainer = rootJsonObject.optJSONObject("ivigeographiclocationcontainer")
        if (ivigeographiclocationcontainer != null){
            message.latitude = ivigeographiclocationcontainer.optDouble("latitude",0.0)
            message.longitude = ivigeographiclocationcontainer.optDouble("longitude",0.0)
        }
        message.topic =topic
        return message

    }

    fun parseMAPMessage(jsonString:String,topic:Topic): MAPUserMessage {
        var message = MAPUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject != null){
            message.indexNumber = rootJsonObject.optInt("indexnumber",0)
            message.latitude = rootJsonObject.optDouble("latitude",0.0)
            message.longitude = rootJsonObject.optDouble("longitude",0.0)
        }
        message.topic =topic
        return message
    }

    fun parseSPATMessage(jsonString:String,topic:Topic): SPATUserMessage {
        var message = SPATUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject != null){
            message.indexNumber = rootJsonObject.optInt("indexnumber",0)
            message.eventState = rootJsonObject.optString("eventstate")
            message.likelyTime = rootJsonObject.optString("likelytime")
        }
        message.topic =topic
        return message
    }

    fun parseTopic(topicString:String): Topic{
        var topic = Topic()
       var topicSplit = topicString.split("/")
        topic.basePath =topicSplit.get(0)
        topic.type = topicSplit.get(1)
        var tmpQuadtree = ""
        for (i in 2 until  topicSplit.size-1 step 1) {

            if(i==topicSplit.size-1){
                tmpQuadtree = tmpQuadtree+ topicSplit.get(i)

            }else{
                tmpQuadtree = tmpQuadtree+ topicSplit.get(i)+"/"
            }
        }

        topic.quadTree = tmpQuadtree
        topic.data = topicSplit.get(topicSplit.size-1)
        return topic

    }

    fun toKmPerHour(speed: Float): Int {
        return (speed *3.6).toInt()
    }

    fun grapMinutes(eta: String?): String? {
        var parts = eta!!.split(":")
        var minutes = parts.get(1)
        return minutes;

    }


    fun appendLog(text:String,filename:String)
    {
        val externalStorageDir = Environment.getExternalStorageDirectory()
        val logFile = File(externalStorageDir, filename+".txt")
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch ( ex: IOException)
            {
                ex.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            var buf = BufferedWriter( FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch ( e:IOException)
        {

            e.printStackTrace();
        }
    }

    fun parseVIVIEgnatiaUserMessage(jsonString: String, topic: Topic): EgnatiaUserMessage {

        var message = EgnatiaUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        var ivigeneralivicontainer = rootJsonObject.optJSONObject("ivigeneralivicontainer")
        if (ivigeneralivicontainer != null){
            var iviglcpartroadsigncode = ivigeneralivicontainer.optJSONObject("iviglcpartroadsigncode")
            if(iviglcpartroadsigncode!=null){
                message.name = iviglcpartroadsigncode.optString("Name","")
                message.egantiaMessage = iviglcpartroadsigncode.optString("MESSAGE","")
            }
        }

        message.topic =topic
        return message
    }

    fun parseDENMUserMessage(jsonString: String, topic: Topic): DENMUserMessage {
        var message = DENMUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        var denm = rootJsonObject.optJSONObject("denm")
        if(denm != null){
            var situation = denm.optJSONObject("situation")
            if(situation != null){
                var eventType = situation.optJSONObject("eventType")
                if(eventType!= null){
                    message.causeCode = eventType.optInt("causeCode",0)
                    message.subCauseCode = eventType.optInt("subCauseCode",0)
                }

            }
            var management =  denm.optJSONObject("management")
            if(management != null){
              var eventPosition =  management.optJSONObject("eventPosition")
                if(eventPosition != null){
                  var lat =  eventPosition.optInt("latitude")
                  var digits = countDigits(lat)
                    message.latitude =  lat/  Math.pow(10.0,digits-1)
                    var long =  eventPosition.optInt("longitude")
                    digits = countDigits(long)
                    message.longitude =  long/ Math.pow(10.0,digits-1)
                }

            }

        }
        message.topic =topic
        return message
    }
        fun countDigits (number:Int):Double{
            var count = 0
            var num = number

            while (num != 0) {
                // num = num/10
                num /= 10
                ++count
            }
            return count.toDouble()
        }

}
