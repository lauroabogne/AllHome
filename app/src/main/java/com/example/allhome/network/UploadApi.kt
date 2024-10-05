package com.example.allhome.network

import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.network.datamodels.BillUploadDataModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadApi {
    @POST("mobileapi/MobileApiBills/uploadBills")
    suspend fun uploadBills(@Body bill: List<BillUploadDataModel>): Response<ResponseBody>
    @POST("mobileapi/MobileApiBills/uploadBill")
    suspend fun uploadBill(@Body bill: BillUploadDataModel): Response<ResponseBody>

//    @POST("upload/expenses")
//    suspend fun uploadExpenses(@Body expenses: List<ExpensesEntity>): Response<Any>

    @POST("/")
    suspend fun uploadExpenses(@Body expenses: List<ExpensesEntity>): Response<ResponseBody>
    @POST("upload/grocery-lists")
    suspend fun uploadGroceryLists(@Body groceryLists: List<GroceryListEntity>): Response<ResponseBody>
}