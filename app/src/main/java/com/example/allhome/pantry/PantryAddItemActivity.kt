package com.example.allhome.pantry

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
import com.example.allhome.data.entities.PantryItemEntity
import com.example.allhome.data.entities.PantryItemEntityValues
import com.example.allhome.data.entities.PantryItemExpirationEntity
import com.example.allhome.databinding.ActivityPantryAddItemBinding
import com.example.allhome.databinding.PantryExpirationLayoutBinding
import com.example.allhome.pantry.viewmodel.PantryAddItemViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PantryAddItemActivity : AppCompatActivity() {
    private lateinit var mPantryAddItemViewModel: PantryAddItemViewModel
    private lateinit var mActivityPantryAddItemBinding:ActivityPantryAddItemBinding
    var mAction = ADD_NEW_RECORD_ACTION
    companion object {

        val ADD_NEW_RECORD_ACTION = 1
        val UPDATE_RECORD_ACTION = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = "Add Pantry Item"

        mPantryAddItemViewModel = ViewModelProvider(this).get(PantryAddItemViewModel::class.java)
        mActivityPantryAddItemBinding =  DataBindingUtil.setContentView(this,R.layout.activity_pantry_add_item)
        mActivityPantryAddItemBinding.lifecycleOwner = this
        mActivityPantryAddItemBinding.pantryAddItemViewModel = mPantryAddItemViewModel


        val pantryItemsExpirationRecyclerViewAdapter = PantryItemRecyclerViewAdapter()
        mActivityPantryAddItemBinding.pantryItemExpirationRecyclerview.adapter = pantryItemsExpirationRecyclerViewAdapter

        mActivityPantryAddItemBinding.pantryAddExpirationBtn.setOnClickListener {
            showCalendar()
        }

        mPantryAddItemViewModel.coroutineScope.launch {
            AllHomeDatabase.getDatabase(this@PantryAddItemActivity).withTransaction {  }
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
                Toast.makeText(this,"Update",Toast.LENGTH_SHORT).show()
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
        val storage = "Pantry"

        val pantryItemStockWeightIntValue = PantryUtil.stockWeightIntegerIdToIntegerValue(pantryItemStockWeightCheckedId)
        val quantityIntValue = if (pantryItemQuantity.length <=0)  PantryItemEntityValues.NO_QUANTITY_INPUT.toDouble()  else pantryItemQuantity.toDouble()

        if(pantryItem.length <=0){
            Toast.makeText(this,"Please provide pantry item name",Toast.LENGTH_SHORT).show()
            return
        }
        var pantryItemUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


        val pantryItemEntity = PantryItemEntity(
            uniqueId = pantryItemUniqueID,
            name=pantryItem,
            quantity =quantityIntValue,
            unit = pantryItemUnit,
            stockWeight = pantryItemStockWeightIntValue,
            category = "",
            storage = storage,
            notes = pantryItemNotes,
            imageName = "",
            created = currentDatetime,
            modified = currentDatetime
        )



        mPantryAddItemViewModel.coroutineScope.launch {
            var savedSuccessfully = true
            val allHomeDatabase = AllHomeDatabase.getDatabase(this@PantryAddItemActivity);
            try{
                allHomeDatabase.withTransaction {
                    val pantryItemId = mPantryAddItemViewModel.savePantryItemEntity(this@PantryAddItemActivity,pantryItemEntity)

                    if(pantryItemId <= 0){
                        throw Exception("Failed to save")
                    }
                    for(pantryItemExpirationEntity in mPantryAddItemViewModel.pantryItemExpirationsEntity){
                        val pantryItemExpirationUniqueID = UUID.randomUUID().toString()

                        pantryItemExpirationEntity.uniqueId = pantryItemExpirationUniqueID
                        pantryItemExpirationEntity.created = currentDatetime
                        pantryItemExpirationEntity.pantryItemName = pantryItemEntity.name

                        val pantryItemExpirationId = mPantryAddItemViewModel.savePantryItemExpirationEntity(this@PantryAddItemActivity,pantryItemExpirationEntity)

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

            mPantryAddItemViewModel.coroutineScope.launch {
                val pantryItemExpirationEntity = PantryItemExpirationEntity(uniqueId = uniqueID,pantryItemName="",expirationDate = stringDate,created = "")
                mPantryAddItemViewModel.addExpiration(pantryItemExpirationEntity)

                withContext(Main){
                    val pantryItemRecyclerViewAdapter = mActivityPantryAddItemBinding.pantryItemExpirationRecyclerview.adapter as PantryItemRecyclerViewAdapter
                    pantryItemRecyclerViewAdapter.pantryItemExpirationsEntity = mPantryAddItemViewModel.pantryItemExpirationsEntity
                    pantryItemRecyclerViewAdapter.notifyDataSetChanged()
                }
                Log.e("Expiration count",mPantryAddItemViewModel.pantryItemExpirationsEntity.size.toString())

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
class PantryItemRecyclerViewAdapter: RecyclerView.Adapter<PantryItemRecyclerViewAdapter.ItemViewHolder>() {

    var pantryItemExpirationsEntity:List<PantryItemExpirationEntity> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val pantryExpirationLayoutBinding = PantryExpirationLayoutBinding.inflate(layoutInflater,parent,false)
        val itemViewHolder = ItemViewHolder(pantryExpirationLayoutBinding)
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationsEntity = pantryItemExpirationsEntity[position]
        holder.pantryExpirationLayoutBinding.pantryItemExpirationEntity = pantryItemExpirationsEntity
        holder.pantryExpirationLayoutBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        Log.e("COUNT",pantryItemExpirationsEntity.size.toString())
        return pantryItemExpirationsEntity.size

    }

    inner class  ItemViewHolder(var pantryExpirationLayoutBinding:PantryExpirationLayoutBinding): RecyclerView.ViewHolder(pantryExpirationLayoutBinding.root){

    }
}