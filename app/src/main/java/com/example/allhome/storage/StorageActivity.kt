package com.example.allhome.storage

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.data.entities.StorageItemWithExpirations
import com.example.allhome.databinding.ActivityStorageBinding
import com.example.allhome.databinding.PantryItemLayoutBinding
import com.example.allhome.databinding.PantrySimpleExpirationLayoutBinding
import com.example.allhome.storage.viewmodel.StorageViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PantryStorageActivity : AppCompatActivity() {

    lateinit var mStorageViewModel:StorageViewModel
    lateinit var mActivityPantryStorageBinding:ActivityStorageBinding

    companion object{
        val UPDATE_REQUEST_CODE = 1986
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        mActivityPantryStorageBinding = DataBindingUtil.setContentView(this,R.layout.activity_storage)
        mActivityPantryStorageBinding.lifecycleOwner = this
        mActivityPantryStorageBinding.pantryStorageViewModel = mStorageViewModel

        mStorageViewModel.coroutineScope.launch {
            val pantryItemWithExpirations = mStorageViewModel.getPatryItemWithExpirations(this@PantryStorageActivity,"Pantry")

            withContext(Main){

                val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as PantryStorageRecyclerviewViewAdapater
                pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }

        mActivityPantryStorageBinding.fab.setOnClickListener {
            val addPantryItemActivity = Intent(this, PantryAddItemActivity::class.java)
            addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_NAME_TAG,"Pantry")
            startActivity(addPantryItemActivity)
        }
        val pantryStorageRecyclerviewViewAdapater = PantryStorageRecyclerviewViewAdapater(this)
        mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater
    }


    class PantryStorageRecyclerviewViewAdapater(pantryStorageActivity:PantryStorageActivity): RecyclerView.Adapter<PantryStorageRecyclerviewViewAdapater.ItemViewHolder>() {
        val mPantryStorageActivity = pantryStorageActivity
        var mStorageItemWithExpirations:List<StorageItemWithExpirations> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val pantryItemLayoutBinding = PantryItemLayoutBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding)
            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val pantryItemWithExpirations = mStorageItemWithExpirations[position]
            holder.pantryItemLayoutBinding.pantryItemWithExpirations = pantryItemWithExpirations
            holder.pantryItemLayoutBinding.executePendingBindings()
            holder.generateExpirationDates()
        }

        override fun getItemCount(): Int {

            return mStorageItemWithExpirations.size
        }

        inner class  ItemViewHolder(var pantryItemLayoutBinding: PantryItemLayoutBinding): RecyclerView.ViewHolder(pantryItemLayoutBinding.root),View.OnClickListener{
            init {
                pantryItemLayoutBinding.moreActionImageBtn.setOnClickListener(this)

            }
            fun generateExpirationDates(){

                var expirations = mStorageItemWithExpirations[adapterPosition].expirations
                val pantryItemExpirationViewAdapter = PantryItemExpirationViewAdapter()
                pantryItemExpirationViewAdapter.storageItemExpirationEntities = expirations
                pantryItemLayoutBinding.pantryItemExpirationRecyclerview.adapter = pantryItemExpirationViewAdapter
                pantryItemExpirationViewAdapter.notifyDataSetChanged()
            }

            override fun onClick(view: View?) {
                val popupMenu = PopupMenu(view!!.context,view)
                popupMenu.menuInflater.inflate(R.menu.pantry_item_menu, popupMenu.menu)
               popupMenu.setOnMenuItemClickListener(CustomPopupMenuItemCLickListener(view.context, adapterPosition,mPantryStorageActivity))
                popupMenu.show()
            }
        }
    }

}


 class CustomPopupMenuItemCLickListener(val context: Context, val adapterPostion:Int,val pantryStorageActivity:PantryStorageActivity):PopupMenu.OnMenuItemClickListener{
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.pantry_item_edit_menu->{

                val pantryItemEntity = pantryStorageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion].storageItemEntity

                val addPantryItemActivity = Intent(context, PantryAddItemActivity::class.java)
                addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_NAME_TAG,"Pantry")
                addPantryItemActivity.putExtra(PantryAddItemActivity.ACTION_TAG,PantryAddItemActivity.UPDATE_RECORD_ACTION)
                addPantryItemActivity.putExtra(PantryAddItemActivity.PANTRY_ITEM_UNIQUE_ID_TAG,pantryItemEntity.uniqueId)
                addPantryItemActivity.putExtra(PantryAddItemActivity.PANTRY_ITEM_NAME_TAG,pantryItemEntity.name)


                val pantryStorageActivity = context as PantryStorageActivity
                pantryStorageActivity.startActivityForResult(addPantryItemActivity,PantryAddItemActivity.UPDATE_RECORD_ACTION)


            }
            R.id.pantry_item_move_storage_menu->{

            }
            R.id.pantry_item_delete_menu->{

            }
        }
        return true
    }

}

class PantryItemExpirationViewAdapter(): RecyclerView.Adapter<PantryItemExpirationViewAdapter.ItemViewHolder>() {
    var storageItemExpirationEntities:List<StorageItemExpirationEntity> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val pantrySimpleExpirationLayoutBinding = PantrySimpleExpirationLayoutBinding.inflate(layoutInflater,parent,false)
        val itemViewHolder = ItemViewHolder(pantrySimpleExpirationLayoutBinding)
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationEntity = storageItemExpirationEntities[position]
        holder.pantrySimpleExpirationLayoutBinding.pantryItemExpirationEntity = pantryItemExpirationEntity
        holder.pantrySimpleExpirationLayoutBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
       return storageItemExpirationEntities.size
    }
    inner class  ItemViewHolder(var pantrySimpleExpirationLayoutBinding: PantrySimpleExpirationLayoutBinding): RecyclerView.ViewHolder(pantrySimpleExpirationLayoutBinding.root){
        init {
            pantrySimpleExpirationLayoutBinding.pantryItemExpirationEntity
        }


    }

}