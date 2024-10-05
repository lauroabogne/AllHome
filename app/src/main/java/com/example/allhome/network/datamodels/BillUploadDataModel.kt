package com.example.allhome.network.datamodels

data class BillUploadDataModel(
    val uniqueId: String,
    val groupUniqueId: String,
    val amount: Double,
    val name: String,
    val category: String,
    val dueDate: String,
    val isRecurring: Int,
    val repeatEvery: Int,
    val repeatBy: String,
    val repeatUntil: String,
    val repeatCount: Int,
    val imageName: String,
    val status: Int,
    val uploaded: Int,
    val created: String,
    val modified: String,
)

