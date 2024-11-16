package com.example.allhome.storage

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.StorageItemDAO
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.ActivityStorageGroceryListBinding
import com.example.allhome.databinding.StorageGroceryListLayoutBinding
import com.example.allhome.global_ui.CustomConfirmationDialog
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.example.allhome.utils.ImageUtil
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StorageGroceryListActivity : AppCompatActivity() {

    lateinit var mStorageViewModel: StorageViewModel
    lateinit var mActivityStorageGroceryListBinding: ActivityStorageGroceryListBinding

    var mAction = ADD_SINGLE_PRODUCT_ACTION
    lateinit var mStorageName:String
    lateinit var mStorageEntity:StorageEntity

    companion object{
        const val ACTION_TAG = "ACTION_TAG"
        const val ITEM_NAME_TAG = "ITEM_NAME_TAG"
        const val ITEM_UNIT_TAG = "ITEM_UNIT_TAG"
        const val IMAGE_NAME_TAG = "IMAGE_NAME_TAG"
        const val STORAGE_NAME_TAG = "STORAGE_NAME_TAG"
        const val STORAGE_TAG = "STORAGE_TAG"
        const val ADD_MULTIPLE_PRODUCT_CONDITION_TAG = "ADD_MULTIPLE_PRODUCT_CONDITION_TAG"
        const val ADD_SINGLE_PRODUCT_ACTION = 0
        const val ADD_MULTIPLE_PRODUCT_ACTION = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Select grocery list"
        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        intent.getParcelableExtra<StorageEntity>(STORAGE_TAG)?.let{
            mStorageEntity = it
        }?: kotlin.run {
            Toast.makeText(this,"Storage is require",Toast.LENGTH_SHORT).show()
            finish()
        }
        intent.getIntExtra(ACTION_TAG, ADD_SINGLE_PRODUCT_ACTION).let { action->
            if(action == ADD_SINGLE_PRODUCT_ACTION){
                mAction = action
                val itemName = intent.getStringExtra(ITEM_NAME_TAG)
                val itemUnit =  intent.getStringExtra(ITEM_UNIT_TAG)
                val imageName =  intent.getStringExtra(IMAGE_NAME_TAG)
                if(itemName != null && itemUnit !=null && imageName !=null){
                    mStorageViewModel.newGroceryItemEntity = GroceryItemEntity(
                        groceryListUniqueId = "",
                        itemName = itemName,
                        unit = itemUnit,
                        imageName = imageName
                    )
                }
            }
            if(action == ADD_MULTIPLE_PRODUCT_ACTION){
                mAction = action
                mStorageName = intent.getStringExtra(STORAGE_NAME_TAG)!!
                mStorageViewModel.addMultipleGroceryItemEntityCondition = intent.getIntegerArrayListExtra(ADD_MULTIPLE_PRODUCT_CONDITION_TAG)!!

            }
        }

        mActivityStorageGroceryListBinding = DataBindingUtil.setContentView<ActivityStorageGroceryListBinding>(this,R.layout.activity_storage_grocery_list).apply {
            lifecycleOwner = this@StorageGroceryListActivity
            storageViewModel = mStorageViewModel
        }

        val pantryStorageRecyclerviewViewAdapater = StorageGroceryListRecyclerviewViewAdapater(this)
        mActivityStorageGroceryListBinding.storageGroceryListRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater
        mStorageViewModel.coroutineScope.launch {
            mStorageViewModel.getGroceryLists(this@StorageGroceryListActivity)
            val storageGroceryListRecyclerviewViewAdapater= mActivityStorageGroceryListBinding.storageGroceryListRecyclerview.adapter as StorageGroceryListRecyclerviewViewAdapater
            storageGroceryListRecyclerviewViewAdapater.mGroceryListWithItemCount = mStorageViewModel.groceryLists
            withContext(Main){
                storageGroceryListRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
        mActivityStorageGroceryListBinding.fab.setOnClickListener{
            showGroceryListNameInput()
        }


        mActivityStorageGroceryListBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        mActivityStorageGroceryListBinding.toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(mActivityStorageGroceryListBinding.toolbar)

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
                val id = mStorageViewModel.createNewGroceryList(this@StorageGroceryListActivity,groceryListEntity)
                if(id > 0){
                    val groceryListWithItemCount = mStorageViewModel.getGroceryList(this@StorageGroceryListActivity,groceryListEntity.autoGeneratedUniqueId)
                    mStorageViewModel.groceryLists.add(0,groceryListWithItemCount)
                }
                withContext(Main){
                    groceryListNameInputDialog.mAlertDialog.dismiss()
                    mActivityStorageGroceryListBinding.storageGroceryListRecyclerview.adapter?.notifyDataSetChanged()
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


    private fun copyStoreItemImageToGroceryItemImage(storeItemItemImageName:String, groceryItemName:String, groceryItemUniqueId:String): String? {

        if(storeItemItemImageName == null || storeItemItemImageName.isEmpty()){
            return null
        }
        val storageItemImageDir: File? = getExternalFilesDir(ImageUtil.STORAGE_ITEM_IMAGES_FINAL_LOCATION)
        if(storageItemImageDir?.exists() == true){
            val fileToCopy  = File(storageItemImageDir, storeItemItemImageName)
            if(fileToCopy.exists()){

                val destinationGroceryItemImageDir: File = getExternalFilesDir(GroceryUtil.GROCERY_ITEM_IMAGES_LOCATION)!!
                if(!destinationGroceryItemImageDir.exists()){
                    destinationGroceryItemImageDir.mkdir()
                }

                val finalFile  = File(destinationGroceryItemImageDir, groceryItemUniqueId.plus(groceryItemName))

                var source: FileChannel? = FileInputStream(fileToCopy).channel
                var destination: FileChannel? =  FileOutputStream(finalFile).channel

                if (destination != null && source != null) {
                    destination.transferFrom(source, 0, source.size())
                }
                source?.close()
                destination?.close()

                return finalFile.name

            }
        }
        return null

    }


    class StorageGroceryListRecyclerviewViewAdapater(val storageGroceryListActivity:StorageGroceryListActivity): RecyclerView.Adapter<StorageGroceryListRecyclerviewViewAdapater.ItemViewHolder>() {
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

                            if(storageGroceryListActivity.mAction == ADD_SINGLE_PRODUCT_ACTION){
                                val itemName = storageGroceryListActivity.mStorageViewModel.newGroceryItemEntity!!.itemName
                                val itemUnit = storageGroceryListActivity.mStorageViewModel.newGroceryItemEntity!!.unit
                                val itemImageName = storageGroceryListActivity.mStorageViewModel.newGroceryItemEntity!!.imageName

                                storageGroceryListActivity.mStorageViewModel.coroutineScope.launch {
                                    val groceryItemEntity = storageGroceryListActivity.mStorageViewModel.getSingleGroceryItemEntity(storageGroceryListActivity,groceryListAutoGeneratedId, itemName,itemUnit)
                                    var id:Long = 0

                                    if(groceryItemEntity == null){

                                        val autoGeneratedUniqueID = UUID.randomUUID().toString()
                                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        val currentDatetime: String = simpleDateFormat.format(Date())
                                        val newGroceryItemEntity = storageGroceryListActivity.mStorageViewModel.newGroceryItemEntity as GroceryItemEntity
                                        val latestPrice = storageGroceryListActivity.mStorageViewModel.getLatestPrice(storageGroceryListActivity,itemName,itemUnit)


                                        val groceryListItemImageName = storageGroceryListActivity.copyStoreItemImageToGroceryItemImage(itemImageName, itemName, autoGeneratedUniqueID)?.let { imageName->imageName }?:run{""}
                                        newGroceryItemEntity.uniqueId = autoGeneratedUniqueID
                                        newGroceryItemEntity.quantity = 1.0
                                        newGroceryItemEntity.imageName = groceryListItemImageName
                                        newGroceryItemEntity.groceryListUniqueId = groceryListAutoGeneratedId
                                        newGroceryItemEntity.datetimeCreated = currentDatetime
                                        newGroceryItemEntity.datetimeModified  = currentDatetime
                                        newGroceryItemEntity.pricePerUnit = latestPrice
                                        id = storageGroceryListActivity.mStorageViewModel.addGroceryListItem(storageGroceryListActivity,newGroceryItemEntity)
                                    }

                                    withContext(Main){
                                        if(id > 0 || groceryItemEntity !=null){
                                            Toast.makeText(storageGroceryListActivity,"Item added successfully",Toast.LENGTH_SHORT).show()
                                            storageGroceryListActivity.finish()
                                        }else{
                                            Toast.makeText(storageGroceryListActivity,"Item failed to add. Please try again.",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }else{
                                val containExpirationCondition = storageGroceryListActivity.mStorageViewModel.addMultipleGroceryItemEntityCondition.contains(StorageItemEntityValues.EXPIRED)
                                val addMultipleGroceryItemEntityConditionCount = storageGroceryListActivity.mStorageViewModel.addMultipleGroceryItemEntityCondition.size
                                if(containExpirationCondition && addMultipleGroceryItemEntityConditionCount == 1){
                                    addStorageItemToGroceryListThatExpired(groceryListAutoGeneratedId)
                                }else if(containExpirationCondition && addMultipleGroceryItemEntityConditionCount > 1){
                                    addStorageItemToGroceryListThatExpiredAndWithOtherCondition(groceryListAutoGeneratedId)
                                }else{
                                    addStorageItemToGroceryListWithStockWeights(groceryListAutoGeneratedId)
                                }
                            }
                        }
                    })
                    customConfirmationDialog.show()

                }
            }
        }

        fun addStorageItemToGroceryListThatExpired(groceryListAutoGeneratedId:String){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            storageGroceryListActivity.mStorageViewModel.coroutineScope.launch {

                var allInserted = true
                val allHomeDatabase = AllHomeDatabase.getDatabase(storageGroceryListActivity)

                try{
                    allHomeDatabase.withTransaction {

                        val expiredItems:List<StorageItemDAO.SimpleGroceryLisItem> = storageGroceryListActivity.mStorageViewModel.getExpiredItemByStorage(storageGroceryListActivity,storageGroceryListActivity.mStorageEntity.uniqueId,currentDatetime)
                        expiredItems.forEach { simpleGroceryLisItem->

                            val groceryItemEntity:GroceryItemEntity? = storageGroceryListActivity.mStorageViewModel.getSingleGroceryItemEntity(storageGroceryListActivity,groceryListAutoGeneratedId,simpleGroceryLisItem.itemName,simpleGroceryLisItem.unit)
                            groceryItemEntity?: kotlin.run {
                                // if null the execute
                                val autoGeneratedUniqueID = UUID.randomUUID().toString()
                                val itemName = simpleGroceryLisItem.itemName
                                val itemUnit = simpleGroceryLisItem.unit
                                val itemImageName = simpleGroceryLisItem.imageName
                                val latestPrice = storageGroceryListActivity.mStorageViewModel.getLatestPrice(storageGroceryListActivity,itemName,itemUnit)

                                val groceryListItemImageName = storageGroceryListActivity.copyStoreItemImageToGroceryItemImage(itemImageName, itemName, autoGeneratedUniqueID)?.let { imageName->imageName }?:run{""}
                                val newGroceryItemEntity = GroceryItemEntity(
                                    groceryListUniqueId = groceryListAutoGeneratedId,
                                    uniqueId = autoGeneratedUniqueID,
                                    itemName = itemName,
                                    imageName = groceryListItemImageName,
                                    unit = itemUnit,
                                    quantity = 1.0,
                                    pricePerUnit =latestPrice,
                                    datetimeCreated = currentDatetime,
                                    datetimeModified = currentDatetime
                                )

                                val id = storageGroceryListActivity.mStorageViewModel.addGroceryListItem(storageGroceryListActivity,newGroceryItemEntity)
                                if(id <= 0){
                                    throw Exception("Failed to add item in grocery list")
                                }
                            }
                        }
                    }
                }catch (ex: Exception){

                    allInserted = false
                }

                withContext(Main){
                    if(allInserted){
                        Toast.makeText(storageGroceryListActivity,"Item added successfully",Toast.LENGTH_SHORT).show()
                        storageGroceryListActivity.finish()
                    }else{
                        Toast.makeText(storageGroceryListActivity,"Item failed to add. Please try again.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun addStorageItemToGroceryListThatExpiredAndWithOtherCondition(groceryListAutoGeneratedId:String){

            val stockWeights:ArrayList<Int> = ArrayList(storageGroceryListActivity.mStorageViewModel.addMultipleGroceryItemEntityCondition)

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            storageGroceryListActivity.mStorageViewModel.coroutineScope.launch {

                var allInserted = true
                val allHomeDatabase = AllHomeDatabase.getDatabase(storageGroceryListActivity)

                try{
                    allHomeDatabase.withTransaction {

                        val stockWeights:ArrayList<Int> = ArrayList(storageGroceryListActivity.mStorageViewModel.addMultipleGroceryItemEntityCondition)


                        val expiredItems:List<StorageItemDAO.SimpleGroceryLisItem> = storageGroceryListActivity.mStorageViewModel.getExpiredItemsWithStockWeight(storageGroceryListActivity,storageGroceryListActivity.mStorageEntity.uniqueId,currentDatetime,stockWeights)

                         expiredItems.forEach { simpleGroceryLisItem->
                            val groceryItemEntity:GroceryItemEntity? = storageGroceryListActivity.mStorageViewModel.getSingleGroceryItemEntity(storageGroceryListActivity,groceryListAutoGeneratedId,simpleGroceryLisItem.itemName,simpleGroceryLisItem.unit)
                            groceryItemEntity?: kotlin.run {
                                // if null the execute

                                val autoGeneratedUniqueID = UUID.randomUUID().toString()
                                val itemName = simpleGroceryLisItem.itemName
                                val itemUnit = simpleGroceryLisItem.unit
                                val itemImageName = simpleGroceryLisItem.imageName
                                val latestPrice = storageGroceryListActivity.mStorageViewModel.getLatestPrice(storageGroceryListActivity,itemName,itemUnit)

                                val groceryListItemImageName = storageGroceryListActivity.copyStoreItemImageToGroceryItemImage(itemImageName, itemName, autoGeneratedUniqueID)?.let { imageName->imageName }?:run{""}

                                val newGroceryItemEntity = GroceryItemEntity(
                                    uniqueId = autoGeneratedUniqueID,
                                    groceryListUniqueId = groceryListAutoGeneratedId,
                                    itemName = itemName,
                                    unit = itemUnit,
                                    imageName = groceryListItemImageName,
                                    quantity = 1.0,
                                    pricePerUnit=latestPrice,
                                    datetimeCreated = currentDatetime,
                                    datetimeModified = currentDatetime
                                )

                                val id = storageGroceryListActivity.mStorageViewModel.addGroceryListItem(storageGroceryListActivity,newGroceryItemEntity)
                                if(id <= 0){
                                    throw Exception("Failed to add item in grocery list")
                                }
                            }
                        }
                    }
                }catch (ex: Exception){

                    allInserted = false
                }

                withContext(Main){
                    if(allInserted){
                        Toast.makeText(storageGroceryListActivity,"Item added successfully",Toast.LENGTH_SHORT).show()
                        storageGroceryListActivity.finish()
                    }else{
                        Toast.makeText(storageGroceryListActivity,"Item failed to add. Please try again.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun addStorageItemToGroceryListWithStockWeights(groceryListAutoGeneratedId:String){

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            storageGroceryListActivity.mStorageViewModel.coroutineScope.launch {

                var allInserted = true
                val allHomeDatabase = AllHomeDatabase.getDatabase(storageGroceryListActivity)

                try{
                    allHomeDatabase.withTransaction {


                        val stockWeights:ArrayList<Int> = ArrayList(storageGroceryListActivity.mStorageViewModel.addMultipleGroceryItemEntityCondition)
                        val expiredItems:List<StorageItemDAO.SimpleGroceryLisItem> = storageGroceryListActivity.mStorageViewModel.getItemsWithStockWeight(storageGroceryListActivity,storageGroceryListActivity.mStorageEntity.uniqueId,stockWeights)
                        expiredItems.forEach { simpleGroceryLisItem->
                            val groceryItemEntity:GroceryItemEntity? = storageGroceryListActivity.mStorageViewModel.getSingleGroceryItemEntity(storageGroceryListActivity,groceryListAutoGeneratedId,simpleGroceryLisItem.itemName,simpleGroceryLisItem.unit)
                            groceryItemEntity?: kotlin.run {
                                // if null the execute

                                val autoGeneratedUniqueID = UUID.randomUUID().toString()
                                val itemName = simpleGroceryLisItem.itemName
                                val itemUnit = simpleGroceryLisItem.unit
                                val itemImageName = simpleGroceryLisItem.imageName
                                val latestPrice = storageGroceryListActivity.mStorageViewModel.getLatestPrice(storageGroceryListActivity,itemName,itemUnit)
                                val groceryListItemImageName = storageGroceryListActivity.copyStoreItemImageToGroceryItemImage(itemImageName, itemName, autoGeneratedUniqueID)?.let { imageName->imageName }?:run{""}

                                val newGroceryItemEntity = GroceryItemEntity(
                                    uniqueId = autoGeneratedUniqueID,
                                    groceryListUniqueId = groceryListAutoGeneratedId,
                                    itemName = itemName,
                                    unit = itemUnit,
                                    imageName = groceryListItemImageName,
                                    quantity = 1.0,
                                    pricePerUnit = latestPrice,
                                    datetimeCreated = currentDatetime,
                                    datetimeModified = currentDatetime
                                )

                                val id = storageGroceryListActivity.mStorageViewModel.addGroceryListItem(storageGroceryListActivity,newGroceryItemEntity)
                                if(id <= 0){
                                    throw Exception("Failed to add item in grocery list")
                                }
                            }
                        }
                    }
                }catch (ex: Exception){

                    allInserted = false
                }

                withContext(Main){
                    if(allInserted){
                        Toast.makeText(storageGroceryListActivity,"Item added successfully",Toast.LENGTH_SHORT).show()
                        storageGroceryListActivity.finish()
                    }else{
                        Toast.makeText(storageGroceryListActivity,"Item failed to add. Please try again.",Toast.LENGTH_SHORT).show()
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



