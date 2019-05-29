package certh.hit.cmobile.model

/**
 * Created by anmpout on 13/02/2019
 */
class SPATUserMessage : UserMessage() {
    var indexNumber: Int? = null

    var mapMessage: MAPUserMessage? = null
    var relevanceZoneNr :Int? = null
    var relevanceZones : List<RelevanceZone>? = null
    var spatIdentificationNumber :Int? = null
    var spatStatus :Int? = null
    var timestamp :Long? = null
    var eventState: String? = null
    var likelyTime: Int? = null
}
