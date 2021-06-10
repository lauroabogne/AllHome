package com.example.allhome.recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.databinding.FragmentViewRecipeStepsBinding
import com.example.allhome.databinding.RecipeStepTextviewBinding
import com.example.allhome.databinding.ViewIngredientItemBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ViewRecipeStepsFragment : Fragment() {

    lateinit var mRecipeEntity:RecipeEntity
    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    lateinit var mFragmentViewRecipeStepsBinding:FragmentViewRecipeStepsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mRecipeEntity = it.getParcelable(RECIPE_INTENT_TAG)!!

        }
        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentViewRecipeStepsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_recipe_steps, container, false)

        val recipeSteptRecyclerviewViewAdapater = RecipeSteptRecyclerviewViewAdapater( arrayListOf<RecipeStepEntity>(),this)
        mFragmentViewRecipeStepsBinding.viewStepRecyclerview.adapter = recipeSteptRecyclerviewViewAdapater

        mRecipesFragmentViewModel.mCoroutineScope.launch {
            val steps = mRecipesFragmentViewModel.getSteps(requireContext(),mRecipeEntity.uniqueId)
            withContext(Main){
                recipeSteptRecyclerviewViewAdapater.mSteps = steps as ArrayList<RecipeStepEntity>
                recipeSteptRecyclerviewViewAdapater.notifyDataSetChanged()
            }

        }


        return mFragmentViewRecipeStepsBinding.root
    }

    companion object {
        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"
        @JvmStatic fun newInstance(recipeEntity: RecipeEntity) =
            ViewRecipeStepsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)
                }
            }
    }


    class RecipeSteptRecyclerviewViewAdapater(var mSteps:ArrayList<RecipeStepEntity>, val mViewRecipeStepsFragment: ViewRecipeStepsFragment):
        RecyclerView.Adapter<RecipeSteptRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutInflater = LayoutInflater.from(parent.context)

            val recipeStepTextviewBinding =  RecipeStepTextviewBinding.inflate(layoutInflater,parent,false)
            val itemViewHolder = ItemViewHolder(recipeStepTextviewBinding)




            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val step = mSteps[position]
            holder.setStep(step)


        }

        override fun getItemCount(): Int {

            return mSteps.size
        }


        inner class  ItemViewHolder(val recipeStepTextviewBinding: RecipeStepTextviewBinding): RecyclerView.ViewHolder(recipeStepTextviewBinding.root),View.OnClickListener{


            override fun onClick(view: View?) {
                Toast.makeText(view?.context,"Clicked", Toast.LENGTH_SHORT).show()

            }

            fun setStep(recipeStepEntity: RecipeStepEntity){

                recipeStepTextviewBinding.recipeStepEntity = recipeStepEntity

            }

        }


    }
}