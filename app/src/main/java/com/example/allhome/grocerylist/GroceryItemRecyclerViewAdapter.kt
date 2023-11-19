package com.example.allhome.grocerylist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.*
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
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
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.storage.StorageFragment
import com.example.allhome.storage.StorageStorageListActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GroceryItemRecyclerViewAdapter(private val contextParams: Context, private val productImageClickListener:View.OnClickListener) : RecyclerView.Adapter<GroceryItemRecyclerViewAdapter.ItemViewHolder>() {

    var mGroceryItems: List<GroceryItemEntity> = arrayListOf()
    var mTouchHelper: ItemTouchHelper? = null
    var mDraggable: Boolean = true
    var context: Context = contextParams
    lateinit var mSelectedView: View
    val mSingleGroceryListActivity: SingleGroceryListActivity = contextParams as SingleGroceryListActivity
    val mGroceryListViewModel = mSingleGroceryListActivity.mGroceryListViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryItemRecyclerViewAdapter.ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val groceryListItemBinding = GroceryListProductBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(groceryListItemBinding,productImageClickListener)

        return itemViewHolder
    }

    override fun getItemCount(): Int {
        if (mGroceryItems != null) {
            return mGroceryItems.size
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
        val groceryItemEntity = mGroceryItems.get(position)
        holder.groceryListItemBinding.groceryItemEntity = groceryItemEntity
        holder.groceryListItemBinding.executePendingBindings()

        holder.groceryListItemBinding.itemImage.tag = groceryItemEntity

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



    inner class ItemViewHolder(groceryListItemBindingParams: GroceryListProductBinding,productImageClickListenerParams:View.OnClickListener) : RecyclerView.ViewHolder(groceryListItemBindingParams.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        var groceryListItemBinding: GroceryListProductBinding = groceryListItemBindingParams
        var productImageClickListener:View.OnClickListener = productImageClickListenerParams

        init {
            groceryListItemBinding.groceryItemNameTextview.setOnClickListener(this)
            groceryListItemBinding.otherInformationTextview.setOnClickListener(this)
            groceryListItemBinding.checkBox.setOnCheckedChangeListener(this)
            groceryListItemBinding.itemImage.setOnClickListener(productImageClickListener)
        }

        override fun onClick(view: View?) {
            val singleGroceryListActivity: SingleGroceryListActivity = context as SingleGroceryListActivity
            val groceryItemEntity = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList[adapterPosition]

            val id = view?.id
            if(id == R.id.grocery_item_name_textview || id == R.id.other_information_textview){
                if (groceryItemEntity.bought == 1) {
                    // do nothing
                    val popupMenu = PopupMenu(context, groceryListItemBinding.groceryItemNameTextview)
                    popupMenu.menuInflater.inflate(R.menu.add_to_storage_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { item ->
                        popupMenu.dismiss()

                        when (item!!.itemId) {
                            R.id.addToStorageMenu -> {
                                val storageStorageListActiviy = Intent(context, StorageStorageListActivity::class.java)
                                storageStorageListActiviy.putExtra(StorageFragment.ACTION_TAG, StorageFragment.STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION)
                                storageStorageListActiviy.putExtra(StorageFragment.GROCERY_ITEM_ENTITY_TAG, groceryItemEntity)
                                context.startActivity(storageStorageListActiviy)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                    return
                }else{
                    val popupMenu = PopupMenu(context, groceryListItemBinding.groceryItemNameTextview)
                    popupMenu.menuInflater.inflate(R.menu.grocery_item_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener(CustomPopupMenu(context, adapterPosition,groceryListItemBinding.checkBox))
                    popupMenu.show()
                }


            }

        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val singleGroceryListActivity: SingleGroceryListActivity = context as SingleGroceryListActivity
            val groceryItemEntity: GroceryItemEntity = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList[adapterPosition]



            if (isChecked && buttonView!!.isPressed) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    (groceryListItemBinding.root as CardView).cardElevation = 0F
                    (groceryListItemBinding.root as CardView).elevation = 0F
                }

                mGroceryListViewModel.coroutineScope.launch {
                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDatetime: String = simpleDateFormat.format(Date())

                    singleGroceryListActivity.mGroceryListViewModel.updateGroceryItem(context, 1, groceryItemEntity.uniqueId, groceryItemEntity.itemName,currentDatetime)


                    mGroceryListViewModel.updateGroceryListAsNotUploaded(context,groceryItemEntity.groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)

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

                        groceryListItemBinding.groceryItemParentLayout.setBackgroundColor( ResourcesCompat.getColor(context.resources,R.color.gray_background,null))
                    }
                }

            } else if (!isChecked && buttonView!!.isPressed) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    (groceryListItemBinding.root as CardView).cardElevation = 10F
                    (groceryListItemBinding.root as CardView).elevation = 10F
                }

                mGroceryListViewModel.coroutineScope.launch {

                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDatetime: String = simpleDateFormat.format(Date())

                    singleGroceryListActivity.mGroceryListViewModel.updateGroceryItem(context, 0, groceryItemEntity.uniqueId, groceryItemEntity.itemName,currentDatetime)


                    mGroceryListViewModel.updateGroceryListAsNotUploaded(context,groceryItemEntity.groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)

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

    inner class CustomPopupMenu(var context: Context, var adapterPosition: Int,var checkbox:CheckBox) : PopupMenu.OnMenuItemClickListener {
        val singleGroceryListActivity: SingleGroceryListActivity = context as SingleGroceryListActivity

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val groceryItemEntity = singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.get(adapterPosition)

            when (item!!.itemId) {
                R.id.edit -> {


                    val selectedGroceryListAutoGeneratedId = groceryItemEntity.groceryListUniqueId
                    val intent = Intent(context, AddGroceryListItemActivity::class.java)

                    intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, selectedGroceryListAutoGeneratedId)
                    intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, groceryItemEntity.uniqueId)
                    intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, adapterPosition)
                    intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_ACTION_EXTRA_DATA_TAG, AddGroceryListItemFragment.UPDATE_RECORD_ACTION)

//                    singleGroceryListActivity.startActivityForResult(
//                            intent,
//                            SingleGroceryListActivity.UPDATE_ITEM_REQUEST
//                    )

                    singleGroceryListActivity.openEditGroceryListItemContract.launch(intent)


                }
                R.id.delete -> {


                    mGroceryListViewModel.coroutineScope.launch {
                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val currentDatetime: String = simpleDateFormat.format(Date())
                        mGroceryListViewModel.updateGroceryListAsNotUploaded(context,groceryItemEntity.groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)

                        singleGroceryListActivity.mGroceryListViewModel.deleteGroceryListItem(context, groceryItemEntity.uniqueId, groceryItemEntity.groceryListUniqueId, GroceryItemEntityValues.DELETED_STATUS)
                        singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems.remove(groceryItemEntity)
                        singleGroceryListActivity.mGroceryListViewModel.mergeToBuyAndBoughtItems(singleGroceryListActivity.mGroceryListViewModel.toBuyGroceryItems, singleGroceryListActivity.mGroceryListViewModel.boughtGroceryItems)



                        withContext(Dispatchers.Main) {

                            if(singleGroceryListActivity.mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.GROUP_BY_CATEGORY){
                                singleGroceryListActivity.mGroceryListViewModel.groupByCategory()
                            }

                            //singleGroceryListActivity.dataBindingUtil.groceryItemRecyclerview.recycledViewPool.clear()
                            notifyItemRemoved(adapterPosition)
                            notifyDataSetChanged()
                            //notifyItemRangeChanged(adapterPosition, singleGroceryListActivity.mGroceryListViewModel.selectedGroceryListItemList.size)

                            mSingleGroceryListActivity.hideOrShowGroceryNoItemTextView()
                        }
                    }

                }

            }

            return true
        }
    }

}


