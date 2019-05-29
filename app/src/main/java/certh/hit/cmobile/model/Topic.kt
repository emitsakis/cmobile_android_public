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
    var BASE :String = ""

    @Retention(RetentionPolicy.SOURCE)
    @StringDef(IVI, VIVI,DENM, MAP, SPAT, CAM, SSM, SRM,BASE)
    annotation class Type

    companion object {
        fun createIVI(quadTree: String): Topic {
        var returnTopic = Topic()
            returnTopic.basePath = BASE
                returnTopic.type = IVI
                returnTopic.quadTree=quadTree
                returnTopic.data = "#"
            return returnTopic
        }

        fun createMAP(quadTree: String): Topic {
            var returnTopic = Topic()
            returnTopic.basePath = BASE
            returnTopic.type = MAP
            returnTopic.quadTree=quadTree
            returnTopic.data = "#"
            return returnTopic
        }
        fun createSPAT(quadTree: String): Topic {
            var returnTopic = Topic()
            returnTopic.basePath = BASE
            returnTopic.type = SPAT
            returnTopic.quadTree=quadTree
           // returnTopic.data = "#"
            return returnTopic
        }
        fun createDenm(quadTree: String): Topic {
            var returnTopic = Topic()
            returnTopic.basePath = BASE
            returnTopic.type = DENM
            returnTopic.quadTree=quadTree
            returnTopic.data = "#"
            return returnTopic
        }

//        fun createEgnatia(quadTree: String): Topic {
//            var returnTopic = Topic()
//            returnTopic.basePath = BASE_EGNATIA
//            returnTopic.type = VIVI_EGNATIA
//            returnTopic.quadTree=quadTree
//            returnTopic.data = "#"
//            return returnTopic
//        }


//        fun createVIVI(quadTree: String): Topic {
//            var returnTopic = Topic()
//            returnTopic.basePath = BASE
//            returnTopic.type = VIVI
//            returnTopic.quadTree=quadTree
//            returnTopic.data = "#"
//            return returnTopic
//        }
        const val IVI = "ivi"
        const val DENM = "denm"
        const val MAP = "map"
        const val SPAT = "spat"
        const val BASE ="certh_hit"

        const val VIVI = "v-ivi_hit"
        const val VIVI_EGNATIA = "v-ivi_egnatia"
        const val CAM = "cam"
        const val SSM = "ssm"
        const val SRM = "srm"
        const val BASE_EGNATIA ="egnatia_sa"
        const val BASE_FR ="tt"


    }

    override fun toString(): String {
        return "$basePath/$type/$quadTree/$data"
    }

    fun toStringSpat(): String {
        return "$basePath/$type/$quadTree"
    }


}
