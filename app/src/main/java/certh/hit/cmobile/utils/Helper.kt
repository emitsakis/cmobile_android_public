package certh.hit.cmobile.utils


import android.content.Context
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
import java.util.*
import kotlin.collections.ArrayList as ArrayList1


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

    private fun insertPeriodically(text: String, insert: String, period: Int): String {
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
        if(rootJsonObject.has("header") && rootJsonObject.optJSONObject("header")!= null){
            message.header = parseHeaderUserMessage(rootJsonObject.optJSONObject("header").toString())
        }
        if(rootJsonObject.has("location") && rootJsonObject.optJSONObject("location")!= null){
           var locationJson = rootJsonObject.optJSONObject("location")
            if(locationJson.has("relevanceZoneNr")) {
                message.relevanceZoneNr = locationJson.optInt("relevanceZoneNr")
            }
            if(locationJson.has("relevanceZoneType")) {
                message.relevanceZoneType = locationJson.optString("relevanceZoneType")
            }
            if(locationJson.has("latitude")) {
                message.latitude = locationJson.optInt("latitude")
            }
            if(locationJson.has("longitude")) {
                message.longitude = locationJson.optInt("longitude")
            }
            if(locationJson.has("relevanceZone") && locationJson.optJSONArray("relevanceZone")!= null) {
              message.relevanceZones = ArrayList()
                var relevanceZoneJson = locationJson.optJSONArray("relevanceZone")
                for(i in 0 until relevanceZoneJson.length()){
                    var tmpRelevanceZone = parseRelevanceZone(relevanceZoneJson.get(i).toString(),message.latitude!!,
                        message.longitude!!
                    )
                    (message.relevanceZones as ArrayList<RelevanceZone>).add(tmpRelevanceZone)
                }

            }

        }
        if(rootJsonObject.has("ivi") && rootJsonObject.optJSONObject("ivi")!= null){
            var iviJson = rootJsonObject.optJSONObject("ivi")
            if(iviJson.has("iviIdentificationNumber")){
                message.iviIdentificationNumber = iviJson.optInt("iviIdentificationNumber")
            }
            if(iviJson.has("iviStatus")){
                message.iviStatus = iviJson.optInt("iviStatus")
            }
            if(iviJson.has("timestamp")){
                message.timestamp = iviJson.optLong("timestamp")
            }
            if(iviJson.has("iviType")){
                message.iviType = iviJson.optInt("iviType")
            }
            if(iviJson.has("travelTime")){
                message.travelTime = iviJson.optInt("travelTime")
            }
            if(iviJson.has("serviceCategoryCode")){
                message.serviceCategoryCode = iviJson.optInt("serviceCategoryCode")
            }
            if(iviJson.has("pictogramCategoryCode")){
                message.pictogramCategoryCode = iviJson.optInt("pictogramCategoryCode")
            }

        if(iviJson.has("extraText") && iviJson.optJSONArray("extraText")!= null) {
            var extraTextJson = iviJson.optJSONArray("extraText")
                message.extraTexts = ArrayList()
                for (i in 0 until extraTextJson.length()) {
                var tmpExtraText =  parseExtraText(extraTextJson.get(i).toString())
                    (message.extraTexts as ArrayList<ExtraText>).add(tmpExtraText)
                }
        }
        }
        message.topic =topic
        return message
    }

    private fun parseExtraText(jsonString: String): ExtraText {
        var extraText = ExtraText()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject.has("textContent")){
            extraText.textContent = rootJsonObject.optString("textContent","")
        }
        if(rootJsonObject.has("language")){
            extraText.language = rootJsonObject.optString("language","")
        }
        return extraText
    }

    fun parseRelevanceZone(jsonString: String,referenceLat:Int,referenceLon:Int):RelevanceZone{
        var relevanceZone = RelevanceZone()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject.has("zone") && rootJsonObject.optJSONArray("zone") != null) {
            var zoneArray = rootJsonObject.optJSONArray("zone")
            var zoneArrayList = ArrayList<Zone>()
            for (i in 0 until zoneArray.length()) {
                var zoneCoordinates = zoneArray.optJSONObject(i)

                if (zoneCoordinates != null) {
                    var tmpZone = Zone()
                    if (zoneCoordinates.has("deltaLongitude")) {
                        tmpZone.deltaLongitude = zoneCoordinates.optInt("deltaLongitude", 0)
                        tmpZone.actualLongitude = calculateDeltas(tmpZone.deltaLongitude!!,referenceLon)

                    }
                    if (zoneCoordinates.has("deltaLatitude")) {
                        tmpZone.deltaLatitude = zoneCoordinates.optInt("deltaLatitude", 0)
                        tmpZone.actualLatitude = calculateDeltas(tmpZone.deltaLatitude!!,referenceLat)


                    }
                    zoneArrayList.add(tmpZone)
                }
            }
            relevanceZone.zones = zoneArrayList
        }
        return relevanceZone
    }

    fun parseHeaderUserMessage(jsonString: String):MessageHeader{
        var header = MessageHeader()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject.has("stationID")) {
            header.stationID = rootJsonObject.optInt("stationID")
        }
        if(rootJsonObject.has("messageID")) {
            header.messageID = rootJsonObject.optInt("messageID")
        }
        if(rootJsonObject.has("protocolVersion")) {
            header.protocolVersion = rootJsonObject.optInt("protocolVersion")
        }
        if (rootJsonObject.has("serviceProviderId") && rootJsonObject.optJSONObject("serviceProviderId")!= null){
            var serviceProviderIdJson = rootJsonObject.optJSONObject("serviceProviderId")
            if(serviceProviderIdJson.has("providerIdentifier") && serviceProviderIdJson.optString("providerIdentifier")!=null){
                header.providerIdentifier = serviceProviderIdJson.optString("providerIdentifier")
            }
            if(serviceProviderIdJson.has("countryCode") && serviceProviderIdJson.optString("countryCode")!=null){
                header.countryCode = serviceProviderIdJson.optString("countryCode")
            }
        }
        return header
    }

    fun parseVIVIUserMessage(jsonString: String,topic: Topic):VIVIUserMessage{
        var message = VIVIUserMessage()
//        val rootJsonObject = JSONObject(jsonString)
//        var ivigeneralivicontainer = rootJsonObject.optJSONObject("ivigeneralivicontainer")
//        if (ivigeneralivicontainer != null){
//            var iviglcpartroadsigncode = ivigeneralivicontainer.optJSONObject("iviglcpartroadsigncode")
//            if(iviglcpartroadsigncode!=null){
//                message.route = iviglcpartroadsigncode.optString("Route","")
//                message.eta = iviglcpartroadsigncode.optString("ETA","")
//
//            }
//        }
//        var ivigeographiclocationcontainer = rootJsonObject.optJSONObject("ivigeographiclocationcontainer")
//        if (ivigeographiclocationcontainer != null){
//            message.latitude = ivigeographiclocationcontainer.optDouble("latitude",0.0)
//            message.longitude = ivigeographiclocationcontainer.optDouble("longitude",0.0)
//        }
        message.topic =topic
        return message

    }

    fun parseMAPMessage(jsonString:String,topic:Topic): MAPUserMessage {
        var message = MAPUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject.has("header") && rootJsonObject.optJSONObject("header")!= null){
            message.header = parseHeaderUserMessage(rootJsonObject.optJSONObject("header").toString())
        }
        if(rootJsonObject.has("location") && rootJsonObject.optJSONObject("location")!= null){
            var locationJson = rootJsonObject.optJSONObject("location")
            if(locationJson.has("relevanceZoneNr")) {
                message.relevanceZoneNr = locationJson.optInt("relevanceZoneNr")
            }
            if(locationJson.has("latitude")) {
                message.latitude = locationJson.optInt("latitude")
            }
            if(locationJson.has("longitude")) {
                message.longitude = locationJson.optInt("longitude")
            }
            if(locationJson.has("relevanceZone") && locationJson.optJSONArray("relevanceZone")!= null) {
                message.relevanceZones = ArrayList()
                var relevanceZoneJson = locationJson.optJSONArray("relevanceZone")
                for(i in 0 until relevanceZoneJson.length()){
                    var tmpRelevanceZone = parseRelevanceZone(relevanceZoneJson.get(i).toString(),message.latitude!!,
                        message.longitude!!)
                    (message.relevanceZones as ArrayList<RelevanceZone>).add(tmpRelevanceZone)
                }

            }

        }
        if(rootJsonObject.has("map") && rootJsonObject.optJSONObject("map")!= null) {
            var mapJson = rootJsonObject.optJSONObject("map")
            if (mapJson.has("mapIdentificationNumber")) {
                message.mapIdentificationNumber = mapJson.optInt("mapIdentificationNumber")
            }
            if (mapJson.has("mapStatus")) {
                message.mapStatus = mapJson.optInt("mapStatus")
            }
            if (mapJson.has("timestamp")) {
                message.timestamp = mapJson.optLong("timestamp")
            }
        }
        message.topic =topic
        return message
    }

    fun parseSPATMessage(jsonString:String,topic:Topic): SPATUserMessage {
        var message = SPATUserMessage()
        val rootJsonObject = JSONObject(jsonString)
        if(rootJsonObject.has("header") && rootJsonObject.optJSONObject("header")!= null){
            message.header = parseHeaderUserMessage(rootJsonObject.optJSONObject("header").toString())
        }
        if(rootJsonObject.has("location") && rootJsonObject.optJSONObject("location")!= null){
            var locationJson = rootJsonObject.optJSONObject("location")
            if(locationJson.has("relevanceZoneNr")) {
                message.relevanceZoneNr = locationJson.optInt("relevanceZoneNr")
            }
            if(locationJson.has("latitude")) {
                message.latitude = locationJson.optInt("latitude")
            }
            if(locationJson.has("longitude")) {
                message.longitude = locationJson.optInt("longitude")
            }
            if(locationJson.has("relevanceZone") && locationJson.optJSONArray("relevanceZone")!= null) {
                message.relevanceZones = ArrayList<RelevanceZone>()
                var relevanceZoneJson = locationJson.optJSONArray("relevanceZone")
                for(i in 0 until relevanceZoneJson.length()){
                    var tmpRelevanceZone = parseRelevanceZone(relevanceZoneJson.get(i).toString(),message.latitude!!,
                        message.longitude!!)
                    (message.relevanceZones as ArrayList<RelevanceZone>).add(tmpRelevanceZone)
                }

            }

        }
        if(rootJsonObject.has("spat") && rootJsonObject.optJSONObject("spat")!= null) {
            var spatJson = rootJsonObject.optJSONObject("spat")
            if (spatJson.has("spatIdentificationNumber")) {
                message.spatIdentificationNumber = spatJson.optInt("spatIdentificationNumber")
            }
            if (spatJson.has("spatStatus")) {
                message.spatStatus = spatJson.optInt("spatStatus")
            }
            if (spatJson.has("timestamp")) {
                message.timestamp = spatJson.optLong("timestamp")
            }
            if (spatJson.has("eventState")) {
                message.eventState = spatJson.optString("eventState","")
            }
            if (spatJson.has("likelyTime")) {
                message.likelyTime = spatJson.optInt("likelyTime")
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
        var tmpQuadtree = ""
        for (i in 2 until  topicSplit.size-1 step 1) {

            if(i==topicSplit.size-1){
                tmpQuadtree = tmpQuadtree + topicSplit.get(i)

            }else{
                tmpQuadtree = tmpQuadtree + topicSplit.get(i)+"/"
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


    fun appendLog(text:String,filename:String){
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
        if(rootJsonObject.has("header") && rootJsonObject.optJSONObject("header")!= null){
            message.header = parseHeaderUserMessage(rootJsonObject.optJSONObject("header").toString())
        }
        if(rootJsonObject.has("location") && rootJsonObject.optJSONObject("location")!= null){
            var locationJson = rootJsonObject.optJSONObject("location")
            if(locationJson.has("relevanceZoneNr")) {
                message.relevanceZoneNr = locationJson.optInt("relevanceZoneNr")
            }
            if(locationJson.has("latitude")) {
                message.latitude = locationJson.optInt("latitude")
            }
            if(locationJson.has("longitude")) {
                message.longitude = locationJson.optInt("longitude")
            }
            if(locationJson.has("relevanceZone") && locationJson.optJSONArray("relevanceZone")!= null) {
                message.relevanceZones = ArrayList()
                var relevanceZoneJson = locationJson.optJSONArray("relevanceZone")
                for(i in 0 until relevanceZoneJson.length()){
                    var tmpRelevanceZone = parseRelevanceZone(relevanceZoneJson.get(i).toString(),message.latitude!!,
                        message.longitude!!)
                    (message.relevanceZones as ArrayList<RelevanceZone>).add(tmpRelevanceZone)
                }

            }

        }
        if(rootJsonObject.has("denm") && rootJsonObject.optJSONObject("denm")!= null) {
            var denmJson = rootJsonObject.optJSONObject("denm")
            if(denmJson.has("denmIdentificationNumber")){
               message.denmIdentificationNumber = denmJson.optInt("denmIdentificationNumber")
            }
            if(denmJson.has("denmStatus")){
                message.denmStatus = denmJson.optInt("denmStatus")
            }
            if(denmJson.has("timestamp")){
                message.timestamp = denmJson.optLong("timestamp")
            }
            if(denmJson.has("duration")){
                message.duration = denmJson.optInt("duration")
            }
            if(denmJson.has("causeCode")){
                message.causeCode = denmJson.optInt("causeCode")
            }
            if(denmJson.has("subCauseCode")){
                message.subCauseCode = denmJson.optInt("subCauseCode")
            }
            if (denmJson.has("extraText") && denmJson.optJSONArray("extraText") != null) {
                var extraTextJson = denmJson.optJSONArray("extraText")
                message.extraTexts = ArrayList()
                for (i in 0 until extraTextJson.length()) {
                    var tmpExtraText = parseExtraText(extraTextJson.get(i).toString())
                    (message.extraTexts as ArrayList<ExtraText>).add(tmpExtraText)
                }
            }
        }
//        val rootJsonObject = JSONObject(jsonString)
//        var denm = rootJsonObject.optJSONObject("denm")
//        if(denm != null){
//            var situation = denm.optJSONObject("situation")
//            if(situation != null){
//                var eventType = situation.optJSONObject("eventType")
//                if(eventType!= null){
//                    message.causeCode = eventType.optInt("causeCode",0)
//                    message.subCauseCode = eventType.optInt("subCauseCode",0)
//                }
//
//            }
//            var management =  denm.optJSONObject("management")
//            if(management != null){
//              var eventPosition =  management.optJSONObject("eventPosition")
//                if(eventPosition != null){
//                  var lat =  eventPosition.optInt("latitude")
//                  var digits = countDigits(lat)
//                    message.latitude =  lat/  Math.pow(10.0,digits-1)
//                    var long =  eventPosition.optInt("longitude")
//                    digits = countDigits(long)
//                    message.longitude =  long/ Math.pow(10.0,digits-1)
//                }
//
//            }


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

    fun getAssetJsonData(context: Context,filename:String): String? {
        var json: String? = null
        try {
            val inStream = context.getAssets().open(filename)
            val size =  inStream.available()
            val buffer = ByteArray(size)
            inStream.read(buffer)
            inStream.close()
            json = String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        Log.e("data", json)
        return json

    }

    fun convertSecToMin(travelTime: Int?): String {
        var minutes = travelTime!!.div(60)
    return minutes.toString()
    }
    fun getViviNameFromID(iviIdentificationNumber: Int?):String {
        var viviName = "Path Name"
        if(iviIdentificationNumber==7){
            viviName = "V.OLGAS - YMCA"
        }
    return viviName
    }

    fun calculateDeltas(delta:Int,referenceCor:Int):Double{
        var finalCoordinates = referenceCor - delta
        var digits = countDigits(finalCoordinates)
        Log.d("calculateDeltas",finalCoordinates.toString())
        var returnValue =  finalCoordinates/  Math.pow(10.0,digits-2)
      //  returnValue = referenceCor.div(10000000.0)
        Log.d("calculateDeltas",returnValue.toString())
    return returnValue
    }

}
