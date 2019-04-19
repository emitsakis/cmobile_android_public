package certh.hit.cmobile.model

/**
 * Created by anmpout on 18/02/2019
 */
class DENMUserMessage : UserMessage(){
    var name :String? = null
    var causeCode :Int? =null
    var subCauseCode :Int? =null
    var relevanceZoneNr :Int? = null
    var relevanceZones : List<RelevanceZone>? = null
    var extraTexts :List<ExtraText>? = null
    var denmIdentificationNumber :Int? = null
    var denmStatus :Int? = null
    var timestamp :Long? = null
    var duration :Int? = null

}
