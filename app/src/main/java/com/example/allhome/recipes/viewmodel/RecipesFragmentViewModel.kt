package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import com.example.allhome.recipes.RecipesFragment
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.StringBuilder

class RecipesFragmentViewModel : ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RecipesFragmentViewModel"))


    var mFilter = RecipesFragment.NO_FILTER
    var mFiltering = false

    var mSelectedCategory: RecipeCategoryEntity? = null

    var mCostCondition: String = ""
    var mServingCondition: String = ""
    var mPrepPlusCookTimeCondition: String = ""

    var mCostString: String = "0"
    var mServingString: String = "0"
    var mPrepPlusCookHourString: String = "0"
    var mPrepPlusCookMinutesString: String = "0"

    var mHasCostInput: Boolean = false
    var mHasServingInput: Boolean = false
    var mHasHourOrMinuteInput: Boolean = false
    var mFilterIngredients = arrayListOf<String>()
    var mRecipeViewing = AppSettingEntity.RECIPE_LIST_VIEWING

    suspend fun getRecipes(context: Context, searchTerm: String): List<RecipeEntityWithTotalIngredient> {
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(searchTerm)
    }

    suspend fun getRecipesByAssignedCategory(context: Context, searchTerm: String, categoryUniqueId: String) {


    }

    suspend fun getRecipe(context: Context, uniqueId: String): RecipeEntityWithTotalIngredient {
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipe(uniqueId)
    }

    suspend fun getIngredients(context: Context, recipeUniqueId: String): List<IngredientEntity> {
        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun getRecipesByIngredients(context: Context, searchTerm: String, ingredients: List<String>): List<RecipeEntityWithTotalIngredient> {

        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipesByIngredients(searchTerm, ingredients)
    }
    suspend fun getRecipesByIngredientsWithCategorySelected(context: Context, searchTerm: String, ingredients: List<String>,categoryUniqueId: String): List<RecipeEntityWithTotalIngredient> {
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipesByIngredientsWithCategorySelected(searchTerm, ingredients,categoryUniqueId)
    }
    suspend fun getRecipeCategories(context: Context, recipeUniqueId: String): List<RecipeCategoryEntity> {

        return AllHomeDatabase.getDatabase(context).getRecipeCategoryAssignmentDAO().getRecipeCategories(recipeUniqueId)

    }

    fun createQuery(ingredients: List<String>): String {
        val query = StringBuilder(
            "SELECT ${RecipeEntity.TABLE_NAME}.* FROM ${RecipeEntity.TABLE_NAME} " +
                    " LEFT JOIN ${IngredientEntity.TABLE_NAME} " +
                    " ON ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} = ${IngredientEntity.TABLE_NAME}.${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID}" +
                    " WHERE ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_NAME} LIKE '%'||:searchTerm||'%' "
        )

        ingredients.forEach {
            query.append(" AND ${IngredientEntity.TABLE_NAME}.${IngredientEntity.COLUMN_NAME} = ? ")
        }
        query.append(" AND ${IngredientEntity.TABLE_NAME}.${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS}")
        query.append(" GROUP BY ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID}")

        return query.toString()
    }


    suspend fun getIngredientsForTransferringInGroceryList(context: Context, recipeUniqueId: String): List<IngredientEntityTransferringToGroceryList> {
        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsForGroceryListByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun getIngredientsForTransferringInGroceryListByIds(context: Context, recipeUniqueIds: List<String>): List<IngredientEntityTransferringToGroceryList> {
        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsForGroceryListByRecipeUniqueIds(recipeUniqueIds)
    }


    suspend fun getSteps(context: Context, recipeUniqueId: String): List<RecipeStepEntity> {
        return AllHomeDatabase.getDatabase(context).getRecipeStepDAO().getStepsByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun deleteRecipe(context: Context, recipeUniqueId: String) {
        val recipeDAO = AllHomeDatabase.getDatabase(context).getRecipeDAO()
        val recipeStepDAO = AllHomeDatabase.getDatabase(context).getRecipeStepDAO()
        val ingredientDAO = AllHomeDatabase.getDatabase(context).getIngredientDAO()

        recipeDAO.updateRecipeByUniqueIdAsDeleted(recipeUniqueId)
        recipeStepDAO.updateStepsByRecipeUniqueIdAsDeleted(recipeUniqueId)
        ingredientDAO.updateIngredientByRecipeUniqueIdAsDeleted(recipeUniqueId)
    }

    suspend fun getItemByGroceryListAutoGeneratedIDnameAndUnit(context: Context, groceryListAutoGeneratedId: String, name: String): GroceryItemEntity? {

        return AllHomeDatabase.getDatabase(context).groceryItemDAO().getItemByGroceryListAutoGeneratedIDnameAndUnit(groceryListAutoGeneratedId, name)

    }

    suspend fun addGroceryListItem(context: Context, groceryItemEntity: GroceryItemEntity) {
        AllHomeDatabase.getDatabase(context).groceryItemDAO().insert(groceryItemEntity)
    }

    suspend fun updateItemQuantityDatetimeModified(context: Context, id: String, datetimeModified: String): Int {
        return AllHomeDatabase.getDatabase(context).groceryItemDAO().updateItemQuantityDatetimeModified(id, datetimeModified)

    }

    suspend fun filterByCostServingAndTotalPrepAndCookTime(
        context: Context, searchQuery: String, costCondition: String, cost: Double, servingCondition: String, serving: Int,
        totalPrepAndCookTimeInMinutesCondtion: String, totalPrepAndCookTimeInMinutes: Int
    ): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByCostServingAndTotalPrepAndCookTime(costCondition, servingCondition, totalPrepAndCookTimeInMinutesCondtion)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", cost, serving, totalPrepAndCookTimeInMinutes))


        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    suspend fun filterByCostServingAndTotalPrepAndCookTimeWithCategory(context: Context, searchQuery: String, costCondition: String, cost: Double, servingCondition: String, serving: Int, totalPrepAndCookTimeInMinutesCondition: String, totalPrepAndCookTimeInMinutes: Int,categoryUniqueId:String): List<RecipeEntityWithTotalIngredient> {
        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} $costCondition ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} $servingCondition ? " +
                " AND ((${RecipeEntity.COLUMN_PREPARATION_HOUR} * 60) +  (${RecipeEntity.COLUMN_COOKING_HOUR} * 60) + ${ RecipeEntity.COLUMN_PREPARATION_MINUTES} + ${RecipeEntity.COLUMN_COOKING_MINUTES}) ${totalPrepAndCookTimeInMinutesCondition} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}" +
                " AND ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";
        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", cost, serving, totalPrepAndCookTimeInMinutes,categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)
    }

    fun createQueryForAllProductWithCategorySelected(): String {
        val query = " SELECT * FROM ${RecipeEntity.TABLE_NAME}" +
                "  WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? AND " +
                " ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)" +
                " ORDER BY ${RecipeEntity.COLUMN_NAME}";

        return query

    }

    private fun createForFilterByCostServingAndTotalPrepAndCookTime(costCondition: String, servingCondition: String, totalPrepAndCookTimeInMinutesCondtion: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${costCondition} ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} ${servingCondition} ? " +
                " AND ((${RecipeEntity.COLUMN_PREPARATION_HOUR} * 60) +  (${RecipeEntity.COLUMN_COOKING_HOUR} * 60) + ${RecipeEntity.COLUMN_PREPARATION_MINUTES} + ${RecipeEntity.COLUMN_COOKING_MINUTES}) ${totalPrepAndCookTimeInMinutesCondtion} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }

    suspend fun filterByCostAndServing(
        context: Context, searchQuery: String, costCondition: String, cost: Double, servingCondition: String, serving: Int,
    ): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByCostAndServing(costCondition, servingCondition)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", cost, serving))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    suspend fun filterByCostAndServingWithCategorySelected(
        context: Context, searchQuery: String, costCondition: String, cost: Double, servingCondition: String, serving: Int,categoryUniqueId:String): List<RecipeEntityWithTotalIngredient> {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} $costCondition ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} $servingCondition ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}" +
                " AND ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                 " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";

        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", cost, serving,categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    private fun createForFilterByCostAndServing(costCondition: String, servingCondition: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${costCondition} ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} ${servingCondition} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }

    suspend fun filterByCostAndTotalPrepAndCookTime(context: Context, searchQuery: String, costCondition: String, cost: Double, totalPrepAndCookTimeInMinutesCondtion: String, totalPrepAndCookTimeInMinutes: Int): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByCostAndTotalPrepAndCookTime(costCondition, totalPrepAndCookTimeInMinutesCondtion)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", cost, totalPrepAndCookTimeInMinutes))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    suspend fun filterByCostAndTotalPrepAndCookTimeWithCategorySelected(context: Context, searchQuery: String, costCondition: String, cost: Double,
                                                                        totalPrepAndCookTimeInMinutesCondtion: String, totalPrepAndCookTimeInMinutes: Int,categoryUniqueId:String): List<RecipeEntityWithTotalIngredient> {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${costCondition} ? " +
                " AND ((${RecipeEntity.COLUMN_PREPARATION_HOUR} * 60) +  (${RecipeEntity.COLUMN_COOKING_HOUR} * 60) + ${RecipeEntity.COLUMN_PREPARATION_MINUTES} + ${RecipeEntity.COLUMN_COOKING_MINUTES}) ${totalPrepAndCookTimeInMinutesCondtion} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS} " +
                " AND ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";

        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", cost, totalPrepAndCookTimeInMinutes,categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    private fun createForFilterByCostAndTotalPrepAndCookTime(costCondition: String, totalPrepAndCookTimeInMinutesCondtion: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${costCondition} ? " +
                " AND ((${RecipeEntity.COLUMN_PREPARATION_HOUR} * 60) +  (${RecipeEntity.COLUMN_COOKING_HOUR} * 60) + ${RecipeEntity.COLUMN_PREPARATION_MINUTES} + ${RecipeEntity.COLUMN_COOKING_MINUTES}) ${totalPrepAndCookTimeInMinutesCondtion} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }

    suspend fun filterByServingAndTotalPrepAndCookTime(context: Context, searchQuery: String, servingCondition: String, serving: Int, totalPrepAndCookTimeInMinutesCondtion: String, totalPrepAndCookTimeInMinutes: Int): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByServingAndTotalPrepAndCookTime(servingCondition, totalPrepAndCookTimeInMinutesCondtion)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", serving, totalPrepAndCookTimeInMinutes))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    suspend fun filterByServingAndTotalPrepAndCookTimeWithCategorySelected(context: Context, searchQuery: String, servingCondition: String, serving: Int, totalPrepAndCookTimeInMinutesCondition: String, totalPrepAndCookTimeInMinutes: Int,categoryUniqueId: String): List<RecipeEntityWithTotalIngredient> {
        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${servingCondition} ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} ${totalPrepAndCookTimeInMinutesCondition} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}" +
                " AND ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";

        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", serving, totalPrepAndCookTimeInMinutes,categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    private fun createForFilterByServingAndTotalPrepAndCookTime(servingCondition: String, totalPrepAndCookTimeInMinutesCondition: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ?" +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${servingCondition} ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} ${totalPrepAndCookTimeInMinutesCondition} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }

    suspend fun filterByCost(context: Context, searchQuery: String, costCondition: String, cost: Double): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByCost(costCondition)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", cost))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    suspend fun filterByCostAndCategory(context: Context, searchQuery: String, costCondition: String, cost: Double, categoryUniqueId: String): List<RecipeEntityWithTotalIngredient> {
        val query = " SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME}" +
                "  WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? AND " +
                " ${RecipeEntity.COLUMN_ESTIMATED_COST} $costCondition ? AND " +
                " ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";

        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", cost, categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)
    }

    private fun createForFilterByCost(costCondition: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? " +
                " AND ${RecipeEntity.COLUMN_ESTIMATED_COST} ${costCondition} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }


    suspend fun filterByServing(context: Context, searchQuery: String, servingCondition: String, serving: Int): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByServing(servingCondition)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", serving))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    suspend fun filterByServingWithCategorySelected(context: Context, searchQuery: String, servingCondition: String, serving: Int,categoryUniqueId: String): List<RecipeEntityWithTotalIngredient> {
        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} $servingCondition ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS} AND " +
                " ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";
        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", serving,categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    private fun createForFilterByServing(servingCondition: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? " +
                " AND ${RecipeEntity.COLUMN_SERVING} ${servingCondition} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }

    suspend fun filterByTotalPrepAndCookTime(context: Context, searchQuery: String, totalPrepAndCookTimeInMinutesCondtion: String, totalPrepAndCookTimeInMinutes: Int): List<RecipeEntityWithTotalIngredient> {
        val queryString = createForFilterByTotalPrepAndCookTime(totalPrepAndCookTimeInMinutesCondtion)
        val simpleSqliteQuery = SimpleSQLiteQuery(queryString, arrayOf("%${searchQuery}%", totalPrepAndCookTimeInMinutes))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }
    suspend fun filterByTotalPrepAndCookTimeWithCategorySelected(context: Context, searchQuery: String, totalPrepAndCookTimeInMinutesCondtion: String, totalPrepAndCookTimeInMinutes: Int,categoryUniqueId: String): List<RecipeEntityWithTotalIngredient> {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? " +
                " AND ((${RecipeEntity.COLUMN_PREPARATION_HOUR} * 60) +  (${RecipeEntity.COLUMN_COOKING_HOUR} * 60) + ${RecipeEntity.COLUMN_PREPARATION_MINUTES} + ${RecipeEntity.COLUMN_COOKING_MINUTES}) ${totalPrepAndCookTimeInMinutesCondtion} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS} AND "+
                " ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} IN " +
                " (SELECT ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_UNIQUE_ID}  FROM ${RecipeCategoryAssignmentEntity.TABLE_NAME} WHERE ${RecipeCategoryAssignmentEntity.COLUMN_RECIPE_CATEGORY_UNIQUE_ID} = ? AND ${RecipeCategoryAssignmentEntity.COLUMN_STATUS}  = 0)";

        val simpleSqliteQuery = SimpleSQLiteQuery(query, arrayOf("%${searchQuery}%", totalPrepAndCookTimeInMinutes,categoryUniqueId))
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes(simpleSqliteQuery)

    }

    private fun createForFilterByTotalPrepAndCookTime(totalPrepAndCookTimeInMinutesCondtion: String): String {

        val query = "SELECT *," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
                " 0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}" +
                " FROM ${RecipeEntity.TABLE_NAME} WHERE " +
                " ${RecipeEntity.COLUMN_NAME} LIKE ? " +
                " AND ((${RecipeEntity.COLUMN_PREPARATION_HOUR} * 60) +  (${RecipeEntity.COLUMN_COOKING_HOUR} * 60) + ${RecipeEntity.COLUMN_PREPARATION_MINUTES} + ${RecipeEntity.COLUMN_COOKING_MINUTES}) ${totalPrepAndCookTimeInMinutesCondtion} ? " +
                " AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}"

        return query
    }

    suspend fun getIngredientForAutoSuggest(context: Context, searchTerm: String): List<String> {

        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientForAutousuggest(searchTerm)
    }
    suspend fun setRecipeViewingAppSetting(context: Context,appSettingEntity: AppSettingEntity):Long{
        return AllHomeDatabase.getDatabase(context).getAppSettingDAO().insert(appSettingEntity)
    }
    suspend fun updateRecipeViewingAppSetting(context:Context,recipeViewing:String,currentDate:String){
        AllHomeDatabase.getDatabase(context).getAppSettingDAO().updateRecipeViewingSetting(recipeViewing,currentDate)
    }
    suspend fun getRecipeViewingSetting(context: Context):String{
        val recipeViewing:String?  = AllHomeDatabase.getDatabase(context).getAppSettingDAO().getRecipeViewing()
        mRecipeViewing = recipeViewing ?:AppSettingEntity.RECIPE_GRID_VIEWING
        return mRecipeViewing
    }
    suspend fun getRecipeViewingSettingEntity(context: Context):AppSettingEntity?{
        return AllHomeDatabase.getDatabase(context).getAppSettingDAO().getRecipeViewingAppSettingEntity()
    }

}

