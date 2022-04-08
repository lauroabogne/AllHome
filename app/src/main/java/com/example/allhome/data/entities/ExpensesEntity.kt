package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "expenses")
data class ExpensesEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= "unique_id") val uniqueId:String,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "expense_date") val expenseDate: String?,
    @ColumnInfo(name = "amount") val amount: Double,

    )

data class ExpensesEntityWithItemNameAndType(
    @Embedded val expensesEntity: ExpensesEntity,
    var expense_type:String,
    var item_name:String

)


