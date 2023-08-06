package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = AlarmRecordsEntity.TABLE_NAME)
data class AlarmRecordsEntity(

    @PrimaryKey(autoGenerate = true) // Use autoGenerate = true for auto-incrementing ID
    @ColumnInfo(name = COLUMN_ID) var id: Long = 0, // Auto-incrementing ID field
    @ColumnInfo(name= COLUMN_TYPE) var type:String,
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_CREATED,defaultValue="CURRENT_TIMESTAMP") var created:String
): Parcelable {
    companion object{

        const val TYPE_GROCERY_LIST = "grocery_list"
        const val TYPE_GROCERY_TODO = "todo"

        const val TABLE_NAME ="alarm_records"
        const val COLUMN_ID ="id"
        const val COLUMN_TYPE ="type"
        const val COLUMN_UNIQUE_ID = "unique_id"
        const val COLUMN_CREATED ="created"

    }
}
