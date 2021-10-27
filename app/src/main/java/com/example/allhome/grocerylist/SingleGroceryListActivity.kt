package com.example.allhome.grocerylist

import android.animation.*
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityValues
import com.example.allhome.databinding.ActivitySingleGroceryListBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.grocerylist.viewmodel_factory.GroceryListViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import kotlin.collections.HashSet


class SingleGroceryListActivity : AppCompatActivity() {

    lateinit var mGroceryListViewModel: GroceryListViewModel
    lateinit var dataBindingUtil: ActivitySingleGroceryListBinding
    lateinit var mItemTouchHelper: ItemTouchHelper
    var groceryListUniqueId: String = ""

    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private var shortAnimationDuration: Int = 0

    var mMenu:Menu? = null

    companion object {
        val ADD_ITEM_REQUEST = 1
        val UPDATE_ITEM_REQUEST = 2
        val REQUEST_IMAGE_CAPTURE = 3
        val REQUEST_PICK_IMAGE = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("ACTION","onCreate")
        setContentView(R.layout.activity_single_grocery_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        intent.getStringExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
            groceryListUniqueId = it
        }

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        // create AddGroceryListItemActivityViewModel using AddGroceryListItemActivityViewModelFactory
        val addGroceryListItemActivityViewModelFactory = GroceryListViewModelFactory(null, null)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(
            GroceryListViewModel::class.java
        )


        //Bind data
        dataBindingUtil = DataBindingUtil.setContentView<ActivitySingleGroceryListBinding>(
            this, R.layout.activity_single_grocery_list
        ).apply {
            this.lifecycleOwner = this@SingleGroceryListActivity
            this.groceryListViewModel = mGroceryListViewModel


        }

        mGroceryListViewModel.selectedGroceryList.observe(this, Observer {
            supportActionBar?.title = it.name


            if (it.viewingType == GroceryListViewModel.GROUP_BY_CATEGORY) {

                mGroceryListViewModel.coroutineScope.launch {

                    mGroceryListViewModel.groupByCategory()
                    withContext(Main) {
                        mGroceryListViewModel.sortingAndGrouping.value = GroceryListViewModel.GROUP_BY_CATEGORY
                        dataBindingUtil.groceryItemRecyclerview.adapter?.notifyDataSetChanged()

                    }
                }
            }


        })



        dataBindingUtil.fab.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AddGroceryListItemActivity::class.java)
            intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
            startActivityForResult(intent, ADD_ITEM_REQUEST)

        })

        dataBindingUtil.swipeRefresh.isEnabled = false
        dataBindingUtil.swipeRefresh.setOnRefreshListener {
            iniItems()
            dataBindingUtil.swipeRefresh.isRefreshing = false
        }


        iniItems()

    }


    private fun iniItems(){
        val groceryItemRecyclerViewAdapter = GroceryItemRecyclerViewAdapter(this, productImageClickListener)

        dataBindingUtil.groceryItemRecyclerview.adapter = groceryItemRecyclerViewAdapter


        mGroceryListViewModel.coroutineScope.launch {
            mGroceryListViewModel.setSelectedGroceryList(this@SingleGroceryListActivity, groceryListUniqueId)
        }



        if (mGroceryListViewModel.selectedGroceryListEntity == null) {

            mGroceryListViewModel.coroutineScope.launch {
                mGroceryListViewModel.setSelectedGroceryList(this@SingleGroceryListActivity, groceryListUniqueId)


            }
        }
        if (mGroceryListViewModel.selectedGroceryListItemList.isNullOrEmpty()) {

            mGroceryListViewModel.coroutineScope.launch {
                val groceryItemEntities = mGroceryListViewModel.getGroceryItems(this@SingleGroceryListActivity, groceryListUniqueId,GroceryItemEntityValues.ACTIVE_STATUS)
                mGroceryListViewModel.separateBougthItems(groceryItemEntities)
                mGroceryListViewModel.mergeToBuyAndBoughtItems(mGroceryListViewModel.toBuyGroceryItems, mGroceryListViewModel.boughtGroceryItems)
                groceryItemRecyclerViewAdapter.mGroceryItems = mGroceryListViewModel.selectedGroceryListItemList

                withContext(Main) {

                    groceryItemRecyclerViewAdapter.notifyDataSetChanged()
                }
            }
        } else {
            groceryItemRecyclerViewAdapter.mGroceryItems = mGroceryListViewModel.selectedGroceryListItemList
            groceryItemRecyclerViewAdapter.notifyDataSetChanged()
        }

        mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {

                val itemViewHolder: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder
                val groceryItemEntity = itemViewHolder.groceryListItemBinding.groceryItemEntity

                if (groceryItemEntity!!.bought == 1 || groceryItemEntity.forCategoryDivider) {
                    return ItemTouchHelper.Callback.makeMovementFlags(
                        0,
                        0
                    )
                }
                val dragFlags = if (groceryItemRecyclerViewAdapter.mDraggable) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
                return ItemTouchHelper.Callback.makeMovementFlags(
                    dragFlags,
                    0
                )
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition

                if (mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.SORT_ALPHABETICALLY) {
                    if (mGroceryListViewModel.toBuyGroceryItems.size - 1 >= targetPosition) {
                        Collections.swap(groceryItemRecyclerViewAdapter.mGroceryItems, sourcePosition, targetPosition)
                        groceryItemRecyclerViewAdapter.notifyItemMoved(sourcePosition, targetPosition)

                        return true
                    }
                } else {
                    val itemViewHolderSource: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder
                    val itemViewHolderTarget: GroceryItemRecyclerViewAdapter.ItemViewHolder = target as GroceryItemRecyclerViewAdapter.ItemViewHolder

                    val sourceGroceryItemEntity = itemViewHolderSource.groceryListItemBinding.groceryItemEntity
                    var targetGroceryItemEntity = itemViewHolderTarget.groceryListItemBinding.groceryItemEntity

                    if (targetGroceryItemEntity!!.forCategoryDivider || !sourceGroceryItemEntity!!.category.equals(targetGroceryItemEntity.category)
                        || targetGroceryItemEntity.bought == 1
                    ) {
                        return false
                    }
                    Collections.swap(groceryItemRecyclerViewAdapter.mGroceryItems, sourcePosition, targetPosition)
                    groceryItemRecyclerViewAdapter.notifyItemMoved(sourcePosition, targetPosition)
                    return true
                }


                return false

            }

            @SuppressLint("ResourceAsColor")
            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                groceryItemRecyclerViewAdapter.itemDroped()

                mGroceryListViewModel.selectedGroceryListItemList = groceryItemRecyclerViewAdapter.mGroceryItems as ArrayList<GroceryItemEntity>


            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {

                    val itemViewHolder: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder

                    //val cardView:CardView = itemViewHolder.groceryListItemBinding.groceryItemParentLayout

                }

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })
        mItemTouchHelper.attachToRecyclerView(dataBindingUtil.groceryItemRecyclerview)
        groceryItemRecyclerViewAdapter.mTouchHelper = mItemTouchHelper
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        finish()
        startActivity(intent)
        Toast.makeText(this, "New intent", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {

            CoroutineScope(IO).launch {

                val groceryItemEntity = mGroceryListViewModel.getGroceryListItem(this@SingleGroceryListActivity, groceryListUniqueId)
                withContext(Main) {
                    val groceryItemRecyclerviewAdapter:GroceryItemRecyclerViewAdapter = dataBindingUtil.groceryItemRecyclerview.adapter as GroceryItemRecyclerViewAdapter

                    //store to temporary variable
                    val oldGroceryItems = groceryItemRecyclerviewAdapter.mGroceryItems.toList()
                     mGroceryListViewModel.addGroceryListItemToBuy(mGroceryListViewModel.toBuyGroceryItems, mGroceryListViewModel.boughtGroceryItems, groceryItemEntity)

                    var newItemsAdded: ArrayList<GroceryItemEntity> = arrayListOf()
                    if(mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.GROUP_BY_CATEGORY){

                        mGroceryListViewModel.groupByCategory()

                    }

                    newItemsAdded = newAddedItems(oldGroceryItems, groceryItemRecyclerviewAdapter.mGroceryItems)
                    val indexOfNewItem = mGroceryListViewModel.selectedGroceryListItemList.indexOf(newItemsAdded[0])


                    val scrollListener = object : OnScrollListener() {
                        var found = false
                        val newItemsAddedCount = newItemsAdded.size
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                            fadeInAnimation.duration = 500
                            fadeInAnimation.fillAfter = true

                            for(item in newItemsAdded){
                                val index = dataBindingUtil.groceryListViewModel!!.selectedGroceryListItemList.indexOf(item)
                                val viewHolder = dataBindingUtil.groceryItemRecyclerview.findViewHolderForAdapterPosition(index)
                                if (viewHolder != null) {

                                    val itemViewHolder: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder
                                    itemViewHolder.groceryListItemBinding.groceryItemParentLayout.startAnimation(fadeInAnimation)

                                    dataBindingUtil.groceryItemRecyclerview.removeOnScrollListener(this)
                                    found = true
                                }
                            }

                        }
                    }

                    val firstVisibleItemPosition = ( dataBindingUtil.groceryItemRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = ( dataBindingUtil.groceryItemRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()



                    if(indexOfNewItem >= firstVisibleItemPosition && indexOfNewItem <= lastVisibleItemPosition){
                       // val viewHolder = dataBindingUtil.groceryItemRecyclerview.findViewHolderForAdapterPosition(indexOfNewItem)
                        //animateItem(viewHolder!!)
                        for(item in newItemsAdded){
                            val index = dataBindingUtil.groceryListViewModel!!.selectedGroceryListItemList.indexOf(item)
                            dataBindingUtil.groceryItemRecyclerview.adapter?.notifyItemInserted(index)
                        }


                    }else{

                        dataBindingUtil.groceryItemRecyclerview.addOnScrollListener(scrollListener)
                        for(item in newItemsAdded){
                            val index = dataBindingUtil.groceryListViewModel!!.selectedGroceryListItemList.indexOf(item)
                            dataBindingUtil.groceryItemRecyclerview.adapter?.notifyItemInserted(index)
                        }
                        dataBindingUtil.groceryItemRecyclerview.scrollToPosition(indexOfNewItem)

                    }



                }

            }
        }
        else if (requestCode == UPDATE_ITEM_REQUEST && resultCode == RESULT_OK) {
            val updatedGroceryListId = data?.getIntExtra(AddGroceryListItemActivity.GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, -1)
            val oldItemIndex = data?.getIntExtra(AddGroceryListItemActivity.GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, -1)


            CoroutineScope(IO).launch {
                val groceryItemEntityUpdated = mGroceryListViewModel.getGroceryListItem(this@SingleGroceryListActivity, updatedGroceryListId!!, groceryListUniqueId)

                withContext(Main) {

                    val groceryItemEntity:GroceryItemEntity = mGroceryListViewModel.toBuyGroceryItems.find { it.id == updatedGroceryListId }!!

                    var itemNewIndex = 0

                    if(groceryItemEntity.bought == 1){
                       // val groceryItemEntity:GroceryItemEntity = mGroceryListViewModel.boughtGroceryItems.find { it.id == updatedGroceryListId }!!
                        mGroceryListViewModel.boughtGroceryItems.set(mGroceryListViewModel.toBuyGroceryItems.indexOf(groceryItemEntity), groceryItemEntityUpdated!!)
                    }else{


                        mGroceryListViewModel.toBuyGroceryItems.set(mGroceryListViewModel.toBuyGroceryItems.indexOf(groceryItemEntity), groceryItemEntityUpdated!!)
                        //Log.e("NEW_DATA",groceryItemEntity.itemName+" "+groceryItemEntity.category)

                        //Log.e("found not bought",groceryItemEntity.itemName+" "+itemIndex )
                    }

                    mGroceryListViewModel.mergeToBuyAndBoughtItems(mGroceryListViewModel.toBuyGroceryItems, mGroceryListViewModel.boughtGroceryItems)

                    if(mGroceryListViewModel.sortingAndGrouping.value == GroceryListViewModel.SORT_ALPHABETICALLY){

                        itemNewIndex = mGroceryListViewModel.selectedGroceryListItemList.indexOf(groceryItemEntityUpdated)
                        dataBindingUtil.groceryItemRecyclerview.adapter?.notifyItemMoved(oldItemIndex!!, itemNewIndex)
                    }else{

                        mGroceryListViewModel.groupByCategory()
                        itemNewIndex = mGroceryListViewModel.selectedGroceryListItemList.indexOf(groceryItemEntityUpdated)
                        dataBindingUtil.groceryItemRecyclerview.adapter?.notifyItemMoved(oldItemIndex!!, itemNewIndex)



                    }

                    val scrollListener = object : OnScrollListener() {
                        var found = false
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val viewHolder = dataBindingUtil.groceryItemRecyclerview.findViewHolderForAdapterPosition(itemNewIndex)
                            if (viewHolder != null && !found) {
                                val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                                fadeInAnimation.duration = 500
                                fadeInAnimation.fillAfter = true
                                val itemViewHolder: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder
                                itemViewHolder.groceryListItemBinding.groceryItemParentLayout.startAnimation(fadeInAnimation)
                                dataBindingUtil.groceryItemRecyclerview.removeOnScrollListener(this)


                                found = true
                            }


                        }
                    }

                    val firstVisibleItemPosition = ( dataBindingUtil.groceryItemRecyclerview.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = ( dataBindingUtil.groceryItemRecyclerview.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                    if(itemNewIndex >= firstVisibleItemPosition && itemNewIndex <= lastVisibleItemPosition){

                        val viewHolder = dataBindingUtil.groceryItemRecyclerview.findViewHolderForAdapterPosition(itemNewIndex)
                        //animateItem(viewHolder!!)

                    }else{

                        dataBindingUtil.groceryItemRecyclerview.addOnScrollListener(scrollListener)
                        dataBindingUtil.groceryItemRecyclerview.scrollToPosition(itemNewIndex)
                    }

                    dataBindingUtil.groceryItemRecyclerview.adapter?.notifyItemChanged(itemNewIndex)
                }
            }


        }else {
            Toast.makeText(this, "OTHER", Toast.LENGTH_SHORT).show()
        }
    }

    fun newAddedItems(oldItems:List<GroceryItemEntity>, newItems:List<GroceryItemEntity>) : ArrayList<GroceryItemEntity>{
        var newItemsAdded: ArrayList<GroceryItemEntity> = arrayListOf()

        for(groceryListItem in newItems){
            if(!oldItems.contains(groceryListItem)){
                newItemsAdded.add(groceryListItem)
            }
        }
        return newItemsAdded
    }
    fun animateItem(viewHolder: RecyclerView.ViewHolder){


            /*val fadeInAnimation = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            fadeInAnimation.duration = 1000
            fadeInAnimation.fillAfter = true
            val itemViewHolder: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder
            itemViewHolder.groceryListItemBinding.root.startAnimation(fadeInAnimation)*/




    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
       menuInflater.inflate(R.menu.single_grocery_item_menu, menu)
        mMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//
//        menuInflater.inflate(R.menu.single_grocery_item_menu, menu)
//        mMenu = menu
//
//        return true
//    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
        setResult(RESULT_OK, returnIntent)
        finish()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {

                val returnIntent = Intent()
                returnIntent.putExtra(GroceryListInformationActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
                setResult(RESULT_OK, returnIntent)
                finish()


            }
            R.id.menu_sort_by_category -> {

                mGroceryListViewModel.sortingAndGrouping.value = GroceryListViewModel.GROUP_BY_CATEGORY
                mGroceryListViewModel.coroutineScope.launch {
                    mGroceryListViewModel.updateGroceryListViewing(this@SingleGroceryListActivity, GroceryListViewModel.GROUP_BY_CATEGORY, mGroceryListViewModel.selectedGroceryList.value!!.autoGeneratedUniqueId)
                    mGroceryListViewModel.groupByCategory()

                    withContext(Main) {
                        dataBindingUtil.groceryItemRecyclerview.adapter?.notifyDataSetChanged()
                    }
                }



            }
            R.id.menu_sort_by_item -> {
                mGroceryListViewModel.sortingAndGrouping.value = GroceryListViewModel.SORT_ALPHABETICALLY

                mGroceryListViewModel.coroutineScope.launch {
                    mGroceryListViewModel.updateGroceryListViewing(this@SingleGroceryListActivity, GroceryListViewModel.SORT_ALPHABETICALLY, mGroceryListViewModel.selectedGroceryList.value!!.autoGeneratedUniqueId)
                    mGroceryListViewModel.sortAlpahetically(mGroceryListViewModel.toBuyGroceryItems, mGroceryListViewModel.boughtGroceryItems)
                    mGroceryListViewModel.mergeToBuyAndBoughtItems(mGroceryListViewModel.toBuyGroceryItems, mGroceryListViewModel.boughtGroceryItems)

                    withContext(Main) {
                        dataBindingUtil.groceryItemRecyclerview.adapter?.notifyDataSetChanged()
                    }
                }


            }
        }
        return true
    }

    private fun zoomImageFromThumb(thumbView: View, imageUri: Uri) {
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


        val groceryItemEntity:GroceryItemEntity = it.tag as GroceryItemEntity
        groceryItemEntity.imageName
        val imageUri = GroceryUtil.getImageFromPath(it.context, groceryItemEntity.imageName)

        zoomImageFromThumb(it, imageUri!!)

    }


}