package com.example.allhome.storage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.data.entities.StorageItemWithExpirations
import com.example.allhome.databinding.ActivityStorageBinding
import com.example.allhome.databinding.PantryItemLayoutBinding
import com.example.allhome.databinding.PantrySimpleExpirationLayoutBinding
import com.example.allhome.storage.viewmodel.StorageViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class StorageActivity : AppCompatActivity() {

    lateinit var mStorageViewModel:StorageViewModel
    lateinit var mActivityPantryStorageBinding:ActivityStorageBinding

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null

    //var mStorage:String? = null
    lateinit var mStorageEntity:StorageEntity


    companion object{
        val UPDATE_REQUEST_CODE = 1986
        val STORAGE_EXTRA_DATA_TAG = "STORAGE_EXTRA_DATA_TAG";
        val STORAGE_EXTRA_TAG = "STORAGE_EXTRA_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mStorageEntity  = intent.getParcelableExtra(STORAGE_EXTRA_TAG)!!
        title = mStorageEntity.name

        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        mActivityPantryStorageBinding = DataBindingUtil.setContentView(this,R.layout.activity_storage)
        mActivityPantryStorageBinding.lifecycleOwner = this
        mActivityPantryStorageBinding.pantryStorageViewModel = mStorageViewModel


        mStorageViewModel.coroutineScope.launch {
            val pantryItemWithExpirations = mStorageViewModel.getPatryItemWithExpirations(this@StorageActivity,mStorageEntity.name)

            withContext(Main){

                val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as PantryStorageRecyclerviewViewAdapater
                pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }

        mActivityPantryStorageBinding.fab.setOnClickListener {
            val addPantryItemActivity = Intent(this, PantryAddItemActivity::class.java)
            addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_NAME_TAG,mStorageEntity.name)
            startActivity(addPantryItemActivity)
        }
        val pantryStorageRecyclerviewViewAdapater = PantryStorageRecyclerviewViewAdapater(this)
        mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
    private fun zoomImageFromThumb(thumbView: View, imageUri: Uri) {

        // The system "short" animation time duration, in milliseconds. This
        // duration is ideal for subtle animations or animations that occur
        // very frequently.
        val shortAnimationDuration: Int = 200

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        val expandedImageView: ImageView = findViewById(R.id.expanded_image)
        expandedImageView.setImageURI(imageUri)

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
        findViewById<View>(R.id.container).getGlobalVisibleRect(finalBoundsInt, globalOffset)
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
    val productImageClickListener = View.OnClickListener {
        val storageItemWithExpirations: StorageItemWithExpirations = it.tag as StorageItemWithExpirations
        val imageUri = StorageUtil.getStorageItemImageUriFromPath(it.context,storageItemWithExpirations.storageItemEntity.imageName)
        zoomImageFromThumb(it, imageUri!!)

    }



    class PantryStorageRecyclerviewViewAdapater(val storageActivity:StorageActivity): RecyclerView.Adapter<PantryStorageRecyclerviewViewAdapater.ItemViewHolder>(),OnItemRemovedListener {
        val mPantryStorageActivity = storageActivity
        var mStorageItemWithExpirations:List<StorageItemWithExpirations> = arrayListOf()

        /*val onItemRemovedListener = object : OnItemRemovedListener {
            override fun doneRemoving(index: Int) {
                this@PantryStorageRecyclerviewViewAdapater.notifyItemRemoved(index)
            }
        }*/
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val pantryItemLayoutBinding = PantryItemLayoutBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding,this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val pantryItemWithExpirations = mStorageItemWithExpirations[position]
            holder.pantryItemLayoutBinding.pantryItemWithExpirations = pantryItemWithExpirations
            holder.pantryItemLayoutBinding.storageImageView.setTag(pantryItemWithExpirations)
            holder.pantryItemLayoutBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {

            return mStorageItemWithExpirations.size
        }

        override fun doneRemoving(index: Int) {
            mPantryStorageActivity.mStorageViewModel.storageItemWithExpirations.removeAt(index)
            notifyItemRemoved(index)
        }


        inner class  ItemViewHolder(var pantryItemLayoutBinding: PantryItemLayoutBinding,val pantryStorageRecyclerviewViewAdapater:PantryStorageRecyclerviewViewAdapater): RecyclerView.ViewHolder(pantryItemLayoutBinding.root),View.OnClickListener{
            init {
                pantryItemLayoutBinding.moreActionImageBtn.setOnClickListener(this)
                pantryItemLayoutBinding.storageImageView.setOnClickListener(mPantryStorageActivity.productImageClickListener)
                pantryItemLayoutBinding.pantryItemParentLayout.setOnClickListener(this)
            //pantryItemLayoutBinding.storageItemNameTextView.setOnClickListener(this)


            }


            override fun onClick(view: View?) {

                when(view?.id){
                    R.id.moreActionImageBtn->{
                        val popupMenu = PopupMenu(view!!.context,pantryItemLayoutBinding.moreActionImageBtn)
                        popupMenu.menuInflater.inflate(R.menu.pantry_item_menu, popupMenu.menu)
                        popupMenu.setOnMenuItemClickListener(CustomPopupMenuItemCLickListener(view.context,this, adapterPosition,mPantryStorageActivity))
                        popupMenu.show()
                    }
                    R.id.pantryItemParentLayout->{
                        val pantryItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPosition].storageItemEntity

                        val addPantryItemActivity = Intent(storageActivity, PantryAddItemActivity::class.java)
                        addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_NAME_TAG,storageActivity.mStorageEntity.name)
                        addPantryItemActivity.putExtra(PantryAddItemActivity.ACTION_TAG,PantryAddItemActivity.UPDATE_RECORD_ACTION)
                        addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG,pantryItemEntity.uniqueId)
                        addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_ITEM_NAME_TAG,pantryItemEntity.name)
                        storageActivity.startActivityForResult(addPantryItemActivity,PantryAddItemActivity.UPDATE_RECORD_ACTION)
                    }
                }

            }

        }


    }


}

interface OnItemRemovedListener{
    fun doneRemoving(index:Int)
}
 class CustomPopupMenuItemCLickListener(val context: Context, val itemViewHolder: StorageActivity.PantryStorageRecyclerviewViewAdapater.ItemViewHolder, val adapterPostion:Int, val storageActivity:StorageActivity):PopupMenu.OnMenuItemClickListener{
    override fun onMenuItemClick(item: MenuItem?): Boolean {

        val storageItemWithExpirations = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion]
        val storageItemEntity =storageItemWithExpirations.storageItemEntity

        when(item?.itemId){

            R.id.pantryItemEditMenu->{

                val pantryItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion].storageItemEntity

                val addPantryItemActivity = Intent(context, PantryAddItemActivity::class.java)
                addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_NAME_TAG,storageActivity.mStorageEntity.name)
                addPantryItemActivity.putExtra(PantryAddItemActivity.ACTION_TAG,PantryAddItemActivity.UPDATE_RECORD_ACTION)
                addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG,pantryItemEntity.uniqueId)
                addPantryItemActivity.putExtra(PantryAddItemActivity.STORAGE_ITEM_NAME_TAG,pantryItemEntity.name)


                val pantryStorageActivity = context as StorageActivity
                pantryStorageActivity.startActivityForResult(addPantryItemActivity,PantryAddItemActivity.UPDATE_RECORD_ACTION)


            }
            R.id.pantryItemMoveStorageMenu->{

                val storageItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion]
                val storageStorageListActiviy = Intent(context,StorageStogeListActivity::class.java)

                storageStorageListActiviy.putExtra(StorageFragment.STORAGE_ITEM_ENTITY_TAG,storageItemEntity)
                storageStorageListActiviy.putExtra(StorageFragment.STORAGE_ENTITY_TAG,storageActivity.mStorageEntity)




                val storageActivity = context as StorageActivity
                storageActivity.startActivity(storageStorageListActiviy)
            }
            R.id.pantryItemDeleteMenu->{



                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val currentDatetime: String = simpleDateFormat.format(Date())
                var updatedAll = true


                storageActivity.mStorageViewModel.coroutineScope.launch {
                    val allHomeDatabase = AllHomeDatabase.getDatabase(storageActivity)

                    try{
                        allHomeDatabase.withTransaction {

                            val updatedStorageItemRecordCount = storageActivity.mStorageViewModel.updateItemAsDeleted(storageActivity,currentDatetime,storageItemEntity)
                            val updatedStorageDeliverItemCount =  storageActivity.mStorageViewModel.updateStorageExpirationDateAsDeleted(storageActivity,currentDatetime, storageItemEntity)

                            if(updatedStorageItemRecordCount <=0 || updatedStorageDeliverItemCount<=0){
                                throw Exception("Failed to update record")
                            }
                        }
                    }catch (ex:Exception){
                        Log.e("error",ex.message.toString())
                        updatedAll = false
                    }

                    withContext(Main){
                        if(!updatedAll){
                            Toast.makeText(storageActivity,"Failed to update record",Toast.LENGTH_SHORT).show()
                            return@withContext
                        }


                        itemViewHolder.pantryStorageRecyclerviewViewAdapater.doneRemoving(adapterPostion)



                    }

                }


            }
            R.id.pantryAddToGroceryListMenu->{

                val storageGroceryListActivity = Intent(context, StorageGroceryListActivity::class.java)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ACTION_TAG,StorageGroceryListActivity.ADD_SINGLE_PRODUCT_ACTION)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_NAME_TAG,storageItemEntity.name)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_UNIT_TAG,storageItemEntity.unit)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.IMAGE_NAME_TAG,storageItemEntity.imageName)

                val storageActivity = context as StorageActivity
                storageActivity.startActivity(storageGroceryListActivity)

            }
        }
        return true
    }

}

