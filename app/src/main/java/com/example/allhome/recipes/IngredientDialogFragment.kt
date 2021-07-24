package com.example.allhome.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.IngredientEntityTransferringToGroceryList
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.IngredientDialogFragmentBinding
import com.example.allhome.databinding.IngredientDialogFragmentItemBinding
import com.example.allhome.databinding.MessageLayoutBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientDialogFragment(var title:String ): DialogFragment() {

    var mRecipeEntity:RecipeEntity? = null
    var mRecipeUniqueIds:ArrayList<String> = ArrayList()

    constructor(title:String,  recipeEntity:RecipeEntity?) : this(title) {
        this.mRecipeEntity = recipeEntity
    }
    constructor(title:String,  recipeUniqueIds:ArrayList<String>) : this(title) {
        this.mRecipeUniqueIds = recipeUniqueIds
    }

    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    var mIngredientEntityTransferringToGroceryList = arrayListOf<IngredientEntityTransferringToGroceryList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val ingredientDialogFragmentBinding: IngredientDialogFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.ingredient_dialog_fragment,null,false)
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        title.let{
            alertDialogBuilder.setTitle(title)
        }

        val recipesRecyclerviewViewAdapater = IngredientsRecyclerviewViewAdapater(arrayListOf())
        ingredientDialogFragmentBinding.ingredientsRecyclerview.adapter = recipesRecyclerviewViewAdapater

        mRecipesFragmentViewModel.mCoroutineScope.launch {
            mRecipeEntity?.let{recipe->
                mIngredientEntityTransferringToGroceryList = mRecipesFragmentViewModel.getIngredientsForTransferringInGroceryList(requireContext(),recipe.uniqueId) as ArrayList<IngredientEntityTransferringToGroceryList>
            }?:run{
                mIngredientEntityTransferringToGroceryList = mRecipesFragmentViewModel.getIngredientsForTransferringInGroceryListByIds(requireContext(),mRecipeUniqueIds) as ArrayList<IngredientEntityTransferringToGroceryList>
            }


            withContext(Main){
                recipesRecyclerviewViewAdapater.mIngredientEntities = mIngredientEntityTransferringToGroceryList
                recipesRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }


        alertDialogBuilder.setView(ingredientDialogFragmentBinding.root)
        alertDialogBuilder.setPositiveButton("Continue", DialogInterface.OnClickListener { dialog, which ->
            // continue

            val recipeGroceryListActivity = Intent(requireContext(), RecipeGroceryListActivity::class.java)
            recipeGroceryListActivity.putParcelableArrayListExtra(RecipeGroceryListActivity.INGREDIENT_ENTITY_TAG,selectedIngredient())
            requireContext().startActivity(recipeGroceryListActivity)


        })
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })

        return alertDialogBuilder.create()
    }

    fun selectedIngredient(): ArrayList<IngredientEntityTransferringToGroceryList> {
          val ingredients = mIngredientEntityTransferringToGroceryList.filter {
             it.isSelected == IngredientEntityTransferringToGroceryList.SELECTED
            }
        return ingredients as ArrayList<IngredientEntityTransferringToGroceryList>

    }
    /**
     *
     */
    class IngredientsRecyclerviewViewAdapater(var mIngredientEntities:ArrayList<IngredientEntityTransferringToGroceryList>):
        RecyclerView.Adapter<IngredientsRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutInflater = LayoutInflater.from(parent.context)

            val ingredientDialogFragmentItemBinding =  IngredientDialogFragmentItemBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(ingredientDialogFragmentItemBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val ingredientEntityTransferringToGroceryList = mIngredientEntities[position]
            holder.ingredientDialogFragmentItemBinding.ingredientEntityTransferringToGroceryList = ingredientEntityTransferringToGroceryList

            holder.ingredientDialogFragmentItemBinding.executePendingBindings()

            holder.ingredientDialogFragmentItemBinding.groceryItemParentLayout.setOnClickListener(holder)
            holder.ingredientDialogFragmentItemBinding.checkBox.setOnClickListener(holder)

        }

        override fun getItemCount(): Int {

            return mIngredientEntities.size
        }


        inner class  ItemViewHolder(var ingredientDialogFragmentItemBinding: IngredientDialogFragmentItemBinding, val ingredientsRecyclerviewViewAdapater: IngredientsRecyclerviewViewAdapater): RecyclerView.ViewHolder(ingredientDialogFragmentItemBinding.root),
            View.OnClickListener,CompoundButton.OnCheckedChangeListener{


            override fun onClick(view: View?) {

                val ingredientEntity = ingredientsRecyclerviewViewAdapater.mIngredientEntities[adapterPosition]
                if(ingredientEntity.isSelected == IngredientEntityTransferringToGroceryList.SELECTED){
                    ingredientEntity.isSelected = IngredientEntityTransferringToGroceryList.NOT_SELECTED
                    ingredientDialogFragmentItemBinding.checkBox.isChecked = false
                }else{
                    ingredientEntity.isSelected = IngredientEntityTransferringToGroceryList.SELECTED
                    ingredientDialogFragmentItemBinding.checkBox.isChecked = true
                }


            }

            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

                val ingredientEntity = ingredientsRecyclerviewViewAdapater.mIngredientEntities[adapterPosition]
                ingredientEntity.isSelected = if(isChecked) IngredientEntityTransferringToGroceryList.NOT_SELECTED else IngredientEntityTransferringToGroceryList.SELECTED

            }


        }


    }


}