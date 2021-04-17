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

class PantryAddItemViewModel: ViewModel() {
    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("PantryAddItemViewModel"))
    lateinit var pantryItemEntity:PantryItemEntity
    var pantryItemExpirationsEntity:ArrayList<PantryItemExpirationEntity> = arrayListOf()

    suspend fun setPantryItemEntity(pantryItemEntity:PantryItemEntity){
        this.pantryItemEntity = pantryItemEntity
    }
    suspend fun addExpiration(pantryItemExpirationEntity:PantryItemExpirationEntity){
        pantryItemExpirationsEntity.add(pantryItemExpirationEntity)
    }

    suspend fun savePantryItemEntity(context: Context, pantryItemEntity: PantryItemEntity):Long{
        return AllHomeDatabase.getDatabase(context).getPantryItemDAO().addItem(pantryItemEntity)
    }
    suspend fun savePantryItemExpirationEntity(context: Context, pantryItemExpirationEntity: PantryItemExpirationEntity):Long{
        return AllHomeDatabase.getDatabase(context).getPantryItemExpirationDAO().addItem(pantryItemExpirationEntity)
    }
}