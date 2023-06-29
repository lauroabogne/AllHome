package com.example.allhome.grocerylist.viewmodel_factory

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import java.lang.IllegalArgumentException

class GroceryListViewModelFactory( private var groceryListEntity: GroceryListEntity? = null, private var groceryItemEntity: GroceryItemEntity?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(GroceryListViewModel::class.java)){

            return GroceryListViewModel(groceryListEntity,groceryItemEntity) as T
        }
        throw IllegalArgumentException("View model not found")
    }
}