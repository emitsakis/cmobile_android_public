package certh.hit.cmobile.model

/**
 * Created by anmpout on 04/02/2019
 */
class IVIUserMessage : UserMessage(){
    var name :String? = null
    var relevanceZoneNr :Int? = null
    var relevanceZones : List<RelevanceZone>? = null
    var iviIdentificationNumber :Int? = null
    var iviStatus :Int? = null
    var timestamp :Long? = null
    var iviType :Int? = null
    var travelTime :Int? = null
    var serviceCategoryCode :Int? = null
    var pictogramCategoryCode :Int? = null
    var extraTexts :List<ExtraText>? = null
//    {"header":{"stationID":57001,"messageID":6,"protocolVersion":1,"serviceProviderId":{"providerIdentifier":"CERTH","countryCode":"GR"}},"location":{"relevanceZoneNr":1,"relevanceZone":[{"zone":[{"deltaLongitude":96,"deltaLatitude":680},{"zoneId":1,"type":"area"}]}],"latitude":406150060,"longitude":229541950},"ivi":{"iviIdentificationNumber":7,"iviStatus":0,"timestamp":1555504200,"iviType":2,"travelTime":150,"serviceCategoryCode":13,"pictogramCategoryCode":887,"extraText":[{"textContent":"","language":"EN"},{"textContent":"","language":"EN"},{"textContent":"","language":"EN"},{"textContent":"","language":"EN"}]}}
}
