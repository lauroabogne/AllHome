package com.example.allhome.storage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.data.entities.StorageItemWithExpirations
import com.example.allhome.databinding.ActivityStorageBinding
import com.example.allhome.databinding.StorageItemLayoutBinding
import com.example.allhome.databinding.StorageQuantityFilterBinding
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val ADD_ITEM_REQUEST_CODE = 1986;
        val UPDATE_ITEM_REQUEST_CODE = 1987

        val STORAGE_EXTRA_DATA_TAG = "STORAGE_EXTRA_DATA_TAG";
        val STORAGE_EXTRA_TAG = "STORAGE_EXTRA_TAG"
        val STORAGE_ITEM_UNIQUE_ID_TAG = "STORAGE_ITEM_UNIQUE_ID_TAG"

        val ADDED_NEW_ELEMENT = 0
        val UPDATED_NEW_ELEMENT = 1


    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mStorageEntity  = intent.getParcelableExtra(STORAGE_EXTRA_TAG)!!
        title = mStorageEntity.name

        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        mActivityPantryStorageBinding = DataBindingUtil.setContentView(this, R.layout.activity_storage)
        mActivityPantryStorageBinding.lifecycleOwner = this
        mActivityPantryStorageBinding.pantryStorageViewModel = mStorageViewModel


        mStorageViewModel.coroutineScope.launch {
            val pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, mStorageEntity.name)

            withContext(Main){

                val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }

        mActivityPantryStorageBinding.fab.setOnClickListener {
            val addPantryItemActivity = Intent(this, StorageAddItemActivity::class.java)
            addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, mStorageEntity.name)
            startActivityForResult(addPantryItemActivity, ADD_ITEM_REQUEST_CODE)
        }
        val pantryStorageRecyclerviewViewAdapater = StorageRecyclerviewViewAdapater(this)
        mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter = pantryStorageRecyclerviewViewAdapater
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.noFilterMenu -> {

                Toast.makeText(this, "NO FILTER MENU", Toast.LENGTH_SHORT).show()
            }
            R.id.stockWeightMenu -> {
                showStockWeightPopupForFilter()
            }
            R.id.quantityMenu -> {
                showFilterByQuantityPopup()
            }
            R.id.lastUpdateMenu -> {
                Toast.makeText(this, "lastUpdateMenu", Toast.LENGTH_SHORT).show()
            }
            R.id.expiredMenu -> {
                filterByExpiredItem()
            }
        }


        return super.onOptionsItemSelected(item)
    }
    fun showStockWeightPopupForFilter(){
        val choices = arrayOf(
            getString(R.string.no_stock) + " items",
            getString(R.string.low_stock) + " stock items",
            getString(R.string.high_stock) + " stock  items"
        )
        val choicesInitial = booleanArrayOf(false, false, false)
        val alertDialog =  MaterialAlertDialogBuilder(this)
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

                val filterOptions = arrayListOf<Int>()

                alertDialog.listView.checkedItemPositions.forEach { key, isChecked ->
                     if(key == 0 && isChecked){
                        filterOptions.add(StorageItemEntityValues.NO_STOCK)
                    }else if(key == 1 && isChecked){
                        filterOptions.add(StorageItemEntityValues.LOW_STOCK)
                    }else if(key == 2 && isChecked){
                        filterOptions.add(StorageItemEntityValues.HIGH_STOCK)
                    }
                }

                mStorageViewModel.coroutineScope.launch {

                    val pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByStockWeight(this@StorageActivity, mStorageEntity.name, filterOptions)

                    withContext(Main){

                        alertDialog.dismiss()

                        val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                        pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                        pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
                    }
                }

            }
            negativeBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        alertDialog.show()


    }
    fun filterByExpiredItem(){

        mStorageViewModel.coroutineScope.launch {

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            val pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterByExpiredItem(this@StorageActivity, mStorageEntity.name, currentDatetime)
            withContext(Main){
                 val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations
                pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
    }
    fun showFilterByQuantityPopup(){

        val storageQuanityFilterBinding = DataBindingUtil.inflate<StorageQuantityFilterBinding>(LayoutInflater.from(this), R.layout.storage_quantity_filter, null, false)
        val alertDialog =  MaterialAlertDialogBuilder(this)
            .setTitle("Filter by quantity")
            .setView(storageQuanityFilterBinding.root)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.setOnShowListener {

            val positiveBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeBtn = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            positiveBtn.setOnClickListener {


                var hasError = false
                var errorMessage = ""

                mStorageViewModel.coroutineScope.launch {

                    var pantryItemWithExpirations:ArrayList<StorageItemWithExpirations>? = null

                    if(storageQuanityFilterBinding.greaterThanRadioButton.isChecked){
                        val quantityString = storageQuanityFilterBinding.greaterThanEditText.text.toString()
                        if(quantityString.trim().isEmpty()){
                            hasError = true
                            errorMessage ="Please input quantity in greater than input"

                        }else{

                            pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterGreaterThan(this@StorageActivity, mStorageEntity.name, quantityString.toInt())

                        }

                    }else if(storageQuanityFilterBinding.lessThanRadioButton.isChecked){
                        val quantityString = storageQuanityFilterBinding.lessThanEditText.text.toString()
                        if(quantityString.trim().isEmpty()){
                            hasError = true
                            errorMessage ="Please input quantity in greater than input"

                        }else{
                            pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterLessThan(this@StorageActivity, mStorageEntity.name, quantityString.toInt())

                        }

                    }else if(storageQuanityFilterBinding.betweenRadioButton.isChecked){
                        val quantityFromString = storageQuanityFilterBinding.betweenFromInput.text.toString()
                        val quantityToString = storageQuanityFilterBinding.betweenToInput.text.toString()

                        if(quantityFromString.trim().isEmpty() || quantityToString.trim().isEmpty() ){
                            hasError = true
                            errorMessage ="Please input between quantity"

                        }else{

                            pantryItemWithExpirations = mStorageViewModel.getStorageItemWithExpirationsFilterBetween(this@StorageActivity, mStorageEntity.name, quantityFromString.toInt(), quantityToString.toInt())
                        }

                    }else{
                        hasError = true
                        errorMessage = "Please select option"
                    }

                    withContext(Main){

                        if(hasError){
                            Toast.makeText(this@StorageActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            return@withContext
                        }
                        alertDialog.dismiss()

                        val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                        pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = pantryItemWithExpirations!!
                        pantryStorageRecyclerviewViewAdapater.notifyDataSetChanged()

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
    fun quantityFilterCheckListener(storageQuanityFilterBinding: StorageQuantityFilterBinding){
        storageQuanityFilterBinding.greaterThanRadioButton.setOnCheckedChangeListener{ buttonView, isChecked->
            if(isChecked){

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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
    fun showSoftKeyboard(view: View){
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0
        )
    }

    fun hideKeyboard() {

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Handler(Looper.getMainLooper()).postDelayed({
            if(resultCode == Activity.RESULT_OK && requestCode == ADD_ITEM_REQUEST_CODE){

                val storageItemUniqueId = data!!.getStringExtra(STORAGE_ITEM_UNIQUE_ID_TAG)!!
                displayItem(storageItemUniqueId, ADDED_NEW_ELEMENT)

            }else if(resultCode == Activity.RESULT_OK && requestCode == UPDATE_ITEM_REQUEST_CODE){

                val storageItemUniqueId = data!!.getStringExtra(STORAGE_ITEM_UNIQUE_ID_TAG)!!
                displayItem(storageItemUniqueId, UPDATED_NEW_ELEMENT)
            }

        //}, 200)



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.storage_item_activity_menu, menu)

        /*menu?.findItem(R.id.appBarSearch)?.setOnActionExpandListener(object:MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                Log.e("MENU_ITEM","EXPANDED")
                Toast.makeText(this@StorageActivity,"MENU ITEM EXPANDED",Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {

                Toast.makeText(this@StorageActivity,"MENU ITEM NOT EXPANDED",Toast.LENGTH_SHORT).show()
                return true
            }

        })*/

        /*val searchView:SearchView = menu?.findItem(R.id.appBarSearch)?.actionView as SearchView
        searchView.queryHint = "Search item"*/
        return true
    }
    private fun displayItem(storageItemUniqueId: String, itemChangeType: Int){

        mStorageViewModel.coroutineScope.launch {
            val storageItemWithExpirations:List<StorageItemWithExpirations> = mStorageViewModel.getStorageItemWithExpirations(this@StorageActivity, mStorageEntity.name)
            val newItemIndex = storageItemWithExpirations.indexOfFirst { it.storageItemEntity.uniqueId == storageItemUniqueId }

            withContext(Main){

                val pantryStorageRecyclerviewViewAdapater = mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter as StorageRecyclerviewViewAdapater
                pantryStorageRecyclerviewViewAdapater.mStorageItemWithExpirations = storageItemWithExpirations


                if(itemChangeType == UPDATED_NEW_ELEMENT){
                    pantryStorageRecyclerviewViewAdapater.notifyItemChanged(newItemIndex)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    animateElement(newItemIndex, itemChangeType)

                }, 200)

                //animateElement(newItemIndex,itemChangeType)


            }
        }


    }
    private fun animateElement(indexOfNewItem: Int, itemChangeType: Int){

        val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        fadeInAnimation.duration = 500
        fadeInAnimation.fillAfter = true

        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if (itemChangeType == UPDATED_NEW_ELEMENT) {
                    mActivityPantryStorageBinding.pantryStorageRecyclerview.adapter?.notifyItemChanged(indexOfNewItem)
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })

        val firstVisibleItemPosition = ( mActivityPantryStorageBinding.pantryStorageRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val lastVisibleItemPosition = ( mActivityPantryStorageBinding.pantryStorageRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        if(indexOfNewItem >= firstVisibleItemPosition && indexOfNewItem <= lastVisibleItemPosition){

            val viewHolder = mActivityPantryStorageBinding.pantryStorageRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
            val itemViewHolder = viewHolder as StorageRecyclerviewViewAdapater.ItemViewHolder
            itemViewHolder.storageItemLayoutBinding.pantryItemParentLayout.startAnimation(fadeInAnimation)


        }else{


            mActivityPantryStorageBinding.pantryStorageRecyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var found = false
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val viewHolder = mActivityPantryStorageBinding.pantryStorageRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
                    if (viewHolder != null) {

                        val itemViewHolder = viewHolder as StorageRecyclerviewViewAdapater.ItemViewHolder
                        itemViewHolder.storageItemLayoutBinding.pantryItemParentLayout.startAnimation(fadeInAnimation)

                        found = true
                        mActivityPantryStorageBinding.pantryStorageRecyclerview.removeOnScrollListener(this)

                    }
                }

            })
            mActivityPantryStorageBinding.pantryStorageRecyclerview.scrollToPosition(indexOfNewItem)

        }
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
        val imageUri = StorageUtil.getStorageItemImageUriFromPath(it.context, storageItemWithExpirations.storageItemEntity.imageName)
        zoomImageFromThumb(it, imageUri!!)

    }



    class StorageRecyclerviewViewAdapater(val storageActivity: StorageActivity): RecyclerView.Adapter<StorageRecyclerviewViewAdapater.ItemViewHolder>(),OnItemRemovedListener {
        val mPantryStorageActivity = storageActivity
        var mStorageItemWithExpirations:List<StorageItemWithExpirations> = arrayListOf()

        /*val onItemRemovedListener = object : OnItemRemovedListener {
            override fun doneRemoving(index: Int) {
                this@PantryStorageRecyclerviewViewAdapater.notifyItemRemoved(index)
            }
        }*/
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val pantryItemLayoutBinding = StorageItemLayoutBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val storageItemWithExpirations = mStorageItemWithExpirations[position]
            holder.storageItemLayoutBinding.pantryItemWithExpirations = storageItemWithExpirations
            holder.storageItemLayoutBinding.storageImageView.setTag(storageItemWithExpirations)
            holder.storageItemLayoutBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {

            return mStorageItemWithExpirations.size
        }

        override fun doneRemoving(index: Int) {
            mPantryStorageActivity.mStorageViewModel.storageItemWithExpirations.removeAt(index)
            notifyItemRemoved(index)
        }


        inner class  ItemViewHolder(var storageItemLayoutBinding: StorageItemLayoutBinding, val storageRecyclerviewViewAdapater: StorageRecyclerviewViewAdapater): RecyclerView.ViewHolder(storageItemLayoutBinding.root),View.OnClickListener{
            init {
                storageItemLayoutBinding.moreActionImageBtn.setOnClickListener(this)
                storageItemLayoutBinding.storageImageView.setOnClickListener(mPantryStorageActivity.productImageClickListener)
                storageItemLayoutBinding.pantryItemParentLayout.setOnClickListener(this)



            }


            override fun onClick(view: View?) {

                when(view?.id){
                    R.id.moreActionImageBtn -> {
                        val popupMenu = PopupMenu(view!!.context, storageItemLayoutBinding.moreActionImageBtn)
                        popupMenu.menuInflater.inflate(R.menu.pantry_item_menu, popupMenu.menu)
                        popupMenu.setOnMenuItemClickListener(CustomPopupMenuItemCLickListener(view.context, this, adapterPosition, mPantryStorageActivity))
                        popupMenu.show()
                    }
                    R.id.pantryItemParentLayout -> {
                        val pantryItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPosition].storageItemEntity

                        val addPantryItemActivity = Intent(storageActivity, StorageAddItemActivity::class.java)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, storageActivity.mStorageEntity.name)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.ACTION_TAG, StorageAddItemActivity.UPDATE_RECORD_ACTION)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG, pantryItemEntity.uniqueId)
                        addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_NAME_TAG, pantryItemEntity.name)
                        storageActivity.startActivityForResult(addPantryItemActivity, UPDATE_ITEM_REQUEST_CODE)

                    }
                }

            }

        }


    }

}

interface OnItemRemovedListener{
    fun doneRemoving(index: Int)
}
 class CustomPopupMenuItemCLickListener(val context: Context, val itemViewHolder: StorageActivity.StorageRecyclerviewViewAdapater.ItemViewHolder, val adapterPostion: Int, val storageActivity: StorageActivity):PopupMenu.OnMenuItemClickListener{
    override fun onMenuItemClick(item: MenuItem?): Boolean {

        val storageItemWithExpirations = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion]
        val storageItemEntity =storageItemWithExpirations.storageItemEntity

        when(item?.itemId){

            R.id.pantryItemEditMenu -> {

                val pantryItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion].storageItemEntity

                val addPantryItemActivity = Intent(context, StorageAddItemActivity::class.java)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_NAME_TAG, storageActivity.mStorageEntity.name)
                addPantryItemActivity.putExtra(StorageAddItemActivity.ACTION_TAG, StorageAddItemActivity.UPDATE_RECORD_ACTION)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_UNIQUE_ID_TAG, pantryItemEntity.uniqueId)
                addPantryItemActivity.putExtra(StorageAddItemActivity.STORAGE_ITEM_NAME_TAG, pantryItemEntity.name)


                val pantryStorageActivity = context as StorageActivity
                pantryStorageActivity.startActivityForResult(addPantryItemActivity, StorageActivity.UPDATE_ITEM_REQUEST_CODE)


            }
            R.id.pantryItemMoveStorageMenu -> {

                val storageItemEntity = storageActivity.mStorageViewModel.storageItemWithExpirations[adapterPostion]
                val storageStorageListActiviy = Intent(context, StorageStogeListActivity::class.java)

                storageStorageListActiviy.putExtra(StorageFragment.STORAGE_ITEM_ENTITY_TAG, storageItemEntity)
                storageStorageListActiviy.putExtra(StorageFragment.STORAGE_ENTITY_TAG, storageActivity.mStorageEntity)


                val storageActivity = context as StorageActivity
                storageActivity.startActivity(storageStorageListActiviy)
            }
            R.id.pantryItemDeleteMenu -> {


                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val currentDatetime: String = simpleDateFormat.format(Date())
                var updatedAll = true


                storageActivity.mStorageViewModel.coroutineScope.launch {
                    val allHomeDatabase = AllHomeDatabase.getDatabase(storageActivity)

                    try {
                        allHomeDatabase.withTransaction {

                            val updatedStorageItemRecordCount = storageActivity.mStorageViewModel.updateItemAsDeleted(storageActivity, currentDatetime, storageItemEntity)
                            val updatedStorageDeliverItemCount = storageActivity.mStorageViewModel.updateStorageExpirationDateAsDeleted(storageActivity, currentDatetime, storageItemEntity)

                            if (updatedStorageItemRecordCount <= 0) {
                                throw Exception("Failed to update record")
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("error", ex.message.toString())
                        updatedAll = false
                    }

                    withContext(Main) {
                        if (!updatedAll) {
                            Toast.makeText(storageActivity, "Failed to update record", Toast.LENGTH_SHORT).show()
                            return@withContext
                        }


                        itemViewHolder.storageRecyclerviewViewAdapater.doneRemoving(adapterPostion)


                    }

                }


            }
            R.id.pantryAddToGroceryListMenu -> {

                val storageGroceryListActivity = Intent(context, StorageGroceryListActivity::class.java)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ACTION_TAG, StorageGroceryListActivity.ADD_SINGLE_PRODUCT_ACTION)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_NAME_TAG, storageItemEntity.name)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.ITEM_UNIT_TAG, storageItemEntity.unit)
                storageGroceryListActivity.putExtra(StorageGroceryListActivity.IMAGE_NAME_TAG, storageItemEntity.imageName)

                val storageActivity = context as StorageActivity
                storageActivity.startActivity(storageGroceryListActivity)

            }
        }
        return true
    }

}
