package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ExpensesCategoriesEntity.TABLE_NAME)
data class ExpensesCategoriesEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) val uniqueId:String,
    @ColumnInfo(name = COLUMN_NAME) val name: String,
    @ColumnInfo(name = COLUMN_DESCRIPTION) val description: String?,
    @ColumnInfo(name= BillEntity.COLUMN_STATUS,defaultValue="${NOT_DELETED_STATUS}") var status:Int,
    @ColumnInfo(name= BillEntity.COLUMN_UPLOADED,defaultValue="${NOT_UPLOADED}") var uploaded:Int,
    @ColumnInfo(name= BillEntity.COLUMN_CREATED,defaultValue="CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name= BillEntity.COLUMN_MODIFIED,defaultValue="CURRENT_TIMESTAMP") var modified:String
){
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1

        const val TABLE_NAME ="expenses_categories"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_NAME ="name"
        const val COLUMN_DESCRIPTION="description"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}