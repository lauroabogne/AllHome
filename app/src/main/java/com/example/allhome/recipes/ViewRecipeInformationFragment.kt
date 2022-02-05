package com.example.allhome.recipes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentAddRecipeInformationBinding
import com.example.allhome.databinding.FragmentViewRecipeBinding
import com.example.allhome.databinding.FragmentViewRecipeInformationBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ViewRecipeInformationFragment : Fragment() {
    private lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    private lateinit var mRecipeCategories:List<RecipeCategoryEntity>
    private lateinit var mFragmentViewRecipeInformationBinding: FragmentViewRecipeInformationBinding
    lateinit var mRecipeEntity: RecipeEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mRecipeEntity = it.getParcelable(RECIPE_INTENT_TAG)!!

        }

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentViewRecipeInformationBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_recipe_information, container, false)
        mFragmentViewRecipeInformationBinding.recipeEntity = mRecipeEntity
        mRecipesFragmentViewModel.mCoroutineScope.launch {
            mRecipeCategories = mRecipesFragmentViewModel.getRecipeCategories(requireContext(),mRecipeEntity.uniqueId)
            val concatinatedCategories = mRecipeCategories.map { it.name }.joinToString(",")
            with(Main){
                mFragmentViewRecipeInformationBinding.category = concatinatedCategories
            }
        }
        return mFragmentViewRecipeInformationBinding.root
    }

    companion object {
        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"
        @JvmStatic fun newInstance(recipeEntity: RecipeEntity) =
            ViewRecipeInformationFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG,recipeEntity)
                }
            }
    }
}