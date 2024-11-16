package com.example.allhome.network.uploads

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.DAO.GroceryItemDAO
import com.example.allhome.data.DAO.GroceryListDAO
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.network.SyncResult
import com.example.allhome.network.UploadApi
import com.example.allhome.network.datamodels.BillUploadDataModel
import com.example.allhome.utils.ImageUtil
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException



class GroceryListUpload(
    private val apiService: UploadApi,
    private val groceryListDAO:  GroceryListDAO,
    private val groceryItemDAO:  GroceryItemDAO,
    ) {
    suspend fun getGroceryListUniqueIdToUpload(): List<String> {
        return groceryListDAO.getNotUploadedGroceryListsUniqueId(GroceryListEntity.NOT_YET_UPLOADED)

    }
    suspend fun getGroceryListByUniqueId(uniqueId: String): GroceryListEntity? {
        return groceryListDAO.getGroceryList(uniqueId)
    }
    suspend fun getGroceryListItems(groceryListAutoGenerateId: String ):List<GroceryItemEntity>?{

        return groceryItemDAO.getGroceryListItems(groceryListAutoGenerateId)
    }

    suspend fun updateGroceryListAsUploaded(uniqueId: String): Int{
        return groceryListDAO.updateGroceryListAsUploaded(uniqueId, GroceryListEntity.UPLOADED)
    }

    suspend fun updateGroceryListItemsAsUploaded(uniqueId: String): Int{
       return groceryItemDAO.updateGroceryListItemsAsUploaded(uniqueId, GroceryItemEntity.UPLOADED)

    }
    suspend fun uploadGroceryList(context:Context, groceryListEntity: GroceryListEntity, groceryListItems: List<GroceryItemEntity>?): SyncResult {
        val syncResult = SyncResult(
            isSuccess = false,
            message = "",
            errorMessage = "Failed to upload due to network error",
            dataType = "bill",
            process = "upload"
        )



        try {

            val groceryListPartMap = createGroceryListPartMap(groceryListEntity, groceryListItems)
            // add image part to each item
            val imageParts = groceryListItems?.mapIndexedNotNull { index, item ->
                item.imageName.let { path ->
                    createImagePart(context,path, "grocery_list[items][$index][image]")
                }
            }

            val response = apiService.uploadGroceryListWithItems(groceryListPartMap, imageParts)


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
                syncResult.message = "Failed to upload grocery list"
                syncResult.errorMessage = "HTTP error $errorCode: $errorBody"
            }
        } catch (e: Exception) {
            syncResult.isSuccess = false
            syncResult.message = ""
            syncResult.errorMessage = e.message ?: "Unknown error"
          //  throw e
        }
        return syncResult
    }


    private fun createGroceryListPartMap(groceryList: GroceryListEntity, groceryListItems: List<GroceryItemEntity>?): Map<String, RequestBody> {
        val partMap = mutableMapOf<String, RequestBody>()


        // Add fields for the main grocery list
        partMap["grocery_list[${GroceryListEntity.COLUMN_NAME}]"] = groceryList.name.toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_AUTO_GENERATED_UNIQUE_ID}]"] = groceryList.autoGeneratedUniqueId.toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_DATETIME_CREATED}]"] = groceryList.datetimeCreated.toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_SHOPPING_DATETIME}]"] = groceryList.shoppingDatetime.toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_LOCATION}]"] = groceryList.location.toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_LONGITUDE}]"] = groceryList.longitude.toString().toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_LATITUDE}]"] = groceryList.latitude.toString().toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_VIEWING_TYPE}]"] = groceryList.viewingType.toString().toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_NOTIFY}]"] = groceryList.notify.toString().toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_NOTIFY_TYPE}]"] = groceryList.notifyType.toRequestBody()
        partMap["grocery_list[${GroceryListEntity.COLUMN_ITEM_STATUS}]"] = groceryList.itemStatus.toString().toRequestBody()

        groceryListItems?.forEachIndexed{ index, item ->
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_UNIQUE_ID}]"] = item.uniqueId.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_GROCERY_LIST_UNIQUE_ID}]"] = item.groceryListUniqueId.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_SEQUENCE}]"] = item.sequence.toString().toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_ITEM_NAME}]"] = item.itemName.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_QUANTITY}]"] = item.quantity.toString().toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_UNIT}]"] = item.unit.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_PRICE_PER_UNIT}]"] = item.pricePerUnit.toString().toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_CATEGORY}]"] = item.category.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_NOTES}]"] = item.notes.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_IMAGE_NAME}]"] = item.imageName.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_BOUGHT}]"] = item.bought.toString().toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_ITEM_STATUS}]"] = item.itemStatus.toString().toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_DATETIME_CREATED}]"] = item.datetimeCreated.toRequestBody()
            partMap["grocery_list[items][$index][${GroceryItemEntity.COLUMN_DATETIME_MODIFIED}]"] = item.datetimeModified.toRequestBody()
        }


        return partMap
    }
    private fun createImagePart(context: Context, imageName: String, partName: String): MultipartBody.Part? {

        val imageURI = ImageUtil.getImageUriFromPath(context, GroceryUtil.GROCERY_ITEM_IMAGES_LOCATION , imageName)
        val imageInputStream = if(imageURI != null) ImageUtil.getInputStreamFromUri(context, imageURI) else null

        val imagePart: MultipartBody.Part? = if (imageInputStream != null) {
            val bytes = imageInputStream.readBytes()
            val requestBody = bytes.toRequestBody(contentType = "image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, imageName, requestBody)
        } else {
            null // Or pass an empty part if required by the server
        }
        return imagePart
    }
}