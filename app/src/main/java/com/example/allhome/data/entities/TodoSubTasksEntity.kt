package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "todo_subtasks")
data class TodoSubTasksEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id",index = true) var uniqueId:String,
    @ColumnInfo(name = "todo_unique_id",index = true) var todoUniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "item_status",defaultValue="0") var itemStatus:Int,//0 active,1=deleted,2=permanently deleted
    @ColumnInfo(name = "uploaded",defaultValue="0") var uploaded:Int, //0=not yet uploaded,1=uploaded
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String
)