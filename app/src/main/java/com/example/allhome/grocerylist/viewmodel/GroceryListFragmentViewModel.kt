package com.example.allhome.grocerylist.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryListWithItemCount

class GroceryListFragmentViewModel: ViewModel() {

    lateinit var groceryList:List<GroceryListWithItemCount>

    fun getGroceryLists(context: Context):List<GroceryListWithItemCount>{
        groceryList = AllHomeDatabase.getDatabase(context).groceryListDAO().selectGroceryListWithItemCount()
        return groceryList
    }

}