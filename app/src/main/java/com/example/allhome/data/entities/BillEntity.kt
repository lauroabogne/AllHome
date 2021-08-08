package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = BillEntity.TABLE_NAME)
data class BillEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_GROUP_UNIQUE_ID) var groupUniqueId:String,
    @ColumnInfo(name= COLUMN_AMOUNT) var amount:Double,
    @ColumnInfo(name= COLUMN_NAME) var name:String,
    @ColumnInfo(name= COLUMN_DUE_DATE) var dueDate:String,
    @ColumnInfo(name= COLUMN_IS_RECURRING) var isRecurring:Int,
    @ColumnInfo(name= COLUMN_REPEAT_EVERY) var repeatEvery:Int,
    @ColumnInfo(name= COLUMN_REPEAT_BY) var repeatBy:String,
    @ColumnInfo(name= COLUMN_REPEAT_UNTIL) var repeatUntil:String,
    @ColumnInfo(name= COLUMN_REPEAT_COUNT) var repeatCount:Int,
    @ColumnInfo(name= COLUMN_IMAGE_NAME) var imageName:String,
    @ColumnInfo(name= COLUMN_STATUS,defaultValue="${NOT_DELETED_STATUS}") var status:Int,
    @ColumnInfo(name= COLUMN_UPLOADED,defaultValue="${NOT_UPLOADED}") var uploaded:Int,
    @ColumnInfo(name= COLUMN_CREATED,defaultValue="CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED,defaultValue="CURRENT_TIMESTAMP") var modified:String
): Parcelable {
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
        const val NOT_RECURRING = 0
        const val RECURRING = 1

        const val TABLE_NAME ="bills"
        const val COLUMN_GROUP_UNIQUE_ID ="group_unique_id"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_AMOUNT ="amount"
        const val COLUMN_NAME ="name"
        const val COLUMN_DUE_DATE ="due_date"
        const val COLUMN_IS_RECURRING ="is_recurring"
        const val COLUMN_REPEAT_EVERY ="repeat_every"
        const val COLUMN_REPEAT_BY ="repeat_by"
        const val COLUMN_REPEAT_UNTIL ="repeat_until"
        const val COLUMN_REPEAT_COUNT ="repeat_count"
        const val COLUMN_IMAGE_NAME ="image_name"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}
