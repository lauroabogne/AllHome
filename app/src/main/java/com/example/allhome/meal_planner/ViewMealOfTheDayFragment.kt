package com.example.allhome.meal_planner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.databinding.FragmentViewMealOfTheDayBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.recipes.RecipesFragment
import java.text.SimpleDateFormat
import java.util.*


class ViewMealOfTheDayFragment : Fragment() {

    private var mStringDateSelected:String? = null
    private var mDateSelected: Date? = null

    lateinit var mFragmentViewMealOfTheDayBinding:FragmentViewMealOfTheDayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mStringDateSelected = it.getString(DATE_SELECTED_PARAM)
            mDateSelected = SimpleDateFormat("yyyy-MM-dd").parse(mStringDateSelected)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentViewMealOfTheDayBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_meal_of_the_day, container, false)
        mFragmentViewMealOfTheDayBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        mFragmentViewMealOfTheDayBinding.toolbar.inflateMenu(R.menu.view_meal_of_the_day_menu)
        mDateSelected?.let {
            mFragmentViewMealOfTheDayBinding.toolbar.title = SimpleDateFormat("MMMM dd,yyyy").format(it)
        }

        mFragmentViewMealOfTheDayBinding.toolbar.setNavigationOnClickListener {
            activity?.finish()
        }
        mFragmentViewMealOfTheDayBinding.toolbar.setOnMenuItemClickListener(toolbarMenuItemClickListener)
        return mFragmentViewMealOfTheDayBinding.root
    }

    val toolbarMenuItemClickListener = object:Toolbar.OnMenuItemClickListener{
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.addMeal->{

                    Toast.makeText(requireContext(),"addToGrocer dasf asdf yList",Toast.LENGTH_SHORT).show()

                    var dialog = AddMealDialogFragment()
                    dialog.show(childFragmentManager,"AddMealDialogFragment")


                }
                R.id.addToGroceryList->{
                    Toast.makeText(requireContext(),"addToGroceryList",Toast.LENGTH_SHORT).show()




                }
            }
           return true
        }

    }
    companion object {

        const val DATE_SELECTED_PARAM = "DATE_SELECTED_PARAM"

        @JvmStatic fun newInstance(dateSelected: String) =
            ViewMealOfTheDayFragment().apply {
                arguments = Bundle().apply {
                    putString(DATE_SELECTED_PARAM, dateSelected)
                }
            }


    }
}


