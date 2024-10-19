package com.example.allhome.network

import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.network.datamodels.BillPaymentSyncDataModel
import com.example.allhome.network.datamodels.BillUploadDataModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface UploadApi {
    @POST("mobileapi/MobileApiBills/uploadBills")
    suspend fun uploadBills(@Body bill: List<BillUploadDataModel>): Response<ResponseBody>
    @POST("mobileapi/MobileApiBills/uploadBill")
    suspend fun uploadBill(@Body bill: BillUploadDataModel): Response<ResponseBody>
    @Multipart
    @POST("mobileapi/MobileApiBillsPayments/addOrUpdateIfExists")
    suspend fun uploadBillPayment(@PartMap billData: Map<String, @JvmSuppressWildcards RequestBody>, @Part image: MultipartBody.Part? = null): Response<ResponseBody>

    @Multipart
    @POST("mobileapi/MobileApiExpenses/uploadExpense")
    suspend fun uploadExpenses(@PartMap billData: Map<String, @JvmSuppressWildcards RequestBody>): Response<ResponseBody>

//    @POST("/")
//    suspend fun uploadExpenses(@Body expenses: List<ExpensesEntity>): Response<ResponseBody>
//    @POST("upload/grocery-lists")
    suspend fun uploadGroceryLists(@Body groceryLists: List<GroceryListEntity>): Response<ResponseBody>
}