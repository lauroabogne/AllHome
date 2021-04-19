package com.example.allhome.storage

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.databinding.ActivityStorageAddItemBinding
import com.example.allhome.databinding.StorageExpirationLayoutBinding
import com.example.allhome.storage.viewmodel.StorageAddItemViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PantryAddItemActivity : AppCompatActivity() {
    internal lateinit var mStorageAddItemViewModel: StorageAddItemViewModel
    private lateinit var mActivityPantryAddItemBinding: ActivityStorageAddItemBinding
    var mAction = ADD_NEW_RECORD_ACTION
    var mStorage:String? = null
    var mStorageItemUniqueId:String? = null
    var mStorageItemName:String? = null

    companion object {
        val PANTRY_ITEM_UNIQUE_ID_TAG = "PANTRY_ITEM_UNIQUE_ID_TAG"
        val PANTRY_ITEM_NAME_TAG = "PANTRY_ITEM_NAME_TAG"
        val STORAGE_NAME_TAG = "STORAGE_NAME_TAG"
        val ACTION_TAG = "ACTION_TAG"
        val ADD_NEW_RECORD_ACTION = 1
        val UPDATE_RECORD_ACTION = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = "Add Pantry Item"
        mStorageAddItemViewModel = ViewModelProvider(this).get(StorageAddItemViewModel::class.java)
        intent.getStringExtra(STORAGE_NAME_TAG)?.let {
            mStorage = it
        }?:run {
            Toast.makeText(this@PantryAddItemActivity,"Storage require",Toast.LENGTH_SHORT).show()
            finish()
        }

        intent.getIntExtra(ACTION_TAG,ADD_NEW_RECORD_ACTION)?.let {

            mAction = it
            if(mAction == UPDATE_RECORD_ACTION){

                mStorageItemUniqueId = intent.getStringExtra(PANTRY_ITEM_UNIQUE_ID_TAG)
                mStorageItemName = intent.getStringExtra(PANTRY_ITEM_NAME_TAG)


                mStorageAddItemViewModel.coroutineScope.launch {
                    mStorageAddItemViewModel.setStorageItemAndExpirations(this@PantryAddItemActivity, mStorageItemUniqueId!!, mStorageItemName!!)
                }
            }
        }

        mActivityPantryAddItemBinding =  DataBindingUtil.setContentView(this,R.layout.activity_storage_add_item)
        mActivityPantryAddItemBinding.lifecycleOwner = this
        mActivityPantryAddItemBinding.pantryAddItemViewModel = mStorageAddItemViewModel


        val pantryItemsExpirationRecyclerViewAdapter = PantryItemRecyclerViewAdapter(this)
        if(mAction == UPDATE_RECORD_ACTION){
            pantryItemsExpirationRecyclerViewAdapter.storageItemExpirationsEntity = mStorageAddItemViewModel.storageItemExpirationsEntity
        }
        mActivityPantryAddItemBinding.pantryItemExpirationRecyclerview.adapter = pantryItemsExpirationRecyclerViewAdapter

        mActivityPantryAddItemBinding.pantryAddExpirationBtn.setOnClickListener {
            showCalendar()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pantry_add_item_menu, menu)
        if(mAction == ADD_NEW_RECORD_ACTION){

            menu?.findItem(R.id.update_pantry_item_menu)?.setVisible(false)
            menu?.findItem(R.id.save_pantry_item_menu)?.setVisible(true)

        }else{
            menu?.findItem(R.id.update_pantry_item_menu)?.setVisible(true)
            menu?.findItem(R.id.save_pantry_item_menu)?.setVisible(false)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
            R.id.save_pantry_item_menu->{
                saveNewRecord()
            }
            R.id.update_pantry_item_menu->{
                updateRecord()
            }
        }
        return true
    }

    fun saveNewRecord(){
        val pantryItem = mActivityPantryAddItemBinding.pantryItemTextinput.text.toString().trim()
        val pantryItemQuantity = mActivityPantryAddItemBinding.pantryItemQuantityTextinput.text.toString().trim()
        val pantryItemUnit = mActivityPantryAddItemBinding.pantryItemUnitTextinput.text.toString().trim()
        val pantryItemStockWeightCheckedId = mActivityPantryAddItemBinding.pantryItemStockWeightRadiogroup.checkedRadioButtonId
        val pantryItemNotes = mActivityPantryAddItemBinding.pantryItemNotesTextinput.text.toString().trim()


        val pantryItemStockWeightIntValue = StorageUtil.stockWeightIntegerIdToIntegerValue(pantryItemStockWeightCheckedId)
        val quantityIntValue = if (pantryItemQuantity.length <=0)  StorageItemEntityValues.NO_QUANTITY_INPUT.toDouble()  else pantryItemQuantity.toDouble()

        if(pantryItem.length <=0){
            Toast.makeText(this,"Please provide pantry item name",Toast.LENGTH_SHORT).show()
            return
        }
        var pantryItemUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


        val pantryItemEntity = StorageItemEntity(
            uniqueId = pantryItemUniqueID,
            name=pantryItem,
            quantity =quantityIntValue,
            unit = pantryItemUnit,
            stockWeight = pantryItemStockWeightIntValue,
            category = "",
            storage = mStorage!!,
            notes = pantryItemNotes,
            imageName = "",
            created = currentDatetime,
            modified = currentDatetime
        )



        mStorageAddItemViewModel.coroutineScope.launch {
            var savedSuccessfully = true
            val allHomeDatabase = AllHomeDatabase.getDatabase(this@PantryAddItemActivity);
            try{
                allHomeDatabase.withTransaction {
                    val pantryItemId = mStorageAddItemViewModel.saveStorageItemEntity(this@PantryAddItemActivity,pantryItemEntity)

                    if(pantryItemId <= 0){
                        throw Exception("Failed to save")
                    }
                    for(pantryItemExpirationEntity in mStorageAddItemViewModel.storageItemExpirationsEntity){
                        val pantryItemExpirationUniqueID = UUID.randomUUID().toString()

                        pantryItemExpirationEntity.uniqueId = pantryItemExpirationUniqueID
                        pantryItemExpirationEntity.created = currentDatetime
                        pantryItemExpirationEntity.storage = mStorage!!
                        pantryItemExpirationEntity.pantryItemName = pantryItemEntity.name

                        val pantryItemExpirationId = mStorageAddItemViewModel.saveStorageItemExpirationEntity(this@PantryAddItemActivity,pantryItemExpirationEntity)

                        if(pantryItemExpirationId <= 0){
                            throw Exception("Failed to save")
                        }
                    }

                }
            }catch (ex:java.lang.Exception){
                savedSuccessfully = false
            }

            withContext(Main){
                if(savedSuccessfully){
                    Toast.makeText(this@PantryAddItemActivity,"Pantry item saved",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@PantryAddItemActivity,"Failed to save pantry item",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateRecord(){
        val pantryItem = mActivityPantryAddItemBinding.pantryItemTextinput.text.toString().trim()
        val pantryItemQuantity = mActivityPantryAddItemBinding.pantryItemQuantityTextinput.text.toString().trim()
        val pantryItemUnit = mActivityPantryAddItemBinding.pantryItemUnitTextinput.text.toString().trim()
        val pantryItemStockWeightCheckedId = mActivityPantryAddItemBinding.pantryItemStockWeightRadiogroup.checkedRadioButtonId
        val pantryItemNotes = mActivityPantryAddItemBinding.pantryItemNotesTextinput.text.toString().trim()


        val pantryItemStockWeightIntValue = StorageUtil.stockWeightIntegerIdToIntegerValue(pantryItemStockWeightCheckedId)
        val quantityIntValue = if (pantryItemQuantity.length <=0)  StorageItemEntityValues.NO_QUANTITY_INPUT.toDouble()  else pantryItemQuantity.toDouble()

        if(pantryItem.length <=0){
            Toast.makeText(this,"Please provide pantry item name",Toast.LENGTH_SHORT).show()
            return
        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        val imageName = ""


       /* val pantryItemEntity = StorageItemEntity(
            uniqueId = mStorageItemUniqueId,
            name=pantryItem,
            quantity =quantityIntValue,
            unit = pantryItemUnit,
            stockWeight = pantryItemStockWeightIntValue,
            category = "",
            storage = mStorage!!,
            notes = pantryItemNotes,
            imageName = "",
            created = currentDatetime,
            modified = currentDatetime
        )
*/


        mStorageAddItemViewModel.coroutineScope.launch {
            var savedSuccessfully = true
            val allHomeDatabase = AllHomeDatabase.getDatabase(this@PantryAddItemActivity);
            try{
                allHomeDatabase.withTransaction {
                    val affectedRowCount = mStorageAddItemViewModel.updateStorageItemEntity(this@PantryAddItemActivity,pantryItem,quantityIntValue,pantryItemUnit,pantryItemStockWeightIntValue,
                    mStorage!!,pantryItemNotes,imageName,currentDatetime,mStorageItemUniqueId!!)

                    if(affectedRowCount <=0){
                        throw Exception("Failed to update record")
                    }

                    for(pantryItemExpirationEntity in mStorageAddItemViewModel.storageItemExpirationsEntity){
                        val pantryItemExpirationUniqueID = UUID.randomUUID().toString()

                        pantryItemExpirationEntity.uniqueId = pantryItemExpirationUniqueID
                        pantryItemExpirationEntity.created = currentDatetime
                        pantryItemExpirationEntity.storage = mStorage!!
                        pantryItemExpirationEntity.pantryItemName = pantryItem

                        val pantryItemExpirationId = mStorageAddItemViewModel.saveStorageItemExpirationEntity(this@PantryAddItemActivity,pantryItemExpirationEntity)

                        if(pantryItemExpirationId <= 0){
                            throw Exception("Failed to update expiration")
                        }
                    }

                }
            }catch (ex:java.lang.Exception){
                Log.e("error",ex.message.toString())
                savedSuccessfully = false
            }

            withContext(Main){
                if(savedSuccessfully){
                    Toast.makeText(this@PantryAddItemActivity,"Item updated",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@PantryAddItemActivity,"Failed to update item",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val stringDate= SimpleDateFormat("yyyy-MM-dd").format(date)

            var uniqueID = UUID.randomUUID().toString()

            mStorageAddItemViewModel.coroutineScope.launch {
                val pantryItemExpirationEntity = StorageItemExpirationEntity(uniqueId = uniqueID,pantryItemName="",expirationDate = stringDate,created = "",storage = mStorage!!)
                mStorageAddItemViewModel.addExpiration(pantryItemExpirationEntity)

                withContext(Main){
                    val pantryItemRecyclerViewAdapter = mActivityPantryAddItemBinding.pantryItemExpirationRecyclerview.adapter as PantryItemRecyclerViewAdapter
                    pantryItemRecyclerViewAdapter.storageItemExpirationsEntity = mStorageAddItemViewModel.storageItemExpirationsEntity
                    pantryItemRecyclerViewAdapter.notifyDataSetChanged()
                }
                Log.e("Expiration count",mStorageAddItemViewModel.storageItemExpirationsEntity.size.toString())

            }

            Log.e("expiration",stringDate)
        }

        val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        datePickerDialog.show()

    }
}

/**
 *
 */
class PantryItemRecyclerViewAdapter(val pantryAddItemActivity:PantryAddItemActivity): RecyclerView.Adapter<PantryItemRecyclerViewAdapter.ItemViewHolder>() {

    var storageItemExpirationsEntity:List<StorageItemExpirationEntity> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val pantryExpirationLayoutBinding = StorageExpirationLayoutBinding.inflate(layoutInflater,parent,false)
        val itemViewHolder = ItemViewHolder(pantryExpirationLayoutBinding)
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationsEntity = storageItemExpirationsEntity[position]
        holder.pantryExpirationLayoutBinding.pantryItemExpirationEntity = pantryItemExpirationsEntity
        holder.pantryExpirationLayoutBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {

        return storageItemExpirationsEntity.size

    }

    inner class  ItemViewHolder(var pantryExpirationLayoutBinding:StorageExpirationLayoutBinding): RecyclerView.ViewHolder(pantryExpirationLayoutBinding.root),View.OnClickListener{

        init{
            pantryExpirationLayoutBinding.deleteExpirationDateBtn.setOnClickListener(this)
            pantryExpirationLayoutBinding.editExpirationDateBtn.setOnClickListener(this)

        }
        override fun onClick(view: View?) {
            when(view!!.id){
                R.id.deleteExpirationDateBtn->{
                    pantryAddItemActivity.mStorageAddItemViewModel.storageItemExpirationsEntity.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
                R.id.editExpirationDateBtn->{

                }
            }
        }

    }
}