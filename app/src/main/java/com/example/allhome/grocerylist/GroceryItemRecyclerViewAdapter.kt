package com.example.allhome.grocerylist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.databinding.GroceryListProductBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class GroceryItemRecyclerViewAdapter(val contextParams: Context) : RecyclerView.Adapter<GroceryItemRecyclerViewAdapter.ItemViewHolder>() {

    var mGroceryItems: List<GroceryItemEntity> = arrayListOf()
    var mTouchHelper: ItemTouchHelper? = null
    var mDraggable: Boolean = true
    var context: Context = contextParams;
    lateinit var mSelectedView: View
    val mSingleGroceryListActivity: SingleGroceryListActivity = contextParams as SingleGroceryListActivity

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryItemRecyclerViewAdapter.ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val groceryListItemBinding = GroceryListProductBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(groceryListItemBinding)

        return itemViewHolder;
    }

    override fun getItemCount(): Int {
        if (mGroceryItems != null) {
            return mGroceryItems!!.size
        }
        return 0
    }

    fun itemDroped() {


        /*val attrs = intArrayOf(R.attr.selectableItemBackground)
        val typedArray = context!!.obtainStyledAttributes(attrs)
        val backgroundResource = typedArray.getResourceId(0, 0)
        mSelectedView.setBackgroundResource(backgroundResource)
        typedArray.recycle()*/


    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val groceryItemEntity = mGroceryItems?.get(position)
        holder.groceryListItemBinding.groceryItemEntity = groceryItemEntity
        holder.groceryListItemBinding.executePendingBindings()

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
            } else {
                holder.groceryListItemBinding.groceryItemParentLayout.setBackgroundColor(Color.parseColor("#E5E2E2"))
            }
        }


    }

    inner class ItemViewHolder(groceryListItemBindingParams: GroceryListProductBinding) : RecyclerView.ViewHolder(groceryListItemBindingParams.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        var groceryListItemBinding: GroceryListProductBinding

        init {
            groceryListItemBinding = groceryListItemBindingParams
            groceryListItemBinding.groceryItemNameTextview.setOnClickListener(this)
            groceryListItemBinding.otherInformationTextview.setOnClickListener(this)
            groceryListItemBinding.checkBox.setOnCheckedChangeListener(this)
        }

        override fun onClick(view: View?) {
            val singleGroceryListActivity: SingleGroceryListActivity = context as SingleGroceryListActivity
            val groceryItemEntity = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList[adapterPosition]

            if (groceryItemEntity.bought == 1) {
                // do nothing
                return;
            }
            val popupMenu = PopupMenu(context, groceryListItemBinding.groceryItemNameTextview)
            popupMenu.menuInflater.inflate(R.menu.grocery_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(CustomPopupMenu(context, adapterPosition))
            popupMenu.show()
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val singleGroceryListActivity: SingleGroceryListActivity = context as SingleGroceryListActivity
            val groceryItemEntity: GroceryItemEntity = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList[adapterPosition]

            if (isChecked && buttonView!!.isPressed) {

                CoroutineScope(IO).launch {
                    singleGroceryListActivity.mGroceryListViewModel.updateGroceryItem(context, 1, groceryItemEntity!!.id, groceryItemEntity!!.itemName)
                    withContext(Main) {

                        groceryItemEntity.bought = 1
                        singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems.remove(groceryItemEntity)
                        singleGroceryListActivity.mGroceryListViewModel.boughtGroceryItems.add(groceryItemEntity)
                        singleGroceryListActivity.mGroceryListViewModel.mergeToBuyAndBoughtItems(singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems, singleGroceryListActivity.mGroceryListViewModel.boughtGroceryItems)

                        if(singleGroceryListActivity.mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.SORT_ALPHABETICALLY){
                            val newIndex = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.indexOf(groceryItemEntity)
                            notifyItemMoved(adapterPosition, newIndex)

                        }else{
                            mSingleGroceryListActivity.mGroceryListViewModel.groupByCategory()
                            val newIndex = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.indexOf(groceryItemEntity)
                            notifyItemMoved(adapterPosition, newIndex)
                        }



                        val layoutManager: LinearLayoutManager = singleGroceryListActivity.dataBindingUtil.groceryItemRecyclerview.layoutManager as LinearLayoutManager
                        val visible: Int = layoutManager.findFirstVisibleItemPosition()
                        layoutManager.scrollToPosition(visible)
                        groceryListItemBinding.groceryItemParentLayout.setBackgroundColor(Color.parseColor("#E5E2E2"))
                    }
                }

            } else if (!isChecked && buttonView!!.isPressed) {


                CoroutineScope(IO).launch {
                    singleGroceryListActivity.mGroceryListViewModel.updateGroceryItem(context, 0, groceryItemEntity!!.id, groceryItemEntity!!.itemName)
                    withContext(Main) {
                        groceryItemEntity.bought = 0
                        singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems.add(groceryItemEntity)
                        singleGroceryListActivity.mGroceryListViewModel.boughtGroceryItems.remove(groceryItemEntity)
                        singleGroceryListActivity.mGroceryListViewModel.mergeToBuyAndBoughtItems(singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems, singleGroceryListActivity.mGroceryListViewModel.boughtGroceryItems)

                        if(singleGroceryListActivity.mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.SORT_ALPHABETICALLY){
                            val newIndex = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.indexOf(groceryItemEntity)
                            notifyItemMoved(adapterPosition, newIndex)

                        }else{
                            mSingleGroceryListActivity.mGroceryListViewModel.groupByCategory()
                            val newIndex = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.indexOf(groceryItemEntity)
                            notifyItemMoved(adapterPosition, newIndex)
                        }



                        val layoutManager: LinearLayoutManager = singleGroceryListActivity.dataBindingUtil.groceryItemRecyclerview.layoutManager as LinearLayoutManager
                        val visible: Int = layoutManager.findLastVisibleItemPosition()
                        layoutManager.scrollToPosition(visible)
                        groceryListItemBinding.groceryItemParentLayout.setBackgroundColor(Color.WHITE)
                    }
                }
            }

        }

    }

    inner class CustomPopupMenu(var context: Context, var adapterPosition: Int) : PopupMenu.OnMenuItemClickListener {
        val singleGroceryListActivity: SingleGroceryListActivity = context as SingleGroceryListActivity

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val groceryItemEntity = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.get(adapterPosition)

            when (item!!.itemId) {
                R.id.check -> {

                    Toast.makeText(context, groceryItemEntity.itemName, Toast.LENGTH_SHORT).show()

                }
                R.id.edit -> {


                    val selectedGroceryListAutoGeneratedId = groceryItemEntity.groceryListUniqueId
                    val intent = Intent(context, AddGroceryListItemActivity::class.java)

                    intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, selectedGroceryListAutoGeneratedId)
                    intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, groceryItemEntity.id)
                    intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, adapterPosition)
                    intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_ACTION_EXTRA_DATA_TAG, AddGroceryListItemActivity.UPDATE_RECORD_ACTION)

                    singleGroceryListActivity.startActivityForResult(
                            intent,
                            SingleGroceryListActivity.UPDATE_ITEM_REQUEST
                    )


                }
                R.id.delete -> {


                    CoroutineScope(IO).launch {
                        singleGroceryListActivity.mGroceryListViewModel.deleteGroceryListItem(context, groceryItemEntity.id, groceryItemEntity.groceryListUniqueId)
                        singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems.remove(groceryItemEntity)
                        singleGroceryListActivity.mGroceryListViewModel.mergeToBuyAndBoughtItems(singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems, singleGroceryListActivity.mGroceryListViewModel.boughtGroceryItems)

                        withContext(Dispatchers.Main) {

                            if(singleGroceryListActivity.mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.GROUP_BY_CATEGORY){
                                singleGroceryListActivity.mGroceryListViewModel.groupByCategory()
                            }

                            notifyItemRemoved(adapterPosition)
                             notifyItemRangeChanged(adapterPosition, singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.size)


                        }
                    }

                }

            }

            return true
        }
    }

}


