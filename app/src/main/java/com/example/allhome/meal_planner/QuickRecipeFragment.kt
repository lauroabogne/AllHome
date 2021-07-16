package com.example.allhome.meal_planner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.databinding.FragmentQuickRecipeBinding


private const val NAME = "NAME"
private const val COST = "COST"


class QuickRecipeFragment : Fragment() {

    private var mName: String? = null
    private var mCost: Double = 0.0
    lateinit var mFragmentQuickRecipeBinding:FragmentQuickRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mName = it.getString(NAME)
            mCost = it.getDouble(COST)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mFragmentQuickRecipeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quick_recipe, container, false)
        mFragmentQuickRecipeBinding.quickRecipeTextInput.setText(mName)
        mFragmentQuickRecipeBinding.quickRecipeCostTextInput.setText(if(mCost ==0.0) "" else mCost.toString())
        return mFragmentQuickRecipeBinding.root
    }

    companion object {

        @JvmStatic fun newInstance(name: String, cost: Double) =
            QuickRecipeFragment().apply {
                arguments = Bundle().apply {
                    putString(NAME, name)
                    putDouble(COST, cost)
                }
            }
    }
}