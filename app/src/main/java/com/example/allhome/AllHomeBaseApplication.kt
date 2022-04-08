package com.example.allhome

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.*

class AllHomeBaseApplication : Application() {

    val database by lazy { AllHomeDatabase.getDatabase(this) }
    val groceryItemDAO by lazy {database.groceryItemDAO()}
    val groceryListDAO by lazy { database.groceryListDAO()}
    val storageItemDAO by lazy { database.getStorageItemDAO()}
    val storageItemExpirationDAO  by lazy { database.getStorageItemExpirationDAO()}
    val storageDAO  by lazy { database.getStorageDAO()}
    val recipeDAO  by lazy { database.getRecipeDAO()}
    val ingredientDAO by lazy{database.getIngredientDAO()}
    val recipeStepDAO by lazy{database.getRecipeStepDAO()}
    val mealDAO by lazy {database.getMealDAO() }
    val billDAO by lazy {database.getBillItemDAO() }
    val billPaymentDAO by lazy{database.getBillPaymentDAO()}
    val billCategoryDAO by lazy {database.getBillCategoryDAO()}
    val groceryListItemCategoryDAO by lazy{database.getGroceryListItemCategoryDAO()}
    val recipeCategoryDAO by lazy { database.getRecipeCategoryDAO() }
    val recipeCategoryAssignmentDAO by lazy { database.getRecipeCategoryAssignmentDAO() }
    val appSettingDAO by lazy { database.getAppSettingDAO() }
    val expensesGroceryListDAO by lazy { database.getExpensesGroceryListDAO() }
    val expensesGroceryItemDAO by lazy { database.getExpensesGroceryItemDAO() }
    val expensesGroceryListItemCategoryDAO by lazy { database.getExpensesGroceryListItemCategoryDAO() }
    val expensesDAO by lazy { database.getExpensesDAO()}


}