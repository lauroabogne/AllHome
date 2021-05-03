package com.example.allhome.storage.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.StorageItemDAO
import com.example.allhome.data.entities.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class StorageViewModel: ViewModel() {

    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("PantryStorageViewModel"))
    lateinit var storageItemWithExpirations:ArrayList<StorageItemWithExpirations>
    var storageEntitiesWithExtraInformation:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()
    var storageItemEntity:StorageItemEntity? = null
    var storageEntity:StorageEntity? = null

    var storagePreviousImageUri: Uri? = null
    var storageNewImageUri: Uri? = null
    var groceryLists = ArrayList<GroceryListWithItemCount>()

    var newGroceryItemEntity:GroceryItemEntity? = null

    var addMultipleGroceryItemEntityCondition:List<Int> = arrayListOf()

    suspend fun getStorageItemWithExpirations(context: Context, storage:String):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorage(storage,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    suspend fun getStorageItemWithExpirationsFilterByStockWeight(context: Context, storage:String,stockWeight:List<Int>):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageFilterByStockWeight(stockWeight,storage,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    suspend fun getStorageItemWithExpirationsFilterByExpiredItem(context: Context, storage:String,currentDate:String):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

       val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemFilterByExpiredItems(storage,currentDate)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
    suspend fun getStorageItemWithExpirationsFilterGreaterThan(context: Context, storage:String,quantity:Int):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageGreaterThan(storage,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    suspend fun getStorageItemWithExpirationsFilterLessThan(context: Context, storage:String,quantity:Int):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageLessThan(storage,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    suspend fun getStorageItemWithExpirationsFilterBetween(context: Context, storage:String,fromQuantity:Int,toQuantity:Int):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageQuantityBetween(storage,fromQuantity,toQuantity,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }


    suspend fun updateItemAsDeleted(context: Context, currentDatetime:String, storageItemEntity:StorageItemEntity):Int{
      return AllHomeDatabase.getDatabase(context).getStorageItemDAO().updateItemAsDeleted(StorageItemEntityValues.DELETED_STATUS,currentDatetime,storageItemEntity.uniqueId)
    }
    suspend fun updateItemAsDeleted(context: Context, currentDatetime:String, storageItemUniqueId:String):Int{
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().updateItemAsDeleted(StorageItemEntityValues.DELETED_STATUS,currentDatetime,storageItemUniqueId)
    }
    suspend fun updateStorageExpirationDateAsDeleted(context:Context,currentDatetime:String, storageItemEntity:StorageItemEntity):Int{
        return AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().updateItemAsDeleted(StorageItemEntityValues.DELETED_STATUS,currentDatetime,storageItemEntity.uniqueId,storageItemEntity.storage)

    }
    suspend fun addStorage(context:Context,storageEntity:StorageEntity):Long{
        return AllHomeDatabase.getDatabase(context).getStorageDAO().addItem(storageEntity)
    }
    suspend fun getAllStorage(context:Context):ArrayList<StorageEntityWithExtraInformation>{

        val currentDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now())


        val storageItemDAO = AllHomeDatabase.getDatabase(context).getStorageItemDAO()
        val storageEntities = AllHomeDatabase.getDatabase(context).getStorageDAO().getStorages() as ArrayList<StorageEntity>

        storageEntities.forEach {

            val storageItemCount = storageItemDAO.getStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val noStockItemCount = storageItemDAO.getNoStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val lowStockItemCount = storageItemDAO.getLowStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val highStockItemCount = storageItemDAO.getHighStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val soonToExpireInDayString = storageItemDAO.getItemThatExpireSoon(it.name,currentDate)
            val itemSoonToExpireInDays = if(soonToExpireInDayString == null) 0 else soonToExpireInDayString.toInt()

            val expiredItemCount = storageItemDAO.getItemCountThatExpired(it.name,currentDate)

            val storageEntityWithExtraInformation = StorageEntityWithExtraInformation(
                storageEntity = it,
                itemCount = storageItemCount,
                noStockItemCount = noStockItemCount,
                lowStockItemCount = lowStockItemCount,
                highStockItemCount = highStockItemCount,
                expiredItemCount = expiredItemCount,
                itemToExpireDayCount = if(itemSoonToExpireInDays != null) itemSoonToExpireInDays else 0
            )

            storageEntitiesWithExtraInformation.add(storageEntityWithExtraInformation)
        }

        Log.e("COUNT",storageEntitiesWithExtraInformation.size.toString())
        return storageEntitiesWithExtraInformation

    }
    suspend fun getAllStorageExceptSome(context:Context,storageName:List<String>):ArrayList<StorageEntityWithExtraInformation>{

        val currentDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now())


        val storageItemDAO = AllHomeDatabase.getDatabase(context).getStorageItemDAO()
        val storageEntities = AllHomeDatabase.getDatabase(context).getStorageDAO().getAllStorageExceptSome(storageName) as ArrayList<StorageEntity>

        storageEntities.forEach {

            val storageItemCount = storageItemDAO.getStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val noStockItemCount = storageItemDAO.getNoStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val lowStockItemCount = storageItemDAO.getLowStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val highStockItemCount = storageItemDAO.getHighStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            val soonToExpireInDayString = storageItemDAO.getItemThatExpireSoon(it.name,currentDate)
            val itemSoonToExpireInDays = if(soonToExpireInDayString == null) 0 else soonToExpireInDayString.toInt()

            val expiredItemCount = storageItemDAO.getItemCountThatExpired(it.name,currentDate)

            val storageEntityWithExtraInformation = StorageEntityWithExtraInformation(
                storageEntity = it,
                itemCount = storageItemCount,
                noStockItemCount = noStockItemCount,
                lowStockItemCount = lowStockItemCount,
                highStockItemCount = highStockItemCount,
                expiredItemCount = expiredItemCount,
                itemToExpireDayCount = if(itemSoonToExpireInDays != null) itemSoonToExpireInDays else 0
            )

            storageEntitiesWithExtraInformation.add(storageEntityWithExtraInformation)
        }

        Log.e("COUNT",storageEntitiesWithExtraInformation.size.toString())
        return storageEntitiesWithExtraInformation

    }
    suspend fun getStorage(context:Context,storageUniqueId:String):StorageEntity{
        storageEntity =  AllHomeDatabase.getDatabase(context).getStorageDAO().getStorage(storageUniqueId)
        return storageEntity as StorageEntity
    }

    suspend fun updateStorage(context:Context,storageUniquedId:String,name:String,description:String,imageName:String,modified:String):Int{
       return AllHomeDatabase.getDatabase(context).getStorageDAO().updateStorage(storageUniquedId,name,description,imageName,modified)

    }

    suspend fun getGroceryLists(context: Context):List<GroceryListWithItemCount>{
        groceryLists = AllHomeDatabase.getDatabase(context).groceryListDAO().selectGroceryListsWithItemCount(GroceryListEntityValues.ACTIVE_STATUS) as ArrayList<GroceryListWithItemCount>
        return groceryLists
    }
    suspend fun getGroceryList(context: Context,groceryListUniqueId:String):GroceryListWithItemCount{


        return AllHomeDatabase.getDatabase(context).groceryListDAO().getGroceryListWithItemCount(groceryListUniqueId)

    }
    suspend fun createNewGroceryList(context: Context,groceryListEntity: GroceryListEntity):Long{
        return AllHomeDatabase.getDatabase(context).groceryListDAO().addItem(groceryListEntity)
    }
    suspend fun getSingleGroceryItemEntity(context:Context, groceryListUniqueId:String, itemName:String, unit:String):GroceryItemEntity{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getItemByNameAndUnit(groceryListUniqueId,itemName,unit)

    }
    suspend fun addGroceryListItem(context:Context,groceryItemEntity:GroceryItemEntity):Long{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().addItem(groceryItemEntity)
    }
    suspend fun getExpiredItemByStorage(context:Context,storage:String,currentDate:String): List<StorageItemDAO.SimpleGroceryLisItem> {
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getExpiredItems(storage,currentDate)
    }
    suspend fun getExpiredItemsWithStockWeight(context:Context,storage:String,currentDate:String,stockWeight:List<Int>): List<StorageItemDAO.SimpleGroceryLisItem> {

        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getExpiredItemsWithStockWeight(stockWeight,storage,currentDate)

    }

    suspend fun getItemByNameAndUnitAndStorage(context:Context, name:String, unit:String, storage:String):StorageItemEntity?{

        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getItemByNameAndUnitAndStorage(name,unit,storage)

    }

    suspend fun saveStorageItemEntity(context: Context, storageItemEntity: StorageItemEntity):Long{
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().addItem(storageItemEntity)
    }
    suspend fun updateStorageItemEntity(context: Context, storageItemEntity: StorageItemEntity):Int{
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().update(storageItemEntity)
    }

    suspend fun saveStorageItemExpirationEntity(context: Context, storageItemExpirationEntity: StorageItemExpirationEntity):Long{
        return AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().addItem(storageItemExpirationEntity)
    }
    suspend fun getStorageItemsExpiratinsByStorageUniquedIdItemNameAndCreated(context:Context, storageUniqueId:String, itemName:String, dateModified:String):List<StorageItemExpirationEntity>{
       return AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpiratinsByStorageUniquedIdItemNameAndCreated(storageUniqueId,itemName,dateModified)
    }

}