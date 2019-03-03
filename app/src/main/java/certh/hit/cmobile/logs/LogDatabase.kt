package certh.hit.cmobile.logs

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * Created by anmpout on 28/02/2019
 */
@Database(entities = arrayOf(GPSLogger::class,MessageLogger::class), version = 1)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao



    companion object {
        private var INSTANCE: LogDatabase? = null

        fun getDatabase(context: Context): LogDatabase? {
            if (INSTANCE == null) {
                synchronized(LogDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder<LogDatabase>(
                            context.applicationContext,
                            LogDatabase::class.java!!, "database_cmobile")
                            .build()

                    }
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}