package com.example.allhome.network.uploads

import android.util.Log
import com.example.allhome.data.DAO.BillPaymentDAO
import com.example.allhome.data.entities.BillPaymentEntity
import com.example.allhome.network.SyncResult
import com.example.allhome.network.UploadApi
import com.example.allhome.network.datamodels.BillPaymentSyncDataModel
import com.example.allhome.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.MultipartBody
import java.io.InputStream
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class BillsPaymentsUpload(
    private val apiService: UploadApi,
    private val billPaymentDAO:  BillPaymentDAO
    ) {

    suspend fun getBillsPaymentToUpload(): List<String> {

        return billPaymentDAO.getUniqueIdsToUpload(BillPaymentEntity.NOT_UPLOADED)
    }
    suspend fun getBillPaymentByUniqueId(uniqueId: String): BillPaymentEntity? {

        return billPaymentDAO.getBillPaymentByUniqueId(uniqueId)
    }
    suspend fun updateBillPaymentAsUploaded(uniqueId: String): Int {
       return billPaymentDAO.updateBillPaymentAsUploaded(uniqueId,BillPaymentEntity.UPLOADED)
    }
    suspend fun uploadBillPayments_(
        billPaymentEntity: BillPaymentEntity,
        imageInputStream: InputStream?
    ): SyncResult {

        val billPaymentDataModel = BillPaymentSyncDataModel(
            uniqueId = billPaymentEntity.uniqueId,
            billUniqueId = billPaymentEntity.billUniqueId,
            billGroupUniqueId = billPaymentEntity.billGroupUniqueId,
            paymentAmount = billPaymentEntity.paymentAmount,
            paymentDate = billPaymentEntity.paymentDate,
            paymentNote = billPaymentEntity.paymentNote,
            imageName = billPaymentEntity.imageName,
            status = billPaymentEntity.status,
            uploaded = billPaymentEntity.uploaded,
            created = billPaymentEntity.created,
            modified = billPaymentEntity.modified
        )

        val syncResult = SyncResult(
            isSuccess = false,
            message = "",
            errorMessage = "Failed to upload due to network error",
            dataType = "bill",
            process = "upload"
        )

        try {
            // Convert BillPaymentSyncDataModel to JSON and then to RequestBody
           // val billDataJson = Gson().toJson(billPaymentDataModel)
            //val billDataRequestBody = billData.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            // Create a MultipartBody.Part for the JSON data
//            val billDataPart = MultipartBody.Part.createFormData(
//                "billPaymentData",
//                null,
//                billDataJson.toRequestBody("application/json".toMediaTypeOrNull())
//            )

            // Create a MultipartBody with just the JSON data
//            val requestBody = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("billPaymentData", billDataJson)
//                .build()

// Create a RequestBody from the JSON string
          //  val requestBody = billDataJson.toRequestBody("application/json".toMediaTypeOrNull())
            // Make the API call

            // Create a RequestBody from the JSON string
            // Convert BillPaymentSyncDataModel to JSON
            val billDataJson = Gson().toJson(billPaymentDataModel)

            // Create a RequestBody from the JSON string
            val requestBody = billDataJson.toRequestBody("multipart/form-data".toMediaTypeOrNull())

          // val requestBody = MultipartBody.Part.createFormData("billPaymentData", billDataJson)

            val userIdRequestBody = "1234567".toRequestBody("multipart/form-data".toMediaTypeOrNull())
            val userNameRequestBody = "username".toRequestBody("multipart/form-data".toMediaTypeOrNull())

           // val response = apiService.uploadBillPayment(userIdRequestBody,userNameRequestBody)


        } catch (e: Exception) {
            syncResult.isSuccess = false
            syncResult.message = ""
            syncResult.errorMessage = e.message ?: "Unknown error"
            throw e
        }

        return syncResult
    }

    suspend fun uploadBillPayments(
        billPaymentEntity: BillPaymentEntity,
        imageInputStream: InputStream?
    ): SyncResult {

        val billPaymentDataModel = BillPaymentSyncDataModel(
            uniqueId = billPaymentEntity.uniqueId,
            billUniqueId = billPaymentEntity.billUniqueId,
            billGroupUniqueId = billPaymentEntity.billGroupUniqueId,
            paymentAmount = billPaymentEntity.paymentAmount,
            paymentDate = billPaymentEntity.paymentDate,
            paymentNote = billPaymentEntity.paymentNote,
            imageName = billPaymentEntity.imageName,
            status = billPaymentEntity.status,
            uploaded = billPaymentEntity.uploaded,
            created = billPaymentEntity.created,
            modified = billPaymentEntity.modified
        )

        val syncResult = SyncResult(
            isSuccess = false,
            message = "",
            errorMessage = "Failed to upload due to network error",
            dataType = "bill",
            process = "upload"
        )

        try {
            val billPaymentData = createPartMap(billPaymentDataModel)
            // Prepare the image file or an empty part if no image is provided
            val imagePart: MultipartBody.Part? = if (imageInputStream != null) {
                val bytes = imageInputStream.readBytes()
                val requestBody = bytes.toRequestBody(contentType = "image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", billPaymentEntity.imageName, requestBody)
            } else {
                null // Or pass an empty part if required by the server
            }

            // Make the API call
            //val response = apiService.uploadBillPayment(billDataRequestBody, imagePart)
            val response = apiService.uploadBillPayment(billPaymentData, imagePart)


            if (response.isSuccessful) {
                response.body()?.let {
                    val jsonObjectResponse = JsonParser.parseString(it.string()).asJsonObject
                    val isSuccess = jsonObjectResponse.get("is_success")?.asBoolean ?: false
                    val message = jsonObjectResponse.get("message")?.asString ?: "No message"

                    syncResult.isSuccess = isSuccess
                    syncResult.message = message
                    syncResult.errorMessage = if (isSuccess) "" else message
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
           throw e
        }

        return syncResult
    }

    // Function to create the partMap from the BillPaymentSyncDataModel
    private fun createPartMap(billPaymentDataModel: BillPaymentSyncDataModel): Map<String, RequestBody> {
        val partMap = mutableMapOf<String, RequestBody>()

        // Convert each field of BillPaymentSyncDataModel to RequestBody and add to the map
        partMap["uniqueId"] = billPaymentDataModel.uniqueId.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["billUniqueId"] = billPaymentDataModel.billUniqueId.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["billGroupUniqueId"] = billPaymentDataModel.billGroupUniqueId.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["paymentAmount"] = billPaymentDataModel.paymentAmount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["paymentDate"] = billPaymentDataModel.paymentDate.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["paymentNote"] = billPaymentDataModel.paymentNote.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["imageName"] = billPaymentDataModel.imageName.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["status"] = billPaymentDataModel.status.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["uploaded"] = billPaymentDataModel.uploaded.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["created"] = billPaymentDataModel.created.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap["modified"] = billPaymentDataModel.modified.toRequestBody("text/plain".toMediaTypeOrNull())

        return partMap
    }


}