package com.example.allhome.pantry.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.PantryItemEntity
import com.example.allhome.data.entities.PantryItemExpirationEntity
import com.example.allhome.data.entities.PantryItemWithExpirations
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class PantryStorageViewModel: ViewModel() {

    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("PantryStorageViewModel"))
    lateinit var pantryItemWithExpirations:ArrayList<PantryItemWithExpirations>

    suspend fun getPatryItemWithExpirations(context: Context,storage:String):ArrayList<PantryItemWithExpirations>{

        pantryItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getPantryItemDAO().getPantryItemsByStorage(storage)
        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getPantryItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            pantryItemWithExpirations.add(PantryItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return pantryItemWithExpirations
    }
}