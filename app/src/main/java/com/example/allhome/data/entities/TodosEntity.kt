package com.example.allhome.data.entities

import androidx.room.*
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id",index = true) var uniqueId:String,
    @ColumnInfo(name = "group_unique_id",index = true) var groupUniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "due_date",defaultValue="0000-00-00 00:00:00") var dueDate:String,
    @ColumnInfo(name = "repeat_every",defaultValue="0") var repeatEvery:Int,
    @ColumnInfo(name = "repeat_every_type",defaultValue="0000-00-00 00:00:00") var repeatEveryType:String,
    @ColumnInfo(name = "repeat_until",defaultValue="0000-00-00 00:00:00") var repeatUntil:String,
    @ColumnInfo(name = "notify_every",defaultValue="0") var notifyEvery:Int,
    @ColumnInfo(name = "notify_every_type",defaultValue="none") var notifyEveryType:String,
    @ColumnInfo(name = "item_status",defaultValue="0") var itemStatus:Int,//0 active,1=deleted,2=permanently deleted
    @ColumnInfo(name = "uploaded",defaultValue="0") var uploaded:Int, //0=not yet uploaded,1=uploaded
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
    }
}
data class TodosWithSubTaskCount(
    @Embedded val todoEntity:TodoEntity,
    var totalSubTaskCount: Int = 0,
)