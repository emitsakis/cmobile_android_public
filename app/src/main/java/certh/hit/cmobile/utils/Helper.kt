package certh.hit.cmobile.utils


import android.os.Build
import android.util.Base64
import android.util.Log
import certh.hit.cmobile.BuildConfig
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


}
