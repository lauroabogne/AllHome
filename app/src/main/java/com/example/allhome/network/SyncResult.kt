package com.example.allhome.network

data class SyncResult(
    var isSuccess: Boolean,
    var message: String,
    var errorMessage: String,
    val dataType: String,
    val process: String
)