package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import kotlinx.android.parcel.Parcelize

data class ExpensesEntity(
    @ColumnInfo(name = "expense_date") val expenseDate: String?,
    @ColumnInfo(name = "total_amount") val totalAmount: Double
)

data class ExpensesEntityWithItemNameAndType(
    @Embedded val expensesEntity: ExpensesEntity,
    var expense_type:String,
    var item_name:String

)
