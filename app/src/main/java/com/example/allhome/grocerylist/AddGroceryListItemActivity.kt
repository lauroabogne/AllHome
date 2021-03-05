package com.example.allhome.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.databinding.ActivityAddGroceryListItemBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.grocerylist.viewmodel_factory.GroceryListViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import kotlin.collections.ArrayList


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

        groceryListItemId = intent.getIntExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, 0)
        action = intent.getIntExtra(GROCERY_LIST_ACTION_EXTRA_DATA_TAG, ADD_NEW_RECORD_ACTION);
        groceryListItemIndex = intent.getIntExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, -1);


        // create AddGroceryListItemActivityViewModel using AddGroceryListItemActivityViewModelFactory
        val addGroceryListItemActivityViewModelFactory = GroceryListViewModelFactory(null, null)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(GroceryListViewModel::class.java)

        if(action == UPDATE_RECORD_ACTION){
            CoroutineScope(IO).launch {
                mGroceryListViewModel.getGroceryListItem(this@AddGroceryListItemActivity, groceryListItemId, groceryListUniqueId)
            }
        }
        //Bind data
        dataBindingUtil = DataBindingUtil.setContentView<ActivityAddGroceryListItemBinding>(this, R.layout.activity_add_grocery_list_item).apply {
            this.lifecycleOwner = this@AddGroceryListItemActivity
            this.groceryListViewModel = mGroceryListViewModel

        }

        /*dataBindingUtil.addImgBtn.setOnClickListener(View.OnClickListener {
            CoroutineScope(IO).launch {

                mGroceryListViewModel.getGroceryListItem(this@AddGroceryListItemActivity, groceryListItemId, groceryListUniqueId)
                dataBindingUtil.invalidateAll()

            }
        })*/

        val itemNameAutoSuggestCustomAdapter = ItemNameAutoSuggestCustomAdapter(this, arrayListOf())
        dataBindingUtil.itemNameTextinput.threshold = 0
        dataBindingUtil.itemNameTextinput.setAdapter(itemNameAutoSuggestCustomAdapter)
        dataBindingUtil.itemNameTextinput.onItemClickListener =  OnItemClickListener { parent, view, position, id ->
            val groceryItemEntity:GroceryItemEntity = parent.getItemAtPosition(position) as GroceryItemEntity
            dataBindingUtil.groceryListViewModel?.selectedGroceryItem= groceryItemEntity
            // set 1 as default value
            dataBindingUtil.groceryListViewModel?.selectedGroceryItem!!.quantity = 1.0
            dataBindingUtil.invalidateAll()
        }

        val itemUnitAutoSuggestCustomAdapter = UnitAutoSuggestCustomAdapter(this, arrayListOf())
        dataBindingUtil.unitTextinput.threshold = 0
        dataBindingUtil.unitTextinput.setAdapter(itemUnitAutoSuggestCustomAdapter)


        val itemCategoryAutoSuggestCustomAdapter = CategoryAutoSuggestCustomAdapter(this, arrayListOf())
        dataBindingUtil.itemCategoryTextinput.threshold = 0
        dataBindingUtil.itemCategoryTextinput.setAdapter(itemCategoryAutoSuggestCustomAdapter)
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
                groceryListUniqueId = groceryListUniqueId, sequence = 1, itemName = itemName, quantity = doubleQuantity, unit = unit, pricePerUnit = doublePricePerUnit, category = category, notes = notes, imageName = "", bought = 0
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

        CoroutineScope(IO).launch {

            mGroceryListViewModel.updateGroceryItem(this@AddGroceryListItemActivity, itemName, doubleQuantity, unit, doublePricePerUnit, category, notes, "", groceryListItemId)


            withContext(Main){

                Toast.makeText(this@AddGroceryListItemActivity, "Updated record", Toast.LENGTH_SHORT).show()
                val intent = Intent()
                intent.putExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, groceryListItemIndex)
                intent.putExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, groceryListItemId)
                setResult(RESULT_OK, intent)
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
                finish()
            }
            R.id.add_item -> {
                addRecord()
            }
            R.id.update_item -> {


                updateRecord()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Item name auto suggest adapter
     */
    class ItemNameAutoSuggestCustomAdapter(context: Context, var groceryItemEntitiesParams: List<GroceryItemEntity>):
        ArrayAdapter<GroceryItemEntity>(context, 0, groceryItemEntitiesParams) {
        private var groceryItemEntities: List<GroceryItemEntity>? = null
        init{
            groceryItemEntities = ArrayList(groceryItemEntitiesParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntities(searchTerm)
                        results.apply {
                            results.values = arrayList
                            results.count = arrayList.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                val res:List<GroceryItemEntity> = results?.values as ArrayList<GroceryItemEntity>
                addAll(res)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as GroceryItemEntity).itemName
            }
        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var textView:TextView? = null
            if(convertView == null){
                textView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                textView = convertView as TextView?
            }
            val groceryItemEntity = getItem(position)
            textView!!.setText(groceryItemEntity?.itemName)
            return textView!!
        }
    }
    /**
     * Item unit auto suggest adapter
     */
    class UnitAutoSuggestCustomAdapter(context: Context, var groceryItemEntityUnitsParams: List<String>):
            ArrayAdapter<String>(context, 0, groceryItemEntityUnitsParams) {
            private var groceryItemEntityUnits: List<String>? = null
                init{
                    groceryItemEntityUnits = ArrayList(groceryItemEntityUnitsParams)
                }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntityUnits(searchTerm)
                        results.apply {
                            results.values = arrayList
                            results.count = arrayList.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                val res:List<String> = results?.values as ArrayList<String>
                addAll(res)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue as CharSequence
            }
        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var textView:TextView? = null
            if(convertView == null){
                textView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                textView = convertView as TextView?
            }
            val unit = getItem(position)
            textView!!.setText(unit)
            return textView!!
        }
    }


    /**
     * Item category auto suggest adapter
     */
    class CategoryAutoSuggestCustomAdapter(context: Context, var groceryItemEntityCategoriesParams: List<String>):
        ArrayAdapter<String>(context, 0, groceryItemEntityCategoriesParams) {
        private var groceryItemEntityCategories: List<String>? = null
        init{
            groceryItemEntityCategories = ArrayList(groceryItemEntityCategoriesParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntityCategories(searchTerm)
                        results.apply {
                            results.values = arrayList
                            results.count = arrayList.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                val res:List<String> = results?.values as ArrayList<String>
                addAll(res)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue as CharSequence
            }
        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var textView:TextView? = null
            if(convertView == null){
                textView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                textView = convertView as TextView?
            }
            val category = getItem(position)
            textView!!.setText(category)
            return textView!!
        }
    }





}

