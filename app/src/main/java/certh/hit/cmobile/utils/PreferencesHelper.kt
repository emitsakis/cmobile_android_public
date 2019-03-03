package certh.hit.cmobile.utils

import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by anmpout on 03/03/2019
 */
class PreferencesHelper(context: Context){
    companion object {
        const val DEVELOP_MODE = false
        private const val DEVICE_TOKEN = "certh.hit.prefs.DEVICE_TOKEN"
        private const val DATA_COLLECTION = "certh.hit.prefs.DATA_COLLECTION"
    }

    private val preferences = PreferenceManager .getDefaultSharedPreferences(context)
    var deviceToken = preferences.getString(DEVICE_TOKEN, "")
        set(value) = preferences.edit().putString(DEVICE_TOKEN,value).apply()
    var dataCollection = preferences.getBoolean(DATA_COLLECTION, true)
        set(value) = preferences.edit().putBoolean(DATA_COLLECTION,value).apply()
}