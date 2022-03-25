package com.example.allhome.meal_planner

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.meal_planner.calendar.CalendarFragment
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.recipes.IngredientDialogFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MealPlannerFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    var mSelectedFragment:Fragment? = null
    lateinit var mMealPlannerViewModel:MealPlannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // hide option menu
        mMealPlannerViewModel = ViewModelProvider(this).get(MealPlannerViewModel::class.java)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        activity?.title = "Meal Planner"

        mSelectedFragment = CalendarFragment.newInstance("","")

        fragmentProcessor(mSelectedFragment!!)
        return inflater.inflate(R.layout.fragment_meal_planner, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar_meal_planner_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.addToGroceryList->{

                if(mSelectedFragment is CalendarFragment){
                    val selectedDateCalendar = (mSelectedFragment as CalendarFragment).getSelectedDate()
                    selectedDateCalendar?.let { calendar->

                        val totalDaysInMonth: Int = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        val year = calendar.get(java.util.Calendar.YEAR)
                        val numericMonth = SimpleDateFormat("MM").format(calendar.time)

                        val startDate = "${year}-${numericMonth}-01"
                        val endDate = "${year}-${numericMonth}-${totalDaysInMonth}"

                        mMealPlannerViewModel.mCoroutineScope.launch {

                                val uniqueIds = mMealPlannerViewModel.getRecipeUniqueIDs(requireContext(),startDate,endDate)
                                uniqueIds?.let{
                                    val ingredientDialogFragment = IngredientDialogFragment("Ingredients",uniqueIds as ArrayList<String>)
                                    ingredientDialogFragment.show(childFragmentManager,"ingredientDialogFragment")
                                }
                        }
                    }

                }

            }
        }

        return true
    }

    private fun fragmentProcessor(fragment: Fragment){
        childFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer,fragment).commit()
        }
    }

    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            MealPlannerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}