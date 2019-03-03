package certh.hit.cmobile.logs

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

/**
 * Created by anmpout on 28/02/2019
 */

@Dao
interface LogDao {
    @Query("SELECT * FROM GPSLogger")
    fun getAllGPSLogger(): List<GPSLogger>

    @Query("DELETE FROM GPSLogger")
    fun deleteAllGPSLogger()

    @Insert
    fun insertAllGPS(vararg logs: GPSLogger)

    @Insert
    fun insertGPSLogger(log: GPSLogger)

    @Query("SELECT * FROM MessageLogger")
    fun getAllMessages(): List<MessageLogger>

    @Query("DELETE FROM MessageLogger")
    fun deleteAllMessage()

    @Insert
    fun insertAllMessages(vararg messages: MessageLogger)

    @Insert
    fun insertMessage(message: MessageLogger)
}