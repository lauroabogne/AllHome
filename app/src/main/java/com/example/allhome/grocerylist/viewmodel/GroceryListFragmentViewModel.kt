package com.example.allhome.grocerylist.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.data.entities.GroceryListWithItemCount
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GroceryListFragmentViewModel: ViewModel() {
    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("GroceryListFragmentViewModel"))
     var groceryLists = ArrayList<GroceryListWithItemCount>()

    suspend fun getGroceryLists(context: Context):List<GroceryListWithItemCount>{
        groceryLists = AllHomeDatabase.getDatabase(context).groceryListDAO().selectGroceryListsWithItemCount() as ArrayList<GroceryListWithItemCount>
        return groceryLists
    }

    suspend fun addItem(context: Context,groceryListEntity: GroceryListEntity){
        AllHomeDatabase.getDatabase(context).groceryListDAO().addItem(groceryListEntity)
    }
    suspend fun deleteGroceryList(context: Context,autoGeneratedUniqueId:String):Int{
        val id = AllHomeDatabase.getDatabase(context).groceryListDAO().deleteGroceryList(autoGeneratedUniqueId)
       return id
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

}