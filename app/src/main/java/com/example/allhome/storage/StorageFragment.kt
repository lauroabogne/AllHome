package com.example.allhome.storage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.*
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StorageFragment : Fragment() {
    private lateinit var mStorageViewModel: StorageViewModel
    private lateinit var mDataBindingUtil: FragmentStorageBinding

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null
    private var mAction = STORAGE_VIEWING_ACTION

    private lateinit var mStorageItemWithExpirationsToTransfer:StorageItemWithExpirations
    lateinit var mStorageEntityOrigin:StorageEntity
    var mViewing = VIEW_BY_STORAGE

    companion object{
        const val ACTION_TAG = "ACTION_TAG"
        const val STORAGE_ENTITY_TAG = "STORAGE_ENTITY_TAG"
        const val STORAGE_ITEM_ENTITY_TAG = "STORAGE_ITEM_ENTITY_TAG"
        const val STORAGE_VIEWING_ACTION = 1
        const val STORAGE_TRASFERING_ITEM_ACTION = 2

        const val VIEW_BY_STORAGE = 1
        const val  VIEW_PER_PRODUCT = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

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
            }
        }


        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_storage, container, false)
        mDataBindingUtil.lifecycleOwner = this


        mDataBindingUtil.fab.setOnClickListener{
            val createStorageActivity = Intent(this.context, CreateStorageActivity::class.java)
            startActivity(createStorageActivity)
        }

        mDataBindingUtil.storageTabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if(tab?.position == 0){
                    Toast.makeText(this@StorageFragment.requireContext(),"VIEW BY STORAGE",Toast.LENGTH_SHORT).show()

                    mDataBindingUtil.storageStorageRecyclerview.adapter = StorageViewAdapter(this@StorageFragment) as RecyclerView.Adapter<RecyclerView.ViewHolder>
                    getItemViewByStorage()
                }else if(tab?.position == 1){

                    mDataBindingUtil.storageStorageRecyclerview.adapter =  StoragePerItemRecyclerviewViewAdapater() as RecyclerView.Adapter<RecyclerView.ViewHolder>

                    getItemViewByItem()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        var storageViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>?

        //RecyclerView.ViewHolder
        if(mAction == STORAGE_VIEWING_ACTION && mViewing == VIEW_BY_STORAGE){
            storageViewAdapter = StorageViewAdapter(this) as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter

            getItemViewByStorage()


        }else if(mAction == STORAGE_TRASFERING_ITEM_ACTION && mViewing == VIEW_BY_STORAGE){
            storageViewAdapter = StorageViewForTransferingItemsAdapter(this)  as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter
            getItemViewByStorage()

        }else if(mAction == STORAGE_VIEWING_ACTION && mViewing == VIEW_PER_PRODUCT){

            storageViewAdapter = StoragePerItemRecyclerviewViewAdapater() as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter

            getItemViewByItem()
        }


        //StoragePerItemRecyclerviewViewAdapater



        return mDataBindingUtil.root
    }

    fun getItemViewByStorage(){
        mStorageViewModel.coroutineScope.launch {

            if(mAction == STORAGE_VIEWING_ACTION){
                mStorageViewModel.storageEntitiesWithExtraInformation =  mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation

            }else{
                mStorageViewModel.storageEntitiesWithExtraInformation = mStorageViewModel.getAllStorageExceptSome(this@StorageFragment.requireContext(),arrayListOf(mStorageEntityOrigin!!.uniqueId))
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation
            }

            withContext(Main){

                if(mAction == STORAGE_VIEWING_ACTION){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).notifyDataSetChanged()
                }else{
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).notifyDataSetChanged()
                }


            }
        }
    }
    fun getItemViewByItem(){
        mStorageViewModel.coroutineScope.launch {

            val storageEntitiesWithExpirationsAndStoragesInnerScope = mStorageViewModel.getStorageItemWithExpirationsWithTotalQuantity(this@StorageFragment.requireContext())
            mStorageViewModel.storageEntitiesWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStoragesInnerScope

            withContext(Main){

                Log.e("DATA_COUNT",mStorageViewModel.storageEntitiesWithExpirationsAndStorages.size.toString())
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).mStorageEntitiesWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStoragesInnerScope
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StoragePerItemRecyclerviewViewAdapater).notifyDataSetChanged()

                /*if(mAction == STORAGE_VIEWING_ACTION){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).notifyDataSetChanged()
                }else{
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).notifyDataSetChanged()
                }*/


            }
        }
    }

    fun zoomImageFromThumb(thumbView: View, imageUri: Uri) {

        // The system "short" animation time duration, in milliseconds. This
        // duration is ideal for subtle animations or animations that occur
        // very frequently.
        val shortAnimationDuration: Int = 200

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        val expandedImageView: ImageView? = activity?.findViewById(R.id.expanded_image)
        expandedImageView!!.setImageURI(imageUri)

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        activity?.findViewById<View>(R.id.container)?.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }
    fun showTransferStorageItemAlertDialog(storageEntity: StorageEntity){

        val choices = arrayOf(
            "Merge items",
            "Replace existing items"
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
                    mergeStorageItem(storageEntity)
                }else if(checkedItemPosition == 1){
                    replaceStorageItem(storageEntity)
                }
                alertDialog.dismiss()
            }
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        alertDialog.show()

    }
    @Throws(Exception::class)
    fun replaceStorageItem(distinationStorageEntity: StorageEntity){


        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mStorageViewModel.coroutineScope.launch {

            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext());
            val storageItemEntity = mStorageViewModel.getItemByNameAndUnitAndStorage(this@StorageFragment.requireContext(),mStorageItemWithExpirationsToTransfer.storageItemEntity.name,mStorageItemWithExpirationsToTransfer.storageItemEntity.unit,distinationStorageEntity.name)
            var movedSuccessfully = true
            try{
                allHomeDatabase.withTransaction {

                    storageItemEntity?.let{

                        mStorageViewModel.updateItemAsDeleted(this@StorageFragment.requireContext(),currentDatetime,it)
                    }

                    insertStorageItemThanInSelectedStorage(distinationStorageEntity)

                }
            }catch (ex:java.lang.Exception){
                movedSuccessfully = false
            }

            withContext(Main){
                if(movedSuccessfully){
                    Toast.makeText(this@StorageFragment.requireContext(),"Moved successfully",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@StorageFragment.requireContext(),"Failed to move item",Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
    fun  mergeStorageItem(distinationStorageEntity: StorageEntity){
        val name = mStorageItemWithExpirationsToTransfer.storageItemEntity.name
        val unit =   mStorageItemWithExpirationsToTransfer.storageItemEntity.unit

        mStorageViewModel.coroutineScope.launch {

            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageFragment.requireContext());
            val storageItemEntity = mStorageViewModel.getItemByNameAndUnitAndStorage(this@StorageFragment.requireContext(),name,unit,distinationStorageEntity.name)
            var movedSuccessfully = true
            try{
                allHomeDatabase.withTransaction {
                    storageItemEntity?.let {
                        insertStorageItemThatExistsInSelectedStorage(it)
                    }?:run {
                        // item not exists just insert
                        insertStorageItemThanInSelectedStorage(distinationStorageEntity)
                    }
                    /**
                     * @toDo Delete storage item from source storage
                     */
                }
            }catch (ex:java.lang.Exception){
                movedSuccessfully = false
            }

            withContext(Main){
                if(movedSuccessfully){
                    Toast.makeText(this@StorageFragment.requireContext(),"Moved successfully",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@StorageFragment.requireContext(),"Failed to move item",Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
    @Throws(Exception::class)
    private suspend fun insertStorageItemThanInSelectedStorage(distinationStorageEntity: StorageEntity) {

        // update storage item as deleted in origin storage
        val updateAsDeletedAffectedRowCount = mStorageViewModel.updateItemAsDeleted(this.requireContext(),mStorageItemWithExpirationsToTransfer.storageItemEntity.modified,mStorageItemWithExpirationsToTransfer.storageItemEntity.uniqueId)

        if(updateAsDeletedAffectedRowCount <=0){

            throw Exception("Failed to move item")
        }


        var itemUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val newStorageEntity = mStorageItemWithExpirationsToTransfer.storageItemEntity
        newStorageEntity.uniqueId = itemUniqueID
        newStorageEntity.storage = distinationStorageEntity.name
        newStorageEntity.created = currentDatetime
        newStorageEntity.modified = currentDatetime


        val storageItemId = mStorageViewModel.saveStorageItemEntity(this@StorageFragment.requireContext(),newStorageEntity)

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
    private suspend fun insertStorageItemThatExistsInSelectedStorage(distinationStorageItemEntity:StorageItemEntity){


        // update storage item as deleted in origin storage
        val updateAsDeletedAffectedRowCount = mStorageViewModel.updateItemAsDeleted(this.requireContext(),mStorageItemWithExpirationsToTransfer.storageItemEntity.modified,mStorageItemWithExpirationsToTransfer.storageItemEntity.uniqueId)

        if(updateAsDeletedAffectedRowCount <=0){
            throw Exception("Failed to move item")
        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        // get expiration of storage items
        val expirations = mStorageViewModel.getStorageItemsExpiratinsByStorageUniquedIdItemNameAndCreated(this.requireContext(),distinationStorageItemEntity.uniqueId,distinationStorageItemEntity.name,distinationStorageItemEntity.modified)
        // merge expirations date
        val mergeExpirationDates = mergeExpirationDates(mStorageItemWithExpirationsToTransfer.expirations , expirations)

        val assembledDistinationStorageItemEntity = assembleNewStorageItemEntity(distinationStorageItemEntity,currentDatetime)
        val affectedRowCount = mStorageViewModel.updateStorageItemEntity(this@StorageFragment.requireContext(),assembledDistinationStorageItemEntity)

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

    private fun assembleNewStorageItemEntity(distinationStorageItemEntity:StorageItemEntity,currentDate:String):StorageItemEntity{
        val stockWeight = mergeStockWeight(mStorageItemWithExpirationsToTransfer.storageItemEntity,distinationStorageItemEntity)
        val quantity = mStorageItemWithExpirationsToTransfer.storageItemEntity.quantity + distinationStorageItemEntity.quantity

        distinationStorageItemEntity.stockWeight = stockWeight
        distinationStorageItemEntity.quantity = quantity
        distinationStorageItemEntity.modified = currentDate
        return distinationStorageItemEntity
    }

    private fun mergeStockWeight(storageItemEntity1:StorageItemEntity,storageItemEntity2:StorageItemEntity):Int{
        if(storageItemEntity1.stockWeight > storageItemEntity2.stockWeight){
            return storageItemEntity1.stockWeight
        }else{
            return storageItemEntity2.stockWeight
        }
    }
    private fun mergeExpirationDates(expirations1:List<StorageItemExpirationEntity>,expirations2:List<StorageItemExpirationEntity>):List<StorageItemExpirationEntity>{
        return (expirations1 + expirations2).distinctBy {
            it.expirationDate
        }
    }
}

/**
 * storage recyclierview adapater for viewing storage items
 */
class StorageViewAdapter(val storageFragment:StorageFragment): RecyclerView.Adapter<StorageViewAdapter.ItemViewHolder>() {

    var storageEntities:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val storageItemBinding = StorageItemBinding.inflate(layoutInflater,parent,false)
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
                R.id.storageItemParentLayout->{


                    val storageActivity = Intent(view!!.context, StorageActivity::class.java)
                    storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG,storageEntity.name)
                    storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_TAG,storageEntity)
                    storageFragment.requireActivity().startActivity(storageActivity)
                }

                R.id.moreActionImageView -> {

                    val popupMenu = PopupMenu(view.context, view)
                    popupMenu.menuInflater.inflate(R.menu.storage_item_menu, popupMenu.menu)
                    popupMenu.show()

                    popupMenu.setOnMenuItemClickListener {
                        val storageEntity = storageEntities[adapterPosition].storageEntity

                        when (it.itemId) {
                            R.id.viewInformationMenu -> {
                                val createStorageActivity = Intent(view!!.context, CreateStorageActivity::class.java)
                                createStorageActivity.putExtra(CreateStorageActivity.ACTION_TAG, CreateStorageActivity.UPDATE_RECORD_ACTION)
                                createStorageActivity.putExtra(CreateStorageActivity.STORAGE_UNIQUE_ID_TAG, storageEntity.uniqueId)
                                storageFragment.requireActivity().startActivity(createStorageActivity)

                            }
                            R.id.viewItemsMenu -> {

                                val storageActivity = Intent(view!!.context, StorageActivity::class.java)
                                storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG, storageEntity.name)
                                storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_TAG,storageEntity)
                                storageFragment.requireActivity().startActivity(storageActivity)
                            }
                            R.id.deleteStorageMenu -> {

                            }
                            R.id.addToGroceryListMenu -> {
                                
                                val choices = arrayOf(
                                        storageFragment.requireActivity().getString(R.string.expired_stock)+" items",
                                        storageFragment.requireActivity().getString(R.string.no_stock)+" items",
                                        storageFragment.requireActivity().getString(R.string.low_stock)+" items",
                                        storageFragment.requireActivity().getString(R.string.high_stock)+" items"
                                )
                                val choicesInitial = booleanArrayOf(false,false, false, false)
                                val alertDialog =  MaterialAlertDialogBuilder(storageFragment.requireActivity())
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
                                            if(key == 0 && isChecked){
                                                filterOptions.add(StorageItemEntityValues.EXPIRED)
                                            }else if(key == 1 && isChecked){
                                                filterOptions.add(StorageItemEntityValues.NO_STOCK)
                                            }else if(key == 2 && isChecked){
                                                filterOptions.add(StorageItemEntityValues.LOW_STOCK)
                                            }else if(key == 3 && isChecked){
                                                filterOptions.add(StorageItemEntityValues.HIGH_STOCK)
                                            }
                                        }

                                        val storageGroceryListActivity = Intent(storageFragment.requireContext(), StorageGroceryListActivity::class.java)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ACTION_TAG,StorageGroceryListActivity.ADD_MULTIPLE_PRODUCT_ACTION)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.ADD_MULTIPLE_PRODUCT_CONDITION_TAG,filterOptions)
                                        storageGroceryListActivity.putExtra(StorageGroceryListActivity.STORAGE_NAME_TAG,storageEntity.name)

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

                R.id.storageImageView->{

                    val storageEntity = storageEntities[adapterPosition].storageEntity
                    val imageUri = StorageUtil.getImageUriFromPath(view.context,StorageUtil.STORAGE_IMAGES_FINAL_LOCATION,storageEntity.imageName)
                    imageUri?.let{
                        storageFragment.zoomImageFromThumb(view,it)
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
class StorageViewForTransferingItemsAdapter(val storageFragment:StorageFragment): RecyclerView.Adapter<StorageViewForTransferingItemsAdapter.ItemViewHolder>() {

    var storageEntities:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val storageItemForTransferingBinding = StorageItemForTransferingBinding.inflate(layoutInflater,parent,false)
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
                R.id.storageItemParentLayout->{

                    storageFragment.showTransferStorageItemAlertDialog(storageEntity)
                    //Toast.makeText(view.context,"test",Toast.LENGTH_SHORT).show()
                    /*val storageActivity = Intent(view!!.context, StorageActivity::class.java)
                    storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG,storageEntity.name)
                    storageFragment.requireActivity().startActivity(storageActivity)*/
                }

                R.id.storageImageView->{

                    val storageEntity = storageEntities[adapterPosition].storageEntity
                    val imageUri = StorageUtil.getImageUriFromPath(view.context,StorageUtil.STORAGE_IMAGES_FINAL_LOCATION,storageEntity.imageName)
                    imageUri?.let{
                        storageFragment.zoomImageFromThumb(view,it)
                    }
                }
            }
            //startActivity(pantryStorageActivity
        }




    }

}

class StoragePerItemRecyclerviewViewAdapater(): RecyclerView.Adapter<StoragePerItemRecyclerviewViewAdapater.ItemViewHolder>() {

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

    }

    override fun getItemCount(): Int {

        return mStorageEntitiesWithExpirationsAndStorages.size
    }




    inner class  ItemViewHolder(var storageItemLayoutBinding: StoragePerItemLayoutBinding, val storageRecyclerviewViewAdapater: StoragePerItemRecyclerviewViewAdapater): RecyclerView.ViewHolder(storageItemLayoutBinding.root),View.OnClickListener{
        init {


        }


        override fun onClick(view: View?) {


        }

    }


}
