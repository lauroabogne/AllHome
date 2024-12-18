package com.example.allhome.grocerylist.archived_grocery_list

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityValues
import com.example.allhome.databinding.ActivityTrashSingleGroceryListBinding
import com.example.allhome.grocerylist.*
import com.example.allhome.grocerylist.viewmodel.ArchivedGroceryListViewModel
import com.example.allhome.grocerylist.viewmodel_factory.ArchiveGroceryListViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArchivedSingleGroceryListActivity : AppCompatActivity() {

    lateinit var mGroceryListViewModel: ArchivedGroceryListViewModel
    lateinit var dataBindingUtil: ActivityTrashSingleGroceryListBinding
    var groceryListUniqueId: String = ""
    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private var currentAnimator: Animator? = null

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash_single_grocery_list)

//        title = "Items"
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)




        intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
            groceryListUniqueId = it
        }

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        // create AddGroceryListItemActivityViewModel using AddGroceryListItemActivityViewModelFactory
        val addGroceryListItemActivityViewModelFactory = ArchiveGroceryListViewModelFactory(null, null)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(
            ArchivedGroceryListViewModel::class.java
        )

        val trashGroceryListViewModel: ArchivedGroceryListViewModel = ViewModelProvider(this)[ArchivedGroceryListViewModel::class.java]


        //Bind data
        dataBindingUtil = DataBindingUtil.setContentView<ActivityTrashSingleGroceryListBinding>(
            this, R.layout.activity_trash_single_grocery_list
        ).apply {
            this.lifecycleOwner = this@ArchivedSingleGroceryListActivity
            this.groceryListViewModel = mGroceryListViewModel


        }

        dataBindingUtil.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        dataBindingUtil.toolbar.title = "Items"
        dataBindingUtil.toolbar.setNavigationOnClickListener {
            finish()
        }

        val groceryItemRecyclerViewAdapter = ArchivedGroceryItemRecyclerViewAdapter(this, productImageClickListener)
        dataBindingUtil.groceryItemRecyclerview.adapter = groceryItemRecyclerViewAdapter

        mGroceryListViewModel.coroutineScope.launch {
            mGroceryListViewModel.setSelectedGroceryList(this@ArchivedSingleGroceryListActivity, groceryListUniqueId)
        }

        if (mGroceryListViewModel.selectedGroceryListEntity == null) {

            mGroceryListViewModel.coroutineScope.launch {
                mGroceryListViewModel.setSelectedGroceryList(this@ArchivedSingleGroceryListActivity, groceryListUniqueId)
                withContext(Dispatchers.Main) {
                    //supportActionBar?.title = mGroceryListViewModel.selectedGroceryList?.name;
                    //Log.e("SELECTED ",mGroceryListViewModel.selectedGroceryList?.autoGeneratedUniqueId+" aa")

                }

            }
        }
        if (mGroceryListViewModel.selectedGroceryListItemList.isNullOrEmpty()) {

            mGroceryListViewModel.coroutineScope.launch {
                val groceryItemEntities = mGroceryListViewModel.getGroceryItems(this@ArchivedSingleGroceryListActivity, groceryListUniqueId, GroceryItemEntityValues.ACTIVE_STATUS)
                mGroceryListViewModel.separateBougthItems(groceryItemEntities)
                mGroceryListViewModel.mergeToBuyAndBoughtItems(mGroceryListViewModel.toBuyGroceryItems, mGroceryListViewModel.boughtGroceryItems)
                groceryItemRecyclerViewAdapter.mGroceryItems = mGroceryListViewModel.selectedGroceryListItemList

                withContext(Dispatchers.Main) {

                    groceryItemRecyclerViewAdapter.notifyDataSetChanged()
                }
            }
        } else {
            groceryItemRecyclerViewAdapter.mGroceryItems = mGroceryListViewModel.selectedGroceryListItemList
            groceryItemRecyclerViewAdapter.notifyDataSetChanged()
        }

    }
    override fun onBackPressed() {
        finish()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
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


        val groceryItemEntity: GroceryItemEntity = it.tag as GroceryItemEntity
        groceryItemEntity.imageName
        val imageUri = GroceryUtil.getImageFromPath(it.context, groceryItemEntity.imageName)

        zoomImageFromThumb(it, imageUri!!)

    }
}