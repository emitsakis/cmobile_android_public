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
    fun insertAll(vararg logs :GPSLogger)

    @Insert
    fun insert(user:GPSLogger)
}