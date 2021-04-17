package com.example.allhome.pantry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.PantryItemExpirationEntity
import com.example.allhome.data.entities.PantryItemWithExpirations
import com.example.allhome.databinding.ActivityPantryStorageBinding
import com.example.allhome.databinding.PantryExpirationLayoutBinding
import com.example.allhome.databinding.PantryItemLayoutBinding
import com.example.allhome.databinding.PantrySimpleExpirationLayoutBinding
import com.example.allhome.pantry.viewmodel.PantryAddItemViewModel
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
                pantryStorageRecyclerviewViewAdapater.pantryItemWithExpirations = pantryItemWithExpirations
                pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
        val pantryStorageRecyclerviewViewAdapater = PantryStorageRecyclerviewViewAdapater()
        mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater
    }


    class PantryStorageRecyclerviewViewAdapater: RecyclerView.Adapter<PantryStorageRecyclerviewViewAdapater.ItemViewHolder>() {

        var pantryItemWithExpirations:List<PantryItemWithExpirations> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val pantryItemLayoutBinding = PantryItemLayoutBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding)
            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val pantryItemWithExpirations = pantryItemWithExpirations[position]
            holder.pantryItemLayoutBinding.pantryItemWithExpirations = pantryItemWithExpirations
            holder.pantryItemLayoutBinding.executePendingBindings()
            holder.test()
        }

        override fun getItemCount(): Int {

            Log.e("DATA",pantryItemWithExpirations.size.toString())
            return pantryItemWithExpirations.size
        }

        inner class  ItemViewHolder(var pantryItemLayoutBinding: PantryItemLayoutBinding): RecyclerView.ViewHolder(pantryItemLayoutBinding.root){
            init {

               // var expirations = pantryItemWithExpirations[adapterPosition].expirations




                /*if(pantryItemLayoutBinding.pantryItemWithExpirations?.expirations != null){*/
                   /* val pantryItemExpirationViewAdapter = PantryItemExpirationViewAdapter()
                    Log.e("TEST","not null")
                    pantryItemExpirationViewAdapter.pantryItemExpirationEntities = expirations
                    pantryItemLayoutBinding.pantryItemExpirationRecyclerview.adapter = pantryItemExpirationViewAdapter
                    pantryItemExpirationViewAdapter.notifyDataSetChanged()*/
               /* }else{
                    Log.e("TEST","null")
                }*/

                Log.e("DATA",pantryItemLayoutBinding.pantryItemWithExpirations.toString())
               /* pantryItemExpirationViewAdapter.pantryItemExpirationEntities = pantryItemLayoutBinding.pantryItemWithExpirations!!.expirations
                pantryItemLayoutBinding.pantryItemExpirationRecyclerview.adapter = pantryItemExpirationViewAdapter
                pantryItemExpirationViewAdapter.notifyDataSetChanged()*/
            }
            fun test(){
                Log.e("adapter_postion",adapterPosition.toString()+" "+layoutPosition.toString())

                var expirations = pantryItemWithExpirations[adapterPosition].expirations
                val pantryItemExpirationViewAdapter = PantryItemExpirationViewAdapter()
                pantryItemExpirationViewAdapter.pantryItemExpirationEntities = expirations
                pantryItemLayoutBinding.pantryItemExpirationRecyclerview.adapter = pantryItemExpirationViewAdapter
                pantryItemExpirationViewAdapter.notifyDataSetChanged()
            }
        }
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

        }
    }

}