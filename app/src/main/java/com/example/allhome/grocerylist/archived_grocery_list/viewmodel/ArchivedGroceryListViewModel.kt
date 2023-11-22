package com.example.allhome.grocerylist.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ArchivedGroceryListViewModel(groceryListEntityParams: GroceryListEntity? = null, groceryItemParams: GroceryItemEntity?) : ViewModel() {

    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("TrashGroceryListViewModel"))

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
            it.itemName
        }

        boughtItems.sortBy {

            it.itemName
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


}