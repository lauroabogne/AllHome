package com.example.allhome.meal_planner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.databinding.FragmentAddMealOptionBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AddMealOptionFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    lateinit var mFragmentAddMealOptionBinding:FragmentAddMealOptionBinding

    var mAddMealOptionFragmentSelectionListener:AddMealDialogFragment.AddMealOptionFragmentSelectionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mFragmentAddMealOptionBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_meal_option, container, false)
        mFragmentAddMealOptionBinding.quickRecipeButton.setOnClickListener {
            mAddMealOptionFragmentSelectionListener?.let { addMealOptionFragmentSelectionListener->
                addMealOptionFragmentSelectionListener.onSelect(it.id)
            }
        }

        mFragmentAddMealOptionBinding.recipeButton.setOnClickListener {
            mAddMealOptionFragmentSelectionListener?.let { addMealOptionFragmentSelectionListener->
                addMealOptionFragmentSelectionListener.onSelect(it.id)
            }
        }
        return mFragmentAddMealOptionBinding.root
    }


    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            AddMealOptionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}