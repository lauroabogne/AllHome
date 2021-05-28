package com.example.allhome.recipes

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.databinding.AddStepItemBinding
import com.example.allhome.databinding.FragmentAddRecipeStepsBinding
import com.example.allhome.recipes.viewmodel.AddRecipeStepsFragmentViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddRecipeStepsFragment : Fragment() {

    lateinit var mAddRecipeStepsFragmentViewModel:AddRecipeStepsFragmentViewModel
    lateinit var mDataBindingUtil:FragmentAddRecipeStepsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        mAddRecipeStepsFragmentViewModel = ViewModelProvider(this).get(AddRecipeStepsFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_steps, container, false)
        mDataBindingUtil.fab.setOnClickListener(fabClickListener)
        val addStepRecyclerviewViewAdapater = AddStepRecyclerviewViewAdapater(mAddRecipeStepsFragmentViewModel.mRecipeStepEntities,this)
        mDataBindingUtil.addRecipeStepRecyclerview.adapter = addStepRecyclerviewViewAdapater
        return mDataBindingUtil.root
    }
    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    val fabClickListener = object:View.OnClickListener{
     override fun onClick(v: View?) {
         val addIngredientRecyclerviewViewAdapater =  mDataBindingUtil.addRecipeStepRecyclerview.adapter as AddStepRecyclerviewViewAdapater

         var uniqueID = UUID.randomUUID().toString()
         val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
         val currentDatetime: String = simpleDateFormat.format(Date())


         val elemetSize = mAddRecipeStepsFragmentViewModel.mRecipeStepEntities.size
         val recipeStep = RecipeStepEntity(
             uniqueId = uniqueID,
             recipeUniqueId="",
             instruction="",
             sequence = elemetSize + 1,
             status = RecipeStepEntity.NOT_DELETED_STATUS,
             uploaded = RecipeStepEntity.NOT_UPLOADED,
             created = currentDatetime,
             modified = currentDatetime
         )




         mAddRecipeStepsFragmentViewModel.mRecipeStepEntities.add(recipeStep)
         addIngredientRecyclerviewViewAdapater.notifyItemInserted(elemetSize)

         mDataBindingUtil.addRecipeStepRecyclerview.scrollToPosition(elemetSize );
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
}

class AddStepRecyclerviewViewAdapater(var mRecipeStepEntities:ArrayList<RecipeStepEntity>, addRecipeStepsFragment: AddRecipeStepsFragment): RecyclerView.Adapter<AddStepRecyclerviewViewAdapater.ItemViewHolder>() {

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

    inner class  ItemViewHolder(var addStepItemBinding: AddStepItemBinding, val addStepRecyclerviewViewAdapater: AddStepRecyclerviewViewAdapater): RecyclerView.ViewHolder(addStepItemBinding.root),View.OnClickListener{

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


        }

    }


}