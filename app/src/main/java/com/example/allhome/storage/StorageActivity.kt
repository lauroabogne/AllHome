package com.example.allhome.storage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.data.entities.StorageItemWithExpirations
import com.example.allhome.databinding.ActivityStorageBinding
import com.example.allhome.databinding.StorageItemLayoutBinding
import com.example.allhome.databinding.StorageQuantityFilterBinding
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StorageActivity : AppCompatActivity() {

    lateinit var mStorageViewModel:StorageViewModel
    lateinit var mActivityPantryStorageBinding:ActivityStorageBinding

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null

    //var mStorage:String? = null
    lateinit var mStorageEntity:StorageEntity


    var mSearchView: SearchView? = null
    var mSelectedMenuItem:MenuItem? = null
    var mFilter = -1
    var mStockWeightFilterOptions = arrayListOf<Int>()


    var mFilterByDateModifiedDate: String =SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    var mFilterByQuantityOptionSelected = -1
    var mQuantityFilterFirstValue = 0
    var mQuantityFilterSecondValue = 0
    var mIsSearching = false
    var mSearchTerm = ""
    var mSearchJob = Job()

    companion object{
        const val ADD_ITEM_REQUEST_CODE = 1986
        const val UPDATE_ITEM_REQUEST_CODE = 1987

        const val STORAGE_EXTRA_DATA_TAG = "STORAGE_EXTRA_DATA_TAG"
        const val STORAGE_EXTRA_TAG = "STORAGE_EXTRA_TAG"
        const val STORAGE_ITEM_UNIQUE_ID_TAG = "STORAGE_ITEM_UNIQUE_ID_TAG"
        const val STORAGE_ITEM_NAME_TAG = "STORAGE_ITEM_NAME_TAG"
        const val STORAGE_ITEM_UNIT_TAG = "STORAGE_ITEM_UNIT_TAG"


        const val ADDED_NEW_ELEMENT = 0
        const val UPDATED_NEW_ELEMENT = 1

        const val NO_FILTER = 0
        const val FILTER_BY_STOCK_WEIGHT = 1
        const val FILTER_BY_QUANTITY = 2
        const val FILTER_BY_LAST_UPDATE = 3
        const val FILTER_BY_EXPIRED = 4


        const val GREATER_THAN_QUANTITY = 0
        const val LESS_THAN_QUANTITY = 1
        const val BETWEEN_QUANTITY = 2

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mStorageEntity  = intent.getParcelableExtra(STORAGE_EXTRA_TAG)!!


        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        mActivityPantryStorageBinding = DataBindingUtil.setContentView(this, R.layout.activity_storage)
        mActivityPantryStorageBinding.lifecycleOwner = this
        mActivityPantryStorageBinding.pantryStorageViewModel = mStorageViewModel
        mActivityPantryStorageBinding.storageEntity = mStorageEntity


        mActivityPantryStorageBinding.customToolbar.title = mStorageEntity.name
        // customCollapsingToolbarLayout.title = "Collapsing Toolbar"
        setSupportActionBar(mActivityPantryStorageBinding.customToolbar).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val itemName = intent.getStringExtra(STORAGE_ITEM_NAME_TAG)
        val itemUnit = intent.getStringExtra(STORAGE_ITEM_UNIT_TAG)
        mActivityPantryStorageBinding.swipeRefresh.isRefreshing = true
        if(itemName != null && itemUnit!=null){
            showSingleItem(itemName,itemUnit)
        }else{
            showAllItem()
        }
        mActivityPantryStorageBinding.swipeRefresh.isRefreshing = false


        mActivityPantryStorageBinding.swipeRefresh.setOnRefreshListener {
            if(itemName != null && itemUnit!=null){
                showSingleItem(itemName,itemUnit)
            }else{
                showAllItem()
            }
            mActivityPantryStorageBinding.swipeRefresh.isRefreshing = false
        }

        mActivityPantryStorageBinding.fab.setOnClickListener {

            val addStorageItemActivity = Intent(this, StorageAddItemActivity::class.java)
            addStorageItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, mStorageEntity.name)
            addStorageItemActivity.putExtra(StorageAddItemActivity.STORAGE_TAG,mStorageEntity)
            startActivityForResult(addStorageItemActivity, ADD_ITEM_REQUEST_CODE)
        }
        val pantryStorageRecyclerviewViewAdapater = StorageRecyclerviewViewAdapater(this)
        mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        mSelectedMenuItem = item

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.noFilterMenu -> {

                showAllItem()
            }
            R.id.stockWeightMenu -> {

                showStockWeightPopupForFilter()
            }
            R.id.stockMenu -> {

                showFilterByQuantityPopup()
            }
            R.id.lastUpdateMenu -> {
                showCalendarForLastUpdateFilter()
            }
            R.id.expiredMenu -> {


                filterByExpiredItem()
            }
        }


        return super.onOptionsItemSelected(item)
    }
    fun showAllItem(itemName:String? = null){

        mSelectedMenuItem?.isChecked = true
        mFilter = NO_FILTER

        mStorageViewModel.coroutineScope.launch {
            val pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, itemName,mStorageEntity.uniqueId)

            withContext(Main){

                val storageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                storageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                storageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
    }
    fun showSingleItem(itemName:String,itemUnit:String){

        mSelectedMenuItem?.isChecked = true


        mStorageViewModel.coroutineScope.launch {
            //val pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, itemName,mStorageEntity.uniqueId)
            val pantryItemWithExpirations = mStorageViewModel.getSingleStorageItemWithExpirations(this@StorageActivity, itemName,itemUnit,mStorageEntity.uniqueId)

            withContext(Main){

                val storageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                storageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                storageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
    }
    private fun showStockWeightPopupForFilter(){
        val choices = arrayOf(
            getString(R.string.no_stock) + " items",
            getString(R.string.low_stock) + " stock items",
            getString(R.string.high_stock) + " stock  items"
        )
        val choicesInitial = booleanArrayOf(false, false, false)

        if(mFilter == FILTER_BY_STOCK_WEIGHT){
            mStockWeightFilterOptions.forEachIndexed{index,value->
                choicesInitial[value] = true
            }
        }

        val alertDialog =  MaterialAlertDialogBuilder(this)
            .setTitle("Select options")
            .setMultiChoiceItems(choices, choicesInitial, null)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.setOnShowListener {

            val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveBtn.setOnClickListener {

                mSelectedMenuItem?.isChecked = true
                mFilter = FILTER_BY_STOCK_WEIGHT

                mStockWeightFilterOptions = arrayListOf<Int>()

                alertDialog.listView.checkedItemPositions.forEach { key, isChecked ->
                     if(key == 0 && isChecked){
                         mStockWeightFilterOptions.add(StorageItemEntityValues.NO_STOCK)
                    }else if(key == 1 && isChecked){
                         mStockWeightFilterOptions.add(StorageItemEntityValues.LOW_STOCK)
                    }else if(key == 2 && isChecked){
                         mStockWeightFilterOptions.add(StorageItemEntityValues.HIGH_STOCK)
                    }
                }

                mStorageViewModel.coroutineScope.launch {

                    val storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByStockWeight(this@StorageActivity,mSearchTerm, mStorageEntity.uniqueId, mStockWeightFilterOptions)

                    withContext(Main){

                        alertDialog.dismiss()

                        val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                        pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations
                        pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
                    }
                }

            }
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        alertDialog.show()


    }
    private fun filterByExpiredItem(){
        mFilter = FILTER_BY_EXPIRED
        mSelectedMenuItem?.isChecked = true

        mStorageViewModel.coroutineScope.launch {

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            mFilterByDateModifiedDate = simpleDateFormat.format(Date())

            val storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByExpiredItem(this@StorageActivity,"", mStorageEntity.uniqueId, mFilterByDateModifiedDate)
            withContext(Main){
                 val storageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                storageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations
                storageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
    }
    private fun showFilterByQuantityPopup(){

        val storageQuanityFilterBinding = DataBindingUtil.inflate<StorageQuantityFilterBinding>(LayoutInflater.from(this), R.layout.storage_quantity_filter, null, false)

        if(mFilter == FILTER_BY_QUANTITY){

            if(mFilterByQuantityOptionSelected == GREATER_THAN_QUANTITY){
                storageQuanityFilterBinding.greaterThanRadioButton.isChecked = true
                storageQuanityFilterBinding.greaterThanEditText.setText(mQuantityFilterFirstValue.toString())
                storageQuanityFilterBinding.greaterThanEditText.isEnabled = true
                storageQuanityFilterBinding.greaterThanEditText.requestFocus()

            }else if(mFilterByQuantityOptionSelected == LESS_THAN_QUANTITY){
                storageQuanityFilterBinding.lessThanRadioButton.isChecked = true
                storageQuanityFilterBinding.lessThanEditText.setText(mQuantityFilterFirstValue.toString())
                storageQuanityFilterBinding.lessThanEditText.isEnabled = true
                storageQuanityFilterBinding.lessThanEditText.requestFocus()

            }else if(mFilterByQuantityOptionSelected == BETWEEN_QUANTITY){
                storageQuanityFilterBinding.betweenRadioButton.isChecked = true
                storageQuanityFilterBinding.betweenFromInput.setText(mQuantityFilterFirstValue.toString())
                storageQuanityFilterBinding.betweenFromInput.isEnabled = true
                storageQuanityFilterBinding.betweenFromInput.requestFocus()

                storageQuanityFilterBinding.betweenToInput.setText(mQuantityFilterSecondValue.toString())
                storageQuanityFilterBinding.betweenToInput.isEnabled = true
            }
        }
        val alertDialog =  MaterialAlertDialogBuilder(this)
            .setTitle("Filter by stock")
            .setView(storageQuanityFilterBinding.root)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.setOnShowListener {

            val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveBtn.setOnClickListener {

                mSelectedMenuItem?.isChecked = true
                mFilter = FILTER_BY_QUANTITY

                var hasError = false
                var errorMessage = ""

                mStorageViewModel.coroutineScope.launch {

                    var storageItemWithExpirations:ArrayList<StorageItemWithExpirations>? = null

                    if(storageQuanityFilterBinding.greaterThanRadioButton.isChecked){
                        val quantityString = storageQuanityFilterBinding.greaterThanEditText.text.toString()

                        if(quantityString.trim().isEmpty()){
                            hasError = true
                            errorMessage ="Please input quantity in greater than input"

                        }else{
                            withContext(Main){
                                mQuantityFilterFirstValue = quantityString.toInt()
                                mFilterByQuantityOptionSelected = GREATER_THAN_QUANTITY
                            }
                            storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterGreaterThan(this@StorageActivity,"", mStorageEntity.uniqueId, quantityString.toInt())

                        }

                    }else if(storageQuanityFilterBinding.lessThanRadioButton.isChecked){
                        val quantityString = storageQuanityFilterBinding.lessThanEditText.text.toString()
                        if(quantityString.trim().isEmpty()){
                            hasError = true
                            errorMessage ="Please input quantity in greater than input"

                        }else{
                            withContext(Main){
                                mQuantityFilterFirstValue = quantityString.toInt()
                                mFilterByQuantityOptionSelected = LESS_THAN_QUANTITY
                            }

                            storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterLessThan(this@StorageActivity,"", mStorageEntity.uniqueId, quantityString.toInt())

                        }

                    }else if(storageQuanityFilterBinding.betweenRadioButton.isChecked){
                        val quantityFromString = storageQuanityFilterBinding.betweenFromInput.text.toString()
                        val quantityToString = storageQuanityFilterBinding.betweenToInput.text.toString()

                        if(quantityFromString.trim().isEmpty() || quantityToString.trim().isEmpty() ){
                            hasError = true
                            errorMessage ="Please input between quantity"

                        }else{
                            withContext(Main){
                                mFilterByQuantityOptionSelected = BETWEEN_QUANTITY
                                mQuantityFilterFirstValue = quantityFromString.toInt()
                                mQuantityFilterSecondValue = quantityToString.toInt()
                            }

                            storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterBetween(this@StorageActivity, "",mStorageEntity.uniqueId, quantityFromString.toInt(), quantityToString.toInt())
                        }

                    }else{
                        hasError = true
                        errorMessage = "Please select option"
                    }

                    withContext(Main){

                        if(hasError){
                            Toast.makeText(this@StorageActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            return@withContext
                        }
                        alertDialog.dismiss()

                        val storageStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                        storageStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations!!
                        storageStorageRecyclerviewViewAdapater.notifyDataSetChanged()

                        hideKeyboard()
                    }
                }

            }
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
        quantityFilterCheckListener(storageQuanityFilterBinding)

    }
    private fun quantityFilterCheckListener(storageQuanityFilterBinding: StorageQuantityFilterBinding){
        storageQuanityFilterBinding.greaterThanRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){

                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

                storageQuanityFilterBinding.lessThanRadioButton.isChecked = false
                storageQuanityFilterBinding.lessThanEditText.setText("")
                storageQuanityFilterBinding.lessThanEditText.isEnabled = false

                storageQuanityFilterBinding.betweenRadioButton.isChecked = false
                storageQuanityFilterBinding.betweenFromInput.setText("")
                storageQuanityFilterBinding.betweenFromInput.isEnabled = false

                storageQuanityFilterBinding.greaterThanEditText.isEnabled  = true
                storageQuanityFilterBinding.greaterThanEditText.requestFocus()


                showSoftKeyboard(storageQuanityFilterBinding.greaterThanEditText)



            }
        }

        storageQuanityFilterBinding.lessThanRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){

                storageQuanityFilterBinding.greaterThanRadioButton.isChecked = false
                storageQuanityFilterBinding.greaterThanEditText.setText("")
                storageQuanityFilterBinding.greaterThanEditText.isEnabled = false

                storageQuanityFilterBinding.betweenRadioButton.isChecked = false
                storageQuanityFilterBinding.betweenFromInput.setText("")
                storageQuanityFilterBinding.betweenFromInput.isEnabled = false
                storageQuanityFilterBinding.betweenToInput.setText("")
                storageQuanityFilterBinding.betweenToInput.isEnabled = false

                storageQuanityFilterBinding.lessThanEditText.isEnabled = true
                storageQuanityFilterBinding.lessThanEditText.requestFocus()
                showSoftKeyboard(storageQuanityFilterBinding.lessThanEditText)
            }
        }

        storageQuanityFilterBinding.betweenRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){
                storageQuanityFilterBinding.greaterThanRadioButton.isChecked = false
                storageQuanityFilterBinding.greaterThanEditText.setText("")
                storageQuanityFilterBinding.greaterThanEditText.isEnabled = false

                storageQuanityFilterBinding.lessThanRadioButton.isChecked = false
                storageQuanityFilterBinding.lessThanEditText.setText("")
                storageQuanityFilterBinding.lessThanEditText.isEnabled = false


                storageQuanityFilterBinding.betweenFromInput.isEnabled = true
                storageQuanityFilterBinding.betweenToInput.isEnabled = true
                storageQuanityFilterBinding.betweenFromInput.requestFocus()

                showSoftKeyboard(storageQuanityFilterBinding.betweenFromInput)


            }
        }
    }
    fun showSoftKeyboard(view: View){
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            view.applicationWindowToken, InputMethodManager.SHOW_FORCED, 0
        )
    }
    fun hideKeyboard() {

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Handler(Looper.getMainLooper()).postDelayed({
            if(resultCode == Activity.RESULT_OK && requestCode == ADD_ITEM_REQUEST_CODE){

                val storageItemUniqueId = data!!.getStringExtra(STORAGE_ITEM_UNIQUE_ID_TAG)!!

                displayItem(storageItemUniqueId, ADDED_NEW_ELEMENT)

            }else if(resultCode == Activity.RESULT_OK && requestCode == UPDATE_ITEM_REQUEST_CODE){

                if(mIsSearching){
                    Log.e("SEARCHING","TRUE")
                }else{
                    Log.e("SEARCHING","NOT TRUE")
                }


                val storageItemUniqueId = data!!.getStringExtra(STORAGE_ITEM_UNIQUE_ID_TAG)!!
                displayItem(storageItemUniqueId, UPDATED_NEW_ELEMENT)


            }


        //}, 200)



    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.storage_item_activity_menu, menu)

        when(mFilter){
            NO_FILTER->{
                menu?.findItem(R.id.noFilterMenu)?.isChecked = true
            }
            FILTER_BY_STOCK_WEIGHT->{
                menu?.findItem(R.id.stockWeightMenu)?.isChecked = true
            }
            FILTER_BY_QUANTITY->{
                menu?.findItem(R.id.stockMenu)?.isChecked = true
            }
            FILTER_BY_LAST_UPDATE->{
                menu?.findItem(R.id.lastUpdateMenu)?.isChecked = true
            }
            FILTER_BY_EXPIRED->{
                menu?.findItem(R.id.expiredMenu)?.isChecked = true
            }
        }


        mSearchView = menu?.findItem(R.id.appBarSearch)?.actionView as SearchView
        mSearchView?.let {

            it.queryHint = "Enter item name"
            it.setOnSearchClickListener { mIsSearching = true }
            it.setOnCloseListener(object:SearchView.OnCloseListener{
                override fun onClose(): Boolean {
                    mIsSearching = false
                    return false
                }
            })


            it.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    it.clearFocus()
                    query?.let {
                        mSearchTerm = it
                        if(mSearchJob == null){
                            mSearchJob = Job()
                        }else{
                            mSearchJob.cancel()
                            mSearchJob = Job()
                        }

                        CoroutineScope(IO+mSearchJob).launch{
                            val storageItemWithExpirations = getItems(it,mStorageEntity.uniqueId)
                            withContext(Main){
                                val storageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                                storageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations
                                storageRecyclerviewViewAdapater.notifyDataSetChanged()
                            }
                        }

                    }
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        mSearchTerm = it
                        if(mSearchJob == null){
                            mSearchJob = Job()
                        }else{
                            mSearchJob.cancel()
                            mSearchJob = Job()
                        }

                        CoroutineScope(IO+mSearchJob).launch{

                            val storageItemWithExpirations = getItems(it,mStorageEntity.uniqueId)

                            withContext(Main){
                                val storageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                                storageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations
                                storageRecyclerviewViewAdapater.notifyDataSetChanged()
                            }
                        }
                    }

                    return true
                }
            })
        }

        return true
    }
    fun showCalendarForLastUpdateFilter(){

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            mFilter = FILTER_BY_LAST_UPDATE
            mSelectedMenuItem?.isChecked = true

            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            mFilterByDateModifiedDate =  SimpleDateFormat("yyyy-MM-dd").format(date)

            mStorageViewModel.coroutineScope.launch {

                val storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByDateModified(this@StorageActivity, "",mFilterByDateModifiedDate, mStorageEntity.uniqueId)

                withContext(Main){
                    val storageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                    storageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations
                    storageRecyclerviewViewAdapater.notifyDataSetChanged()
                }
            }

        }

        val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    private fun displayItem(storageItemUniqueId: String, itemChangeType: Int){

        mStorageViewModel.coroutineScope.launch {

            var storageItemWithExpirations: ArrayList<StorageItemWithExpirations> = ArrayList()

            if(mFilter == NO_FILTER){
                if(mIsSearching){
                    storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, mSearchTerm,mStorageEntity.uniqueId)
                }else{
                    storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, null,mStorageEntity.uniqueId)
                }
            }else if(mFilter == FILTER_BY_STOCK_WEIGHT){

                if(mIsSearching){

                    storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByStockWeight(this@StorageActivity, mSearchTerm,mStorageEntity.uniqueId, mStockWeightFilterOptions)
                }else{
                    storageItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByStockWeight(this@StorageActivity, null,mStorageEntity.uniqueId, mStockWeightFilterOptions)
                }

            }else if(mFilter == FILTER_BY_QUANTITY){

                if(mFilterByQuantityOptionSelected == GREATER_THAN_QUANTITY){

                    storageItemWithExpirations =  mStorageViewModel.getStorageItemWithExpirationsFilterGreaterThan(this@StorageActivity,mSearchTerm, mStorageEntity.uniqueId, mQuantityFilterFirstValue)

                }else if(mFilterByQuantityOptionSelected == LESS_THAN_QUANTITY){

                    storageItemWithExpirations =  mStorageViewModel.getStorageItemWithExpirationsFilterLessThan(this@StorageActivity,mSearchTerm, mStorageEntity.uniqueId, mQuantityFilterFirstValue)

                }else if(mFilterByQuantityOptionSelected == BETWEEN_QUANTITY){

                    storageItemWithExpirations =  mStorageViewModel.getStorageItemWithExpirationsFilterBetween(this@StorageActivity, mSearchTerm,mStorageEntity.uniqueId, mQuantityFilterFirstValue, mQuantityFilterSecondValue)
                }

            }else if(mFilter == FILTER_BY_EXPIRED){

                storageItemWithExpirations =   mStorageViewModel.getStorageItemWithExpirationsFilterByExpiredItem(this@StorageActivity,mSearchTerm, mStorageEntity.uniqueId, mFilterByDateModifiedDate)


            }else if(mFilter == FILTER_BY_LAST_UPDATE){
                storageItemWithExpirations =   mStorageViewModel.getStorageItemWithExpirationsFilterByDateModified(this@StorageActivity, mSearchTerm,mFilterByDateModifiedDate, mStorageEntity.uniqueId)
            }




            withContext(Main){

                val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                val currentIndexOfItem = pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations.indexOfFirst { it.storageItemEntity.uniqueId == storageItemUniqueId }

                pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations



                val newIndexOfItem = pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations.indexOfFirst {
                    it.storageItemEntity.uniqueId == storageItemUniqueId
                }


                if(itemChangeType == UPDATED_NEW_ELEMENT){
                    if(newIndexOfItem >=0){
                        pantryStorageRecyclerviewViewAdapater.notifyItemChanged(newIndexOfItem)
                    }else{
                        pantryStorageRecyclerviewViewAdapater.notifyItemRemoved(currentIndexOfItem)
                        return@withContext
                    }
                }else if(itemChangeType == ADDED_NEW_ELEMENT && newIndexOfItem >= 0){
                    pantryStorageRecyclerviewViewAdapater.notifyItemInserted(newIndexOfItem)
                }
                else if(itemChangeType == ADDED_NEW_ELEMENT && newIndexOfItem < 0){
                    return@withContext
                }
                Handler(Looper.getMainLooper()).postDelayed(
                    {

                        animateElement(newIndexOfItem, itemChangeType)

                    }, 200)




            }
        }


    }
    private fun animateElement(indexOfNewItem: Int, itemChangeType: Int){

        val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fadeInAnimation.duration = 250
        fadeInAnimation.fillAfter = true

        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if (itemChangeType == UPDATED_NEW_ELEMENT) {
                    mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter?.notifyItemChanged(indexOfNewItem)
                }

            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })

        val firstVisibleItemPosition = ( mActivityPantryStorageBinding.pantryStorageRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val lastVisibleItemPosition = ( mActivityPantryStorageBinding.pantryStorageRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        if(indexOfNewItem >= firstVisibleItemPosition && indexOfNewItem <= lastVisibleItemPosition){

            val viewHolder = mActivityPantryStorageBinding.pantryStorageRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
            val itemViewHolder = viewHolder as StorageRecyclerviewViewAdapater.ItemViewHolder
            itemViewHolder.storageItemLayoutBinding.pantryItemParentLayout.startAnimation(fadeInAnimation)


        }else{

            mActivityPantryStorageBinding.appBar.setExpanded(false)

            mActivityPantryStorageBinding.pantryStorageRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var found = false
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val viewHolder = mActivityPantryStorageBinding.pantryStorageRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
                    if (viewHolder != null) {

                        val itemViewHolder = viewHolder as StorageRecyclerviewViewAdapater.ItemViewHolder
                        itemViewHolder.storageItemLayoutBinding.pantryItemParentLayout.startAnimation(fadeInAnimation)

                        found = true
                        mActivityPantryStorageBinding.pantryStorageRecyclerview.removeOnScrollListener(this)

                    }
                }

            })
            mActivityPantryStorageBinding.pantryStorageRecyclerview.scrollToPosition(indexOfNewItem)

        }
    }
    private fun zoomImageFromThumb(thumbView: View, imageUri: Uri) {

        // The system "short" animation time duration, in milliseconds. This
        // duration is ideal for subtle animations or animations that occur
        // very frequently.
        val shortAnimationDuration: Int = 200

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        val expandedImageView: ImageView = findViewById(R.id.expanded_image)
        expandedImageView.setImageURI(imageUri)
        expandedImageView.visibility = View.VISIBLE



        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        findViewById<View>(R.id.container).getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        Log.e("DATA 1",startBounds.toString())
        Log.e("DATA 2",finalBounds.toString())

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }



        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.INVISIBLE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.INVISIBLE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }
    suspend fun  getItems(searchTerm:String,storageUniqueId:String):ArrayList<StorageItemWithExpirations>{

        if(mFilter == NO_FILTER){
            return mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, searchTerm,storageUniqueId)
        }else if(mFilter == FILTER_BY_STOCK_WEIGHT){
           return mStorageViewModel.getStorageItemWithExpirationsFilterByStockWeight(this@StorageActivity, searchTerm,storageUniqueId, mStockWeightFilterOptions)
        }else if(mFilter == FILTER_BY_QUANTITY){

            if(mFilterByQuantityOptionSelected == GREATER_THAN_QUANTITY){
                return mStorageViewModel.getStorageItemWithExpirationsFilterGreaterThan(this@StorageActivity,searchTerm, mStorageEntity.uniqueId, mQuantityFilterFirstValue)
            }else if(mFilterByQuantityOptionSelected == LESS_THAN_QUANTITY){

                return mStorageViewModel.getStorageItemWithExpirationsFilterLessThan(this@StorageActivity,searchTerm, mStorageEntity.uniqueId, mQuantityFilterFirstValue)

            }else if(mFilterByQuantityOptionSelected == BETWEEN_QUANTITY){

                return mStorageViewModel.getStorageItemWithExpirationsFilterBetween(this@StorageActivity, searchTerm,mStorageEntity.uniqueId, mQuantityFilterFirstValue, mQuantityFilterSecondValue)
            }
            return ArrayList()

        }else if(mFilter == FILTER_BY_EXPIRED){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            return mStorageViewModel.getStorageItemWithExpirationsFilterByExpiredItem(this@StorageActivity,searchTerm, mStorageEntity.uniqueId, currentDatetime)
        }else if(mFilter == FILTER_BY_LAST_UPDATE){

            return mStorageViewModel.getStorageItemWithExpirationsFilterByDateModified(this@StorageActivity, searchTerm,mFilterByDateModifiedDate, mStorageEntity.uniqueId)
        }else{
            return  ArrayList()
        }



    }

    val productImageClickListener = View.OnClickListener {
        val storageItemWithExpirations: StorageItemWithExpirations = it.tag as StorageItemWithExpirations
        val imageUri = StorageUtil.getStorageItemImageUriFromPath(it.context, storageItemWithExpirations.storageItemEntity.imageName)
        zoomImageFromThumb(it, imageUri!!)

    }


    class StorageRecyclerviewViewAdapater(val storageActivity: StorageActivity): RecyclerView.Adapter<StorageRecyclerviewViewAdapater.ItemViewHolder>(),OnItemRemovedListener/*,Filterable*/ {
        val mPantryStorageActivity = storageActivity
        var mStorageItemWithExpirations:List<StorageItemWithExpirations> = arrayListOf()

        /*val onItemRemovedListener = object : OnItemRemovedListener {
            override fun doneRemoving(index: Int) {
                this@PantryStorageRecyclerviewViewAdapater.notifyItemRemoved(index)
            }
        }*/
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val pantryItemLayoutBinding = StorageItemLayoutBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val storageItemWithExpirations = mStorageItemWithExpirations[position]
            holder.storageItemLayoutBinding.pantryItemWithExpirations = storageItemWithExpirations
            holder.storageItemLayoutBinding.storageImageView.tag = storageItemWithExpirations
            holder.storageItemLayoutBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {

            return mStorageItemWithExpirations.size
        }

        override fun doneRemoving(index: Int) {

            mPantryStorageActivity.mStorageViewModel.storageItemWithExpirations.removeAt(index)
            notifyItemRemoved(index)
        }


        inner class  ItemViewHolder(var storageItemLayoutBinding: StorageItemLayoutBinding, val storageRecyclerviewViewAdapater: StorageRecyclerviewViewAdapater): RecyclerView.ViewHolder(storageItemLayoutBinding.root),View.OnClickListener{
            init {
                storageItemLayoutBinding.moreActionImageBtn.setOnClickListener(this)
                storageItemLayoutBinding.storageImageView.setOnClickListener(mPantryStorageActivity.productImageClickListener)
                storageItemLayoutBinding.pantryItemParentLayout.setOnClickListener(this)



            }


            override fun onClick(view: View?) {

                when(view?.id){
                    R.id.moreActionImageBtn -> {
                        val popupMenu = PopupMenu(view.context, storageItemLayoutBinding.moreActionImageBtn)
                        popupMenu.menuInflater.inflate(R.menu.pantry_item_menu, popupMenu.menu)
                        popupMenu.setOnMenuItemClickListener(CustomPopupMenuItemCLickListener(view.context, this, adapterPosition, mPantryStorageActivity))
                        popupMenu.show()
                    }
                    R.id.pantryItemParentLayout -> {

                        val pantryItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPosition].storageItemEntity

                        val addPantryItemActivity = Intent(storageActivity, StorageAddItemActivity::class.java)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, storageActivity.mStorageEntity.name)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.ACTION_TAG, StorageAddItemActivity.UPDATE_RECORD_ACTION)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG, pantryItemEntity.uniqueId)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_NAME_TAG, pantryItemEntity.name)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_TAG, storageActivity.mStorageEntity)


                        storageActivity.startActivityForResult(addPantryItemActivity, UPDATE_ITEM_REQUEST_CODE)

                    }
                }

            }

        }


    }

}

interface OnItemRemovedListener{
    fun doneRemoving(index: Int)
}
 class CustomPopupMenuItemCLickListener(val context: Context, val itemViewHolder: StorageActivity.StorageRecyclerviewViewAdapater.ItemViewHolder, val adapterPostion: Int, val storageActivity: StorageActivity):PopupMenu.OnMenuItemClickListener{
    override fun onMenuItemClick(item: MenuItem?): Boolean {

        val storageItemWithExpirations = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion]
        val storageItemEntity =storageItemWithExpirations.storageItemEntity

        when(item?.itemId){

            R.id.pantryItemEditMenu -> {

                val pantryItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion].storageItemEntity

                val addPantryItemActivity = Intent(context, StorageAddItemActivity::class.java)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, storageActivity.mStorageEntity.name)
                addPantryItemActivity.putExtra(StorageAddItemActivity.ACTION_TAG, StorageAddItemActivity.UPDATE_RECORD_ACTION)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG, pantryItemEntity.uniqueId)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_NAME_TAG, pantryItemEntity.name)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_TAG,storageActivity.mStorageEntity)


                val pantryStorageActivity = context as StorageActivity
                pantryStorageActivity.startActivityForResult(addPantryItemActivity, StorageActivity.UPDATE_ITEM_REQUEST_CODE)


            }
            R.id.pantryItemMoveStorageMenu -> {


                val storageItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion]
                val storageStorageListActiviy = Intent(context, StorageStorageListActivity::class.java)

                storageStorageListActiviy.putExtra(StorageFragment.ACTION_TAG,StorageFragment.STORAGE_TRASFERING_ITEM_ACTION)
                storageStorageListActiviy.putExtra(StorageFragment.STORAGE_ITEM_ENTITY_TAG, storageItemEntity)
                storageStorageListActiviy.putExtra(StorageFragment.STORAGE_ENTITY_TAG, storageActivity.mStorageEntity)


                val storageActivity = context as StorageActivity
                storageActivity.startActivity(storageStorageListActiviy)
            }
            R.id.pantryItemDeleteMenu -> {


                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val currentDatetime: String = simpleDateFormat.format(Date())
                var updatedAll = true


                storageActivity.mStorageViewModel.coroutineScope.launch {
                    val allHomeDatabase = AllHomeDatabase.getDatabase(storageActivity)

                    try {
                        allHomeDatabase.withTransaction {

                            val updatedStorageItemRecordCount = storageActivity.mStorageViewModel.updateItemAsDeleted(storageActivity, currentDatetime, storageItemEntity)
                            val updatedStorageDeliverItemCount = storageActivity.mStorageViewModel.updateStorageExpirationDateAsDeleted(storageActivity, currentDatetime, storageItemEntity)

                            if (updatedStorageItemRecordCount <= 0) {
                                throw Exception("Failed to update record")
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("error", ex.message.toString())
                        updatedAll = false
                    }

                    withContext(Main) {
                        if (!updatedAll) {
                            Toast.makeText(storageActivity, "Failed to update record", Toast.LENGTH_SHORT).show()
                            return@withContext
                        }


                        itemViewHolder.storageRecyclerviewViewAdapater.doneRemoving(adapterPostion)


                    }

                }


            }
            R.id.pantryAddToGroceryListMenu -> {

                Toast.makeText(context,"working here",Toast.LENGTH_SHORT).show()


                val storageGroceryListActivity = Intent(context, StorageGroceryListActivity::class.java)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ACTION_TAG, StorageGroceryListActivity.ADD_SINGLE_PRODUCT_ACTION)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_NAME_TAG, storageItemEntity.name)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_UNIT_TAG, storageItemEntity.unit)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.IMAGE_NAME_TAG, storageItemEntity.imageName)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.STORAGE_TAG, storageActivity.mStorageEntity)

                val storageActivity = context as StorageActivity
                storageActivity.startActivity(storageGroceryListActivity)

            }
        }
        return true
    }

}

