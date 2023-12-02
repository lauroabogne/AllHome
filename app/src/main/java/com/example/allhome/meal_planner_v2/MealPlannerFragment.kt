package com.example.allhome.meal_planner_v2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.R
import com.example.allhome.databinding.FragmentMealPlanner2Binding
import com.example.allhome.databinding.FragmentMealTypeBinding
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.meal_planner_v2.calendar.views.MonthView
import com.example.allhome.recipes.IngredientDialogFragment
import com.example.simplecalendar.calendar.CalendarPagerAdapter
import com.example.simplecalendar.calendar.MonthPagerItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MealPlannerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var calendarViewPager: ViewPager2? = null
    val numberOfMonths: Int = 151

    lateinit var mFragmentMealPlanner2Binding:FragmentMealPlanner2Binding
    private var mSelectedMealTypes:Array<String>? = null
    lateinit var mFragmentMealTypeBinding: FragmentMealTypeBinding
    var mOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    var mSelectedFragment:Fragment? = null
    lateinit var mMealPlannerViewModel:MealPlannerViewModel

    private val openViewMealOfTheDayActivityContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){

                val monthView = getCurrentVisibleAdapterItem()
                val calendarPagerAdapter:CalendarPagerAdapter = calendarViewPager!!.adapter as CalendarPagerAdapter

                monthView.getMonthPagerItem()?.let {
                    val index = calendarPagerAdapter.viewPagerItemArrayList.indexOf(it)
                    calendarPagerAdapter.notifyItemChanged(index)
                }

        }
    }

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        activity?.title = "Meal Planner"
        // Inflate the layout for this fragment
        mFragmentMealPlanner2Binding = DataBindingUtil.inflate(inflater, R.layout.fragment_meal_planner2, container, false)

        calendarViewPager = mFragmentMealPlanner2Binding.calendarViewPager

        val monthPagerItems = generateMonthPagerItems()
        val calendarPagerAdapter = CalendarPagerAdapter(monthPagerItems,requireContext())
        calendarPagerAdapter.setItemDateSelectedListener(object:CalendarPagerAdapter.ItemDateSelectedListener{
            override fun onDateSelected(date: Date, position: Int) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                val intent = Intent(requireContext(), ViewMealOfTheDayActivity::class.java)
                intent.putExtra(ViewMealOfTheDayFragment.DATE_SELECTED_PARAM,dateFormat.format(date))

                openViewMealOfTheDayActivityContract.launch(intent)
            }

        })

        calendarViewPager!!.adapter = calendarPagerAdapter
        calendarViewPager!!.clipToPadding = false
        calendarViewPager!!.clipChildren = false
        calendarViewPager!!.offscreenPageLimit = 1
        calendarViewPager!!.setCurrentItem(74, false)

        calendarViewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
              Log.e("Position selected","${position} ${ calendarViewPager!!.currentItem}")
                val monthView: MonthPagerItem = monthPagerItems.get(position)
                Log.e("Position selected","${ calendarViewPager!!.getChildAt(0)}")


            }
        })

        return mFragmentMealPlanner2Binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar_meal_planner_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun getCurrentVisibleAdapterItem(): MonthView {
        val recyclerView = calendarViewPager!!.getChildAt(0) as? RecyclerView
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
        val currentVisiblePosition = layoutManager?.findFirstVisibleItemPosition()
        val currentVisibleView = currentVisiblePosition?.let { layoutManager?.findViewByPosition(it) }

        return currentVisibleView!!.findViewById(R.id.monthView)
    }
    private fun generateMonthPagerItems(): Array<MonthPagerItem> {

        val calendar = Calendar.getInstance()
        // subtract 75 months to get the starting month
        calendar.add(Calendar.MONTH, -(numberOfMonths/2))


        val monthPagerItems = Array(numberOfMonths) { i ->
            calendar.add(Calendar.MONTH, 1)
            val month = calendar.clone() as Calendar
            MonthPagerItem(month)
        }

        return monthPagerItems

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.addToGroceryList->{
                val monthView = getCurrentVisibleAdapterItem()
                monthView.getMonthPagerItem()?.calendar?.let { calendar->
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


//                //if(mSelectedFragment is CalendarFragment){
//                    val selectedDateCalendar = (mSelectedFragment as CalendarFragment).getSelectedDate()
//                    selectedDateCalendar?.let { calendar1->
//
//                        val totalDaysInMonth: Int = calendar1.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
//                        val year = calendar1.get(java.util.Calendar.YEAR)
//                        val numericMonth = SimpleDateFormat("MM").format(calendar1.time)
//
//                        val startDate = "${year}-${numericMonth}-01"
//                        val endDate = "${year}-${numericMonth}-${totalDaysInMonth}"
//
//                        mMealPlannerViewModel.mCoroutineScope.launch {
//
//                            val uniqueIds = mMealPlannerViewModel.getRecipeUniqueIDs(requireContext(),startDate,endDate)
//                            uniqueIds?.let{
//                                val ingredientDialogFragment = IngredientDialogFragment("Ingredients",uniqueIds as ArrayList<String>)
//                                ingredientDialogFragment.show(childFragmentManager,"ingredientDialogFragment")
//                            }
//                        }
//                    }
//
//                //}

            }
        }

        return true
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