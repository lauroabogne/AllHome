package com.example.allhome.storage

import android.animation.Animator
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.*
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.example.allhome.utils.ImageUtil
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*


class StorageFragment : Fragment(),SearchView.OnQueryTextListener {
    internal lateinit var mStorageViewModel: StorageViewModel
    lateinit var mDataBindingUtil: FragmentStorageBinding
    lateinit var mSelectedImageThumbnailView:View
    private val TAG = "StorageFragment"
    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null
    private var mAction = STORAGE_VIEWING_ACTION

    private lateinit var mStorageItemWithExpirationsToTransfer:StorageItemWithExpirations
    var mStorageEntityOrigin:StorageEntity? = null
    var mGroceryItemEntity:GroceryItemEntity? = null
    var mGroceryListEntity:GroceryListEntity? = null




    var mViewing = VIEW_BY_STORAGE
    var mSearchJob = Job()
    var mSearchView: SearchView? = null
    var mSelectedMenuItem:MenuItem? = null

    var mFilterByDateModifiedDate: String =SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
    var mFilter = NO_FILTER
    var mFilterByQuantityOptionSelected = -1
    var mQuantityFilterFirstValue = 0
    var mQuantityFilterSecondValue = 0

    var isViewingImage:Boolean = false


    companion object{
        const val ADD_STORAGE_REQUEST_CODE = 1986
        const val UPDATE_STORAGE_REQUEST_CODE = 1987
        const val ACTION_TAG = "ACTION_TAG"
        const val STORAGE_ENTITY_TAG = "STORAGE_ENTITY_TAG"
        const val STORAGE_ITEM_ENTITY_TAG = "STORAGE_ITEM_ENTITY_TAG"
        const val GROCERY_ITEM_ENTITY_TAG = "GROCERY_ITEM_ENTITY_TAG"
        const val GROCERY_ENTITY_TAG = "GROCERY_ENTITY_TAG"
        const val STORAGE_VIEWING_ACTION = 1
        const val STORAGE_TRASFERING_ITEM_ACTION = 2
        const val STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION = 3
        const val STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION = 4

        const val VIEW_BY_STORAGE = 1
        const val  VIEW_PER_PRODUCT = 2

        const val NO_FILTER = 0
        const val FILTER_BY_STOCK = 1
        const val FILTER_BY_LAST_UPDATE = 2
        const val FILTER_BY_EXPIRED = 3


        const val GREATER_THAN_QUANTITY = 0
        const val LESS_THAN_QUANTITY = 1
        const val BETWEEN_QUANTITY = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        requireArguments().getInt(ACTION_TAG, STORAGE_VIEWING_ACTION).let {
            mAction = it
            when (mAction) {
                STORAGE_VIEWING_ACTION -> {
                    requireActivity().title = "Storage"
                }
                STORAGE_TRASFERING_ITEM_ACTION -> {
                    requireActivity().title = "Select Storage"

                    mStorageItemWithExpirationsToTransfer = requireArguments().getParcelable(STORAGE_ITEM_ENTITY_TAG)!!
                    mStorageEntityOrigin = requireArguments().getParcelable(STORAGE_ENTITY_TAG)!!

                }
                STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION->{
                    requireActivity().title = "Select Storage"
                    mGroceryItemEntity = requireArguments().getParcelable(GROCERY_ITEM_ENTITY_TAG)!!


                }
                STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION->{
                    requireActivity().title = "Select Storage"
                    mGroceryListEntity = requireArguments().getParcelable(GROCERY_ENTITY_TAG)
                }
            }
        }

        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_storage, container, false)
        mDataBindingUtil.lifecycleOwner = this


        mDataBindingUtil.fab.setOnClickListener{
            val createStorageActivity = Intent(this.context, CreateStorageActivity::class.java)
            startActivityForResult(createStorageActivity, ADD_STORAGE_REQUEST_CODE)

        }

        mDataBindingUtil.storageTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (tab?.position == 0) {

                    mViewing = VIEW_BY_STORAGE

                    // hide option menu
                    setHasOptionsMenu(false)

                    mDataBindingUtil.fab.show()
                    mDataBindingUtil.storageStorageRecyclerview.adapter = StorageViewAdapter(this@StorageFragment) as RecyclerView.Adapter<RecyclerView.ViewHolder>
                    getItemViewByStorage()
                    mDataBindingUtil.fab.show()

                } else if (tab?.position == 1) {


                    mViewing = VIEW_PER_PRODUCT

                    // show option menu
                    setHasOptionsMenu(true)

                    mDataBindingUtil.storageStorageRecyclerview.adapter = StoragePerItemRecyclerviewViewAdapater(this@StorageFragment) as RecyclerView.Adapter<RecyclerView.ViewHolder>
                    getItemViewByItem("")
                    mDataBindingUtil.fab.hide()


                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })


        requireActivity().onBackPressedDispatcher.addCallback(object:OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                if(isViewingImage){
                    closeImageViewing()
                }
            }

        })

        mDataBindingUtil.imageContainerIncludedLayout.imageViewingCloseButton.setOnClickListener(closeImageViewingClickLister)
        mDataBindingUtil.swipeRefresh.setOnRefreshListener {
            loadItems()
            mDataBindingUtil.swipeRefresh.isRefreshing = false
        }

        mDataBindingUtil.swipeRefresh.isRefreshing = true
        loadItems()
        mDataBindingUtil.swipeRefresh.isRefreshing = false
        return mDataBindingUtil.root
    }

    private fun loadItems(){
        val storageViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>?

        //RecyclerView.ViewHolder
        if(mAction == STORAGE_VIEWING_ACTION && mViewing == VIEW_BY_STORAGE){
            storageViewAdapter = StorageViewAdapter(this) as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter

            getItemViewByStorage()


        }else if((mAction == STORAGE_TRASFERING_ITEM_ACTION && mViewing == VIEW_BY_STORAGE)
            || (mAction == STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION && mViewing == VIEW_BY_STORAGE)
            || (mAction == STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION && mViewing == VIEW_BY_STORAGE)){


            mDataBindingUtil.storageTabLayout.visibility = View.GONE
            storageViewAdapter = StorageViewForTransferingItemsAdapter(this)  as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter
            getItemViewByStorage()

        }else if(mAction == STORAGE_VIEWING_ACTION && mViewing == VIEW_PER_PRODUCT){

            storageViewAdapter = StoragePerItemRecyclerviewViewAdapater(this) as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter

            getItemViewByItem("")
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == ADD_STORAGE_REQUEST_CODE){

           data?.getParcelableExtra<StorageEntity>(STORAGE_ENTITY_TAG)?.let {
               displayItemForInserting(it)

           }
        }else if(resultCode == Activity.RESULT_OK && requestCode == UPDATE_STORAGE_REQUEST_CODE){
            data?.getParcelableExtra<StorageEntity>(STORAGE_ENTITY_TAG)?.let {
                displayItemForUpdating(it)
            }
        }else{

             if((mAction == STORAGE_TRASFERING_ITEM_ACTION && mViewing == VIEW_BY_STORAGE)
                 || (mAction == STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION && mViewing == VIEW_BY_STORAGE)
                 || (mAction == STORAGE_VIEWING_ACTION && mViewing == VIEW_BY_STORAGE)){


                getItemViewByStorage()

            }else if(mAction == STORAGE_VIEWING_ACTION && mViewing == VIEW_PER_PRODUCT){

                getItemViewByItem("")
            }
        }

    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_all_item_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        mSearchView = menu.findItem(R.id.appBarSearch)?.actionView as SearchView
        mSearchView?.setOnQueryTextListener(this)



    }
    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {

            if(mSearchJob == null){
                mSearchJob = Job()
            }else{
                mSearchJob.cancel()
                mSearchJob = Job()
            }

            CoroutineScope(Dispatchers.IO +mSearchJob).launch {
                val searchResult = getSearchItems(it)

                withContext(Main){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = searchResult
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()
                }
            }


        }

        return true
    }
    override fun onQueryTextChange(query: String?): Boolean {

        query?.let {

            if(mSearchJob == null){
                mSearchJob = Job()
            }else{
                mSearchJob.cancel()
                mSearchJob = Job()
            }

            CoroutineScope(Dispatchers.IO +mSearchJob).launch {
                val searchResult = getSearchItems(it)
                withContext(Main){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = searchResult
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()
                }
            }


        }
        return true
    }
    private suspend fun getSearchItems(searchTerm:String):ArrayList<StorageItemWithExpirationsAndStorages>{
        if(mFilter == NO_FILTER){
            return mStorageViewModel.getStorageItemWithExpirationsWithTotalQuantity(this@StorageFragment.requireContext(),searchTerm)

        }else if(mFilter == FILTER_BY_STOCK){
            if(mFilterByQuantityOptionSelected == GREATER_THAN_QUANTITY){

                return mStorageViewModel.getAllItemWithExpirationsFilterGreaterThan(requireContext(), searchTerm,mQuantityFilterFirstValue)

            }else if(mFilterByQuantityOptionSelected == LESS_THAN_QUANTITY){

                return mStorageViewModel.getAllItemWithExpirationsFilterLessThan(requireContext(), searchTerm,mQuantityFilterFirstValue)

            }else if(mFilterByQuantityOptionSelected == BETWEEN_QUANTITY){
                return  mStorageViewModel.getAllItemWithExpirationsFilterQuantityBetween(requireContext(), searchTerm,mQuantityFilterFirstValue,mQuantityFilterSecondValue)
            }

        }else if(mFilter == FILTER_BY_LAST_UPDATE){

            return mStorageViewModel.getAllItemWithExpirationsFilterByDateModified(requireContext(),searchTerm,mFilterByDateModifiedDate)

        }else if(mFilter == FILTER_BY_EXPIRED){

            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
           return mStorageViewModel.getStorageItemWithExpirationsWithTotalQuantityFilterByExpired(requireContext(), searchTerm,currentDate)

        }

        return ArrayList()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        mSelectedMenuItem = item
        when (item.itemId) {

            R.id.noFilterMenu -> {
                mFilter = NO_FILTER
                getItemViewByItem("")

            }
            R.id.stockMenu -> {
                showFilterByQuantityPopup()
            }
            R.id.lastUpdateMenu -> {

                showCalendarForLastUpdateFilter()
            }
            R.id.expiredMenu -> {
                mFilter = FILTER_BY_EXPIRED
                getItemViewByItemFilterByExpired("")
            }
        }


        return super.onOptionsItemSelected(item)
    }
    private fun displayItemForInserting(storageEntity: StorageEntity){

        mStorageViewModel.coroutineScope.launch {


            if(mAction == STORAGE_VIEWING_ACTION){
                mStorageViewModel.storageEntitiesWithExtraInformation =  mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())


            }else{
                mStorageViewModel.storageEntitiesWithExtraInformation = mStorageViewModel.getAllStorageExceptSome(this@StorageFragment.requireContext(), arrayListOf(mStorageEntityOrigin!!.uniqueId))

            }

            val newItemIndex =   mStorageViewModel.storageEntitiesWithExtraInformation.indexOfFirst { it.storageEntity.uniqueId == storageEntity.uniqueId }

            withContext(Main){

                if(mAction == STORAGE_VIEWING_ACTION){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).notifyItemInserted(newItemIndex)

                }else{
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).notifyItemInserted(newItemIndex)


                }

                showOrHideNoStorageTextView()
                Handler(Looper.getMainLooper()).postDelayed({
                    animateElement(newItemIndex, -1)

                }, 200)


            }
        }
    }
    private fun displayItemForUpdating(storageEntity: StorageEntity){

        mStorageViewModel.coroutineScope.launch {


            if(mAction == STORAGE_VIEWING_ACTION){
                mStorageViewModel.storageEntitiesWithExtraInformation =  mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())


            }else{
                mStorageViewModel.storageEntitiesWithExtraInformation = mStorageViewModel.getAllStorageExceptSome(this@StorageFragment.requireContext(), arrayListOf(mStorageEntityOrigin!!.uniqueId))

            }

            val newItemIndex =   mStorageViewModel.storageEntitiesWithExtraInformation.indexOfFirst { it.storageEntity.uniqueId == storageEntity.uniqueId }

            withContext(Main){

                if(mAction == STORAGE_VIEWING_ACTION){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation

                }else{
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation

                }
                mDataBindingUtil.storageStorageRecyclerview.adapter?.notifyItemChanged(newItemIndex)

                Handler(Looper.getMainLooper()).postDelayed({
                    animateElement(newItemIndex, -1)

                }, 200)


            }
        }
    }
    private fun animateElement(indexOfNewItem: Int, itemChangeType: Int){

        val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fadeInAnimation.duration = 250
        fadeInAnimation.fillAfter = true

        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                /* if (itemChangeType == StorageActivity.UPDATED_NEW_ELEMENT) {
                    mDataBindingUtil.storageStorageRecyclerview.adapter?.notifyItemChanged(indexOfNewItem)
                }*/
            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })

        val firstVisibleItemPosition = ( mDataBindingUtil.storageStorageRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val lastVisibleItemPosition = ( mDataBindingUtil.storageStorageRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        if(indexOfNewItem >= firstVisibleItemPosition && indexOfNewItem <= lastVisibleItemPosition){


            val viewHolder = mDataBindingUtil.storageStorageRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
            //val itemViewHolder = viewHolder as StorageActivity.StorageRecyclerviewViewAdapater.ItemViewHolder
            if(viewHolder is StorageViewAdapter.ItemViewHolder ){

                viewHolder.storageItemBinding.storageItemParentLayout.startAnimation(fadeInAnimation)
            }else if(viewHolder is StorageViewForTransferingItemsAdapter.ItemViewHolder ){

                viewHolder.storageItemForTransferingBinding.storageItemParentLayout.startAnimation(fadeInAnimation)

            }



        }else{


            mDataBindingUtil.storageStorageRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var found = false
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val viewHolder = mDataBindingUtil.storageStorageRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
                    if (viewHolder != null) {


                        val itemViewHolder = viewHolder as StorageActivity.StorageRecyclerviewViewAdapater.ItemViewHolder
                        itemViewHolder.storageItemLayoutBinding.pantryItemParentLayout.startAnimation(fadeInAnimation)

                        found = true
                        mDataBindingUtil.storageStorageRecyclerview.removeOnScrollListener(this)

                    }
                }

            })
            mDataBindingUtil.storageStorageRecyclerview.scrollToPosition(indexOfNewItem)

        }
    }
    fun getItemViewByStorage(){
        mStorageViewModel.coroutineScope.launch {

            if(mAction == STORAGE_VIEWING_ACTION){
                mStorageViewModel.storageEntitiesWithExtraInformation =  mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation

            }else if(mAction == STORAGE_TRASFERING_ITEM_ACTION || mAction == STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION || mAction == STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION ){
                mStorageEntityOrigin?.let{
                    mStorageViewModel.storageEntitiesWithExtraInformation = mStorageViewModel.getAllStorageExceptSome(this@StorageFragment.requireContext(), arrayListOf(mStorageEntityOrigin!!.uniqueId))
                }?:run{
                    mStorageViewModel.storageEntitiesWithExtraInformation =  mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())
                }

                (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation
            }


            withContext(Main){

                if(mAction == STORAGE_VIEWING_ACTION){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).notifyDataSetChanged()
                }else{
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).notifyDataSetChanged()
                }

                showOrHideNoStorageTextView()

            }
        }
    }
    fun getItemViewByItem(searchTerm:String){
        mStorageViewModel.coroutineScope.launch {
            val storageEntitiesWithExpirationsAndStoragesInnerScope = mStorageViewModel.getStorageItemWithExpirationsWithTotalQuantity(this@StorageFragment.requireContext(),searchTerm)
            mStorageViewModel.storageEntitiesWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStoragesInnerScope
            withContext(Main){
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStoragesInnerScope
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()
                showOrHideNoItemTextView()

            }
        }
    }
    private fun getItemViewByItemFilterByExpired(itemNameSearchTerm:String){

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = simpleDateFormat.format(Date())

        mStorageViewModel.coroutineScope.launch {
            val storageEntitiesWithExpirationsAndStoragesInnerScope = mStorageViewModel.getStorageItemWithExpirationsWithTotalQuantityFilterByExpired(requireContext(), itemNameSearchTerm,currentDate)
            mStorageViewModel.storageEntitiesWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStoragesInnerScope
            withContext(Main){
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStoragesInnerScope
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()
            }
        }
    }
    private fun showOrHideNoStorageTextView(){
        if(mStorageViewModel.storageEntitiesWithExtraInformation.isEmpty()){
            mDataBindingUtil.noStorageOrItemTextView.visibility = View.VISIBLE
            mDataBindingUtil.noStorageOrItemTextView.text = "No Storage"
        }else{
            mDataBindingUtil.noStorageOrItemTextView.visibility = View.GONE
        }
    }

    private fun showOrHideNoItemTextView(){
        if(mStorageViewModel.storageEntitiesWithExpirationsAndStorages.isEmpty()){
            mDataBindingUtil.noStorageOrItemTextView.visibility = View.VISIBLE
            mDataBindingUtil.noStorageOrItemTextView.text = "No Items"
        }else{
            mDataBindingUtil.noStorageOrItemTextView.visibility = View.GONE
        }
    }
    private fun showFilterByQuantityPopup(){

        val storageQuanityFilterBinding = DataBindingUtil.inflate<StorageQuantityFilterBinding>(LayoutInflater.from(requireContext()), R.layout.storage_quantity_filter, null, false)

        if(mFilter == FILTER_BY_STOCK){

            if(mFilterByQuantityOptionSelected == GREATER_THAN_QUANTITY){
                storageQuanityFilterBinding.greaterThanRadioButton.isChecked = true
                storageQuanityFilterBinding.greaterThanEditText.setText(mQuantityFilterFirstValue.toString())
                storageQuanityFilterBinding.greaterThanEditText.isEnabled = true
                storageQuanityFilterBinding.greaterThanEditText.requestFocus()

            }else if(mFilterByQuantityOptionSelected == LESS_THAN_QUANTITY){
                storageQuanityFilterBinding.lessThanRadioButton.isChecked = true
                storageQuanityFilterBinding.lessThanEditText.setText(mQuantityFilterFirstValue.toString())
                storageQuanityFilterBinding.lessThanEditText.isEnabled = true
                storageQuanityFilterBinding.lessThanEditText.requestFocus()

            }else if(mFilterByQuantityOptionSelected == BETWEEN_QUANTITY){
                storageQuanityFilterBinding.betweenRadioButton.isChecked = true
                storageQuanityFilterBinding.betweenFromInput.setText(mQuantityFilterFirstValue.toString())
                storageQuanityFilterBinding.betweenFromInput.isEnabled = true
                storageQuanityFilterBinding.betweenFromInput.requestFocus()

                storageQuanityFilterBinding.betweenToInput.setText(mQuantityFilterSecondValue.toString())
                storageQuanityFilterBinding.betweenToInput.isEnabled = true
            }
        }
        val alertDialog =  MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by stock")
            .setView(storageQuanityFilterBinding.root)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.setOnShowListener {

            val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveBtn.setOnClickListener {

                mSelectedMenuItem?.isChecked = true
                mFilter = FILTER_BY_STOCK

                var hasError = false
                var errorMessage = ""

                mStorageViewModel.coroutineScope.launch {

                    var storageItemWithExpirations:ArrayList<StorageItemWithExpirationsAndStorages> = arrayListOf()

                    if(storageQuanityFilterBinding.greaterThanRadioButton.isChecked){
                        val quantityString = storageQuanityFilterBinding.greaterThanEditText.text.toString()

                        if(quantityString.trim().isEmpty()){
                            hasError = true
                            errorMessage ="Please input quantity in greater than input"

                        }else{
                            withContext(Main){
                                mQuantityFilterFirstValue = quantityString.toInt()
                                mFilterByQuantityOptionSelected = GREATER_THAN_QUANTITY
                            }

                            storageItemWithExpirations = mStorageViewModel.getAllItemWithExpirationsFilterGreaterThan(requireContext(), "",mQuantityFilterFirstValue)
                        }

                    }else if(storageQuanityFilterBinding.lessThanRadioButton.isChecked){
                        val quantityString = storageQuanityFilterBinding.lessThanEditText.text.toString()
                        if(quantityString.trim().isEmpty()){
                            hasError = true
                            errorMessage ="Please input quantity in greater than input"

                        }else{
                            withContext(Main){
                                mQuantityFilterFirstValue = quantityString.toInt()
                                mFilterByQuantityOptionSelected = LESS_THAN_QUANTITY
                            }

                            storageItemWithExpirations = mStorageViewModel.getAllItemWithExpirationsFilterLessThan(requireContext(), "",mQuantityFilterFirstValue)

                        }

                    }else if(storageQuanityFilterBinding.betweenRadioButton.isChecked){
                        val quantityFromString = storageQuanityFilterBinding.betweenFromInput.text.toString()
                        val quantityToString = storageQuanityFilterBinding.betweenToInput.text.toString()

                        if(quantityFromString.trim().isEmpty() || quantityToString.trim().isEmpty() ){
                            hasError = true
                            errorMessage ="Please input between quantity"

                        }else{
                            withContext(Main){
                                mFilterByQuantityOptionSelected = BETWEEN_QUANTITY
                                mQuantityFilterFirstValue = quantityFromString.toInt()
                                mQuantityFilterSecondValue = quantityToString.toInt()
                            }

                            storageItemWithExpirations = mStorageViewModel.getAllItemWithExpirationsFilterQuantityBetween(requireContext(), "",mQuantityFilterFirstValue,mQuantityFilterSecondValue)

                        }

                    }else{
                        hasError = true
                        errorMessage = "Please select option"
                    }


                    withContext(Main){

                        if(hasError){
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                            return@withContext
                        }
                        alertDialog.dismiss()
                        (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = storageItemWithExpirations
                        (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()




                        hideKeyboard()
                    }


                }

            }
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
        quantityFilterCheckListener(storageQuanityFilterBinding)

    }
    private fun showCalendarForLastUpdateFilter(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            mFilter = FILTER_BY_LAST_UPDATE
            mSelectedMenuItem?.isChecked = true

            val simpleDateFormat = SimpleDateFormat("yyyy-M-d")
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            mFilterByDateModifiedDate =  SimpleDateFormat("yyyy-MM-dd").format(date)

            mStorageViewModel.coroutineScope.launch {

                val storageItemWithExpirations = mStorageViewModel.getAllItemWithExpirationsFilterByDateModified(requireContext(),"",mFilterByDateModifiedDate)

                withContext(Main){

                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = storageItemWithExpirations
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()

                }
            }

        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    private fun quantityFilterCheckListener(storageQuanityFilterBinding: StorageQuantityFilterBinding){
        storageQuanityFilterBinding.greaterThanRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){

                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

                storageQuanityFilterBinding.lessThanRadioButton.isChecked = false
                storageQuanityFilterBinding.lessThanEditText.setText("")
                storageQuanityFilterBinding.lessThanEditText.isEnabled = false

                storageQuanityFilterBinding.betweenRadioButton.isChecked = false
                storageQuanityFilterBinding.betweenFromInput.setText("")
                storageQuanityFilterBinding.betweenFromInput.isEnabled = false

                storageQuanityFilterBinding.greaterThanEditText.isEnabled  = true
                storageQuanityFilterBinding.greaterThanEditText.requestFocus()


                showSoftKeyboard(storageQuanityFilterBinding.greaterThanEditText)



            }
        }

        storageQuanityFilterBinding.lessThanRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){

                storageQuanityFilterBinding.greaterThanRadioButton.isChecked = false
                storageQuanityFilterBinding.greaterThanEditText.setText("")
                storageQuanityFilterBinding.greaterThanEditText.isEnabled = false

                storageQuanityFilterBinding.betweenRadioButton.isChecked = false
                storageQuanityFilterBinding.betweenFromInput.setText("")
                storageQuanityFilterBinding.betweenFromInput.isEnabled = false
                storageQuanityFilterBinding.betweenToInput.setText("")
                storageQuanityFilterBinding.betweenToInput.isEnabled = false

                storageQuanityFilterBinding.lessThanEditText.isEnabled = true
                storageQuanityFilterBinding.lessThanEditText.requestFocus()
                showSoftKeyboard(storageQuanityFilterBinding.lessThanEditText)
            }
        }

        storageQuanityFilterBinding.betweenRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){
                storageQuanityFilterBinding.greaterThanRadioButton.isChecked = false
                storageQuanityFilterBinding.greaterThanEditText.setText("")
                storageQuanityFilterBinding.greaterThanEditText.isEnabled = false

                storageQuanityFilterBinding.lessThanRadioButton.isChecked = false
                storageQuanityFilterBinding.lessThanEditText.setText("")
                storageQuanityFilterBinding.lessThanEditText.isEnabled = false


                storageQuanityFilterBinding.betweenFromInput.isEnabled = true
                storageQuanityFilterBinding.betweenToInput.isEnabled = true
                storageQuanityFilterBinding.betweenFromInput.requestFocus()

                showSoftKeyboard(storageQuanityFilterBinding.betweenFromInput)


            }
        }
    }
    private fun showSoftKeyboard(view: View){
        val inputMethodManager = activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            view.applicationWindowToken, InputMethodManager.SHOW_FORCED, 0
        )
    }
    private fun hideKeyboard() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    private val closeImageViewingClickLister = View.OnClickListener {
        closeImageViewing()

    }
    fun closeImageViewing(){

        val mainLayout = mDataBindingUtil.container
        val imageContainer = mDataBindingUtil.imageContainerIncludedLayout.imageContainer
        val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        ImageUtil.zoomOutImageFromThumb(mSelectedImageThumbnailView, mainLayout,  imageContainer, animationDuration)
        isViewingImage = false
    }
    fun zoomImageFromThumb(thumbView: View, imageUri: Uri) {

        mSelectedImageThumbnailView = thumbView
        val mainLayout = mDataBindingUtil.container
        val imageContainer = mDataBindingUtil.imageContainerIncludedLayout.imageContainer
        val imageHolder = mDataBindingUtil.imageContainerIncludedLayout.imageHolder
        val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        ImageUtil.zoomInImageFromThumb(thumbView, imageUri, mainLayout,  imageContainer, imageHolder, animationDuration)

        isViewingImage = true
    }
    fun showTransferStorageItemAlertDialog(storageEntity: StorageEntity){

        val choices = arrayOf(
            "Merge items quantity ",
            "Replace existing items quantity"
        )

        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Select options")
            .setSingleChoiceItems(choices, 0, null)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()
        alertDialog.setOnShowListener {

            val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveBtn.setOnClickListener {
                val checkedItemPosition = alertDialog.listView.checkedItemPosition
                if(checkedItemPosition == 0){
                    if(mAction == STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION){
                        mergeStorageItemFromGroceryList(storageEntity)
                    }else if(mAction == STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION){
                        mergeStorageItemFromAllGroceryListItems(storageEntity)
                    }else if(mAction == STORAGE_TRASFERING_ITEM_ACTION){
                        mergeStorageItem(storageEntity)
                    }

                }else if(checkedItemPosition == 1){
                    if(mAction == STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION){
                        replaceStorageItemFromGroceryList(storageEntity)
                    }else if(mAction == STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION){
                        replaceStorageItemFromFromAllGroceryListItems(storageEntity)
                    }else if(mAction == STORAGE_TRASFERING_ITEM_ACTION){
                        replaceStorageItem(storageEntity)
                    }

                }
                alertDialog.dismiss()
            }
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        alertDialog.show()

    }
    private fun mergeStorageItemFromGroceryList(storageEntity: StorageEntity){
        mStorageViewModel.coroutineScope.launch {

            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext())
            val storageItemEntity = mStorageViewModel.getSingleItemByNameAndUnitAndStorageUniqueId(this@StorageFragment.requireContext(), mGroceryItemEntity!!.itemName,mGroceryItemEntity!!.unit,storageEntity.uniqueId)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            var movedSuccessfully = true
            try {
                allHomeDatabase.withTransaction {

                    if(storageItemEntity == null){
                        // insert
                        var itemUniqueID = UUID.randomUUID().toString()
                        val groceryItemImageName = if(mGroceryItemEntity != null && mGroceryItemEntity?.imageName != null) mGroceryItemEntity?.imageName else ""
                        val storageImageItemName = copyGroceryItemImageImage(groceryItemImageName!!,mGroceryItemEntity!!.itemName,itemUniqueID) ?: ""


                        val storageItemEntity = StorageItemEntity(
                            uniqueId = itemUniqueID,
                            storageUniqueId = storageEntity.uniqueId,
                            name = mGroceryItemEntity!!.itemName,
                            quantity = mGroceryItemEntity!!.quantity,
                            unit = mGroceryItemEntity!!.unit,
                            category = "",
                            storage = storageEntity.name,
                            notes = "",
                            imageName = storageImageItemName,
                            itemStatus = StorageItemEntityValues.NOT_DELETED_STATUS,
                            created = currentDatetime,
                            modified = currentDatetime
                        )
                        mStorageViewModel.saveStorageItemEntity(requireContext(),storageItemEntity)

                    }else{
                        //update
                        val groceryItemImageName = if(mGroceryItemEntity != null && mGroceryItemEntity?.imageName != null) mGroceryItemEntity?.imageName else ""
                        val storageImageItemName =  copyGroceryItemImageImage(groceryItemImageName!!,mGroceryItemEntity!!.itemName,storageItemEntity.uniqueId) ?: storageItemEntity.imageName
                        storageItemEntity.imageName = storageImageItemName
                        storageItemEntity.quantity =  if(storageItemEntity.quantity <= StorageItemEntityValues.NO_QUANTITY_INPUT) mGroceryItemEntity!!.quantity else storageItemEntity.quantity + mGroceryItemEntity!!.quantity

                        val updateItemAffectedRow = mStorageViewModel.updateStorageItemEntity(requireContext(),storageItemEntity)

                        val expirations = mStorageViewModel.getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(requireContext(), storageEntity.uniqueId, storageItemEntity.uniqueId, storageItemEntity.modified)
                        expirations.forEach {
                            val storageExpiration = it.copy()
                            storageExpiration.created = currentDatetime
                            val storageExpirationEntityId = mStorageViewModel.saveStorageItemExpirationEntity(this@StorageFragment.requireContext(), storageExpiration)
                        }
                    }
                }

            }catch (ex: java.lang.Exception){
                ex.printStackTrace()
                movedSuccessfully = false
            }


            withContext(Main){
                if(movedSuccessfully){
                    Toast.makeText(requireContext(),"Item successfully added to storage",Toast.LENGTH_SHORT).show()
                    activity?.finish()
                }else{
                    Toast.makeText(requireContext(),"Failed to add items in storage.",Toast.LENGTH_SHORT).show()

                }

            }

        }
    }
    private fun mergeStorageItemFromAllGroceryListItems(storageEntity: StorageEntity){

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mGroceryListEntity?.let{
            mStorageViewModel.coroutineScope.launch {
                var movedSuccessfully = true
                val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext())
                val boughtGroceryItemEntityies =  mStorageViewModel.getBoughtGroceryListItems(requireContext(),it.autoGeneratedUniqueId)

                try {

                    allHomeDatabase.withTransaction {
                        boughtGroceryItemEntityies.forEach {groceryItemEntity->
                            val groceryItemName = groceryItemEntity.itemName
                            val groceryItemUnit  = groceryItemEntity.unit
                            val groceryItemQuantity = groceryItemEntity.quantity

                            val storageItemEntity = mStorageViewModel.getSingleItemByNameAndUnitAndStorageUniqueId(this@StorageFragment.requireContext(), groceryItemName,groceryItemUnit,storageEntity.uniqueId)

                            storageItemEntity?.let{
                                //update record
                                val storageImageItemName = copyGroceryItemImageImage(groceryItemEntity.imageName,groceryItemName,it.uniqueId) ?: storageItemEntity.imageName

                                storageItemEntity.imageName = storageImageItemName
                                storageItemEntity.quantity =  if(storageItemEntity.quantity <= StorageItemEntityValues.NO_QUANTITY_INPUT) groceryItemQuantity else storageItemEntity.quantity + groceryItemQuantity

                                val updateItemAffectedRow = mStorageViewModel.updateStorageItemEntity(requireContext(),storageItemEntity)
                                val expirations = mStorageViewModel.getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(requireContext(), storageEntity.uniqueId, storageItemEntity.uniqueId, storageItemEntity.modified)
                                expirations.forEach {
                                    val storageExpiration = it.copy()
                                    storageExpiration.created = currentDatetime
                                    val storageExpirationEntityId = mStorageViewModel.saveStorageItemExpirationEntity(this@StorageFragment.requireContext(), storageExpiration)
                                }



                            }?:run{
                                //insert record

                                var itemUniqueID = UUID.randomUUID().toString()
                                val storageImageItemName = copyGroceryItemImageImage(groceryItemEntity.imageName,groceryItemName,itemUniqueID) ?: ""


                                val storageItemEntity = StorageItemEntity(
                                    uniqueId = itemUniqueID,
                                    storageUniqueId = storageEntity.uniqueId,
                                    name =groceryItemName,
                                    quantity =groceryItemQuantity,
                                    unit = groceryItemUnit,
                                    category = "",
                                    storage = storageEntity.name,
                                    notes = "",
                                    imageName = storageImageItemName,
                                    itemStatus = StorageItemEntityValues.NOT_DELETED_STATUS,
                                    created = currentDatetime,
                                    modified = currentDatetime
                                )
                                mStorageViewModel.saveStorageItemEntity(requireContext(),storageItemEntity)

                            }

                        }
                    }
                }catch (ex: java.lang.Exception){
                    ex.printStackTrace()
                    movedSuccessfully = false
                }

                withContext(Main){
                    if(movedSuccessfully){
                        Toast.makeText(requireContext(),"Item successfully added to storage",Toast.LENGTH_SHORT).show()
                        activity?.finish()
                    }else{
                        Toast.makeText(requireContext(),"Failed to add items in storage",Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }

    }
    private fun replaceStorageItemFromFromAllGroceryListItems(storageEntity: StorageEntity){
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mGroceryListEntity?.let{
            mStorageViewModel.coroutineScope.launch {
                var movedSuccessfully = true
                val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext())
                val boughtGroceryItemEntityies =  mStorageViewModel.getBoughtGroceryListItems(requireContext(),it.autoGeneratedUniqueId)

                try {

                    allHomeDatabase.withTransaction {
                        boughtGroceryItemEntityies.forEach {groceryItemEntity->
                            val groceryItemName = groceryItemEntity.itemName
                            val groceryItemUnit  = groceryItemEntity.unit
                            val groceryItemQuantity = groceryItemEntity.quantity

                            val storageItemEntity = mStorageViewModel.getSingleItemByNameAndUnitAndStorageUniqueId(this@StorageFragment.requireContext(), groceryItemName,groceryItemUnit,storageEntity.uniqueId)

                            storageItemEntity?.let{
                                //update record
                                val storageImageItemName = copyGroceryItemImageImage(groceryItemEntity.imageName,groceryItemName,it.uniqueId) ?: storageItemEntity.imageName

                                storageItemEntity.imageName = storageImageItemName
                                storageItemEntity.quantity =  groceryItemQuantity

                                val updateItemAffectedRow = mStorageViewModel.updateStorageItemEntity(requireContext(),storageItemEntity)
                                val expirations = mStorageViewModel.getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(requireContext(), storageEntity.uniqueId, storageItemEntity.uniqueId, storageItemEntity.modified)
                                expirations.forEach {
                                    val storageExpiration = it.copy()
                                    storageExpiration.created = currentDatetime
                                    val storageExpirationEntityId = mStorageViewModel.saveStorageItemExpirationEntity(this@StorageFragment.requireContext(), storageExpiration)
                                }

                            }?:run{
                                //insert record

                                var itemUniqueID = UUID.randomUUID().toString()
                                val storageImageItemName = copyGroceryItemImageImage(groceryItemEntity.imageName,groceryItemName,itemUniqueID) ?: ""

                                val storageItemEntity = StorageItemEntity(
                                    uniqueId = itemUniqueID,
                                    storageUniqueId = storageEntity.uniqueId,
                                    name =groceryItemName,
                                    quantity =groceryItemQuantity,
                                    unit = groceryItemUnit,
                                    category = "",
                                    storage = storageEntity.name,
                                    notes = "",
                                    imageName = storageImageItemName,
                                    itemStatus = StorageItemEntityValues.NOT_DELETED_STATUS,
                                    created = currentDatetime,
                                    modified = currentDatetime
                                )
                                mStorageViewModel.saveStorageItemEntity(requireContext(),storageItemEntity)

                            }

                        }
                    }
                }catch (ex: java.lang.Exception){
                    movedSuccessfully = false
                }

                withContext(Main){
                    if(movedSuccessfully){
                        Toast.makeText(requireContext(),"Item successfully added to storage",Toast.LENGTH_SHORT).show()
                        activity?.finish()
                    }else{
                        Toast.makeText(requireContext(),"Failed to add items in storage",Toast.LENGTH_SHORT).show()

                    }
                }
            }
        }
    }
    private fun replaceStorageItemFromGroceryList(storageEntity: StorageEntity){
        mStorageViewModel.coroutineScope.launch {
            val storageItemEntity = mStorageViewModel.getSingleItemByNameAndUnitAndStorageUniqueId(this@StorageFragment.requireContext(), mGroceryItemEntity!!.itemName,mGroceryItemEntity!!.unit,storageEntity.uniqueId)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            if(storageItemEntity == null){
                // insert
                var itemUniqueID = UUID.randomUUID().toString()
                val groceryItemImageName = if(mGroceryItemEntity != null && mGroceryItemEntity?.imageName != null) mGroceryItemEntity?.imageName else ""
                val storageImageItemName = copyGroceryItemImageImage(groceryItemImageName!!,mGroceryItemEntity!!.itemName,itemUniqueID) ?: ""

                val storageItemEntity = StorageItemEntity(
                    uniqueId = itemUniqueID,
                    storageUniqueId = storageEntity.uniqueId,
                    name = mGroceryItemEntity!!.itemName,
                    quantity = mGroceryItemEntity!!.quantity,
                    unit = mGroceryItemEntity!!.unit,
                    category = "",
                    storage = storageEntity.name,
                    notes = "",
                    imageName = storageImageItemName,
                    itemStatus = StorageItemEntityValues.NOT_DELETED_STATUS,
                    created = currentDatetime,
                    modified = currentDatetime
                )
                mStorageViewModel.saveStorageItemEntity(requireContext(),storageItemEntity)

            }else{
                //update

                val groceryItemImageName = if(mGroceryItemEntity != null && mGroceryItemEntity?.imageName != null) mGroceryItemEntity?.imageName else ""
                val storageImageItemName =  copyGroceryItemImageImage(groceryItemImageName!!,mGroceryItemEntity!!.itemName,storageItemEntity.uniqueId) ?: storageItemEntity.imageName
                storageItemEntity.imageName = storageImageItemName
                storageItemEntity.quantity = mGroceryItemEntity!!.quantity

                val updateItemAffectedRow = mStorageViewModel.updateStorageItemEntity(requireContext(),storageItemEntity)

                val expirations = mStorageViewModel.getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(requireContext(), storageEntity.uniqueId, storageItemEntity.uniqueId, storageItemEntity.modified)
                expirations.forEach {
                    val storageExpiration = it.copy()
                    storageExpiration.created = currentDatetime

                    val storageExpirationEntityId = mStorageViewModel.saveStorageItemExpirationEntity(this@StorageFragment.requireContext(), storageExpiration)
                }


            }

            withContext(Main){
                Toast.makeText(requireContext(),"Item successfully added",Toast.LENGTH_SHORT).show()
                activity?.finish()
            }

        }
    }
    @Throws(Exception::class)
    fun replaceStorageItem(distinationStorageEntity: StorageEntity){
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mStorageViewModel.coroutineScope.launch {

            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext())
            val storageItemEntity = mStorageViewModel.getItemByNameAndUnitAndStorage(this@StorageFragment.requireContext(), mStorageItemWithExpirationsToTransfer.storageItemEntity.name, mStorageItemWithExpirationsToTransfer.storageItemEntity.unit, distinationStorageEntity.name)
            var movedSuccessfully = true

            try{
                allHomeDatabase.withTransaction {

                    storageItemEntity?.let{

                        mStorageViewModel.updateItemAsDeleted(this@StorageFragment.requireContext(), currentDatetime, it)
                    }

                    insertStorageItemThanInSelectedStorage(distinationStorageEntity)

                }
            }catch (ex: java.lang.Exception){
                movedSuccessfully = false
            }

            withContext(Main){
                if(movedSuccessfully){
                    Toast.makeText(this@StorageFragment.requireContext(), "Moved successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@StorageFragment.requireContext(), "Failed to move item", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
    private fun  mergeStorageItem(destinationStorageEntity: StorageEntity){
        val name = mStorageItemWithExpirationsToTransfer.storageItemEntity.name
        val unit =   mStorageItemWithExpirationsToTransfer.storageItemEntity.unit

        mStorageViewModel.coroutineScope.launch {

            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext())
            val storageItemEntity = mStorageViewModel.getItemByNameAndUnitAndStorage(this@StorageFragment.requireContext(), name, unit, destinationStorageEntity.name)
            var movedSuccessfully = true
            try{
                allHomeDatabase.withTransaction {
                    storageItemEntity?.let {

                        insertStorageItemThatExistsInSelectedStorage(it)
                    }?:run {
                        // item not exists just insert
                        insertStorageItemThanInSelectedStorage(destinationStorageEntity)
                    }
                    /**
                     * @toDo Delete storage item from source storage
                     */
                }
            }catch (ex: java.lang.Exception){
                movedSuccessfully = false
            }

            withContext(Main){
                if(movedSuccessfully){
                    Toast.makeText(this@StorageFragment.requireContext(), "Moved successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@StorageFragment.requireContext(), "Failed to move item", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
    @Throws(Exception::class)
    private suspend fun insertStorageItemThanInSelectedStorage(distinationStorageEntity: StorageEntity) {

        // update storage item as deleted in origin storage
        val updateAsDeletedAffectedRowCount = mStorageViewModel.updateItemAsDeleted(this.requireContext(), mStorageItemWithExpirationsToTransfer.storageItemEntity.modified, mStorageItemWithExpirationsToTransfer.storageItemEntity.uniqueId)

        if(updateAsDeletedAffectedRowCount <=0){

            throw Exception("Failed to move item")
        }


        var itemUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val newStorageEntity = mStorageItemWithExpirationsToTransfer.storageItemEntity
        newStorageEntity.uniqueId = itemUniqueID
        newStorageEntity.storageUniqueId = distinationStorageEntity.uniqueId
        newStorageEntity.storage = distinationStorageEntity.name
        newStorageEntity.created = currentDatetime
        newStorageEntity.modified = currentDatetime


        val storageItemId = mStorageViewModel.saveStorageItemEntity(this@StorageFragment.requireContext(), newStorageEntity)

        if(storageItemId <=0){
            throw Exception("Failed to move item")
        }

        mStorageItemWithExpirationsToTransfer.expirations.forEach {
            val newExpiration = it
            newExpiration.uniqueId = UUID.randomUUID().toString()
            newExpiration.storageItemUniqueId = distinationStorageEntity.uniqueId
            newExpiration.storage = distinationStorageEntity.name
            newExpiration.created = currentDatetime
            val storageExpirationEntityId = mStorageViewModel.saveStorageItemExpirationEntity(this@StorageFragment.requireContext(), newExpiration)

            if(storageExpirationEntityId <=0){
                throw Exception("Failed to move item")
            }
        }


    }
    @Throws(Exception::class)
    private suspend fun insertStorageItemThatExistsInSelectedStorage(distinationStorageItemEntity: StorageItemEntity){


        // update storage item as deleted in origin storage
        val updateAsDeletedAffectedRowCount = mStorageViewModel.updateItemAsDeleted(this.requireContext(), mStorageItemWithExpirationsToTransfer.storageItemEntity.modified, mStorageItemWithExpirationsToTransfer.storageItemEntity.uniqueId)

        if(updateAsDeletedAffectedRowCount <=0){
            throw Exception("Failed to move item")
        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        // get expiration of storage items
        val expirations = mStorageViewModel.getStorageItemsExpiratinsByStorageUniquedIdItemNameAndCreated(this.requireContext(), distinationStorageItemEntity.uniqueId, distinationStorageItemEntity.name, distinationStorageItemEntity.modified)
        // merge expirations date
        val mergeExpirationDates = mergeExpirationDates(mStorageItemWithExpirationsToTransfer.expirations, expirations)

        val assembledDistinationStorageItemEntity = assembleNewStorageItemEntity(distinationStorageItemEntity, currentDatetime)
        val affectedRowCount = mStorageViewModel.updateStorageItemEntity(this@StorageFragment.requireContext(), assembledDistinationStorageItemEntity)

        if(affectedRowCount <=0){
            throw Exception("Failed to move item")
        }
        mergeExpirationDates.forEach {
            val newExpiration = it

            newExpiration.uniqueId = UUID.randomUUID().toString()
            newExpiration.storageItemUniqueId = distinationStorageItemEntity.uniqueId
            newExpiration.storage = distinationStorageItemEntity.storage
            newExpiration.created = currentDatetime
            val storageExpirationEntityId = mStorageViewModel.saveStorageItemExpirationEntity(this@StorageFragment.requireContext(), newExpiration)
            if(storageExpirationEntityId <=0){
                throw Exception("Failed to move item")
            }
        }




    }
    private fun assembleNewStorageItemEntity(distinationStorageItemEntity: StorageItemEntity, currentDate: String):StorageItemEntity{
        val stockWeight = mergeStockWeight(mStorageItemWithExpirationsToTransfer.storageItemEntity, distinationStorageItemEntity)
        val quantity = mStorageItemWithExpirationsToTransfer.storageItemEntity.quantity + distinationStorageItemEntity.quantity

        distinationStorageItemEntity.stockWeight = stockWeight
        distinationStorageItemEntity.quantity = quantity
        distinationStorageItemEntity.modified = currentDate
        return distinationStorageItemEntity
    }
    private fun mergeStockWeight(storageItemEntity1: StorageItemEntity, storageItemEntity2: StorageItemEntity):Int{
        if(storageItemEntity1.stockWeight > storageItemEntity2.stockWeight){
            return storageItemEntity1.stockWeight
        }else{
            return storageItemEntity2.stockWeight
        }
    }
    private fun mergeExpirationDates(expirations1: List<StorageItemExpirationEntity>, expirations2: List<StorageItemExpirationEntity>):List<StorageItemExpirationEntity>{
        return (expirations1 + expirations2).distinctBy {
            it.expirationDate
        }
    }
    fun deleteStorage(storageEntityWithExtraInformation: StorageEntityWithExtraInformation){
        val storageEntity = storageEntityWithExtraInformation.storageEntity
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mStorageViewModel.coroutineScope.launch {

            val index = mStorageViewModel.storageEntitiesWithExtraInformation.indexOf(storageEntityWithExtraInformation)
            mStorageViewModel.updateStorageAsDeleted(requireContext(), storageEntity.uniqueId, currentDatetime)
            mStorageViewModel.updateStorageItemAsDeleted(requireContext(), storageEntity.uniqueId,currentDatetime)


            mStorageViewModel.storageEntitiesWithExtraInformation.removeAt(index)

            withContext(Main){
                mDataBindingUtil.storageStorageRecyclerview.adapter?.notifyItemRemoved(index)
                showOrHideNoStorageTextView()

            }

        }
    }
    fun openStorageActivity(storageEntityWithStorageItemInformation: StorageEntityWithStorageItemInformation){

        /*val storageEntity = storageEntityWithStorageItemInformation.storageEntity
        val storageItemName = storageEntityWithStorageItemInformation.storageItemName
        val storageItemUnit = storageEntityWithStorageItemInformation.storageItemUnit

        val storageActivity = Intent(requireContext(), StorageActivity::class.java)
        storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG, storageEntity.name)
        storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_TAG, storageEntity)
        storageActivity.putExtra(StorageActivity.STORAGE_ITEM_NAME_TAG, storageItemName)
        storageActivity.putExtra(StorageActivity.STORAGE_ITEM_UNIT_TAG, storageItemUnit)
        requireActivity().startActivity(storageActivity)*/


        val storageItemName = storageEntityWithStorageItemInformation.storageItemName
        val storageItemUnit = storageEntityWithStorageItemInformation.storageItemUnit
        val storageEntity = storageEntityWithStorageItemInformation.storageEntity

        mStorageViewModel.coroutineScope.launch{
            val storageItemEntity = mStorageViewModel.getSingleItemByNameAndUnitAndStorageUniqueId(requireContext(),storageItemName,storageItemUnit,storageEntity.uniqueId)

            withContext(Main){

                storageItemEntity?.let{

                    val addPantryItemActivity = Intent(requireContext(), StorageAddItemActivity::class.java)
                    addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, storageEntity.name)
                    addPantryItemActivity.putExtra(StorageAddItemActivity.ACTION_TAG, StorageAddItemActivity.UPDATE_RECORD_ACTION)
                    addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG, it.uniqueId)
                    addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_NAME_TAG, it.name)
                    addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_TAG,storageEntity)
                   startActivityForResult(addPantryItemActivity, StorageActivity.UPDATE_ITEM_REQUEST_CODE)
                }?:run{
                    Toast.makeText(requireContext(),"Item not found in storage.",Toast.LENGTH_SHORT).show()
                }


            }
        }
    }
    private fun copyGroceryItemImageImage( groceryItemImageName:String,groceryItemName:String,storageItemUniqueId:String): String? {

        if(groceryItemImageName == null || groceryItemImageName.isEmpty()){
            return null
        }
        val indexOfItemName = groceryItemImageName.indexOf(groceryItemName)
        val groceryItemName = groceryItemImageName.substring(indexOfItemName,groceryItemImageName.length)
        val storageDir: File? = requireContext().getExternalFilesDir(GroceryUtil.GROCERY_ITEM_IMAGES_LOCATION)
        if(storageDir?.exists() == true){
            val fileToCopy  = File(storageDir, groceryItemImageName)
            if(fileToCopy.exists()){

                val destinationStorageDir: File = requireContext().getExternalFilesDir(ImageUtil.STORAGE_ITEM_IMAGES_FINAL_LOCATION)!!
                if(!destinationStorageDir.exists()){
                    destinationStorageDir.mkdir()
                }

                val finalFile  = File(destinationStorageDir, storageItemUniqueId.plus(groceryItemName))

                var source: FileChannel? = FileInputStream(fileToCopy).channel
                var destination: FileChannel? =  FileOutputStream(finalFile).channel

                if (destination != null && source != null) {
                    destination.transferFrom(source, 0, source.size())
                }
                source?.close()
                destination?.close()

                return finalFile.name

            }
        }
        return null

    }

}

/**
 * storage recyclierview adapater for viewing storage items
 */
class StorageViewAdapter(val storageFragment: StorageFragment): RecyclerView.Adapter<StorageViewAdapter.ItemViewHolder>() {

    var storageEntities:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val storageItemBinding = StorageItemBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(storageItemBinding)
        return itemViewHolder
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationEntity = storageEntities[position]
        holder.storageItemBinding.storageEntityWithExtraInformation = pantryItemExpirationEntity
        holder.storageItemBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {

        return storageEntities.size
    }
    inner class  ItemViewHolder(var storageItemBinding: StorageItemBinding): RecyclerView.ViewHolder(storageItemBinding.root),View.OnClickListener{
        init {

            storageItemBinding.storageItemParentLayout.setOnClickListener(this)
            storageItemBinding.moreActionImageView.setOnClickListener(this)

            storageItemBinding.storageImageView.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            val storageEntity = storageEntities[adapterPosition].storageEntity

            when(view?.id){
                R.id.storageItemParentLayout -> {

                    val storageActivity = Intent(view.context, StorageActivity::class.java)
                    storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG, storageEntity.name)
                    storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_TAG, storageEntity)
                    storageFragment.startActivityForResult(storageActivity,1989)


                }

                R.id.moreActionImageView -> {

                    val popupMenu = PopupMenu(view.context, view)
                    popupMenu.menuInflater.inflate(R.menu.storage_item_menu, popupMenu.menu)
                    popupMenu.show()

                    popupMenu.setOnMenuItemClickListener {
                        val storageEntityWithExpirations = storageEntities[adapterPosition]
                        val storageEntity = storageEntityWithExpirations.storageEntity

                        when (it.itemId) {
                            R.id.viewInformationMenu -> {
                                val createStorageActivity = Intent(view.context, CreateStorageActivity::class.java)
                                createStorageActivity.putExtra(CreateStorageActivity.ACTION_TAG, CreateStorageActivity.UPDATE_RECORD_ACTION)
                                createStorageActivity.putExtra(CreateStorageActivity.STORAGE_UNIQUE_ID_TAG, storageEntity.uniqueId)
                                storageFragment.startActivityForResult(createStorageActivity, StorageFragment.UPDATE_STORAGE_REQUEST_CODE)

                            }
                            R.id.viewItemsMenu -> {

                                val storageActivity = Intent(view.context, StorageActivity::class.java)
                                storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG, storageEntity.name)
                                storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_TAG, storageEntity)
                                storageFragment.requireActivity().startActivity(storageActivity)
                            }
                            R.id.deleteStorageMenu -> {

                                storageFragment.deleteStorage(storageEntityWithExpirations)



                            }
                            R.id.addToGroceryListMenu -> {

                                val choices = arrayOf(
                                    storageFragment.requireActivity().getString(R.string.expired_stock) + " items",
                                    storageFragment.requireActivity().getString(R.string.no_stock) + " items",
                                    storageFragment.requireActivity().getString(R.string.low_stock) + " items",
                                    storageFragment.requireActivity().getString(R.string.high_stock) + " items"
                                )
                                val choicesInitial = booleanArrayOf(false, false, false, false)
                                val alertDialog = MaterialAlertDialogBuilder(storageFragment.requireActivity())
                                    .setTitle("Select options")
                                    .setMultiChoiceItems(choices, choicesInitial, null)
                                    .setPositiveButton("Ok", null)
                                    .setNegativeButton("Close", null)
                                    .setCancelable(false)
                                    .create()
                                alertDialog.setOnShowListener {


                                    val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    val negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)


                                    positiveBtn.setOnClickListener {

                                        alertDialog.dismiss()

                                        val filterOptions = arrayListOf<Int>()

                                        alertDialog.listView.checkedItemPositions.forEach { key, isChecked ->
                                            if (key == 0 && isChecked) {
                                                filterOptions.add(StorageItemEntityValues.EXPIRED)
                                            } else if (key == 1 && isChecked) {
                                                filterOptions.add(StorageItemEntityValues.NO_STOCK)
                                            } else if (key == 2 && isChecked) {
                                                filterOptions.add(StorageItemEntityValues.LOW_STOCK)
                                            } else if (key == 3 && isChecked) {
                                                filterOptions.add(StorageItemEntityValues.HIGH_STOCK)
                                            }
                                        }

                                        val storageGroceryListActivity = Intent(storageFragment.requireContext(), StorageGroceryListActivity::class.java)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ACTION_TAG, StorageGroceryListActivity.ADD_MULTIPLE_PRODUCT_ACTION)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ADD_MULTIPLE_PRODUCT_CONDITION_TAG, filterOptions)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.STORAGE_NAME_TAG, storageEntity.name)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.STORAGE_TAG, storageEntity)


                                        storageFragment.startActivity(storageGroceryListActivity)


                                    }
                                    negativeBtn.setOnClickListener {
                                        alertDialog.dismiss()
                                    }
                                }
                                alertDialog.show()
                            }
                        }
                        true
                    }
                }

                R.id.storageImageView -> {

                    val storageEntity = storageEntities[adapterPosition].storageEntity
                    val imageUri = StorageUtil.getImageUriFromPath(view.context, ImageUtil.STORAGE_IMAGES_FINAL_LOCATION, storageEntity.imageName)
                    imageUri?.let {
                        storageFragment.zoomImageFromThumb(view, it)
                    }
                }
            }
            //startActivity(pantryStorageActivity
        }




    }

}

/**
 * storage recyclierview adapter for transferring storage items
 */
class StorageViewForTransferingItemsAdapter(val storageFragment: StorageFragment): RecyclerView.Adapter<StorageViewForTransferingItemsAdapter.ItemViewHolder>() {

    var storageEntities:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val storageItemForTransferingBinding = StorageItemForTransferingBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(storageItemForTransferingBinding)
        return itemViewHolder
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationEntity = storageEntities[position]
        holder.storageItemForTransferingBinding.storageEntityWithExtraInformation = pantryItemExpirationEntity
        holder.storageItemForTransferingBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {

        return storageEntities.size
    }
    inner class  ItemViewHolder(var storageItemForTransferingBinding: StorageItemForTransferingBinding): RecyclerView.ViewHolder(storageItemForTransferingBinding.root),View.OnClickListener{
        init {

            storageItemForTransferingBinding.storageItemParentLayout.setOnClickListener(this)
            storageItemForTransferingBinding.storageImageView.setOnClickListener(this)

        }

        override fun onClick(view: View?) {
            val storageEntity = storageEntities[adapterPosition].storageEntity

            when(view?.id){
                R.id.storageItemParentLayout -> {

                    storageFragment.showTransferStorageItemAlertDialog(storageEntity)
                }

                R.id.storageImageView -> {

                    val storageEntity = storageEntities[adapterPosition].storageEntity
                    val imageUri = StorageUtil.getImageUriFromPath(view.context, ImageUtil.STORAGE_IMAGES_FINAL_LOCATION, storageEntity.imageName)
                    imageUri?.let {
                        storageFragment.zoomImageFromThumb(view, it)
                    }
                }
            }
        }




    }

}

class StoragePerItemRecyclerviewViewAdapater(val storageFragment: StorageFragment): RecyclerView.Adapter<StoragePerItemRecyclerviewViewAdapater.ItemViewHolder>() {

    //var mStorageItemWithExpirations:List<StorageItemWithExpirations> = arrayListOf()
    var mStorageEntitiesWithExpirationsAndStorages:ArrayList<StorageItemWithExpirationsAndStorages>  = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val pantryItemLayoutBinding = StoragePerItemLayoutBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding, this)

        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val storageEntitiesWithExpirationsAndStorages = mStorageEntitiesWithExpirationsAndStorages[position]
        holder.storageItemLayoutBinding.storageItemWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStorages
        holder.storageItemLayoutBinding.executePendingBindings()
        holder.setChipClickListener()
        holder.setImageViewClickListener()
        holder.setMoreActionImageBtnClickListener()

    }

    override fun getItemCount(): Int {

        return mStorageEntitiesWithExpirationsAndStorages.size
    }

    inner class  ItemViewHolder(var storageItemLayoutBinding: StoragePerItemLayoutBinding, val storageRecyclerviewViewAdapater: StoragePerItemRecyclerviewViewAdapater): RecyclerView.ViewHolder(storageItemLayoutBinding.root),View.OnClickListener{
        fun setChipClickListener(){
            val flexboxChildCount = storageItemLayoutBinding.storageFlexboxLayout.childCount
            repeat(flexboxChildCount){ index->
                val child = storageItemLayoutBinding.storageFlexboxLayout.getChildAt(index)
                if(child is Chip){
                    child.setOnClickListener(this)
                }
            }




        }
        fun setImageViewClickListener(){
            storageItemLayoutBinding.storageImageView.setOnClickListener {
                val storageItemEntity = mStorageEntitiesWithExpirationsAndStorages[adapterPosition].storageItemEntity

                val imageUri = StorageUtil.getStorageItemImageUriFromPath(it.context, storageItemEntity.imageName)
                storageFragment.zoomImageFromThumb(it, imageUri!!)


//                val mainLayout = storageRecyclerviewViewAdapater.storageFragment.mDataBindingUtil.container
//                val imageContainer = dataBindingUtil.imageContainerIncludedLayout.imageContainer
//                val imageHolder = dataBindingUtil.imageContainerIncludedLayout.imageHolder
//                val animationDuration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
//
//                ImageUtil.zoomInImageFromThumb(selectedImageThumbnailView, imageUri!!, mainLayout,  imageContainer, imageHolder, animationDuration)
//                isViewingImage = true


            }
        }
        fun setMoreActionImageBtnClickListener(){
            storageItemLayoutBinding.moreActionImageBtn.setOnClickListener {
                val storageItemWithExpirationsAndStorages = mStorageEntitiesWithExpirationsAndStorages[adapterPosition]

                val storage = storageItemWithExpirationsAndStorages.storages[0].storageEntity
                val storageItemName = storageItemWithExpirationsAndStorages.storageItemEntity.name
                val storageItemUnit = storageItemWithExpirationsAndStorages.storageItemEntity.unit
                val storageItemImage = storageItemWithExpirationsAndStorages.storageItemEntity.imageName

                val popupMenu = PopupMenu(it.context, storageItemLayoutBinding.moreActionImageBtn)
                popupMenu.menuInflater.inflate(R.menu.storage_view_by_item_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener (object:PopupMenu.OnMenuItemClickListener{
                    override fun onMenuItemClick(item: MenuItem?): Boolean {

                        popupMenu.dismiss()
                        val storageGroceryListActivity = Intent(it.context, StorageGroceryListActivity::class.java)
                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ACTION_TAG, StorageGroceryListActivity.ADD_SINGLE_PRODUCT_ACTION)
                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_NAME_TAG, storageItemName)
                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_UNIT_TAG, storageItemUnit)
                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.IMAGE_NAME_TAG, storageItemImage)
                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.STORAGE_TAG, storage)

                        it.context.startActivity(storageGroceryListActivity)


                        return true
                    }

                })

                popupMenu.show()
            }
        }
        override fun onClick(view: View?) {

            val storageEntityWithStorageItemInformation = view!!.tag as StorageEntityWithStorageItemInformation
            storageFragment.openStorageActivity(storageEntityWithStorageItemInformation)




        }

    }


}

