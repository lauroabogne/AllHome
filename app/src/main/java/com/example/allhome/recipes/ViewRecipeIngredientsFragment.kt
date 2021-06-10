package com.example.allhome.recipes

import android.content.Entity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentViewRecipeBinding
import com.example.allhome.databinding.FragmentViewRecipeIngredientsBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.databinding.ViewIngredientItemBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewRecipeIngredientsFragment : Fragment() {


    lateinit var mRecipeEntity:RecipeEntity
    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    lateinit var mFragmentViewRecipeIngredientsBinding:FragmentViewRecipeIngredientsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mRecipeEntity = it.getParcelable(RECIPE_INTENT_TAG)!!
        }

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mFragmentViewRecipeIngredientsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_recipe_ingredients, container, false)

        val recipeIngredientRecyclerviewViewAdapater = RecipeIngredientRecyclerviewViewAdapater(arrayListOf(),this)
        mFragmentViewRecipeIngredientsBinding.viewIngredientRecyclerview.adapter = recipeIngredientRecyclerviewViewAdapater

        mRecipesFragmentViewModel.mCoroutineScope.launch {
            val ingredients = mRecipesFragmentViewModel.getIngredients(requireContext(),mRecipeEntity.uniqueId)

            withContext(Main){
                recipeIngredientRecyclerviewViewAdapater.mIngredient = ingredients as ArrayList<IngredientEntity>
                recipeIngredientRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }
        return mFragmentViewRecipeIngredientsBinding.root
    }

    companion object {
        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"

        @JvmStatic fun newInstance(recipeEntity: RecipeEntity) =
            ViewRecipeIngredientsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG,recipeEntity)
                }
            }
    }



    class RecipeIngredientRecyclerviewViewAdapater(var mIngredient:ArrayList<IngredientEntity>, val mViewRecipeIngredientsFragment: ViewRecipeIngredientsFragment):
        RecyclerView.Adapter<RecipeIngredientRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutInflater = LayoutInflater.from(parent.context)
            val viewIngredientItemBinding =  ViewIngredientItemBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(viewIngredientItemBinding)




            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val recipeEntity = mIngredient[position]
            holder.setIngredientText(recipeEntity)


        }

        override fun getItemCount(): Int {

            return mIngredient.size
        }


        inner class  ItemViewHolder(val viewIngredientItemBinding:ViewIngredientItemBinding): RecyclerView.ViewHolder(viewIngredientItemBinding.root),View.OnClickListener{


            override fun onClick(view: View?) {
                Toast.makeText(view?.context,"Clicked", Toast.LENGTH_SHORT).show()

            }

            fun setIngredientText(ingredient:IngredientEntity){

                viewIngredientItemBinding.ingredientEntity = ingredient

            }

        }


    }
}