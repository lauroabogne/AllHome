package com.example.allhome.grocerylist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.data.entities.GroceryListEntityValues
import com.example.allhome.data.entities.GroceryListWithItemCount
import com.example.allhome.databinding.FragmentGroceryListBinding
import com.example.allhome.databinding.GroceryListItemBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListFragmentViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class GroceryListFragment : Fragment(),OnItemAdded {
    private lateinit var mDataBindingUtil:FragmentGroceryListBinding
    private lateinit var mGroceryListFragmentViewModel: GroceryListFragmentViewModel;

    companion object {
        val ACTION_TAG = "ACTION_TAG"
        val VIEW_INFORMATION = 1;
        val UPDATED_ACTION = 2;

        val OPEN_SINGLE_GROCERY_LIST_REQUEST = 3

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mGroceryListFragmentViewModel = ViewModelProvider(this).get(GroceryListFragmentViewModel::class.java)
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_grocery_list, container, false)
        mDataBindingUtil.apply {

        }

        val groceryListRecyclerViewAdapter = GroceryListRecyclerViewAdapter(this, mGroceryListFragmentViewModel)

        mDataBindingUtil.groceryListRecyclerview.adapter = groceryListRecyclerViewAdapter

        mGroceryListFragmentViewModel.coroutineScope.launch {
                mGroceryListFragmentViewModel.getGroceryLists(requireContext())
                withContext(Main) {
                    groceryListRecyclerViewAdapter.mGroceryListWithItemCount = mGroceryListFragmentViewModel.groceryLists
                    groceryListRecyclerViewAdapter.notifyDataSetChanged()
                }

        }

        mDataBindingUtil.groceryListRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && mDataBindingUtil.fab.isShown()) {
                    mDataBindingUtil.fab.hide();
                }
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mDataBindingUtil.fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

        })


        mDataBindingUtil.fab.setOnClickListener{
            showGroceryListNameInput()
        }
        return mDataBindingUtil.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == VIEW_INFORMATION && resultCode == Activity.RESULT_OK){

            val action = data?.getIntExtra(GroceryListFragment.ACTION_TAG, 0)

            if(action == UPDATED_ACTION){

                val groceryListUniqueId = data.getStringExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)
                updateRecyclerViewList(groceryListUniqueId!!)
                /*mGroceryListFragmentViewModel.coroutineScope.launch {

                    val index = mGroceryListFragmentViewModel.getItemIndex(groceryListUniqueId!!)
                    val groceryListWithItemCount = mGroceryListFragmentViewModel.getGroceryListWithItemCount(this@GroceryListFragment.requireContext(), groceryListUniqueId)

                    mGroceryListFragmentViewModel.groceryLists.set(index, groceryListWithItemCount)

                    withContext(Main){

                        val scrollListener = object : RecyclerView.OnScrollListener() {
                            var found = false
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                                val viewHolder = mDataBindingUtil.groceryListRecyclerview.findViewHolderForAdapterPosition(index)

                                if (viewHolder != null && !found) {
                                    animateItem(viewHolder)
                                    mDataBindingUtil.groceryListRecyclerview.removeOnScrollListener(this)
                                    found = true
                                }

                            }
                        }


                        val firstVisibleItemPosition = (mDataBindingUtil.groceryListRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = (mDataBindingUtil.groceryListRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                        if(index >= firstVisibleItemPosition && index <= lastVisibleItemPosition){
                            val viewHolder = mDataBindingUtil.groceryListRecyclerview.findViewHolderForAdapterPosition(index)
                            animateItem(viewHolder!!)
                        }else{
                            mDataBindingUtil.groceryListRecyclerview.addOnScrollListener(scrollListener)
                        }

                        mDataBindingUtil.groceryListRecyclerview.adapter?.notifyItemChanged(index)
                       *//* mDataBindingUtil.groceryListRecyclerview.scrollToPosition(index)*//*

                    }

                }*/

            }
        }else if(requestCode == OPEN_SINGLE_GROCERY_LIST_REQUEST){

            val groceryListUniqueId = data?.getStringExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)
            updateRecyclerViewList(groceryListUniqueId!!)

        }

    }
    fun updateRecyclerViewList(groceryListUniqueId:String){

        mGroceryListFragmentViewModel.coroutineScope.launch {

            val index = mGroceryListFragmentViewModel.getItemIndex(groceryListUniqueId!!)
            val groceryListWithItemCount = mGroceryListFragmentViewModel.getGroceryListWithItemCount(this@GroceryListFragment.requireContext(), groceryListUniqueId)

            mGroceryListFragmentViewModel.groceryLists.set(index, groceryListWithItemCount)

            withContext(Main){

                val scrollListener = object : RecyclerView.OnScrollListener() {
                    var found = false
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                        val viewHolder = mDataBindingUtil.groceryListRecyclerview.findViewHolderForAdapterPosition(index)

                        if (viewHolder != null && !found) {
                            animateItem(viewHolder)
                            mDataBindingUtil.groceryListRecyclerview.removeOnScrollListener(this)
                            found = true
                        }

                    }
                }


                val firstVisibleItemPosition = (mDataBindingUtil.groceryListRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val lastVisibleItemPosition = (mDataBindingUtil.groceryListRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if(index >= firstVisibleItemPosition && index <= lastVisibleItemPosition){
                    val viewHolder = mDataBindingUtil.groceryListRecyclerview.findViewHolderForAdapterPosition(index)
                    animateItem(viewHolder!!)
                }else{
                    mDataBindingUtil.groceryListRecyclerview.addOnScrollListener(scrollListener)
                }

                mDataBindingUtil.groceryListRecyclerview.adapter?.notifyItemChanged(index)
                /* mDataBindingUtil.groceryListRecyclerview.scrollToPosition(index)*/

            }

        }
    }
    fun animateItem(viewHolder: RecyclerView.ViewHolder){
        val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fadeInAnimation.duration = 1000
        fadeInAnimation.fillAfter = true
        val itemViewHolder: GroceryListRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryListRecyclerViewAdapter.ItemViewHolder
        itemViewHolder.groceryListItemBinding.root.startAnimation(fadeInAnimation)
    }
    private fun showGroceryListNameInput(){
        val groceryListNameInputDialog = CustomDialog(context = requireContext())
        groceryListNameInputDialog.setButtonClickListener(View.OnClickListener {
            var groceryListName = groceryListNameInputDialog.groceryListName()

            if (groceryListName.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide name", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            var uniqueID = UUID.randomUUID().toString()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val datetimeCreated: String = simpleDateFormat.format(Date())
            val groceryListEntity = GroceryListEntity(autoGeneratedUniqueId = uniqueID, name = groceryListName,
                datetimeCreated = datetimeCreated, shoppingDatetime = "0000-00-00 00:00:00", location = "",
                longitude = 0.0, latitude = 0.0,viewingType = 0,notify = 0,notifyType = getString(R.string.grocery_notification_none),
                itemStatus = GroceryListEntityValues.ACTIVE_STATUS,datetimeStatusUpdated = datetimeCreated,uploaded =GroceryListEntityValues.NOT_YET_UPLOADED
            );


            CoroutineScope(Dispatchers.IO).launch {

                mGroceryListFragmentViewModel.addItem(requireContext(),groceryListEntity)

                val groceryListWithItemCount = mGroceryListFragmentViewModel.getGroceryListWithItemCount(requireContext(),uniqueID)

                //AllHomeDatabase.getDatabase(requireContext()).groceryListDAO().addItem(groceryListEntity)
                //getGroceryListWithItemCount(context:Context,autogeneratedUniqueId:String)

                withContext(Dispatchers.Main) {

                    mGroceryListFragmentViewModel.groceryLists.add(0,groceryListWithItemCount)
                    mDataBindingUtil.groceryListRecyclerview.adapter?.notifyDataSetChanged()
                    mDataBindingUtil.groceryListRecyclerview.scrollToPosition(0)


                    groceryListNameInputDialog.mAlertDialog.dismiss()
                    val intent = Intent(requireContext(), SingleGroceryListActivity::class.java)
                    intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, uniqueID)
                    startActivityForResult(intent, OPEN_SINGLE_GROCERY_LIST_REQUEST)

                }
            }

            if (true) {

                return@OnClickListener
            }


        })
        groceryListNameInputDialog.createPositiveButton("Continue")
        groceryListNameInputDialog.show()
    }

    /**
     * Custom alertdialog
     */
    class CustomDialog(context: Context) : AlertDialog.Builder(context) {

        companion object{
            val POSITIVE_BUTTON_ID = AlertDialog.BUTTON_POSITIVE

        }

        var mGroceryListNameInput: LinearLayout;
        lateinit var mOnClickListener: View.OnClickListener;
        lateinit var mAlertDialog: AlertDialog;
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
        fun groceryListName():String{
            var groceryListNameTextInput: TextInputEditText = mGroceryListNameInput.findViewById(R.id.grocery_list_name_textinputedittext)
            return groceryListNameTextInput.text.toString();

        }
        fun setGroceryListName(name: String){
            var groceryListNameTextInput: TextInputEditText = mGroceryListNameInput.findViewById(R.id.grocery_list_name_textinputedittext)
            groceryListNameTextInput.setText(name)
        }

        override fun show(): AlertDialog {
            mAlertDialog = super.show()
            val positiveBtn: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

            if(mOnClickListener != null){
                if(positiveBtn !=null){
                    positiveBtn.id = POSITIVE_BUTTON_ID
                    positiveBtn.setOnClickListener(mOnClickListener)
                }
            }

            return mAlertDialog
        }
    }

    override fun onItemDuplicatedSuccessully(position: Int) {

        mDataBindingUtil.groceryListRecyclerview.adapter?.notifyDataSetChanged()
        mDataBindingUtil.groceryListRecyclerview.smoothScrollToPosition(position)



    }

}
interface OnItemAdded{
    fun onItemDuplicatedSuccessully(position: Int)
}
/**
 *
 */
class GroceryListRecyclerViewAdapter(val groceryListFragment: GroceryListFragment, val mGroceryListFragmentViewModel: GroceryListFragmentViewModel) : RecyclerView.Adapter<GroceryListRecyclerViewAdapter.ItemViewHolder>() {
    var mGroceryListWithItemCount: List<GroceryListWithItemCount> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)



        val groceryListItemBinding = GroceryListItemBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(groceryListItemBinding)

        return itemViewHolder;

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val groceryItemEntity = mGroceryListWithItemCount[position]
        holder.groceryListItemBinding.groceryListWithCount= groceryItemEntity
        holder.groceryListItemBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mGroceryListWithItemCount.size
    }

    /**
     *
     */
    inner  class ItemViewHolder(var groceryListItemBinding: GroceryListItemBinding) : RecyclerView.ViewHolder(groceryListItemBinding.root), View.OnClickListener {

        init{
            groceryListItemBinding.root.setOnClickListener(this)
            groceryListItemBinding.moreActionImageView.setOnClickListener(this)
            groceryListItemBinding.itemCountAndBoughtTextView.setOnClickListener(this)
        }
        override fun onClick(view: View?) {


            if(view?.id == R.id.grocery_item_list_parent_layout){

                val groceryListWithCount  = groceryListItemBinding.groceryListWithCount
                val intent = Intent(groceryListFragment.requireContext(), SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListWithCount?.groceryListEntity?.autoGeneratedUniqueId)
                groceryListFragment.startActivityForResult(intent, GroceryListFragment.OPEN_SINGLE_GROCERY_LIST_REQUEST)


            }else if(view?.id == R.id.itemCountAndBoughtTextView || view?.id == R.id.moreActionImageView){
                val groceryListWithCount  = groceryListItemBinding.groceryListWithCount
                val popupMenu = PopupMenu(groceryListFragment.requireContext(), groceryListItemBinding.moreActionImageView)
                popupMenu.menuInflater.inflate(R.menu.grocery_list_action, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {

                    val autoGeneratedUniqueId = groceryListWithCount?.groceryListEntity?.autoGeneratedUniqueId
                    val groceryListName = groceryListWithCount?.groceryListEntity?.name
                    when (it.itemId) {
                        R.id.view_information_menu -> {


                            val intent = Intent(groceryListFragment.requireContext(), GroceryListInformationActivity::class.java)
                            intent.putExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, autoGeneratedUniqueId)
                            groceryListFragment.startActivityForResult(intent, GroceryListFragment.VIEW_INFORMATION)

                        }
                        R.id.go_shopping_menu -> {
                            val intent = Intent(groceryListFragment.requireContext(), SingleGroceryListActivity::class.java)
                            intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, autoGeneratedUniqueId)
                            groceryListFragment.startActivityForResult(intent, GroceryListFragment.OPEN_SINGLE_GROCERY_LIST_REQUEST)

                        }
                        R.id.duplicate_menu -> {


                            val groceryListNameInputDialog = GroceryListFragment.CustomDialog(groceryListFragment.requireContext())
                            groceryListNameInputDialog.setGroceryListName(groceryListName + "- copy")
                            groceryListNameInputDialog.setButtonClickListener(View.OnClickListener {
                                mGroceryListFragmentViewModel.coroutineScope.launch {
                                    val groceryListWithItemCount = mGroceryListFragmentViewModel.copy(groceryListFragment.requireContext(), autoGeneratedUniqueId!!, groceryListNameInputDialog.groceryListName())

                                    withContext(Main) {

                                        mGroceryListFragmentViewModel.groceryLists.add(0, groceryListWithItemCount)

                                        groceryListNameInputDialog.mAlertDialog.dismiss()
                                        val builder = AlertDialog.Builder(groceryListFragment.requireContext())
                                        val customView = LayoutInflater.from(groceryListFragment.requireContext()).inflate(R.layout.success_message_confirmation, null)
                                        builder.setView(customView)
                                        val customDialog = builder.create()
                                        customDialog.show()

                                        customView.findViewById<View>(R.id.message_dialog_btn).setOnClickListener({

                                            (groceryListFragment as OnItemAdded).onItemDuplicatedSuccessully(0)

                                            customDialog.dismiss()
                                        })
                                    }
                                }
                            })
                            groceryListNameInputDialog.createPositiveButton("Continue")
                            groceryListNameInputDialog.show()


                        }
                        R.id.delete_menu -> {
                            mGroceryListFragmentViewModel.coroutineScope.launch {

                                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val datetimeCreated: String = simpleDateFormat.format(Date())

                                mGroceryListFragmentViewModel.deleteGroceryList(groceryListFragment.requireContext(), autoGeneratedUniqueId!!,datetimeCreated,GroceryListEntityValues.DELETED_STATUS)
                                mGroceryListFragmentViewModel.groceryLists.removeAt(adapterPosition)
                                withContext(Main) {
                                    notifyItemRemoved(adapterPosition)
                                    notifyItemRangeChanged(adapterPosition, mGroceryListFragmentViewModel.groceryLists.size)
                                }

                            }

                        }
                        R.id.share_menu->{

                            mGroceryListFragmentViewModel.coroutineScope.launch {
                                val itemsToShare =  mGroceryListFragmentViewModel.getGroceryListItemsForShareInOtherApp(groceryListFragment.requireContext(), autoGeneratedUniqueId!!)
                                withContext(Main){
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, itemsToShare)
                                        type = "text/plain"
                                    }

                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    groceryListFragment.requireContext().startActivity(shareIntent)
                                }

                            }

                        }
                    }
                    true
                })
                popupMenu.show()

            }

        }

    }

}
