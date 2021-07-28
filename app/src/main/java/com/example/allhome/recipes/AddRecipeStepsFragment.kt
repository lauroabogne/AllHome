package com.example.allhome.recipes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.databinding.AddStepItemBinding
import com.example.allhome.databinding.FragmentAddRecipeStepsBinding
import com.example.allhome.recipes.viewmodel.AddRecipeStepsFragmentViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddRecipeStepsFragment : Fragment() {

    lateinit var mAddRecipeStepsFragmentViewModel:AddRecipeStepsFragmentViewModel
    lateinit var mDataBindingUtil:FragmentAddRecipeStepsBinding

    var mRecipeEntity:RecipeEntity? = null
    var mAction = ADD_ACTION

    companion object{
        const val ADD_ACTION = 0
        const val EDIT_ACTION = 1
        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"
        @JvmStatic fun newInstanceForEditing(recipeEntity: RecipeEntity) =
            AddRecipeStepsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)
                    mAction  = EDIT_ACTION
                }
            }
        @JvmStatic fun newInstanceForAdd() =
            AddRecipeStepsFragment().apply {
                    mAction = ADD_ACTION
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mRecipeEntity = it.getParcelable(AddRecipeIngredientsFragment.RECIPE_INTENT_TAG)
        }

        mAddRecipeStepsFragmentViewModel = ViewModelProvider(this).get(AddRecipeStepsFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_steps, container, false)
        mDataBindingUtil.fab.setOnClickListener(fabClickListener)

        recyclerViewTouchHelper.attachToRecyclerView(mDataBindingUtil.addRecipeStepRecyclerview)

        val addStepRecyclerviewViewAdapater = AddStepRecyclerviewViewAdapater(mAddRecipeStepsFragmentViewModel.mRecipeStepEntities,this)
        mDataBindingUtil.addRecipeStepRecyclerview.adapter = addStepRecyclerviewViewAdapater

        if(mAction == EDIT_ACTION){
            mAddRecipeStepsFragmentViewModel.mCoroutineScope.launch {
                val steps = mAddRecipeStepsFragmentViewModel.getSteps(requireContext(),mRecipeEntity!!.uniqueId)
                mAddRecipeStepsFragmentViewModel.mRecipeStepEntities = steps as ArrayList<RecipeStepEntity>

                withContext(Main){
                    addStepRecyclerviewViewAdapater.mRecipeStepEntities = steps
                    addStepRecyclerviewViewAdapater.notifyDataSetChanged()
                }
            }
        }

        return mDataBindingUtil.root
    }
    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    fun getSteps(): ArrayList<RecipeStepEntity> {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        if(mAction == EDIT_ACTION){
            mAddRecipeStepsFragmentViewModel.mRecipeStepEntities.forEach {
                it.modified = currentDatetime
            }
            return mAddRecipeStepsFragmentViewModel.mRecipeStepEntities
        }else{
            mAddRecipeStepsFragmentViewModel.mRecipeStepEntities.forEach{
                it.created = currentDatetime
                it.modified = currentDatetime
            }
            return mAddRecipeStepsFragmentViewModel.mRecipeStepEntities
        }


    }

    val fabClickListener = object:View.OnClickListener{
     override fun onClick(v: View?) {
         val addIngredientRecyclerviewViewAdapater =  mDataBindingUtil.addRecipeStepRecyclerview.adapter as AddStepRecyclerviewViewAdapater

         var uniqueID = UUID.randomUUID().toString()



         val elemetSize = mAddRecipeStepsFragmentViewModel.mRecipeStepEntities.size
         val recipeStep = RecipeStepEntity(
             uniqueId = uniqueID,
             recipeUniqueId="",
             instruction="",
             sequence = elemetSize + 1,
             status = RecipeStepEntity.NOT_DELETED_STATUS,
             uploaded = RecipeStepEntity.NOT_UPLOADED,
             created = "",
             modified = ""
         )




         mAddRecipeStepsFragmentViewModel.mRecipeStepEntities.add(recipeStep)
         addIngredientRecyclerviewViewAdapater.mRecipeStepEntities = mAddRecipeStepsFragmentViewModel.mRecipeStepEntities
         addIngredientRecyclerviewViewAdapater.notifyItemInserted(elemetSize)

         mDataBindingUtil.addRecipeStepRecyclerview.scrollToPosition(elemetSize )
         mDataBindingUtil.fab.isClickable = false

         CoroutineScope(Dispatchers.IO).launch {
             delay(500)
             withContext(Dispatchers.Main) {
                 mDataBindingUtil.fab.isClickable = true

                 val holder = mDataBindingUtil.addRecipeStepRecyclerview.findViewHolderForLayoutPosition(elemetSize) as AddStepRecyclerviewViewAdapater.ItemViewHolder

                 holder.addStepItemBinding.instructionEditTextText.requestFocus()
                 showSoftKeyboard(holder.addStepItemBinding.instructionEditTextText)

             }
         }

     }


 }
    val recyclerViewTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {


        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {

            val dragFlags =  ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(
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


            Collections.swap(mAddRecipeStepsFragmentViewModel.mRecipeStepEntities, sourcePosition, targetPosition)
            mDataBindingUtil.addRecipeStepRecyclerview.adapter?.notifyItemMoved(sourcePosition,targetPosition)

            return false

        }

        @SuppressLint("ResourceAsColor")
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            val addStepRecyclerviewViewAdapater = mDataBindingUtil.addRecipeStepRecyclerview.adapter as AddStepRecyclerviewViewAdapater
            addStepRecyclerviewViewAdapater.itemDropped(viewHolder)

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


}

class AddStepRecyclerviewViewAdapater(var mRecipeStepEntities:ArrayList<RecipeStepEntity>, val addRecipeStepsFragment: AddRecipeStepsFragment): RecyclerView.Adapter<AddStepRecyclerviewViewAdapater.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {


        val layoutInflater = LayoutInflater.from(parent.context)
        val addStepItemBinding = AddStepItemBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(addStepItemBinding, this)

        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val recipeStepEntity = mRecipeStepEntities[position]
        holder.addStepItemBinding.recipeStepEntity = recipeStepEntity
        holder.addStepItemBinding.executePendingBindings()
        holder.setText(recipeStepEntity.instruction)
        holder.setTextWatcher()
    }

    override fun getItemCount(): Int {

        return mRecipeStepEntities.size
    }

    fun itemDropped(viewHolderParams: RecyclerView.ViewHolder){

        var viewHolder  = viewHolderParams as AddStepRecyclerviewViewAdapater.ItemViewHolder
        viewHolder.addStepItemBinding.root.setBackgroundColor(Color.WHITE)

        /**
         * modifiy step
         */
        mRecipeStepEntities.forEachIndexed { index, recipeStepEntity ->
            recipeStepEntity.sequence = index+1
        }



    }

    fun requestDrag(itemViewHolder: AddStepRecyclerviewViewAdapater.ItemViewHolder){
        addRecipeStepsFragment.recyclerViewTouchHelper.startDrag(itemViewHolder)
        val constraintLayout = itemViewHolder.addStepItemBinding.root as ConstraintLayout
        constraintLayout.setBackgroundResource(R.drawable.with_shadow)

    }
    fun removeItem(position: Int){

        mRecipeStepEntities.removeAt(position)
        notifyItemRemoved(position)



    }

    inner class  ItemViewHolder(var addStepItemBinding: AddStepItemBinding, val addStepRecyclerviewViewAdapater: AddStepRecyclerviewViewAdapater): RecyclerView.ViewHolder(addStepItemBinding.root),View.OnClickListener{

        init {
            addStepItemBinding.removeBtn.setOnClickListener(this)
            addStepItemBinding.moveBtn.setOnLongClickListener {
                requestDrag(this)
                true
            }
        }
        fun setText(text:String){
            addStepItemBinding.instructionEditTextText.setText(text.toString())
        }
        fun setTextWatcher(){
            addStepItemBinding.instructionEditTextText.addTextChangedListener{
                val recipeStepEntity =  addStepRecyclerviewViewAdapater.mRecipeStepEntities[adapterPosition]
                recipeStepEntity.instruction = it.toString()
                addStepRecyclerviewViewAdapater.mRecipeStepEntities.set(adapterPosition,recipeStepEntity)
            }
        }
        override fun onClick(view: View?) {


            when(view?.id){
                R.id.removeBtn->{

                    addStepRecyclerviewViewAdapater.removeItem(adapterPosition)

                }
            }


        }

    }


}