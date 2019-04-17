package certh.hit.cmobile.logs

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by anmpout on 28/02/2019
 */
@Entity(tableName = "GPSLogger")
data class GPSLogger(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "timestamp") var timestamp: Long?,
    @ColumnInfo(name = "latitude") var latitude: Double?,
    @ColumnInfo(name = "longitude") var longitude: Double?,
    @ColumnInfo(name = "altitude") var altitude: Double?,
    @ColumnInfo(name = "speed") var speed: Double?,
    @ColumnInfo(name = "orientation") var orientation: Double?,
    @ColumnInfo(name = "polygonId") var polygonId: Int?

) {
    constructor() : this(null,0,0.0,0.0,0.0,0.0,
        0.0,0)
}