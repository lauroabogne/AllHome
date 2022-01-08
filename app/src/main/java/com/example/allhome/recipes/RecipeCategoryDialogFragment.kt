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
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment

import android.widget.EditText
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.data.entities.RecipeEntityWithTotalIngredient
import com.example.allhome.databinding.FilterByInformationDialogFragmentBinding
import com.example.allhome.databinding.RecipeCategoryDialogFragmentLayoutBinding
import com.example.allhome.databinding.RecipeCategoryItemBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.recipes.viewmodel.RecipeCategoryViewModel
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RecipeCategoryDialogFragment() : DialogFragment() {


    lateinit var mRecipeCategoryDialogFragmentLayoutBinding: RecipeCategoryDialogFragmentLayoutBinding
    lateinit var mRecipeCategoryViewModel:RecipeCategoryViewModel
    var mOnSelectCategoryListener:OnSelectCategoryListener? = null


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

        alertDialogBuilder.setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
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

            recipeCategoryEntities.add(0,RecipeCategoryEntity("1","All Recipe",0,0,"",""))
            withContext(Main){
                val recipeCategoryRecyclerviewViewAdapater = mRecipeCategoryDialogFragmentLayoutBinding.recipesRecyclerview.adapter as RecipeCategoryRecyclerviewViewAdapater
                recipeCategoryRecyclerviewViewAdapater.mRecipeCategoryEntities = recipeCategoryEntities
                recipeCategoryRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
    }
    fun setCategoryOnSelectListener( onSelectCategoryListener:OnSelectCategoryListener){
        mOnSelectCategoryListener = onSelectCategoryListener
    }

    class RecipeCategoryRecyclerviewViewAdapater(var mRecipeCategoryEntities:ArrayList<RecipeCategoryEntity>, val mRecipeCategoryDialogFragment: RecipeCategoryDialogFragment):
        RecyclerView.Adapter<RecipeCategoryRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutInflater = LayoutInflater.from(parent.context)
            val recipeItemBinding =  RecipeCategoryItemBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(recipeItemBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val recipeEntity = mRecipeCategoryEntities[position]

            holder.recipeCategoryItemBinding.recipeCategory = recipeEntity
            holder.recipeCategoryItemBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {
            return mRecipeCategoryEntities.size
        }


        inner class  ItemViewHolder(var recipeCategoryItemBinding: RecipeCategoryItemBinding, val recipesRecyclerviewViewAdapater: RecipeCategoryRecyclerviewViewAdapater): RecyclerView.ViewHolder(recipeCategoryItemBinding.root),
        View.OnClickListener{

            init {
                recipeCategoryItemBinding.root.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                val recipeCategory = recipesRecyclerviewViewAdapater.mRecipeCategoryEntities[adapterPosition]
                mRecipeCategoryDialogFragment.mOnSelectCategoryListener?.selected(recipeCategory)

            }
        }
    }

    interface OnSelectCategoryListener{
        fun selected(recipeCategory:RecipeCategoryEntity)
    }
}