package com.example.allhome.grocerylist.viewmodel_factory

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.grocerylist.viewmodel.TrashGroceryListViewModel
import java.lang.IllegalArgumentException

class TrashGroceryListViewModelFactory( private var groceryListEntity: GroceryListEntity? = null, private var groceryItemEntity: GroceryItemEntity?) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(TrashGroceryListViewModel::class.java)){

            return TrashGroceryListViewModel(groceryListEntity,groceryItemEntity) as T
        }
        throw IllegalArgumentException("View model not found")
    }
}