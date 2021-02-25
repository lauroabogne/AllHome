package com.example.allhome.grocerylist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.GroceryItem
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.databinding.ActivityAddGroceryListItemBinding
import com.example.allhome.grocerylist.viewmodel_factory.GroceryListViewModelFactory
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class AddGroceryListItemActivity : AppCompatActivity() {

    private lateinit var dataBindingUtil: ActivityAddGroceryListItemBinding
    private lateinit var mGroceryListViewModel: GroceryListViewModel
    var groceryListUniqueId: String = ""

    var action = ADD_NEW_RECORD_ACTION
    var groceryListItemId = 0
    var groceryListItemIndex = -1;


    companion object {
        val ADD_NEW_RECORD_ACTION = 1
        val UPDATE_RECORD_ACTION = 2;

        val GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG = "GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG";
        val GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG"
        val GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG"
        val GROCERY_LIST_ACTION_EXTRA_DATA_TAG = "GROCERY_LIST_ACTION_EXTRA_DATA_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grocery_list_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getStringExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
            groceryListUniqueId = it
        }

        groceryListItemId = intent.getIntExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG,0)
        action = intent.getIntExtra(GROCERY_LIST_ACTION_EXTRA_DATA_TAG,ADD_NEW_RECORD_ACTION);
        groceryListItemIndex = intent.getIntExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG,-1);


        // create AddGroceryListItemActivityViewModel using AddGroceryListItemActivityViewModelFactory
        val addGroceryListItemActivityViewModelFactory = GroceryListViewModelFactory(null,null)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(GroceryListViewModel::class.java)

        if(action == UPDATE_RECORD_ACTION){
            CoroutineScope(IO).launch {
                mGroceryListViewModel.getGroceryListItem(this@AddGroceryListItemActivity,groceryListItemId,groceryListUniqueId)
            }
        }

        //Bind data
        dataBindingUtil = DataBindingUtil.setContentView<ActivityAddGroceryListItemBinding>(this, R.layout.activity_add_grocery_list_item).apply {
            this.lifecycleOwner = this@AddGroceryListItemActivity
            this.groceryListViewModel = mGroceryListViewModel

        }


    dataBindingUtil.addImgBtn.setOnClickListener(View.OnClickListener {
        CoroutineScope(IO).launch {

            mGroceryListViewModel.getGroceryListItem(this@AddGroceryListItemActivity,groceryListItemId,groceryListUniqueId)
            dataBindingUtil.invalidateAll()


        }
    })




    }


     fun addRecord() {

        val itemName: String = dataBindingUtil.itemNameTextinput.text.toString().trim()
        val quantityString = dataBindingUtil.itemQuantityTextinput.text.toString().trim()
        val unit: String = dataBindingUtil.unitTextinput.text.toString().trim()
        val pricePerUnitString = dataBindingUtil.pricePerUnitTextinput.text.toString() .trim()
         val category = dataBindingUtil.itemCategoryTextinput.text.toString().trim()
        val notes: String = dataBindingUtil.notesTextinput.text.toString().trim()

        if (itemName.trim().isEmpty()) {
            Toast.makeText(this, "Please provide name", Toast.LENGTH_SHORT).show()
            return
        }
         val doubleQuantity = if(quantityString.trim().isNotEmpty()) quantityString.toDouble() else 0.0
         val doublePricePerUnit = if(pricePerUnitString.trim().isNotEmpty()) pricePerUnitString.toDouble() else 0.0

        val groceryItemEntity = GroceryItemEntity(
                groceryListUniqueId = groceryListUniqueId,
                sequence = 1,
                itemName = itemName,
                quantity =doubleQuantity,
                unit = unit,
                pricePerUnit = doublePricePerUnit,
                category = category,
                notes = notes,
                imageName = "",
                bought = 0
        )


         CoroutineScope(IO).launch {
             AllHomeDatabase.getDatabase(this@AddGroceryListItemActivity).groceryItemDAO().addItem(groceryItemEntity)
             withContext(Main){

                    setResult(RESULT_OK)
                    finish()
             }
         }


    }
    fun updateRecord(){
        val itemName: String = dataBindingUtil.itemNameTextinput.text.toString().trim()
        val quantityString = dataBindingUtil.itemQuantityTextinput.text.toString()
        val unit: String = dataBindingUtil.unitTextinput.text.toString().trim()
        val pricePerUnitString = dataBindingUtil.pricePerUnitTextinput.text.toString().trim()
        val category = dataBindingUtil.itemCategoryTextinput.text.toString().trim()
        val notes: String = dataBindingUtil.notesTextinput.text.toString().trim()

        if (itemName.trim().isEmpty()) {
            Toast.makeText(this, "Please provide name", Toast.LENGTH_SHORT).show()
            return
        }
        val doubleQuantity = if(quantityString.trim().isNotEmpty()) quantityString.toDouble() else 0.0
        val doublePricePerUnit = if(pricePerUnitString.trim().isNotEmpty()) pricePerUnitString.toDouble() else 0.0

        val groceryItemEntity = GroceryItemEntity(
                groceryListUniqueId = groceryListUniqueId,
                sequence = 1,
                itemName = itemName,
                quantity =doubleQuantity,
                unit = unit,
                pricePerUnit = doublePricePerUnit,
                category = category,
                notes = notes,
                imageName = "",
                bought = 0
        )


        CoroutineScope(IO).launch {

            mGroceryListViewModel.updateGroceryItem(this@AddGroceryListItemActivity,itemName,doubleQuantity,unit,doublePricePerUnit,category,notes,"",groceryListItemId)


            withContext(Main){

                Toast.makeText(this@AddGroceryListItemActivity, "Updated record", Toast.LENGTH_SHORT).show()
                val intent = Intent()
                intent.putExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG,groceryListItemIndex)
                intent.putExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG,groceryListItemId)
                setResult(RESULT_OK,intent)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_grocery_item, menu)
        if(action == ADD_NEW_RECORD_ACTION){
            menu?.findItem(R.id.add_item)?.setVisible(true)

        }else if(action == UPDATE_RECORD_ACTION){
            menu?.findItem(R.id.update_item)?.setVisible(true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                Toast.makeText(this, "Test clicked home", Toast.LENGTH_SHORT).show()
            }
            R.id.add_item -> {
               addRecord()
            }
            R.id.update_item->{


                updateRecord()
            }
        }

        return super.onOptionsItemSelected(item)
    }


}