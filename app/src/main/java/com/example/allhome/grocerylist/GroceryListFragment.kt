package com.example.allhome.grocerylist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.MainActivity
import com.example.allhome.R
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.FragmentGroceryListBinding
import com.example.allhome.databinding.GroceryListItemBinding
import com.example.allhome.grocerylist.archived_grocery_list.TrashGroceryListFragment
import com.example.allhome.grocerylist.viewmodel.GroceryListFragmentViewModel
import com.example.allhome.storage.StorageFragment
import com.example.allhome.storage.StorageStorageListActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class GroceryListFragment : Fragment(),OnItemAdded {
    private lateinit var mDataBindingUtil:FragmentGroceryListBinding
    private lateinit var mGroceryListFragmentViewModel: GroceryListFragmentViewModel

    companion object {
        const val TAG = "GroceryListFragment"
        val ACTION_TAG = "ACTION_TAG"
        val VIEW_INFORMATION = 1
        val UPDATED_ACTION = 2

        val OPEN_SINGLE_GROCERY_LIST_REQUEST = 3

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().title = "Grocery List"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mGroceryListFragmentViewModel = ViewModelProvider(this).get(GroceryListFragmentViewModel::class.java)
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_grocery_list, container, false)
        mDataBindingUtil.apply {

        }

        val groceryListRecyclerViewAdapter = GroceryListRecyclerViewAdapter(this, mGroceryListFragmentViewModel)
        mDataBindingUtil.groceryListRecyclerview.adapter = groceryListRecyclerViewAdapter

        loadItem()

        mDataBindingUtil.groceryListRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && mDataBindingUtil.fab.isShown) {
                    mDataBindingUtil.fab.hide()
                }
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mDataBindingUtil.fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

        })


        mDataBindingUtil.fab.setOnClickListener{
            showGroceryListNameInput()
        }
        mDataBindingUtil.swipeRefresh.setOnRefreshListener {
            loadItem()
            mDataBindingUtil.swipeRefresh.isRefreshing = false
        }
        return mDataBindingUtil.root
    }
    private fun loadItem(){
        mGroceryListFragmentViewModel.coroutineScope.launch {
            mGroceryListFragmentViewModel.getGroceryLists(requireContext())
            withContext(Main) {

                val adapter = mDataBindingUtil.groceryListRecyclerview.adapter as GroceryListRecyclerViewAdapter
                adapter.mGroceryListWithItemCount = mGroceryListFragmentViewModel.groceryLists
                adapter.notifyDataSetChanged()
                hideOrShowGroceryNoListTextView()

            }

        }
    }

     fun hideOrShowGroceryNoListTextView(){
        if(mGroceryListFragmentViewModel.groceryLists.isEmpty()){
            mDataBindingUtil.noListTextView.visibility = View.VISIBLE
        }else{
            mDataBindingUtil.noListTextView.visibility = View.GONE
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.grocery_list_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.grocery_list_menu->{

                if(activity is MainActivity){
                    (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                        replace(R.id.home_fragment_container,GroceryListFragment())
                        commit()
                    }
                }

            }
            R.id.grocery_list_archived_menu->{

                if(activity is MainActivity){
                    (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                        replace(R.id.home_fragment_container,TrashGroceryListFragment())
                        commit()
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == VIEW_INFORMATION && resultCode == Activity.RESULT_OK){

            val action = data?.getIntExtra(GroceryListFragment.ACTION_TAG, 0)

            if(action == UPDATED_ACTION){

                val groceryListUniqueId = data.getStringExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)
                updateRecyclerViewList(groceryListUniqueId!!)

            }
        }else if(requestCode == OPEN_SINGLE_GROCERY_LIST_REQUEST && resultCode == Activity.RESULT_OK){

            val groceryListUniqueId = data?.getStringExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)
            updateRecyclerViewList(groceryListUniqueId!!)

        }

    }

    fun updateRecyclerViewList(groceryListUniqueId:String){
        mGroceryListFragmentViewModel.coroutineScope.launch {

            val index = mGroceryListFragmentViewModel.getItemIndex(groceryListUniqueId)
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
            )


            CoroutineScope(Dispatchers.IO).launch {

                mGroceryListFragmentViewModel.addItem(requireContext(),groceryListEntity)

                val groceryListWithItemCount = mGroceryListFragmentViewModel.getGroceryListWithItemCount(requireContext(),uniqueID)

                withContext(Dispatchers.Main) {

                    mGroceryListFragmentViewModel.groceryLists.add(0,groceryListWithItemCount)
                    mDataBindingUtil.groceryListRecyclerview.adapter?.notifyDataSetChanged()
                    mDataBindingUtil.groceryListRecyclerview.scrollToPosition(0)


                    groceryListNameInputDialog.mAlertDialog.dismiss()
                    val intent = Intent(requireContext(), SingleGroceryListActivity::class.java)
                    intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, uniqueID)
                    startActivityForResult(intent, OPEN_SINGLE_GROCERY_LIST_REQUEST)

                    hideOrShowGroceryNoListTextView()

                }
            }

           return@OnClickListener



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
        fun groceryListName():String{
            var groceryListNameTextInput: TextInputEditText = mGroceryListNameInput.findViewById(R.id.grocery_list_name_textinputedittext)
            return groceryListNameTextInput.text.toString()

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

        return itemViewHolder

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
                intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListWithCount?.groceryListEntity?.autoGeneratedUniqueId)
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
                            intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, autoGeneratedUniqueId)
                            groceryListFragment.startActivityForResult(intent, GroceryListFragment.OPEN_SINGLE_GROCERY_LIST_REQUEST)

                        }
                        R.id.add_to_expenses_report_menu-> {
                            Toast.makeText(groceryListFragment.requireContext(), "Copy to expenses $autoGeneratedUniqueId", Toast.LENGTH_SHORT).show()

                            autoGeneratedUniqueId?.let {
                                mGroceryListFragmentViewModel.coroutineScope.launch {
                                    val groceryListEntity = mGroceryListFragmentViewModel.getGroceryList(groceryListFragment.requireContext(), autoGeneratedUniqueId)
                                    val groceryItemEntities = mGroceryListFragmentViewModel.getBoughtGroceryListItemsAndTotalAmountGreaterZero(groceryListFragment.requireContext(),autoGeneratedUniqueId)
                                    val hasGroceryToTransfer = groceryListEntity != null && groceryItemEntities.isNotEmpty()

                                    Log.e(GroceryListFragment.TAG,"DATA : $groceryListEntity ${groceryItemEntities.size}")
                                    var isSaveSuccessfully = false
                                    var errorMessage = ""

                                    if(groceryListEntity == null){
                                        withContext(Main) {
                                            Toast.makeText(groceryListFragment.requireContext(),"Failed to save in expenses. Grocery list not found. Please try again.",Toast.LENGTH_SHORT).show()
                                        }
                                        return@launch
                                    }

                                    if(groceryItemEntities.isEmpty()){
                                        withContext(Main) {
                                            Toast.makeText(groceryListFragment.requireContext(),"Failed to save in expenses. Please add price, quantity and check grocery item before adding in expenses report.",Toast.LENGTH_SHORT).show()
                                        }
                                        return@launch
                                    }

                                    if(hasGroceryToTransfer){
                                        val groceryExpenseUniqueId = UUID.randomUUID().toString()
                                        val expensesGroceryListEntity = ExpensesGroceryListEntity(
                                            autoGeneratedUniqueId=groceryExpenseUniqueId,
                                            groceryListAutoGeneratedUniqueId=groceryListEntity.autoGeneratedUniqueId,
                                            name=groceryListEntity.name,
                                            datetimeCreated=groceryListEntity.datetimeCreated,
                                            shoppingDatetime=groceryListEntity.shoppingDatetime,
                                            location=groceryListEntity.location,
                                            longitude=groceryListEntity.longitude,
                                            latitude=groceryListEntity.latitude,
                                            itemStatus=groceryListEntity.itemStatus,
                                            datetimeStatusUpdated=groceryListEntity.datetimeStatusUpdated,
                                            uploaded=0
                                        )

                                        val expensesGroceryItemEntities = groceryItemEntities.map {groceryItemEntity->
                                                ExpensesGroceryItemEntity(
                                                    uniqueId  = UUID.randomUUID().toString(),
                                                    expensesGroceryListUniqueId=groceryExpenseUniqueId,
                                                    groceryListUniqueId=groceryItemEntity.groceryListUniqueId,
                                                    sequence = groceryItemEntity.sequence,
                                                    itemName = groceryItemEntity.itemName,
                                                    quantity = groceryItemEntity.quantity,
                                                    unit = groceryItemEntity.unit,
                                                    pricePerUnit =groceryItemEntity.pricePerUnit,
                                                    category = groceryItemEntity.category,
                                                    notes = groceryItemEntity.notes,
                                                    imageName = groceryItemEntity.imageName,
                                                    bought = groceryItemEntity.bought,
                                                    itemStatus = groceryItemEntity.itemStatus,
                                                    datetimeCreated = groceryItemEntity.datetimeCreated,
                                                    datetimeModified = groceryItemEntity.datetimeModified
                                                )
                                            }
                                        try{
                                            var expenseGroceryListId = mGroceryListFragmentViewModel.insertExpenseGroceryList(groceryListFragment.requireContext(), expensesGroceryListEntity)
                                            if(expenseGroceryListId <= 0){
                                                isSaveSuccessfully =false
                                                errorMessage = "Failed to save grocery to expenses. Please try again."
                                                throw Exception("Failed to save grocery to expenses. Please try again.")
                                            }
                                            var expensesGroceryItemsIds = mGroceryListFragmentViewModel.insertExpensesGroceryListItems(groceryListFragment.requireContext(),expensesGroceryItemEntities)
                                            if(expensesGroceryItemsIds.isEmpty()){
                                                isSaveSuccessfully =false
                                                errorMessage = "Failed to save in expenses."
                                                throw Exception("Failed to save grocery to expenses. Please try again.")
                                            }
                                            isSaveSuccessfully = true

                                        }catch (exception: Exception){
                                            errorMessage = exception.printStackTrace().toString()
                                            isSaveSuccessfully = false
                                        }
                                    }


                                    withContext(Main) {
                                        if(isSaveSuccessfully){
                                            Toast.makeText(groceryListFragment.requireContext(),"Grocery added to expenses successfully.",Toast.LENGTH_SHORT).show()
                                        }else{
                                            Toast.makeText(groceryListFragment.requireContext(),"$errorMessage test error",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } ?: run {
                                Toast.makeText(groceryListFragment.requireContext(), "Auto generated unique id is null.", Toast.LENGTH_SHORT).show()
                            }



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

                                        customView.findViewById<View>(R.id.message_dialog_btn).setOnClickListener {

                                            (groceryListFragment as OnItemAdded).onItemDuplicatedSuccessully(0)

                                            customDialog.dismiss()
                                        }
                                    }
                                }
                            })
                            groceryListNameInputDialog.createPositiveButton("Continue")
                            groceryListNameInputDialog.show()
                        }
                        R.id.move_to_archive -> {
                            mGroceryListFragmentViewModel.coroutineScope.launch {

                                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                val datetimeCreated: String = simpleDateFormat.format(Date())

                                mGroceryListFragmentViewModel.deleteGroceryList(groceryListFragment.requireContext(), autoGeneratedUniqueId!!,datetimeCreated,GroceryListEntityValues.ARCHIVE)
                                mGroceryListFragmentViewModel.groceryLists.removeAt(adapterPosition)
                                withContext(Main) {
                                    notifyItemRemoved(adapterPosition)
                                    notifyItemRangeChanged(adapterPosition, mGroceryListFragmentViewModel.groceryLists.size)
                                    groceryListFragment.hideOrShowGroceryNoListTextView()

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
                        R.id.add_to_storage_menu->{

                            val storageStorageListActiviy = Intent(groceryListFragment.requireContext(), StorageStorageListActivity::class.java)
                            storageStorageListActiviy.putExtra(StorageFragment.ACTION_TAG, StorageFragment.STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION)
                            storageStorageListActiviy.putExtra(StorageFragment.GROCERY_ENTITY_TAG, groceryListWithCount?.groceryListEntity)
                            groceryListFragment.startActivity(storageStorageListActiviy)
                        }
                    }
                    true
                })
                popupMenu.show()

            }

        }

    }

}
