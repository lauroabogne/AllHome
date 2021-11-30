package com.example.allhome.recipes

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.StorageItemDAO
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.ActivityRecipeGroceryListBinding
import com.example.allhome.databinding.ActivityStorageGroceryListBinding
import com.example.allhome.databinding.StorageGroceryListLayoutBinding
import com.example.allhome.global_ui.CustomConfirmationDialog
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.example.allhome.storage.StorageGroceryListActivity
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.example.allhome.utils.IngredientEvaluator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

internal class RecipeGroceryListActivity : AppCompatActivity() {

    lateinit var mStorageViewModel: StorageViewModel
    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    lateinit var mActivityRecipeGroceryListBinding: ActivityRecipeGroceryListBinding
    lateinit var mIngredientEntityTransferringToGroceryList:ArrayList<IngredientEntityTransferringToGroceryList>


    companion object{
        const val INGREDIENT_ENTITY_TAG = "INGREDIENT_ENTITY_TAG"
        const val ACTION_TAG = "ACTION_TAG"
        const val ADD_SINGLE_PRODUCT_ACTION = 0
        const val ADD_MULTIPLE_PRODUCT_ACTION = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Select grocery list"
        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)

        intent.getParcelableArrayListExtra<IngredientEntityTransferringToGroceryList>(INGREDIENT_ENTITY_TAG)?.let{
            mIngredientEntityTransferringToGroceryList  = it
        }?: kotlin.run {
            Toast.makeText(this,"Ingredients are require",Toast.LENGTH_SHORT).show()
            finish()
        }

        mActivityRecipeGroceryListBinding = DataBindingUtil.setContentView<ActivityRecipeGroceryListBinding>(this,R.layout.activity_recipe_grocery_list).apply {
            lifecycleOwner = this@RecipeGroceryListActivity
            storageViewModel = mStorageViewModel
        }

        val pantryStorageRecyclerviewViewAdapater = StorageGroceryListRecyclerviewViewAdapater(this)
        mActivityRecipeGroceryListBinding.storageGroceryListRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater

        mStorageViewModel.coroutineScope.launch {
            mStorageViewModel.getGroceryLists(this@RecipeGroceryListActivity)
            val storageGroceryListRecyclerviewViewAdapater= mActivityRecipeGroceryListBinding.storageGroceryListRecyclerview.adapter as StorageGroceryListRecyclerviewViewAdapater
            storageGroceryListRecyclerviewViewAdapater.mGroceryListWithItemCount = mStorageViewModel.groceryLists
            withContext(Main){
                storageGroceryListRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }

        mActivityRecipeGroceryListBinding.fab.setOnClickListener{
            showGroceryListNameInput()

        }

    }
    private fun showGroceryListNameInput(){
        val groceryListNameInputDialog = CustomDialog(context = this)
        groceryListNameInputDialog.setButtonClickListener(View.OnClickListener {


            if(it.id == CustomDialog.NEGATIVE_BUTTON_ID){
                groceryListNameInputDialog.mAlertDialog.dismiss()
                return@OnClickListener
            }
            var groceryListName = groceryListNameInputDialog.groceryListName()

            if (groceryListName.isEmpty()) {
                Toast.makeText(this, "Please provide name", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var uniqueID = UUID.randomUUID().toString()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val datetimeCreated: String = simpleDateFormat.format(Date())
            val groceryListEntity = GroceryListEntity(autoGeneratedUniqueId = uniqueID, name = groceryListName,
                datetimeCreated = datetimeCreated, shoppingDatetime = "0000-00-00 00:00:00", location = "",
                longitude = 0.0, latitude = 0.0,viewingType = 0,notify = 0,notifyType = getString(R.string.grocery_notification_none),
                itemStatus = GroceryListEntityValues.ACTIVE_STATUS,datetimeStatusUpdated = datetimeCreated,uploaded = GroceryListEntityValues.NOT_YET_UPLOADED
            )

            mStorageViewModel.coroutineScope.launch {
                val id = mStorageViewModel.createNewGroceryList(this@RecipeGroceryListActivity,groceryListEntity)
                if(id > 0){
                    val groceryListWithItemCount = mStorageViewModel.getGroceryList(this@RecipeGroceryListActivity,groceryListEntity.autoGeneratedUniqueId)
                    mStorageViewModel.groceryLists.add(0,groceryListWithItemCount)
                }
                withContext(Main){
                    groceryListNameInputDialog.mAlertDialog.dismiss()
                    mActivityRecipeGroceryListBinding.storageGroceryListRecyclerview.adapter?.notifyDataSetChanged()
                }
            }

        })
        groceryListNameInputDialog.createPositiveButton("Continue")
        groceryListNameInputDialog.createNegativeButton("Cancel")
        groceryListNameInputDialog.setCancelable(false)
        groceryListNameInputDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    class StorageGroceryListRecyclerviewViewAdapater(val recipeGroceryListActivity:RecipeGroceryListActivity): RecyclerView.Adapter<StorageGroceryListRecyclerviewViewAdapater.ItemViewHolder>() {

        var mGroceryListWithItemCount: List<GroceryListWithItemCount> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val storageGroceryListLayoutBinding = StorageGroceryListLayoutBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(storageGroceryListLayoutBinding)
            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val groceryListWithItemCount = mGroceryListWithItemCount[position]
            holder.storageGroceryListLayoutBinding.groceryListWithCount = groceryListWithItemCount
            holder.storageGroceryListLayoutBinding.executePendingBindings()
        }

        override fun getItemCount(): Int {
            return mGroceryListWithItemCount.size
        }

        inner class  ItemViewHolder(var storageGroceryListLayoutBinding:StorageGroceryListLayoutBinding): RecyclerView.ViewHolder(storageGroceryListLayoutBinding.root){

            init{

                storageGroceryListLayoutBinding.root.setOnClickListener {

                    val customConfirmationDialog = CustomConfirmationDialog(it.context)
                    customConfirmationDialog.setCustomMessage("Continue to add item in grocery list?")
                    customConfirmationDialog.createNegativeButton("No")
                    customConfirmationDialog.createPositiveButton("Yes")
                    customConfirmationDialog.setButtonClickListener(View.OnClickListener {view->

                        customConfirmationDialog.mAlertDialog.dismiss()
                        if(view.id == CustomConfirmationDialog.POSITIVE_BUTTON_ID){

                            val groceryListEntity = mGroceryListWithItemCount[adapterPosition].groceryListEntity
                            val groceryListAutoGeneratedId = groceryListEntity.autoGeneratedUniqueId
                            addIngredientToGroceryList(groceryListAutoGeneratedId)
                            //Log.e("INGREDIENTS",recipeGroceryListActivity.mIngredientEntityTransferringToGroceryList.toString())
                        }
                    })
                    customConfirmationDialog.show()

                }
            }
        }
        fun addIngredientToGroceryList(groceryListAutoGeneratedId:String){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            Toast.makeText(recipeGroceryListActivity,"Working here",Toast.LENGTH_SHORT).show()
            recipeGroceryListActivity.mIngredientEntityTransferringToGroceryList.forEach {ingredientEntityTransferringToGrocery->
                val quantity = ingredientEntityTransferringToGrocery.ingredientEntity.quantity
                val quantityDouble = IngredientEvaluator.convertStringQuantityToDouble(quantity)




            }

            var allInserted = true
            recipeGroceryListActivity.mRecipesFragmentViewModel.mCoroutineScope.launch {
                val allHomeDatabase = AllHomeDatabase.getDatabase(recipeGroceryListActivity)
                allHomeDatabase.withTransaction {
                    recipeGroceryListActivity.mIngredientEntityTransferringToGroceryList.forEach {ingredientEntityTransferringToGrocery->
                        val name = ingredientEntityTransferringToGrocery.ingredientEntity.name
                        val unit = ingredientEntityTransferringToGrocery.ingredientEntity.unit
                        val quantity = IngredientEvaluator.convertStringQuantityToDouble(ingredientEntityTransferringToGrocery.ingredientEntity.quantity)

                        val groceryItemEntity = recipeGroceryListActivity.mRecipesFragmentViewModel.getItemByGroceryListAutoGeneratedIDnameAndUnit(recipeGroceryListActivity,groceryListAutoGeneratedId,name)
                        groceryItemEntity?.let{

                            val groceryItemId = it.id
                            recipeGroceryListActivity.mRecipesFragmentViewModel.updateItemQuantityDatetimeModified(recipeGroceryListActivity,groceryItemId,currentDatetime)

                        }?:run{


                            val newGroceryItemEntity = GroceryItemEntity(
                                groceryListUniqueId = groceryListAutoGeneratedId,
                                itemName = name,
                                quantity = quantity,
                                unit= unit,
                                datetimeCreated = currentDatetime,
                                datetimeModified = currentDatetime
                            )

                            recipeGroceryListActivity.mRecipesFragmentViewModel.addGroceryListItem(recipeGroceryListActivity,newGroceryItemEntity)

                        }

                    }
                }

                withContext(Main){
                    if(allInserted){
                        Toast.makeText(recipeGroceryListActivity,"Ingredient added to grocery list",Toast.LENGTH_SHORT).show()
                        recipeGroceryListActivity.finish()
                    }else{
                        Toast.makeText(recipeGroceryListActivity,"Failed to add ingredient to grocery list.Please try again.",Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

    }
}

/**
 * Custom alertdialog
 */
class CustomDialog(context: Context) : AlertDialog.Builder(context) {

    companion object{
        val POSITIVE_BUTTON_ID = AlertDialog.BUTTON_POSITIVE
        val NEGATIVE_BUTTON_ID = AlertDialog.BUTTON_NEGATIVE

    }

    var mGroceryListNameInput: LinearLayout
    lateinit var mOnClickListener: View.OnClickListener
    lateinit var mAlertDialog: AlertDialog

    init {

        mGroceryListNameInput = LayoutInflater.from(context).inflate(R.layout.grocery_list_name_input, null, false) as LinearLayout
        this.setView(mGroceryListNameInput)
    }

    fun setButtonClickListener(onClickListener: View.OnClickListener) {
        mOnClickListener = onClickListener
    }
    fun createPositiveButton(buttonLabel: String){
        this.setPositiveButton(buttonLabel, null)
    }

    fun createNegativeButton(buttonLabel: String){
        this.setNegativeButton(buttonLabel, null)
    }
    fun groceryListName():String{
        var groceryListNameTextInput: TextInputEditText = mGroceryListNameInput.findViewById(R.id.grocery_list_name_textinputedittext)
        return groceryListNameTextInput.text.toString()

    }

    override fun show(): AlertDialog {
        mAlertDialog = super.show()
        val positiveBtn: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeBtn: Button = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        if(mOnClickListener != null){
            if(positiveBtn !=null){
                positiveBtn.id = POSITIVE_BUTTON_ID
                positiveBtn.setOnClickListener(mOnClickListener)
            }

            if(negativeBtn != null){

                negativeBtn.id = NEGATIVE_BUTTON_ID
                negativeBtn.setOnClickListener(mOnClickListener)
            }
        }

        return mAlertDialog
    }
}



