package com.example.allhome.grocerylist.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.data.entities.GroceryListWithItemCount

class GroceryListViewModel(groceryListEntityParams: GroceryListEntity? = null, groceryItemParams: GroceryItemEntity?) : ViewModel() {

    var selectedGroceryListEntity:LiveData<GroceryListEntity> = MutableLiveData()
    var selectedGroceryItem:GroceryItemEntity? = null
    var groceryListWithItemCount: MutableLiveData<LiveData<List<GroceryListWithItemCount>>> = MutableLiveData()

    var selectedGroceryListItemList = ArrayList<GroceryItemEntity>()

    var toBuyGroceryItems = ArrayList<GroceryItemEntity>()
    var boughtGroceryItems = ArrayList<GroceryItemEntity>()

    var selectedGroceryList:LiveData<GroceryListEntity> = MutableLiveData<GroceryListEntity>()

    var totalItemCountToBuy:MutableLiveData<Int> = MutableLiveData<Int>(0)
    var totalItemCountBought:MutableLiveData<Int> = MutableLiveData<Int>(0)


    var totalItemToBuyAmount:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    var totalItemBoughtAmount:MutableLiveData<Double> = MutableLiveData<Double>(0.0)
    var moneySign:MutableLiveData<String> = MutableLiveData("₱")
    var sortingAndGrouping:MutableLiveData<Int> = MutableLiveData(SORT_ALPHABETICALLY)

    companion object{
        val SORT_ALPHABETICALLY = 0
        val GROUP_BY_CATEGORY = 1
    }
    init{



    }

    fun setSelectedGroceryList(context:Context,groceryListAutoGeneratedId: String):LiveData<GroceryListEntity>{
        selectedGroceryList = AllHomeDatabase.getDatabase(context).groceryListDAO().getGroceryList(groceryListAutoGeneratedId)
        return selectedGroceryList

    }
    fun getGroceryItems(context:Context,groceryListAutoGeneratedId:String):ArrayList<GroceryItemEntity>{
        val selectedGroceryListItemListRecord = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryListItems(groceryListAutoGeneratedId) as ArrayList<GroceryItemEntity>
        separateBougthItems(selectedGroceryListItemListRecord)
        boughtGroceryItems.sortBy {
            it.itemName
        }
        toBuyGroceryItems.sortBy {
            it.itemName
        }


        mergeToBuyAndBoughtItems(toBuyGroceryItems,boughtGroceryItems)
        return selectedGroceryListItemList
    }
    fun mergeToBuyAndBoughtItems(toBuyItems:ArrayList<GroceryItemEntity>,boughtItems:ArrayList<GroceryItemEntity>){
        totalItemCountToBuy.postValue(toBuyItems.size)
        totalItemCountBought.postValue(boughtItems.size)

        computeTotalAmountToBuy(toBuyGroceryItems)
        computeTotalAmountBought(boughtGroceryItems)

        toBuyItems.sortBy {
            it.itemName
        }
        boughtItems.sortBy {
            it.itemName
        }

        selectedGroceryListItemList.clear()
        selectedGroceryListItemList.addAll(toBuyItems)
        selectedGroceryListItemList.addAll(boughtItems)
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
    fun addGroceryListItemToBuy(toBuyItems:ArrayList<GroceryItemEntity>,boughtItems:ArrayList<GroceryItemEntity>,groceryListItemEntity: GroceryItemEntity):Int{

        toBuyGroceryItems.add(groceryListItemEntity)

        mergeToBuyAndBoughtItems(toBuyItems,boughtItems)

        return selectedGroceryListItemList.indexOf(groceryListItemEntity)


    }
    fun getGroceryListItem(context:Context,groceryListAutoGeneratedId:String):GroceryItemEntity{
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryListItem(groceryListAutoGeneratedId)

    }
    fun deleteGroceryListItem(context:Context,id:Int,groceryListAutoGeneratedId: String, position:Int){

        AllHomeDatabase.getDatabase(context).groceryItemDAO().deleteGroceryItem(id,groceryListAutoGeneratedId)
        toBuyGroceryItems.removeAt(position)
    }
    fun getGroceryListItem(context:Context,id:Int,groceryListAutoGeneratedId: String):GroceryItemEntity?{

        selectedGroceryItem = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryListItem(id,groceryListAutoGeneratedId)
        return selectedGroceryItem
    }
    fun updateGroceryItem(context:Context,itemName:String,quantity:Double,unit:String,pricePerUnit:Double,category:String,notes:String,imageName:String,id:Int){
        val update = AllHomeDatabase.getDatabase(context).groceryItemDAO().updateItem(itemName,quantity,unit,pricePerUnit,category,notes,imageName,id)

    }
    fun updateGroceryItem(context:Context,bought:Int,id:Int,itemName:String){
        val update = AllHomeDatabase.getDatabase(context).groceryItemDAO().updateItem(bought,id,itemName)
    }
    fun computeTotalAmountToBuy(toBuyGroceryItems:ArrayList<GroceryItemEntity>){
        var total = 0.0
        for (groceryItemEntity in toBuyGroceryItems){
            total += groceryItemEntity.pricePerUnit * groceryItemEntity.quantity
        }

        totalItemToBuyAmount.postValue(total)

    }
    fun computeTotalAmountBought(boughtGroceryItems: ArrayList<GroceryItemEntity>){
        var total = 0.0
        for (groceryItemEntity in boughtGroceryItems){
            total += groceryItemEntity.pricePerUnit * groceryItemEntity.quantity
        }

        totalItemBoughtAmount.postValue(total)
    }
    fun groupByCategory() {
        //remove fake grocery list item for divider if have any
        val noFakeGroceryItemForDivider = selectedGroceryListItemList.filter { !it.forCategoryDivider }

        val groupedItems = noFakeGroceryItemForDivider.groupBy { it.category }
         val groupedItemsSortedAlphabetically = groupedItems.toList().sortedBy {
            it.first
         }

        // clear list
        selectedGroceryListItemList.clear()
        for(items  in groupedItemsSortedAlphabetically){
            Log.e("KEY 123",items.first)
            val groceryListItemEntity = GroceryItemEntity(
                    groceryListUniqueId = "",
                    sequence = 0,
                    itemName = "",
                    quantity =0.0,
                    unit = "",
                    pricePerUnit = 0.0,
                    category = items.first,
                    notes = "",
                    imageName = "",
                    bought = 0
            )
            groceryListItemEntity.forCategoryDivider = true
            // add fake grocery item for category divider in recyclerview
            selectedGroceryListItemList.add(groceryListItemEntity)
            selectedGroceryListItemList.addAll(items.second)
        }
    }

    fun sortAlpahetically(toBuyItems:ArrayList<GroceryItemEntity>,boughtItems:ArrayList<GroceryItemEntity>){

        mergeToBuyAndBoughtItems(toBuyItems,boughtItems)
    }


}