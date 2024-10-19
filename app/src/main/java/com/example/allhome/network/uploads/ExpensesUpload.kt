package com.example.allhome.network.uploads

import android.util.Log
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.network.SyncResult
import com.example.allhome.network.UploadApi
import com.example.allhome.network.datamodels.BillUploadDataModel
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException



class ExpensesUpload(
    private val apiService: UploadApi,
    private val expensesDao:  ExpensesDAO
    ) {
    suspend fun getExpensesToUpload(): List<String> {
        return expensesDao.getUniqueIdsToUpload(ExpensesEntity.NOT_UPLOADED)
    }
    suspend fun getExpensesByUniqueId(uniqueId: String): ExpensesEntity? {
        return expensesDao.getExpensesByUniqueId(uniqueId)
    }
    suspend fun updateExpensesAsUploaded(uniqueId: String): Int {
        return expensesDao.updateExpensesAsUploaded(uniqueId,ExpensesEntity.UPLOADED)
    }
    suspend fun uploadExpense(expensesEntity: ExpensesEntity): SyncResult {

        val syncResult = SyncResult(
            isSuccess = false,
            message = "",
            errorMessage = "Failed to upload due to network error",
            dataType = "bill",
            process = "upload"
        )
        val expenseData = createPartMap(expensesEntity)

        try {
            val response = apiService.uploadExpenses(expenseData)

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

//    suspend fun uploadExpenses(expenses: List<ExpensesEntity>) {
//        try {
//            val response = apiService.uploadExpenses(expenses)
//            if (response.isSuccessful) {
//               // expensesDao.insertExpenses(expenses)
//                Log.e("Upload", "Expenses uploaded successfully")
//            } else {
//                // Handle error
//
//                val errorCode = response.code()
//                val errorMessage = response.errorBody()?.string() ?: "No error body"
//                Log.e("Upload", "Expenses uploaded failed with code $errorCode. Error: $errorMessage")
//            }
//        } catch (e: Exception) {
//            // Handle exception
//            Log.e("Upload", "Expenses upload failed: ${e.message}")
//        }
//    }
    /**
     * Converts an instance of ExpensesEntity into a Map<String, RequestBody> for use in a multipart form data request.
     * Each field of the ExpensesEntity is transformed into a RequestBody with a "text/plain" content type.
     * This map is typically used in Retrofit API calls that require multipart form submissions.
     *
     * @param expensesEntity An instance of ExpensesEntity containing the data to be converted to RequestBody.
     * @return A Map<String, RequestBody> where each key is the field name of the entity, and the value is the field's content
     *         as a RequestBody, suitable for multipart form data submission.
     *
     * ### Example usage:
     * ```kotlin
     * val expensesEntity = ExpensesEntity(
     *     uniqueId = "12345",
     *     name = "Lunch",
     *     category = "Food",
     *     expenseDate = "2024-10-18",
     *     amount = 20.0,
     *     status = 0,
     *     uploaded = 0,
     *     created = "2024-10-18 10:00:00",
     *     modified = "2024-10-18 10:00:00"
     * )
     *
     * val partMap = createPartMap(expensesEntity)
     * ```
     *
     * @see ExpensesEntity
     */
    fun createPartMap(expensesEntity: ExpensesEntity): Map<String, RequestBody> {
        val partMap = mutableMapOf<String, RequestBody>()
        // Convert each field of ExpensesEntity to RequestBody and add to the map
        partMap[ExpensesEntity.COLUMN_UNIQUE_ID] = expensesEntity.uniqueId.toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_NAME] = (expensesEntity.name ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_CATEGORY] = (expensesEntity.category ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_EXPENSES_DATE] = (expensesEntity.expenseDate ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_AMOUNT] = expensesEntity.amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_STATUS] = (expensesEntity.status?.toString() ?: ExpensesEntity.NOT_DELETED_STATUS.toString()).toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_CREATED] = (expensesEntity.created ?: "CURRENT_TIMESTAMP").toRequestBody("text/plain".toMediaTypeOrNull())
        partMap[ExpensesEntity.COLUMN_MODIFIED] = (expensesEntity.modified ?: "CURRENT_TIMESTAMP").toRequestBody("text/plain".toMediaTypeOrNull())

        return partMap
    }
}