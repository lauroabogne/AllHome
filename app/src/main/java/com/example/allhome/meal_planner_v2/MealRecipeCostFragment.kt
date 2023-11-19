package com.example.allhome.meal_planner_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.databinding.AddMealDialogFragmentBinding
import com.example.allhome.databinding.FragmentMealRecipeCostBinding
import com.example.allhome.databinding.FragmentMealRecipeCostBindingImpl

private const val SELECTED_RECIPE_ARG = "SELECTED_RECIPE_ARG"

class MealRecipeCostFragment : Fragment() {
    private var selectedRecipeName: String? = null
    lateinit var mFragmentMealRecipeCostBinding: FragmentMealRecipeCostBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedRecipeName = it.getString(SELECTED_RECIPE_ARG)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val inflater = LayoutInflater.from(requireContext())
         mFragmentMealRecipeCostBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_meal_recipe_cost,null,false)

        mFragmentMealRecipeCostBinding.selectedRecipeTextView.text = selectedRecipeName

        return mFragmentMealRecipeCostBinding.root
    }

    fun getCost():Double{
        val costString =  mFragmentMealRecipeCostBinding.estimateCostTextinputeditedit.text.toString().trim()
        if(costString.isEmpty()){
            return 0.0
        }

        return costString.toDouble()
    }

    companion object {

        @JvmStatic fun newInstance(selectedRecipeName: String) =
            MealRecipeCostFragment().apply {
                arguments = Bundle().apply {
                    putString(SELECTED_RECIPE_ARG, selectedRecipeName)

                }
            }
    }
}