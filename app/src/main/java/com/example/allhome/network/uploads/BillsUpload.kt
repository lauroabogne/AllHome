package com.example.allhome.network.uploads

import android.util.Log
import com.example.allhome.data.DAO.BillDAO
import com.example.allhome.data.DAO.BillPaymentDAO
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.network.SyncResult
import com.example.allhome.network.UploadApi
import com.example.allhome.network.datamodels.BillUploadDataModel
import com.google.gson.JsonParser
import java.io.IOException

class BillsUpload(
    private val apiService: UploadApi,
    private val billDAO:  BillDAO
    ) {

    suspend fun getBillsToUpload(): List<String> {
        return billDAO.getUniqueIdsToUpload(BillEntity.NOT_UPLOADED)
    }
    suspend fun getBillByUniqueId(uniqueId: String): BillEntity? {
        return billDAO.getBillByUniqueId(uniqueId)
    }
    suspend fun updateBillAsUploaded(uniqueId: String): Int {
       return billDAO.updateBillAsUploaded(uniqueId,BillEntity.UPLOADED)
    }
    suspend fun uploadBill(bill: BillEntity):SyncResult {
        // Convert BillEntity to BillUploadDataModel
        val billUploadDataModel = BillUploadDataModel(
            uniqueId = bill.uniqueId,
            groupUniqueId = bill.groupUniqueId,
            amount = bill.amount,
            name = bill.name,
            category = bill.category,
            dueDate = bill.dueDate,
            isRecurring = bill.isRecurring,
            repeatEvery = bill.repeatEvery,
            repeatBy = bill.repeatBy,
            repeatUntil = bill.repeatUntil,
            repeatCount = bill.repeatCount,
            imageName = bill.imageName,
            status = bill.status,
            uploaded = bill.uploaded,
            created = bill.created,
            modified = bill.modified
        )
        val syncResult = SyncResult(
            isSuccess = false,
            message = "",
            errorMessage = "Failed to upload due to network error",
            dataType = "bill",
            process = "upload"
        )


        try {
            val response = apiService.uploadBill(billUploadDataModel)

            if (response.isSuccessful) {


                val jsonObjectResponse = JsonParser.parseString(response.body()?.string()).asJsonObject

                val isSuccess = jsonObjectResponse.get("is_success")?.asBoolean
                val message = jsonObjectResponse.get("message")?.asString

                if(isSuccess == true){
                    syncResult.isSuccess = true
                    syncResult.message = message!!
                    syncResult.errorMessage = ""

                }else{

                    syncResult.isSuccess = true
                    syncResult.message = message!!
                    syncResult.errorMessage = message


                }


            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string() ?: "Unknown error"

                syncResult.isSuccess = false
                syncResult.message = "Failed to upload bill"
                syncResult.errorMessage = "HTTP error $errorCode: $errorBody"
            }
        } catch (e: Exception) {
            syncResult.isSuccess = false
            syncResult.message = ""
            syncResult.errorMessage = e.message ?: "Unknown error"

        } catch (e: IOException) {
            syncResult.isSuccess = false
            syncResult.message = ""
            syncResult.errorMessage = e.message ?: "Unknown error"


        }
        return syncResult

    }
    suspend fun uploadBills() {
        // Fetch bills to upload
        val bills = billDAO.getBillsToUpload( notUploaded = 0)

        // Convert BillEntity to BillUploadDataModel
        val billUploadData = bills.map { bill ->
            BillUploadDataModel(
                uniqueId = bill.uniqueId,
                groupUniqueId = bill.groupUniqueId,
                amount = bill.amount,
                name = bill.name,
                category = bill.category,
                dueDate = bill.dueDate,
                isRecurring = bill.isRecurring,
                repeatEvery = bill.repeatEvery,
                repeatBy = bill.repeatBy,
                repeatUntil = bill.repeatUntil,
                repeatCount = bill.repeatCount,
                imageName = bill.imageName,
                status = bill.status,
                uploaded = bill.uploaded,
                created = bill.created,
                modified = bill.modified
            )
        }


        // Upload bills
        try {
            val response = apiService.uploadBills(billUploadData)
            if (response.isSuccessful) {
                // Update uploaded status in database
                bills.forEach { bill ->
                    bill.uploaded = 1 // Set uploaded to true
                    billDAO.updateBill(bill)
                }
            } else {
                // Handle upload failure
                // You can also log the error or retry if needed
                throw Exception("Failed to upload bills: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle exception
            // You can log the exception or show an error message to the user
            e.printStackTrace()
        }
    }

}