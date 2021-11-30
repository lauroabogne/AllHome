package com.example.allhome.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment

import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.data.entities.RecipeEntityWithTotalIngredient
import com.example.allhome.databinding.*
import com.example.allhome.recipes.viewmodel.RecipeCategoryViewModel
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RecipeCategoryMultipleSelectDialogFragment(val mRecipeCurrentCategoryEntities:ArrayList<RecipeCategoryEntity>, val mSelectRecipeCategoriesListener:SelectRecipeCategoriesListener) : DialogFragment() {


    lateinit var mRecipeCategoryDialogFragmentLayoutBinding: RecipeCategoryDialogFragmentLayoutBinding
    lateinit var mRecipeCategoryViewModel:RecipeCategoryViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        mRecipeCategoryViewModel = ViewModelProvider(this).get(RecipeCategoryViewModel::class.java)

       val inflater = LayoutInflater.from(requireContext())
        mRecipeCategoryDialogFragmentLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.recipe_category_dialog_fragment_layout,null,false)

        mRecipeCategoryDialogFragmentLayoutBinding.toolbar.title = "Categories"
        mRecipeCategoryDialogFragmentLayoutBinding.toolbar.inflateMenu(R.menu.add_category_menu)

        mRecipeCategoryDialogFragmentLayoutBinding.toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.addCategoryMenu->{

                    val addEditRecipeCategoryDialogFragment  = AddEditRecipeCategoryDialogFragment(mRecipeCategoryViewModel)
                    addEditRecipeCategoryDialogFragment.setAddingRecipeCategoryListener(object:AddEditRecipeCategoryDialogFragment.AddingRecipeCategoryListener{
                        override fun onSave(status: Int, categoryUniqueId: String?) {
                            if(status == AddEditRecipeCategoryDialogFragment.SUCCESS){
                                Toast.makeText(requireContext(),"Category saved successfully.",Toast.LENGTH_SHORT).show()
                                addEditRecipeCategoryDialogFragment.dismiss()
                                loadCategories()
                            }else{
                                Toast.makeText(requireContext(),"Failed to save category.Please try again.",Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    addEditRecipeCategoryDialogFragment.show(requireActivity().supportFragmentManager,"AddEditRecipeCategoryDialogFragment")

                }
            }
            true
        }

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(mRecipeCategoryDialogFragmentLayoutBinding.root)

        alertDialogBuilder.setNegativeButton("Close", { dialog, which ->
            this.dismiss()
        })
        alertDialogBuilder.setPositiveButton("Done",{dialog,which->
            val recipeCategoryRecyclerviewViewAdapater = mRecipeCategoryDialogFragmentLayoutBinding.recipesRecyclerview.adapter as RecipeCategoryRecyclerviewViewAdapater
            mSelectRecipeCategoriesListener.onSelect(mRecipeCurrentCategoryEntities)
        })

        val alertDialog = alertDialogBuilder.create()
        mRecipeCategoryDialogFragmentLayoutBinding.toolbar.setNavigationOnClickListener {
            alertDialog.dismiss()
        }

        mRecipeCategoryDialogFragmentLayoutBinding.recipesRecyclerview.addItemDecoration(DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL))

        loadCategories()

        return alertDialog
    }

    fun loadCategories(){
        val recipeCategoryRecyclerviewViewAdapater = RecipeCategoryRecyclerviewViewAdapater(arrayListOf(),this)
        mRecipeCategoryDialogFragmentLayoutBinding.recipesRecyclerview.adapter = recipeCategoryRecyclerviewViewAdapater
        mRecipeCategoryViewModel.mCoroutineScope.launch {
            val recipeCategoryEntities = mRecipeCategoryViewModel.getRecipeCategories(requireContext()) as ArrayList<RecipeCategoryEntity>

            withContext(Main){
                val recipeCategoryRecyclerviewViewAdapater = mRecipeCategoryDialogFragmentLayoutBinding.recipesRecyclerview.adapter as RecipeCategoryRecyclerviewViewAdapater
                recipeCategoryRecyclerviewViewAdapater.mRecipeCategoryEntities = recipeCategoryEntities
                recipeCategoryRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
    }


    inner class RecipeCategoryRecyclerviewViewAdapater(var mRecipeCategoryEntities:ArrayList<RecipeCategoryEntity>, val mRecipeCategoryDialogFragment: RecipeCategoryMultipleSelectDialogFragment):
        RecyclerView.Adapter<RecipeCategoryRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutInflater = LayoutInflater.from(parent.context)
            val recipeItemBinding =  RecipeCategoryItemCheckboxBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(recipeItemBinding, this)


            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {


            val categoryEntity = mRecipeCategoryEntities[position]
            holder.recipeCategoryItemCheckboxBinding.recipeCategory = categoryEntity

            if(doCategorySelected(categoryEntity.name,mRecipeCurrentCategoryEntities)){
                holder.recipeCategoryItemCheckboxBinding.recipeCategoryCheckBox.isChecked = true
            }else{
                holder.recipeCategoryItemCheckboxBinding.recipeCategoryCheckBox.isChecked = false
            }
            holder.recipeCategoryItemCheckboxBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {
            return mRecipeCategoryEntities.size
        }
        fun doCategorySelected( stringCategory:String,  categoriesEntity:ArrayList<RecipeCategoryEntity>):Boolean {

            return categoriesEntity.indexOfFirst { it.name == stringCategory } >= 0

        }

        inner class  ItemViewHolder(var recipeCategoryItemCheckboxBinding: RecipeCategoryItemCheckboxBinding, val recipesRecyclerviewViewAdapater: RecipeCategoryRecyclerviewViewAdapater): RecyclerView.ViewHolder(recipeCategoryItemCheckboxBinding.root),
            View.OnClickListener{

            init {
                recipeCategoryItemCheckboxBinding.recipeCategoryCheckBox.setOnClickListener(this)
            }


            override fun onClick(view: View?) {
                val checkBox = view as CheckBox
                val recipeCategory = recipesRecyclerviewViewAdapater.mRecipeCategoryEntities[adapterPosition]
                if(checkBox.isChecked){
                    checkBox.isChecked = true
                    mRecipeCurrentCategoryEntities.add(recipeCategory)
                }else{
                    checkBox.isChecked = false
                    mRecipeCurrentCategoryEntities.remove(recipeCategory)
                }
            }


        }


    }


    interface SelectRecipeCategoriesListener{
        fun onSelect(recipeCategories: ArrayList<RecipeCategoryEntity>)
    }
}