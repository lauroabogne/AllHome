package com.example.allhome.grocerylist.trash_grocery_list

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityValues
import com.example.allhome.data.entities.GroceryListEntityValues
import com.example.allhome.databinding.GroceryListProductBinding
import com.example.allhome.databinding.TrashGroceryListProductBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TrashGroceryItemRecyclerViewAdapter(val contextParams: Context, val productImageClickListener:View.OnClickListener) : RecyclerView.Adapter<TrashGroceryItemRecyclerViewAdapter.ItemViewHolder>() {

    var mGroceryItems: List<GroceryItemEntity> = arrayListOf()
    var context: Context = contextParams;
    val mSingleGroceryListActivity: TrashSingleGroceryListActivity = contextParams as TrashSingleGroceryListActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrashGroceryItemRecyclerViewAdapter.ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val groceryListItemBinding = TrashGroceryListProductBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(groceryListItemBinding,productImageClickListener)

        return itemViewHolder;
    }

    override fun getItemCount(): Int {
        if (mGroceryItems != null) {
            return mGroceryItems!!.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val groceryItemEntity = mGroceryItems?.get(position)
        holder.groceryListItemBinding.groceryItemEntity = groceryItemEntity
        holder.groceryListItemBinding.executePendingBindings()
        holder.groceryListItemBinding.checkBox.isChecked = holder.groceryListItemBinding.groceryItemEntity?.bought == 1
        holder.groceryListItemBinding.itemImage.setTag(groceryItemEntity)

        if(groceryItemEntity.forCategoryDivider){
            holder.groceryListItemBinding.itemInformationLinearlayout.visibility = View.GONE
            holder.groceryListItemBinding.categoryDivierTextview.visibility = View.VISIBLE
            holder.groceryListItemBinding.categoryDivierTextview.text = groceryItemEntity.category

        }else{
            holder.groceryListItemBinding.itemInformationLinearlayout.visibility = View.VISIBLE
            holder.groceryListItemBinding.categoryDivierTextview.visibility = View.GONE
            holder.groceryListItemBinding.checkBox.isChecked = holder.groceryListItemBinding.groceryItemEntity?.bought == 1
            if (groceryItemEntity.bought == 0) {

                holder.groceryListItemBinding.groceryItemParentLayout.setBackgroundColor(Color.WHITE)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    (holder.groceryListItemBinding.root as CardView).cardElevation = 10F
                }
            } else {
                holder.groceryListItemBinding.groceryItemParentLayout.setBackgroundColor(Color.parseColor("#F2F3F4"))
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    (holder.groceryListItemBinding.root as CardView).cardElevation = 0F
                }
            }

        }


    }



    inner class ItemViewHolder(groceryListItemBindingParams: TrashGroceryListProductBinding,productImageClickListenerParams:View.OnClickListener) : RecyclerView.ViewHolder(groceryListItemBindingParams.root) {
        var groceryListItemBinding: TrashGroceryListProductBinding
        var productImageClickListener:View.OnClickListener
        init {
            productImageClickListener = productImageClickListenerParams
            groceryListItemBinding = groceryListItemBindingParams
            groceryListItemBinding.itemImage.setOnClickListener(productImageClickListener)
            groceryListItemBinding.checkBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener{buttonView, isChecked->

                if(buttonView!!.isPressed){
                    Toast.makeText(buttonView.context,"This list is already deleted. Please go to your grocery list",Toast.LENGTH_SHORT).show()
                    buttonView.isChecked = !isChecked
                }

            })
        }

    }



}


