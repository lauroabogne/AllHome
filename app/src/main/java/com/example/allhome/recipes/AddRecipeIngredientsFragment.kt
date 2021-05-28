package com.example.allhome.recipes

import android.content.Context
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
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.databinding.AddIngredientItemBinding
import com.example.allhome.databinding.FragmentAddRecipeIngredientsBinding
import com.example.allhome.recipes.viewmodel.AddRecipeIngredientsFragmentModel
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class AddRecipeIngredientsFragment : Fragment() {

    private lateinit var mAddRecipeIngredientsFragmentModel: AddRecipeIngredientsFragmentModel
    private lateinit var mDataBindingUtil: FragmentAddRecipeIngredientsBinding

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



}

class AddIngredientRecyclerviewViewAdapater( var mIngredients:ArrayList<IngredientEntity>, addRecipeIngredientsFragment: AddRecipeIngredientsFragment): RecyclerView.Adapter<AddIngredientRecyclerviewViewAdapater.ItemViewHolder>() {

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

    inner class  ItemViewHolder(var addIngredientItemBinding: AddIngredientItemBinding, val addIngredientRecyclerviewViewAdapater: AddIngredientRecyclerviewViewAdapater): RecyclerView.ViewHolder(addIngredientItemBinding.root),View.OnClickListener{

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


        }

    }


}