package com.example.allhome.recipes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.databinding.AddIngredientItemBinding
import com.example.allhome.databinding.FragmentAddRecipeIngredientsBinding
import com.example.allhome.grocerylist.GroceryItemRecyclerViewAdapter
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.recipes.viewmodel.AddRecipeIngredientsFragmentModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AddRecipeIngredientsFragment : Fragment() {

    private lateinit var mAddRecipeIngredientsFragmentModel: AddRecipeIngredientsFragmentModel
     lateinit var mDataBindingUtil: FragmentAddRecipeIngredientsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        mAddRecipeIngredientsFragmentModel = ViewModelProvider(this).get(AddRecipeIngredientsFragmentModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_ingredients, container, false)

        mDataBindingUtil.fab.setOnClickListener {

            val addIngredientRecyclerviewViewAdapater =  mDataBindingUtil.addIngredientRecyclerview.adapter as AddIngredientRecyclerviewViewAdapater
            var itemUniqueID = UUID.randomUUID().toString()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            val ingredientEntity = IngredientEntity(
                uniqueId =itemUniqueID,
                recipeUniqueId="",
                quantity=0.0,
                unit="",
                name="",
                status = IngredientEntity.NOT_DELETED_STATUS,
                uploaded = IngredientEntity.NOT_UPLOADED,
                created = currentDatetime,
                modified = currentDatetime

            )

            val elemetSize = mAddRecipeIngredientsFragmentModel.mIngredients.size

            mAddRecipeIngredientsFragmentModel.mIngredients.add(ingredientEntity)
            addIngredientRecyclerviewViewAdapater.notifyItemInserted(elemetSize)

            mDataBindingUtil.addIngredientRecyclerview.scrollToPosition(elemetSize );
            mDataBindingUtil.fab.isClickable = false

            CoroutineScope(IO).launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    mDataBindingUtil.fab.isClickable = true

                    val holder = mDataBindingUtil.addIngredientRecyclerview.findViewHolderForLayoutPosition(elemetSize) as AddIngredientRecyclerviewViewAdapater.ItemViewHolder

                    holder.addIngredientItemBinding.ingredientEditTextText.requestFocus()
                    showSoftKeyboard(holder.addIngredientItemBinding.ingredientEditTextText)

                }
            }


        }


        recyclerViewTouchHelper.attachToRecyclerView(mDataBindingUtil.addIngredientRecyclerview)

        val addIngredientRecyclerviewViewAdapater = AddIngredientRecyclerviewViewAdapater(mAddRecipeIngredientsFragmentModel.mIngredients,this)

        mDataBindingUtil.addIngredientRecyclerview.adapter = addIngredientRecyclerviewViewAdapater
        return mDataBindingUtil.root
    }


    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }


    val recyclerViewTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {


        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {


            mDataBindingUtil.swipeRefresh.isEnabled = false

            val addIngredientRecyclerviewViewAdapater = mDataBindingUtil.addIngredientRecyclerview.adapter as AddIngredientRecyclerviewViewAdapater
            val dragFlags =  ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return ItemTouchHelper.Callback.makeMovementFlags(
                dragFlags,
                0
            )
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

            val sourcePosition = viewHolder.adapterPosition
            val targetPosition = target.adapterPosition


            Collections.swap(mAddRecipeIngredientsFragmentModel.mIngredients, sourcePosition, targetPosition)
            mDataBindingUtil.addIngredientRecyclerview.adapter?.notifyItemMoved(sourcePosition,targetPosition)

            return false

        }

        @SuppressLint("ResourceAsColor")
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            val addIngredientRecyclerviewViewAdapater = mDataBindingUtil.addIngredientRecyclerview.adapter as AddIngredientRecyclerviewViewAdapater
            //addIngredientRecyclerviewViewAdapater.mDraggable = false
            //mDataBindingUtil.swipeRefresh.isEnabled = true
            addIngredientRecyclerviewViewAdapater.itemDropped(viewHolder)

        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {

                // val itemViewHolder: GroceryItemRecyclerViewAdapter.ItemViewHolder = viewHolder as GroceryItemRecyclerViewAdapter.ItemViewHolder

                //val cardView:CardView = itemViewHolder.groceryListItemBinding.groceryItemParentLayout

            }

        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Not yet implemented")
        }

    })

    fun getIngredents(): ArrayList<IngredientEntity> {
        return mAddRecipeIngredientsFragmentModel.mIngredients
    }


}

class AddIngredientRecyclerviewViewAdapater( var mIngredients:ArrayList<IngredientEntity>, val addRecipeIngredientsFragment: AddRecipeIngredientsFragment): RecyclerView.Adapter<AddIngredientRecyclerviewViewAdapater.ItemViewHolder>() {

    var mDraggable: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {


        val layoutInflater = LayoutInflater.from(parent.context)
        val pantryItemLayoutBinding = AddIngredientItemBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding, this)

        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val ingredient = mIngredients[position]
        holder.addIngredientItemBinding.ingredientEntity = ingredient
        holder.addIngredientItemBinding.executePendingBindings()
        holder.setText(ingredient.name)
        holder.setTextWatcher()
    }

    override fun getItemCount(): Int {

        return mIngredients.size
    }
    fun itemDropped(viewHolderParams: RecyclerView.ViewHolder){
        mDraggable = false
       var viewHolder  = viewHolderParams as AddIngredientRecyclerviewViewAdapater.ItemViewHolder
        viewHolder.addIngredientItemBinding.root.setBackgroundColor(Color.WHITE)

    }

    fun requestDrag(itemViewHolder:ItemViewHolder){
        addRecipeIngredientsFragment.recyclerViewTouchHelper.startDrag(itemViewHolder)
        val constraintLayout = itemViewHolder.addIngredientItemBinding.root as ConstraintLayout
        constraintLayout.setBackgroundResource(R.drawable.with_shadow)

       mDraggable = true
    }
    fun removeItem(position: Int){

        mIngredients.removeAt(position)
        notifyItemRemoved(position)



    }

    inner class  ItemViewHolder(var addIngredientItemBinding: AddIngredientItemBinding, val addIngredientRecyclerviewViewAdapater: AddIngredientRecyclerviewViewAdapater): RecyclerView.ViewHolder(addIngredientItemBinding.root),View.OnClickListener{

        init {
            addIngredientItemBinding.removeBtn.setOnClickListener(this)
            addIngredientItemBinding.moveBtn.setOnLongClickListener {
                requestDrag(this)
                true
            }
        }
        fun setText(text:String){
            addIngredientItemBinding.ingredientEditTextText.setText(text)
        }
        fun setTextWatcher(){
            addIngredientItemBinding.ingredientEditTextText.addTextChangedListener{
                val ingredient =  addIngredientRecyclerviewViewAdapater.mIngredients[adapterPosition]
                ingredient.name = it.toString()
                addIngredientRecyclerviewViewAdapater.mIngredients.set(adapterPosition,ingredient)
            }
        }
        override fun onClick(view: View?) {

            when(view?.id){
                R.id.removeBtn->{

                    addIngredientRecyclerviewViewAdapater.removeItem(adapterPosition)
                }
            }

        }

    }


}