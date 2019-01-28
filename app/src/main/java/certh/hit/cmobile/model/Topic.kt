package certh.hit.cmobile.model

import android.support.annotation.IntRange
import android.support.annotation.StringDef

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by anmpout on 27/01/2019
 */
class Topic {

    var basePath: String? = null
    var type:String? = null
    var provider: String? = null
    var version: String? = null
    var quadTree: String? = null
    var typeId: String? = null
    var data: String? = null

    @Retention(RetentionPolicy.SOURCE)
    @StringDef(IVI, DENM, MAP, SPAT, CAM, SSM, SRM)
    annotation class Type

    companion object {
        const val IVI = "ivi"
        const val DENM = "denm"
        const val MAP = "map"
        const val SPAT = "spat"
        const val CAM = "cam"
        const val SSM = "ssm"
        const val SRM = "srm"
    }
}
