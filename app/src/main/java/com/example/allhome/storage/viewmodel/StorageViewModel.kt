package com.example.allhome.storage.viewmodel

import android.content.Context
import android.net.Uri
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
    var storageEntitiesWithExpirationsAndStorages:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()



    var storageItemEntity:StorageItemEntity? = null
    var storageEntity:StorageEntity? = null

    var storagePreviousImageUri: Uri? = null
    var storageNewImageUri: Uri? = null
    var groceryLists = ArrayList<GroceryListWithItemCount>()

    var newGroceryItemEntity:GroceryItemEntity? = null

    var addMultipleGroceryItemEntityCondition:List<Int> = arrayListOf()

    suspend fun getStorageItemWithExpirations(context: Context, itemNameSearchTerm: String? = null, storageUniqueId:String):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities:List<StorageItemEntity> =  itemNameSearchTerm?.let {
            AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorageFilterByName(it,storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)
        }?: kotlin.run {
            AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorage(storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)
        }


        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
    suspend fun getSingleStorageItemWithExpirations(context: Context, itemName: String,itemUnit:String, storageUniqueId:String):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities:List<StorageItemEntity> =  AllHomeDatabase.getDatabase(context).getStorageItemDAO().getSingleStorageItemsByStorageFilterByName(itemName,itemUnit,storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    suspend fun getStorageItemWithExpirationsWithTotalQuantity(context: Context,searchTerm:String):ArrayList<StorageItemWithExpirationsAndStorages>{

        val storageEntitiesWithExpirationsAndStoragesInnerScope:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsWithTotalQuantity(searchTerm,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {

            val itemName = it.name
            val unit = it.unit
            val dateModified = it.modified

            val storages = AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)
            val pantryItemExpirationEntities = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpirationByStorage(itemName,unit)
            val storageItemWithExpirationsAndStorages = StorageItemWithExpirationsAndStorages(
                it,pantryItemExpirationEntities, storages
            )

            storageEntitiesWithExpirationsAndStoragesInnerScope.add(storageItemWithExpirationsAndStorages)

        }
        return storageEntitiesWithExpirationsAndStoragesInnerScope
    }

    suspend fun getStorageItemWithExpirationsWithTotalQuantityFilterByExpired(context: Context, itemNameSearchTerm: String,currentDate:String ):ArrayList<StorageItemWithExpirationsAndStorages>{

        val storageEntitiesWithExpirationsAndStoragesInnerScope:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getAllItemFilterByExpiredItems(itemNameSearchTerm,currentDate)
        pantryItemEntities.forEach {

            val itemName = it.name
            val unit = it.unit
            val dateModified = it.modified

            val storages = AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)
            val pantryItemExpirationEntities = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpirationByStorage(itemName,unit)
            val storageItemWithExpirationsAndStorages = StorageItemWithExpirationsAndStorages(
                it,pantryItemExpirationEntities, storages
            )

            storageEntitiesWithExpirationsAndStoragesInnerScope.add(storageItemWithExpirationsAndStorages)

        }
        return storageEntitiesWithExpirationsAndStoragesInnerScope
    }
    suspend fun getStorageItemWithExpirationsFilterByStockWeight(context: Context, itemNameSearchTerm: String? = null,storageUniqueId:String,stockWeight:List<Int>):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = itemNameSearchTerm?.let{
            AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageFilterByStockWeightFilterByName(itemNameSearchTerm,stockWeight,storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)
        }?:run{
            AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageFilterByStockWeight(stockWeight,storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)
        }

        /*val pantryItemEntities:List<StorageItemEntity> =  itemNameSearchTerm?.let {
            AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorageFilterByName(it,storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)
        }?: kotlin.run {
            AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorage(storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)
        }*/

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
    suspend fun getStorageItemWithExpirationsFilterByExpiredItem(context: Context, itemNameSearchTerm: String, storageUniqueId:String,currentDate:String):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

       val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemFilterByExpiredItems(itemNameSearchTerm,storageUniqueId,currentDate)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
    suspend fun getStorageItemWithExpirationsFilterByDateModified(context: Context, itemNameSearchTerm: String,dateModified: String, storageUniqueId:String):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageFilterByModified(dateModified,itemNameSearchTerm,storageUniqueId,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
    suspend fun getAllItemWithExpirationsFilterByDateModified(context: Context, itemNameSearchTerm: String,dateModified: String):ArrayList<StorageItemWithExpirationsAndStorages>{

        val storageEntitiesWithExpirationsAndStoragesInnerScope:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()
        val pantryItemEntities =AllHomeDatabase.getDatabase(context).getStorageItemDAO().getAllItemsByStorageFilterByModified(dateModified,itemNameSearchTerm,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {

            val itemName = it.name
            val unit = it.unit
            val dateModified = it.modified

            val storages = AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)
            val pantryItemExpirationEntities = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpirationByStorage(itemName,unit)
            val storageItemWithExpirationsAndStorages = StorageItemWithExpirationsAndStorages(
                it,pantryItemExpirationEntities, storages
            )

            storageEntitiesWithExpirationsAndStoragesInnerScope.add(storageItemWithExpirationsAndStorages)

        }
        return storageEntitiesWithExpirationsAndStoragesInnerScope
    }
    suspend fun getStorageItemWithExpirationsFilterGreaterThan(context: Context, itemNameSearchTerm: String,storageUniqueId:String,quantity:Int):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageGreaterThan(itemNameSearchTerm,storageUniqueId,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)


        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    /*suspend fun getAllItemWithExpirationsFilterGreaterThan(context: Context, itemNameSearchTerm: String,quantity:Int):ArrayList<StorageItemWithExpirationsAndStorages>{


        storageItemWithExpirations = arrayListOf()

         val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getAllItemsByStorageGreaterThan(itemNameSearchTerm,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }*/

    suspend fun getAllItemWithExpirationsFilterGreaterThan(context: Context, itemNameSearchTerm: String,quantity:Int):ArrayList<StorageItemWithExpirationsAndStorages>{
        val storageEntitiesWithExpirationsAndStoragesInnerScope:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()
        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getAllItemsByStorageGreaterThan(itemNameSearchTerm,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {

            val itemName = it.name
            val unit = it.unit
            val dateModified = it.modified

            val storages = AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)
            val pantryItemExpirationEntities = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpirationByStorage(itemName,unit)
            val storageItemWithExpirationsAndStorages = StorageItemWithExpirationsAndStorages(
                it,pantryItemExpirationEntities, storages
            )

            storageEntitiesWithExpirationsAndStoragesInnerScope.add(storageItemWithExpirationsAndStorages)

        }
        return storageEntitiesWithExpirationsAndStoragesInnerScope
    }
    suspend fun getAllItemWithExpirationsFilterLessThan(context: Context, itemNameSearchTerm: String,quantity:Int):ArrayList<StorageItemWithExpirationsAndStorages>{
        val storageEntitiesWithExpirationsAndStoragesInnerScope:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()
        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getAllItemsByStorageLessThan(itemNameSearchTerm,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {

            val itemName = it.name
            val unit = it.unit
            val dateModified = it.modified

            val storages = AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)
            val pantryItemExpirationEntities = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpirationByStorage(itemName,unit)
            val storageItemWithExpirationsAndStorages = StorageItemWithExpirationsAndStorages(
                it,pantryItemExpirationEntities, storages
            )

            storageEntitiesWithExpirationsAndStoragesInnerScope.add(storageItemWithExpirationsAndStorages)

        }
        return storageEntitiesWithExpirationsAndStoragesInnerScope
    }

    suspend fun getAllItemWithExpirationsFilterQuantityBetween(context: Context, itemNameSearchTerm: String,quantityFrom:Int,quantityTo:Int):ArrayList<StorageItemWithExpirationsAndStorages>{
        val storageEntitiesWithExpirationsAndStoragesInnerScope:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getAllItemsByStorageStockBetween(itemNameSearchTerm,quantityFrom,quantityTo,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {

            val itemName = it.name
            val unit = it.unit
            val dateModified = it.modified

            val storages = AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)
            val pantryItemExpirationEntities = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpirationByStorage(itemName,unit)
            val storageItemWithExpirationsAndStorages = StorageItemWithExpirationsAndStorages(
                it,pantryItemExpirationEntities, storages
            )

            storageEntitiesWithExpirationsAndStoragesInnerScope.add(storageItemWithExpirationsAndStorages)

        }
        return storageEntitiesWithExpirationsAndStoragesInnerScope
    }

    suspend fun getStorageItemWithExpirationsFilterLessThan(context: Context, itemNameSearchTerm: String, storageUniqueId:String,quantity:Int):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageLessThan(itemNameSearchTerm,storageUniqueId,quantity,StorageItemEntityValues.NOT_DELETED_STATUS)

        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
    suspend fun getStorageItemWithExpirationsFilterBetween(context: Context, itemNameSearchTerm: String,storageUniqueId:String,fromQuantity:Int,toQuantity:Int):ArrayList<StorageItemWithExpirations>{


        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getStorageItemsByStorageQuantityBetween(itemNameSearchTerm,storageUniqueId,fromQuantity,toQuantity,StorageItemEntityValues.NOT_DELETED_STATUS)

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

        val storageEntitiesWithExtraInformationInnerScope:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()

        val currentDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now())

        val storageItemDAO = AllHomeDatabase.getDatabase(context).getStorageItemDAO()
        val storageEntities = AllHomeDatabase.getDatabase(context).getStorageDAO().getStorages() as ArrayList<StorageEntity>

        storageEntities.forEach {

            val storageItemCount = storageItemDAO.getStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val noStockItemCount = storageItemDAO.getNoStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val lowStockItemCount = storageItemDAO.getLowStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val highStockItemCount = storageItemDAO.getHighStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val soonToExpireInDayString = storageItemDAO.getItemThatExpireSoon(it.uniqueId,currentDate)
            val itemSoonToExpireInDays = if(soonToExpireInDayString == null) 0 else soonToExpireInDayString.toInt()

            val expiredItemCount = storageItemDAO.getItemCountThatExpired(it.uniqueId,currentDate)

            val storageEntityWithExtraInformation = StorageEntityWithExtraInformation(
                storageEntity = it,
                itemCount = storageItemCount,
                noStockItemCount = noStockItemCount,
                lowStockItemCount = lowStockItemCount,
                highStockItemCount = highStockItemCount,
                expiredItemCount = expiredItemCount,
                itemToExpireDayCount = if(itemSoonToExpireInDays != null) itemSoonToExpireInDays else 0
            )

            storageEntitiesWithExtraInformationInnerScope.add(storageEntityWithExtraInformation)
        }

        return storageEntitiesWithExtraInformationInnerScope

    }
    suspend fun getAllStorageExceptSome(context:Context,storageName:List<String>):ArrayList<StorageEntityWithExtraInformation>{

        val storageEntitiesWithExtraInformationInnerScope:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()
        val currentDate = DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now())


        val storageItemDAO = AllHomeDatabase.getDatabase(context).getStorageItemDAO()
        val storageEntities = AllHomeDatabase.getDatabase(context).getStorageDAO().getAllStorageExceptSome(storageName) as ArrayList<StorageEntity>

        storageEntities.forEach {

            val storageItemCount = storageItemDAO.getStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val noStockItemCount = storageItemDAO.getNoStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val lowStockItemCount = storageItemDAO.getLowStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val highStockItemCount = storageItemDAO.getHighStockStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.uniqueId)
            val soonToExpireInDayString = storageItemDAO.getItemThatExpireSoon(it.uniqueId,currentDate)
            val itemSoonToExpireInDays = if(soonToExpireInDayString == null) 0 else soonToExpireInDayString.toInt()

            val expiredItemCount = storageItemDAO.getItemCountThatExpired(it.uniqueId,currentDate)

            val storageEntityWithExtraInformation = StorageEntityWithExtraInformation(
                storageEntity = it,
                itemCount = storageItemCount,
                noStockItemCount = noStockItemCount,
                lowStockItemCount = lowStockItemCount,
                highStockItemCount = highStockItemCount,
                expiredItemCount = expiredItemCount,
                itemToExpireDayCount = if(itemSoonToExpireInDays != null) itemSoonToExpireInDays else 0
            )

            storageEntitiesWithExtraInformationInnerScope.add(storageEntityWithExtraInformation)
        }

        return storageEntitiesWithExtraInformationInnerScope

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
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getItemByGroceryListUniqueIdNameAndUnit(groceryListUniqueId,itemName,unit)

    }
    suspend fun addGroceryListItem(context:Context,groceryItemEntity:GroceryItemEntity):Long{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().insert(groceryItemEntity)
    }
    suspend fun getExpiredItemByStorage(context:Context,storage:String,currentDate:String): List<StorageItemDAO.SimpleGroceryLisItem> {
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getExpiredItems(storage,currentDate)
    }
    suspend fun getExpiredItemsWithStockWeight(context:Context,storageUniqueId:String,currentDate:String,stockWeight:List<Int>): List<StorageItemDAO.SimpleGroceryLisItem> {

        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getExpiredItemsWithStockWeight(stockWeight,storageUniqueId,currentDate)

    }
    suspend fun getItemByNameAndUnitAndStorage(context:Context, name:String, unit:String, storage:String):StorageItemEntity?{

        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getItemByNameAndUnitAndStorage(name,unit,storage)

    }
    suspend fun getSingleItemByNameAndUnitAndStorageUniqueId(context:Context, name:String, unit:String, storageUniqueId:String):StorageItemEntity?{

        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().getItemByNameAndUnitAndStorageUniqueId(name,unit,storageUniqueId)

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
    suspend fun getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(context:Context, storageUniqueId:String, itemUniqueId:String, dateModified:String):List<StorageItemExpirationEntity>{
       return AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(storageUniqueId,itemUniqueId,dateModified)
    }
    suspend fun updateStorageAsDeleted(context:Context,storageUniqueId:String,currentDateTime:String):Int{
       return AllHomeDatabase.getDatabase(context).getStorageDAO().updateStorageAsDeleted(storageUniqueId,currentDateTime,StorageEntityValues.DELETED_STATUS)

    }
    suspend fun getStorageWithItem(context: Context,itemName:String,unit:String):List<StorageEntityWithStorageItemInformation>{
        return AllHomeDatabase.getDatabase(context).getStorageDAO().getStoragesWithItem(itemName,unit)

    }
    suspend fun getBoughtGroceryListItems(context:Context,groceryListUniqueId:String): List<GroceryItemEntity>{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getBoughtGroceryListItems(groceryListUniqueId)

    }

}