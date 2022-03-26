package com.example.allhome.grocerylist.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import com.example.allhome.grocerylist.GroceryUtil
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroceryListFragmentViewModel: ViewModel() {
    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("GroceryListFragmentViewModel"))
     var groceryLists = ArrayList<GroceryListWithItemCount>()

    suspend fun getGroceryLists(context: Context):List<GroceryListWithItemCount>{
        groceryLists = AllHomeDatabase.getDatabase(context).groceryListDAO().selectGroceryListsWithItemCount(GroceryListEntityValues.ACTIVE_STATUS) as ArrayList<GroceryListWithItemCount>
        return groceryLists
    }

    suspend fun addItem(context: Context,groceryListEntity: GroceryListEntity){
        AllHomeDatabase.getDatabase(context).groceryListDAO().addItem(groceryListEntity)
    }
    suspend fun deleteGroceryList(context: Context,autoGeneratedUniqueId:String,datetime:String,status:Int){
        AllHomeDatabase.getDatabase(context).groceryListDAO().updateGroceryListAsDeleted(autoGeneratedUniqueId,datetime,status)

    }

    suspend fun getGroceryListWithItemCount(context:Context,autogeneratedUniqueId:String):GroceryListWithItemCount{
        return AllHomeDatabase.getDatabase(context).groceryListDAO().getGroceryListWithItemCount(autogeneratedUniqueId)
    }

    fun findItemIndex(autoGeneratedUniqueId:String):Int{

        for (groceryList in groceryLists){
            if(groceryList.groceryListEntity.autoGeneratedUniqueId == autoGeneratedUniqueId){
                return groceryLists.indexOf(groceryList)
            }

        }
        return -1
    }

     fun getItemIndex(autoGeneratedUniqueId: String):Int{
        return groceryLists.indexOfFirst {
            it.groceryListEntity.autoGeneratedUniqueId.equals(autoGeneratedUniqueId)
        }
    }

    suspend fun copy(context: Context,oldAutoGeneratedUniqueId: String, newGroceryListName:String):GroceryListWithItemCount{

        var uniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val datetimeCreated: String = simpleDateFormat.format(Date())


        val groceryListDAO =  AllHomeDatabase.getDatabase(context).groceryListDAO()
        val groceryItemDAO = AllHomeDatabase.getDatabase(context).groceryItemDAO()

        groceryListDAO.copy(oldAutoGeneratedUniqueId,uniqueID,newGroceryListName,datetimeCreated)
        groceryItemDAO.copy(oldAutoGeneratedUniqueId,uniqueID)

        return groceryListDAO.getGroceryListWithItemCount(uniqueID)

    }

    suspend fun getGroceryListItemsForShareInOtherApp(context: Context,autoGeneratedUniqueId: String):String{

        val groceryItemDAO = AllHomeDatabase.getDatabase(context).groceryItemDAO()
        val groceryListsItems:List<GroceryItemEntity> = groceryItemDAO.getGroceryListItems(autoGeneratedUniqueId,GroceryItemEntityValues.ACTIVE_STATUS)

        val groupedItems = groceryListsItems.groupBy {
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
        var data = StringBuffer()
        groupedItemsSortedAlphabetically.forEach {

            val category = (if(it.first.trim().isEmpty()) "Uncategorized" else it.first)
            data.append("\r\n"+category+"\r\n")

            val categoryDivier = StringBuilder()
            repeat(category.length){
                categoryDivier.append("=")
            }

            data.append(categoryDivier.toString()+"\r\n")

            it.second.forEach{
                data.append("  - "+it.itemName+"\r\n")
                val otherData = GroceryUtil.quantityPriceAndTotalPerItemUtil(it)
                if(otherData.isNotEmpty()){
                    data.append("    "+otherData+"\r\n")
                }

            }
        }

        return  data.toString()

    }

    suspend fun getGroceryList(context:Context,groceryListUniqueId:String):GroceryListEntity{
        val groceryListDAO = AllHomeDatabase.getDatabase(context).groceryListDAO()
        return groceryListDAO.getGroceryList(groceryListUniqueId)
    }
    suspend fun getBoughtGroceryListItems(context:Context,groceryListUniqueId:String): List<GroceryItemEntity> {
        val groceryItemDAO = AllHomeDatabase.getDatabase(context).groceryItemDAO()
        return groceryItemDAO.getBoughtGroceryListItems(groceryListUniqueId)
    }

    suspend fun insertExpenseGroceryList(context:Context,expensesGroceryListEntity:ExpensesGroceryListEntity):Long{
        val expensesGroceryList = AllHomeDatabase.getDatabase(context).getExpensesGroceryListDAO()
        return expensesGroceryList.addItem(expensesGroceryListEntity)
    }
    suspend fun insertExpensesGroceryListItems(context:Context,expensesGroceryItemEntities:List<ExpensesGroceryItemEntity> ):List<Long>{
        val expensesGroceryItemDAO = AllHomeDatabase.getDatabase(context).getExpensesGroceryItemDAO()
        return expensesGroceryItemDAO.addItems(expensesGroceryItemEntities)

    }

}