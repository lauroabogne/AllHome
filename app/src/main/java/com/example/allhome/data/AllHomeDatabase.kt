package com.example.allhome.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.allhome.data.DAO.*
import com.example.allhome.data.entities.*

@Database(entities = arrayOf(GroceryListEntity::class,GroceryItemEntity::class, StorageItemEntity::class,StorageItemExpirationEntity::class,
    StorageEntity::class,RecipeEntity::class,IngredientEntity::class,RecipeStepEntity::class,MealEntity::class,BillEntity::class,
    BillPaymentEntity::class,BillCategoryEntity::class,GroceryListItemCategoryEntity::class,RecipeCategoryEntity::class,
    RecipeCategoryAssignmentEntity::class,AppSettingEntity::class,ExpensesGroceryListEntity::class,ExpensesGroceryItemEntity::class,ExpensesGroceryListItemCategoryEntity::class,
    ExpensesEntity::class,TodoEntity::class,TodoSubTasksEntity::class,LogsEntity::class,AlarmRecordsEntity::class,
    ),version = 4)
abstract class AllHomeDatabase : RoomDatabase() {
    abstract fun groceryItemDAO(): GroceryItemDAO
    abstract fun groceryListDAO(): GroceryListDAO
    abstract fun getStorageItemDAO():StorageItemDAO
    abstract fun getStorageItemExpirationDAO():StorageItemExpirationDAO
    abstract fun getStorageDAO():StorageDAO
    abstract fun getRecipeDAO():RecipeDAO
    abstract fun getIngredientDAO():IngredientDAO
    abstract fun getRecipeStepDAO():RecipeStepDAO
    abstract fun getMealDAO():MealDAO
    abstract fun getBillItemDAO():BillDAO
    abstract fun getBillPaymentDAO():BillPaymentDAO
    abstract fun getBillCategoryDAO():BillCategoryDAO
    abstract fun getGroceryListItemCategoryDAO():GroceryListItemCategoryDAO
    abstract fun getRecipeCategoryDAO():RecipeCategoryDAO
    abstract fun getRecipeCategoryAssignmentDAO():RecipeCategoryAssignmentDAO
    abstract fun getAppSettingDAO():AppSettingDAO
    abstract fun getExpensesGroceryListDAO():ExpensesGroceryListDAO
    abstract fun getExpensesGroceryItemDAO():ExpensesGroceryItemDAO
    abstract fun getExpensesGroceryListItemCategoryDAO():ExpensesGroceryListItemCategoryDAO
    abstract fun getExpensesDAO():ExpensesDAO
    abstract fun getTodosDAO():TodosDAO
    abstract fun getTodoSubTasksDAO():TodoSubTasksDAO
    abstract fun getLogsDAO():LogsDAO
    abstract fun getAlarmsRecordsDAO():AlarmRecordsDAO




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
                ).fallbackToDestructiveMigration().build()

                INSTANCE = intance
                return intance
            }
        }
    }
}