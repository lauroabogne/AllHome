package com.example.allhome.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.allhome.data.DAO.GroceryItemDAO
import com.example.allhome.data.DAO.GroceryListDAO
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity

@Database(entities = arrayOf(GroceryListEntity::class,GroceryItemEntity::class),version = 3)
abstract class AllHomeDatabase : RoomDatabase() {
    abstract fun groceryItemDAO(): GroceryItemDAO
    abstract fun groceryListDAO(): GroceryListDAO

    companion object{
        @Volatile
        private var INSTANCE:AllHomeDatabase?=null

        fun getDatabase(context: Context):AllHomeDatabase{
            val tempInstance = INSTANCE
            if(tempInstance !=null){
                return tempInstance
            }

            synchronized(this){
                val intance = Room.databaseBuilder(
                    context.applicationContext,
                    AllHomeDatabase::class.java,
                    "all_home_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = intance
                return intance
            }
        }
    }
}