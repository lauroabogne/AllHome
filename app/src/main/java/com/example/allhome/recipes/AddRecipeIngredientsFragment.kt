package com.example.allhome.recipes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.AddIngredientItemBinding
import com.example.allhome.databinding.FragmentAddRecipeIngredientsBinding
import com.example.allhome.recipes.viewmodel.AddRecipeIngredientsFragmentModel
import com.example.allhome.utils.IngredientEvaluator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class AddRecipeIngredientsFragment : Fragment() {

    private lateinit var mAddRecipeIngredientsFragmentModel: AddRecipeIngredientsFragmentModel
    lateinit var mDataBindingUtil: FragmentAddRecipeIngredientsBinding

    var mRecipeEntity:RecipeEntity? = null
    var mAction = ADD_ACTION

    companion object {
        val TAG = "ViewRecipeFragment"
        const val ADD_ACTION = 0
        const val ADD_ACTION_FROM_BROWSER = 1
        const val EDIT_ACTION = 2

        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"
        val INGREDIENT_INTENT_TAG = "INGREDIENT_INTENT_TAG"

        @JvmStatic fun newInstanceForEditing(recipeEntity: RecipeEntity) =
            AddRecipeIngredientsFragment().apply {

                mAction = EDIT_ACTION
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)
                }
            }
        @JvmStatic fun newInstanceForAddingRecipeFromBrowser(ingredientEntities: ArrayList<IngredientEntity>) =
            AddRecipeIngredientsFragment().apply {

                mAction = ADD_ACTION_FROM_BROWSER
                arguments = Bundle().apply {
                    putParcelableArrayList(INGREDIENT_INTENT_TAG, ingredientEntities)
                }
            }
        @JvmStatic fun newInstanceForAdd() =

            AddRecipeIngredientsFragment().apply {
                mAction = ADD_ACTION
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAddRecipeIngredientsFragmentModel = ViewModelProvider(this).get(AddRecipeIngredientsFragmentModel::class.java)

        arguments?.let {

            if(mAction == ADD_ACTION_FROM_BROWSER ){
                mAddRecipeIngredientsFragmentModel.mIngredients = it.getParcelableArrayList(INGREDIENT_INTENT_TAG)!!
            }else{
                mRecipeEntity = it.getParcelable(RECIPE_INTENT_TAG)
            }


        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_ingredients, container, false)
        mDataBindingUtil.fab.setOnClickListener(fabOnClick)
        recyclerViewTouchHelper.attachToRecyclerView(mDataBindingUtil.addIngredientRecyclerview)
        val addIngredientRecyclerviewViewAdapater = AddIngredientRecyclerviewViewAdapater(mAddRecipeIngredientsFragmentModel.mIngredients,this)
        mDataBindingUtil.addIngredientRecyclerview.adapter = addIngredientRecyclerviewViewAdapater

        if(mAction == EDIT_ACTION){

            mAddRecipeIngredientsFragmentModel.mCoroutineScope.launch {
                val ingredientEntities = mAddRecipeIngredientsFragmentModel.getIngredients(requireContext(),mRecipeEntity!!.uniqueId)
                mAddRecipeIngredientsFragmentModel.mIngredients = ingredientEntities as ArrayList<IngredientEntity>

                withContext(Main){
                    addIngredientRecyclerviewViewAdapater.mIngredients = ingredientEntities
                    addIngredientRecyclerviewViewAdapater.notifyDataSetChanged()

                }

            }

        }else if(mAction == ADD_ACTION_FROM_BROWSER){
            addIngredientRecyclerviewViewAdapater.mIngredients = mAddRecipeIngredientsFragmentModel.mIngredients
            addIngredientRecyclerviewViewAdapater.notifyDataSetChanged()
        }



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

        val evaluatedIngredients = arrayListOf<IngredientEntity>()
         val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


        if(mAction == EDIT_ACTION){
            mAddRecipeIngredientsFragmentModel.mIngredients.forEach {

                val ingredientEntity = IngredientEntity(it.uniqueId, it.recipeUniqueId, it.name,
                    IngredientEntity.NOT_DELETED_STATUS, IngredientEntity.NOT_UPLOADED, it.created, currentDatetime)


                evaluatedIngredients.add(ingredientEntity)
            }

            return evaluatedIngredients

        }else{
            mAddRecipeIngredientsFragmentModel.mIngredients.forEach {

                val ingredientEntity = IngredientEntity(it.uniqueId, it.recipeUniqueId, it.name,
                    IngredientEntity.NOT_DELETED_STATUS, IngredientEntity.NOT_UPLOADED, currentDatetime, currentDatetime)

                evaluatedIngredients.add(ingredientEntity)
            }

            return evaluatedIngredients
        }


    }

    val fabOnClick = object:View.OnClickListener {
        override fun onClick(v: View?) {

            val addIngredientRecyclerviewViewAdapater =  mDataBindingUtil.addIngredientRecyclerview.adapter as AddIngredientRecyclerviewViewAdapater
            var itemUniqueID = UUID.randomUUID().toString()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())

            val ingredientEntity = IngredientEntity(
                uniqueId =itemUniqueID,
                recipeUniqueId="",
                name="",
                status = IngredientEntity.NOT_DELETED_STATUS,
                uploaded = IngredientEntity.NOT_UPLOADED,
                created = "",
                modified = ""

            )

            val elemetSize = mAddRecipeIngredientsFragmentModel.mIngredients.size

            mAddRecipeIngredientsFragmentModel.mIngredients.add(ingredientEntity)
            addIngredientRecyclerviewViewAdapater.mIngredients =  mAddRecipeIngredientsFragmentModel.mIngredients
            addIngredientRecyclerviewViewAdapater.notifyItemInserted(elemetSize)

            mDataBindingUtil.addIngredientRecyclerview.scrollToPosition(elemetSize )
            mDataBindingUtil.fab.isClickable = false

            mAddRecipeIngredientsFragmentModel.mCoroutineScope.launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    mDataBindingUtil.fab.isClickable = true

                    val holder = mDataBindingUtil.addIngredientRecyclerview.findViewHolderForLayoutPosition(elemetSize) as AddIngredientRecyclerviewViewAdapater.ItemViewHolder
                    holder.addIngredientItemBinding.ingredientEditTextText.requestFocus()
                   showSoftKeyboard(holder.addIngredientItemBinding.ingredientEditTextText)

                }
            }
        }
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
        fun setTextWatcher(){
            addIngredientItemBinding.ingredientEditTextText.addTextChangedListener{

                if(addIngredientItemBinding.ingredientEditTextText.hasFocus()){
                    val ingredient =  addIngredientRecyclerviewViewAdapater.mIngredients[adapterPosition]
                    ingredient.name = it.toString()
                    addIngredientRecyclerviewViewAdapater.mIngredients.set(adapterPosition,ingredient)
                }


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