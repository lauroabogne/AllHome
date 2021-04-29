package com.example.allhome.storage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
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
import com.example.allhome.R
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.*
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    companion object{
        const val ACTION_TAG = "ACTION_TAG"
        const val STORAGE_ITEM_ENTITY_TAG = "STORAGE_ITEM_ENTITY_TAG"
        const val STORAGE_VIEWING_ACTION = 1
        const val STORAGE_TRASFERING_ITEM_ACTION = 2
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
                    mStorageViewModel.storageItemEntity = requireArguments().getParcelable<StorageItemEntity>(STORAGE_ITEM_ENTITY_TAG)
                }
            }
        }


        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_storage, container, false)
        mDataBindingUtil.lifecycleOwner = this


        mDataBindingUtil.fab.setOnClickListener{
            val createStorageActivity = Intent(this.context, CreateStorageActivity::class.java)
            startActivity(createStorageActivity)
        }


        var storageViewAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>?

        //RecyclerView.ViewHolder
        if(mAction == STORAGE_VIEWING_ACTION){
            storageViewAdapter = StorageViewAdapter(this) as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter

        }else{
            storageViewAdapter = StorageViewForTransferingItemsAdapter(this)  as RecyclerView.Adapter<RecyclerView.ViewHolder>
            mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter
        }


        mStorageViewModel.coroutineScope.launch {
            mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())

            if(mAction == STORAGE_VIEWING_ACTION){
                (storageViewAdapter as StorageViewAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation
            }else{
                (storageViewAdapter as StorageViewForTransferingItemsAdapter).storageEntities =  mStorageViewModel.storageEntitiesWithExtraInformation
            }
            //storageViewAdapter.storageEntities = mStorageViewModel.storageEntitiesWithExtraInformation
            withContext(Main){

                if(mAction == STORAGE_VIEWING_ACTION){
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).notifyDataSetChanged()
                }else{
                    (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewForTransferingItemsAdapter).notifyDataSetChanged()
                }


            }
        }

        return mDataBindingUtil.root
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

    fun transferStorageItem( storageEntity: StorageEntity){
        Toast.makeText(this.requireContext(),"Toast test",Toast.LENGTH_SHORT).show()
        Log.e("DATA",storageEntity.toString())
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

                    storageFragment.transferStorageItem(storageEntity)
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