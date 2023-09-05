package com.example.allhome.data.entities

import androidx.room.*
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "todos", indices = [Index(value=["unique_id"],unique = true)])
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) // Use autoGenerate = true for auto-incrementing ID
    @ColumnInfo(name = "id") var id: Int = 0, // Auto-incrementing ID field
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "group_unique_id",index = true) var groupUniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "description") var description:String,
    @ColumnInfo(name = "due_date",defaultValue="0000-00-00 00:00:00") var dueDate:String,
    @ColumnInfo(name = "repeat_every",defaultValue="0") var repeatEvery:Int,
    @ColumnInfo(name = "repeat_every_type",defaultValue="0000-00-00 00:00:00") var repeatEveryType:String,
    @ColumnInfo(name = "repeat_until",defaultValue="0000-00-00 00:00:00") var repeatUntil:String,
    @ColumnInfo(name = "notify_at",defaultValue="0") var notifyAt:Int,
    @ColumnInfo(name = "notify_every_type",defaultValue="none") var notifyEveryType:String,
    @ColumnInfo(name = "is_set_in_monday", defaultValue = "$NOT_SET") var isSetInMonday: Int,
    @ColumnInfo(name = "is_set_in_tuesday", defaultValue = "$NOT_SET") var isSetInTuesday: Int,
    @ColumnInfo(name = "is_set_in_wednesday", defaultValue = "$NOT_SET") var isSetInWednesday: Int,
    @ColumnInfo(name = "is_set_in_thursday", defaultValue = "$NOT_SET") var isSetInThursday: Int,
    @ColumnInfo(name = "is_set_in_friday", defaultValue = "$NOT_SET") var isSetInFriday: Int,
    @ColumnInfo(name = "is_set_in_saturday", defaultValue = "$NOT_SET") var isSetInSaturday: Int,
    @ColumnInfo(name = "is_set_in_sunday", defaultValue = "$NOT_SET") var isSetInSunday: Int,
    @ColumnInfo(name = "item_status",defaultValue="0") var itemStatus:Int,//0 active,1=deleted,2=permanently deleted
    @ColumnInfo(name = "uploaded",defaultValue="0") var uploaded:Int, //0=not yet uploaded,1=uploaded
    @ColumnInfo(name = "is_finished",defaultValue= "$NOT_FINISHED") var isFinished:Int, //0=not yet done,1=done
    @ColumnInfo(name = "datetime_finished") var datetimeFinished:String,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String
){
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
        const val NOT_RECURRING = 0
        const val RECURRING = 1
        const val NOT_FINISHED = 0
        const val FINISHED = 1
        const val SET = 1
        const val NOT_SET = 0

    }
}
data class TodosWithSubTaskCount(
    @Embedded val todoEntity:TodoEntity,
    var totalSubTaskCount: Int = 0,
)