package certh.hit.cmobile.logs

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by anmpout on 03/03/2019
 */
@Entity(tableName = "MessageLogger")
data class MessageLogger (
@PrimaryKey(autoGenerate = true) var id: Long?,
@ColumnInfo(name = "timestamp") var timestamp: Long?,
@ColumnInfo(name = "topic") var topic: String?,
@ColumnInfo(name = "message") var message: String?

) {
    constructor() : this(null,0,"","")
}