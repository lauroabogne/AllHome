package com.example.allhome.data.relations

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity

data class GroceryListAndGroceryItems(
        @Embedded val groceryListEntity: GroceryListEntity,
        @Relation(
                parentColumn = "auto_generated_unique_id",
                entityColumn = "grocery_list_unique_id"
        )
        val groceryListItemEntity: List<GroceryItemEntity>
)

data class GroceryListAndGroceryItemsName(
        @Embedded val groceryListEntity: GroceryListEntity,
        val itemCount:Int
)

data class GroceryListAndGroceryItemsName1(
        @Embedded val groceryListEntity: GroceryListEntity,
        val itemName:String
)