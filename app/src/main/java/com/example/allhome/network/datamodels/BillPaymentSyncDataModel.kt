package com.example.allhome.network.datamodels

data class BillPaymentSyncDataModel(
    val uniqueId: String,
    val billUniqueId: String,
    val billGroupUniqueId: String,
    val paymentAmount: Double,
    val paymentDate: String,
    val paymentNote: String,
    val imageName: String,
    val status: Int,
    val uploaded: Int,
    val created: String,
    val modified: String
)

