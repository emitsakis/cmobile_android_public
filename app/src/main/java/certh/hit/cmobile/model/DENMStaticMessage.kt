package certh.hit.cmobile.model

import com.google.gson.annotations.SerializedName

import java.util.ArrayList

/**
 * Created by anmpout on 12/03/2019
 */
class DENMStaticMessage {
    @SerializedName("demn")
    var list: ArrayList<DENM>? = null

    class DENM {
        @SerializedName("code")
        var code: Int = 0
        @SerializedName("subcode")
        var subcode: Int = 0
        @SerializedName("message")
        var message: Int = 0
    }
}

