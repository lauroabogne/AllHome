package com.example.allhome.grocerylist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityValues
import com.example.allhome.data.entities.GroceryListEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class GroceryListViewModel(groceryListEntityParams: GroceryListEntity? = null, groceryItemParams: GroceryItemEntity?) : ViewModel() {

    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("SingleGroceryListActivity"))

    var selectedGroceryListEntity:LiveData<GroceryListEntity> = MutableLiveData()
    var selectedGroceryItem:GroceryItemEntity? = null

    var selectedGroceryListItemList = ArrayList<GroceryItemEntity>()

    var toBuyGroceryItems = ArrayList<GroceryItemEntity>()
    var boughtGroceryItems = ArrayList<GroceryItemEntity>()

    var selectedGroceryList:MutableLiveData<GroceryListEntity> = MutableLiveData<GroceryListEntity>()

    var totalItemCountToBuy:MutableLiveData<Int> = MutableLiveData<Int>(0)
    var totalItemCountBought:MutableLiveData<Int> = MutableLiveData<Int>(0)

    var totalItemToBuyAmount:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    var totalItemBoughtAmount:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    var moneySign:MutableLiveData<String> = MutableLiveData("₱")
    var sortingAndGrouping:MutableLiveData<Int> = MutableLiveData(SORT_ALPHABETICALLY)

    var selectedGroceryItemEntityCurrentImageUri: Uri?  = null
    var selectedGroceryItemEntityNewImageUri: Uri?  = null

    companion object{
        val SORT_ALPHABETICALLY = 0
        val GROUP_BY_CATEGORY = 1
    }
    init {
        selectedGroceryItem = groceryItemParams
    }

    suspend fun setSelectedGroceryList(context:Context,groceryListAutoGeneratedId: String){
        selectedGroceryList.postValue(AllHomeDatabase.getDatabase(context).groceryListDAO().getGroceryList(groceryListAutoGeneratedId))

    }
    suspend fun getGroceryItems(context:Context,groceryListAutoGeneratedId:String,status:Int):ArrayList<GroceryItemEntity>{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryListItems(groceryListAutoGeneratedId,status) as ArrayList<GroceryItemEntity>
       /* separateBougthItems(selectedGroceryListItemListRecord)
       *//* boughtGroceryItems.sortBy {
            it.itemName
        }
        toBuyGroceryItems.sortBy {
            it.itemName
        }*//*


        mergeToBuyAndBoughtItems(toBuyGroceryItems,boughtGroceryItems)
        return selectedGroceryListItemList*/
    }
    suspend fun a(){

    }
    suspend fun mergeToBuyAndBoughtItems(toBuyItems:ArrayList<GroceryItemEntity>,boughtItems:ArrayList<GroceryItemEntity>):ArrayList<GroceryItemEntity>{
        totalItemCountToBuy.postValue(toBuyItems.size)
        totalItemCountBought.postValue(boughtItems.size)


        computeTotalAmountToBuy(ArrayList(toBuyGroceryItems))
        computeTotalAmountBought(ArrayList(boughtGroceryItems))


        toBuyItems.sortBy {
            it.itemName.lowercase()
        }

        boughtItems.sortBy {

            it.itemName.lowercase()
        }
       // toBuyItems.addAll(boughtItems)
        selectedGroceryListItemList.clear()
        selectedGroceryListItemList.addAll(toBuyItems)
        selectedGroceryListItemList.addAll(boughtItems)
        return selectedGroceryListItemList
    }
    fun separateBougthItems(selectedGroceryListItemListRecord:ArrayList<GroceryItemEntity>){
        selectedGroceryListItemListRecord.forEachIndexed{index,item->
            if(item.bought == 1){
                boughtGroceryItems.add(item)
            }else{
                toBuyGroceryItems.add(item)
            }
        }
    }
    suspend fun addGroceryListItemToBuy(toBuyItems:ArrayList<GroceryItemEntity>,boughtItems:ArrayList<GroceryItemEntity>,groceryListItemEntity: GroceryItemEntity):Int{

        toBuyGroceryItems.add(groceryListItemEntity)
        mergeToBuyAndBoughtItems(toBuyItems,boughtItems)

        return selectedGroceryListItemList.indexOf(groceryListItemEntity)


    }
    suspend fun getGroceryListItem(context:Context,groceryListAutoGeneratedId:String):GroceryItemEntity{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryListItem(groceryListAutoGeneratedId)

    }
    suspend fun deleteGroceryListItem(context:Context,id:Int,groceryListAutoGeneratedId: String,status:Int){
        AllHomeDatabase.getDatabase(context).groceryItemDAO().updateGroceryItemAsDeleted(id,groceryListAutoGeneratedId,status)

    }
    suspend fun getGroceryListItem(context:Context,id:Int,groceryListAutoGeneratedId: String):GroceryItemEntity?{

        selectedGroceryItem = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryListItem(id,groceryListAutoGeneratedId)
        return selectedGroceryItem
    }
    suspend fun updateGroceryItem(context:Context,itemName:String,quantity:Double,unit:String,pricePerUnit:Double,category:String,notes:String,
                                  imageName:String,id:Int,currentDatetime:String){
        val update = AllHomeDatabase.getDatabase(context).groceryItemDAO().updateItem(itemName,quantity,unit,pricePerUnit,category,notes,imageName,id,currentDatetime)

    }
    suspend  fun updateGroceryItem(context:Context,bought:Int,id:Int,itemName:String,datetimeModified:String){
        val update = AllHomeDatabase.getDatabase(context).groceryItemDAO().updateItem(bought,id,itemName,datetimeModified)
    }
    suspend fun computeTotalAmountToBuy(toBuyGroceryItems:ArrayList<GroceryItemEntity>){
        var total = 0.0
        for (groceryItemEntity in toBuyGroceryItems){
            total += groceryItemEntity.pricePerUnit * groceryItemEntity.quantity
        }
        totalItemToBuyAmount.postValue(total)

    }
     suspend fun computeTotalAmountBought(boughtGroceryItems: ArrayList<GroceryItemEntity>){
        var total = 0.0
        for (groceryItemEntity in boughtGroceryItems){
            total += groceryItemEntity.pricePerUnit * groceryItemEntity.quantity
        }

        totalItemBoughtAmount.postValue(total)
    }
    fun groupByCategory() {
        //remove fake grocery list item for divider if have any
        val noFakeGroceryItemForDivider = selectedGroceryListItemList.filter { !it.forCategoryDivider }

        val groupedItems = noFakeGroceryItemForDivider.groupBy {
            it.category }


         var groupedItemsSortedAlphabetically = groupedItems.toList().sortedBy {

            it.first
         }

        if(groupedItemsSortedAlphabetically.size > 1 && groupedItemsSortedAlphabetically[0].first.trim().equals("",true)){
            val uncategories = groupedItemsSortedAlphabetically[0]
            groupedItemsSortedAlphabetically = groupedItemsSortedAlphabetically.toMutableList().drop(1)

            groupedItemsSortedAlphabetically = groupedItemsSortedAlphabetically.toMutableList()
            groupedItemsSortedAlphabetically.add(uncategories)

        }
        //Log.e("LENGTH",groupedItemsSortedAlphabetically.size.toString())
        // clear list
        selectedGroceryListItemList.clear()
        for(items  in groupedItemsSortedAlphabetically){

            val groceryListItemEntity = GroceryItemEntity(
                    groceryListUniqueId = "",
                    sequence = 0,
                    itemName = "",
                    quantity =0.0,
                    unit = "",
                    pricePerUnit = 0.0,
                    category = if(items.first.trim().isEmpty()) "Uncategorized" else items.first.trim(),
                    notes = "",
                    imageName = "",
                    bought = 0,
                itemStatus = GroceryItemEntityValues.ACTIVE_STATUS,
                datetimeModified = "",
                datetimeCreated = ""
            )
            groceryListItemEntity.forCategoryDivider = true
            // add fake grocery item for category divider in recyclerview
            selectedGroceryListItemList.add(groceryListItemEntity)
            selectedGroceryListItemList.addAll(items.second)
        }
        val a = selectedGroceryListItemList
        val b =a
    }
    suspend fun sortAlpahetically(toBuyItems:ArrayList<GroceryItemEntity>,boughtItems:ArrayList<GroceryItemEntity>){

        mergeToBuyAndBoughtItems(toBuyItems,boughtItems)
    }

    suspend fun  updateGroceryListViewing(context:Context,viewing:Int,autoGeneratedUniqueId:String){
        AllHomeDatabase.getDatabase(context).groceryListDAO().updateGroceryListViewing(viewing,autoGeneratedUniqueId)
    }

    suspend fun addGroceryListItem(context:Context,groceryItemEntity:GroceryItemEntity){
        AllHomeDatabase.getDatabase(context).groceryItemDAO().insert(groceryItemEntity)
    }
    suspend fun updateGroceryListAsNotUploaded(context:Context,autoGeneratedUniqueId:String,datetimeStatusUpdated:String,uploadedStatus:Int){
        AllHomeDatabase.getDatabase(context).groceryListDAO().updateGroceryListAsNotUploaded(autoGeneratedUniqueId,datetimeStatusUpdated,uploadedStatus)
    }



}