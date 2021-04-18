package com.example.allhome.pantry

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.PantryItemExpirationEntity
import com.example.allhome.data.entities.PantryItemWithExpirations
import com.example.allhome.databinding.ActivityPantryStorageBinding
import com.example.allhome.databinding.PantryItemLayoutBinding
import com.example.allhome.databinding.PantrySimpleExpirationLayoutBinding
import com.example.allhome.pantry.viewmodel.PantryStorageViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PantryStorageActivity : AppCompatActivity() {

    lateinit var mPantryStorageViewModel:PantryStorageViewModel
    lateinit var mActivityPantryStorageBinding:ActivityPantryStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPantryStorageViewModel = ViewModelProvider(this).get(PantryStorageViewModel::class.java)
        mActivityPantryStorageBinding = DataBindingUtil.setContentView(this,R.layout.activity_pantry_storage)
        mActivityPantryStorageBinding.lifecycleOwner = this
        mActivityPantryStorageBinding.pantryStorageViewModel = mPantryStorageViewModel

        mPantryStorageViewModel.coroutineScope.launch {
            val pantryItemWithExpirations = mPantryStorageViewModel.getPatryItemWithExpirations(this@PantryStorageActivity,"Pantry")
            Log.e("data",pantryItemWithExpirations.toString())
            withContext(Main){
                Toast.makeText(this@PantryStorageActivity,"data get",Toast.LENGTH_SHORT).show()
                val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as PantryStorageRecyclerviewViewAdapater
                pantryStorageRecyclerviewViewAdapater.mPantryItemWithExpirations = pantryItemWithExpirations
                pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }

        mActivityPantryStorageBinding.fab.setOnClickListener {
            val addPantryItemActivity = Intent(this, PantryAddItemActivity::class.java)
            startActivity(addPantryItemActivity)
        }
        val pantryStorageRecyclerviewViewAdapater = PantryStorageRecyclerviewViewAdapater(this)
        mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater
    }


    class PantryStorageRecyclerviewViewAdapater(pantryStorageActivity:PantryStorageActivity): RecyclerView.Adapter<PantryStorageRecyclerviewViewAdapater.ItemViewHolder>() {
        val mPantryStorageActivity = pantryStorageActivity
        var mPantryItemWithExpirations:List<PantryItemWithExpirations> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val pantryItemLayoutBinding = PantryItemLayoutBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding)
            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val pantryItemWithExpirations = mPantryItemWithExpirations[position]
            holder.pantryItemLayoutBinding.pantryItemWithExpirations = pantryItemWithExpirations
            holder.pantryItemLayoutBinding.executePendingBindings()
            holder.generateExpirationDates()
        }

        override fun getItemCount(): Int {

            return mPantryItemWithExpirations.size
        }

        inner class  ItemViewHolder(var pantryItemLayoutBinding: PantryItemLayoutBinding): RecyclerView.ViewHolder(pantryItemLayoutBinding.root),View.OnClickListener{
            init {
                pantryItemLayoutBinding.moreActionImageBtn.setOnClickListener(this)

            }
            fun generateExpirationDates(){

                var expirations = mPantryItemWithExpirations[adapterPosition].expirations
                val pantryItemExpirationViewAdapter = PantryItemExpirationViewAdapter()
                pantryItemExpirationViewAdapter.pantryItemExpirationEntities = expirations
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


 class CustomPopupMenuItemCLickListener(context: Context, adapterPostion:Int,pantryStorageActivity:PantryStorageActivity):PopupMenu.OnMenuItemClickListener{
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.pantry_item_edit_menu->{

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
    var pantryItemExpirationEntities:List<PantryItemExpirationEntity> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val pantrySimpleExpirationLayoutBinding = PantrySimpleExpirationLayoutBinding.inflate(layoutInflater,parent,false)
        val itemViewHolder = ItemViewHolder(pantrySimpleExpirationLayoutBinding)
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationEntity = pantryItemExpirationEntities[position]
        holder.pantrySimpleExpirationLayoutBinding.pantryItemExpirationEntity = pantryItemExpirationEntity
        holder.pantrySimpleExpirationLayoutBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
       return pantryItemExpirationEntities.size
    }
    inner class  ItemViewHolder(var pantrySimpleExpirationLayoutBinding: PantrySimpleExpirationLayoutBinding): RecyclerView.ViewHolder(pantrySimpleExpirationLayoutBinding.root){
        init {
            pantrySimpleExpirationLayoutBinding.pantryItemExpirationEntity
        }


    }

}