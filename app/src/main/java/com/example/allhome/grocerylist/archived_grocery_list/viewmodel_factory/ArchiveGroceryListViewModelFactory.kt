package com.example.allhome.grocerylist.viewmodel_factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.grocerylist.viewmodel.ArchivedGroceryListViewModel
import java.lang.IllegalArgumentException

class ArchiveGroceryListViewModelFactory(private var groceryListEntity: GroceryListEntity? = null, private var groceryItemEntity: GroceryItemEntity?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ArchivedGroceryListViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ArchivedGroceryListViewModel(groceryListEntity,groceryItemEntity) as T
        }
        throw IllegalArgumentException("View model not found")
    }
}