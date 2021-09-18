package com.example.allhome.data.entities

import androidx.room.ColumnInfo

data class ExpensesEntity(
    @ColumnInfo(name = "total_amount") val totalAmount: Double
)
