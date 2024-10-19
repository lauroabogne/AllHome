package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allhome.data.entities.BillEntity.Companion
import kotlinx.android.parcel.Parcelize

@Entity(tableName = ExpensesEntity.TABLE_NAME)
data class ExpensesEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) val uniqueId:String,
    @ColumnInfo(name = COLUMN_NAME) val name: String?,
    @ColumnInfo(name = COLUMN_CATEGORY) val category: String?,
    @ColumnInfo(name = COLUMN_EXPENSES_DATE) val expenseDate: String?,
    @ColumnInfo(name = COLUMN_AMOUNT) val amount: Double,
    @ColumnInfo(name= COLUMN_STATUS,defaultValue="$NOT_DELETED_STATUS") var status:Int?,
    @ColumnInfo(name= COLUMN_UPLOADED,defaultValue="$NOT_UPLOADED") var uploaded:Int?,
    @ColumnInfo(name= COLUMN_CREATED,defaultValue="CURRENT_TIMESTAMP") var created:String?,
    @ColumnInfo(name= COLUMN_MODIFIED,defaultValue="CURRENT_TIMESTAMP") var modified:String?

    ){
    companion object{

        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1

        const val TABLE_NAME ="expenses"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_AMOUNT ="amount"
        const val COLUMN_NAME ="name"
        const val COLUMN_CATEGORY ="category"
        const val COLUMN_EXPENSES_DATE ="expense_date"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}

data class ExpensesEntityWithItemNameAndType(
    @Embedded val expensesEntity: ExpensesEntity,
    var expense_type:String,
    var item_name:String

)


