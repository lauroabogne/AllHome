package com.example.allhome.meal_planner.calendar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.MealTypes
import com.example.allhome.databinding.FragmentCalendarBinding
import com.example.allhome.meal_planner.ViewMealOfTheDayActivity
import com.example.allhome.meal_planner.ViewMealOfTheDayFragment
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.utils.NumberUtils
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CalendarFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mFragmentCalendarBinding: FragmentCalendarBinding
    lateinit var mMealPlannerViewModel: MealPlannerViewModel

    companion object {
        @JvmStatic fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mMealPlannerViewModel = ViewModelProvider(this).get(MealPlannerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentCalendarBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)

        val dates = generate3Fragment()
        val indexOfCurrentDate = getIndexOfCurrentDate(dates)
        val adapter = ViewPagerFragmentAdapter(dates, requireActivity().supportFragmentManager, lifecycle)

        mFragmentCalendarBinding.calendarViewPager.adapter = adapter
        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(indexOfCurrentDate,false)
        mFragmentCalendarBinding.calendarViewPager.offscreenPageLimit = 2
        setTotalCostOfTheMonth()

        mFragmentCalendarBinding.calendarViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if(position == 0){

                    mFragmentCalendarBinding.calendarViewPager.setUserInputEnabled(false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val firstFragment = adapter.fragmentList[0]

                        firstFragment.mCalendar = addMonth(firstFragment.mCalendar!!,-1)
                        firstFragment.generateData()

                        val secondFragment = adapter.fragmentList[1]
                        secondFragment.mCalendar = addMonth(secondFragment.mCalendar!!,-1)
                        secondFragment.generateData()

                        val thirdFragment = adapter.fragmentList[2]
                         thirdFragment.mCalendar = addMonth(thirdFragment.mCalendar!!,-1)
                        thirdFragment.generateData()

                        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(1,false)
                        mFragmentCalendarBinding.calendarViewPager.setUserInputEnabled(true)

                    },300)
                }
                if(position == 2){

                    mFragmentCalendarBinding.calendarViewPager.setUserInputEnabled(false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val firstFragment = adapter.fragmentList[0]
                        firstFragment.mCalendar = addMonth(firstFragment.mCalendar!!,1)
                        firstFragment.generateData()

                        val secondFragment = adapter.fragmentList[1]
                        secondFragment.mCalendar = addMonth(secondFragment.mCalendar!!,1)
                        secondFragment.generateData()

                        val thirdFragment = adapter.fragmentList[2]
                        thirdFragment.mCalendar = addMonth(thirdFragment.mCalendar!!,1)
                        thirdFragment.generateData()

                        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(1,false)
                        mFragmentCalendarBinding.calendarViewPager.setUserInputEnabled(true)
                    },300)
                }
                setTotalCostOfTheMonth()
            }
        })
        return mFragmentCalendarBinding.root
    }

    fun addMonth(calendar:java.util.Calendar,month:Int):java.util.Calendar{
        calendar.add(java.util.Calendar.MONTH,month)
        return calendar
    }

    fun getIndexOfCurrentDate(calendars:ArrayList<Calendar>):Int{
        val currenDateCalendar = java.util.Calendar.getInstance()
        return calendars.indexOfFirst {
            var fragmentCalendar = it.mCalendar!!
            currenDateCalendar.get(java.util.Calendar.YEAR) == fragmentCalendar.get(java.util.Calendar.YEAR)&& currenDateCalendar.get(java.util.Calendar.MONTH) == fragmentCalendar.get(java.util.Calendar.MONTH)

        }

    }
    fun getSelectedDate(): java.util.Calendar? {
        val viewPagerFragmentAdapter = mFragmentCalendarBinding.calendarViewPager.adapter as ViewPagerFragmentAdapter
        return viewPagerFragmentAdapter.fragmentList[1].mCalendar

    }
    fun generate3Fragment():ArrayList<Calendar>{

        val dates = arrayListOf<Calendar>()

        val lastMonthCalendar = java.util.Calendar.getInstance()
        lastMonthCalendar.add(java.util.Calendar.MONTH,-1)
        val lastMonthCalendarFragment = Calendar.newInstance("","")
        lastMonthCalendarFragment.mCalendarRenderingListener = calendarRenderingListener
        lastMonthCalendarFragment.mCalendar = lastMonthCalendar
        dates.add(lastMonthCalendarFragment)


        val currentMonthCalendar = java.util.Calendar.getInstance()
        val currentMonthFragment  = Calendar.newInstance("","")
        currentMonthFragment.mCalendarRenderingListener = calendarRenderingListener
        currentMonthFragment.mCalendar = currentMonthCalendar
        dates.add(currentMonthFragment)

        val nextMonthCalendar = java.util.Calendar.getInstance()
        nextMonthCalendar.add(java.util.Calendar.MONTH,1)
        val nextMonthCalendarFragment   = Calendar.newInstance("","")
        nextMonthCalendarFragment.mCalendarRenderingListener = calendarRenderingListener
        nextMonthCalendarFragment.mCalendar = nextMonthCalendar
        dates.add(nextMonthCalendarFragment)



        return dates
    }

    private fun showMealIndicator(indicatorTextView:TextView,mealPlans:List<MealTypes>){

        val builder = SpannableStringBuilder()

        mealPlans.forEachIndexed{index,mealType->

            if(mealType.type == MealEntity.BREAKFAST_TYPE){
                val redSpannable = SpannableString(".")
                redSpannable.setSpan(ForegroundColorSpan(Color.RED), 0, 1, 0)
                builder.append(redSpannable)
            }else if(mealType.type == MealEntity.LUNCK_TYPE){
                val redSpannable = SpannableString(".")
                redSpannable.setSpan(ForegroundColorSpan(Color.BLUE), 0, 1, 0)
                builder.append(redSpannable)
            }else if(mealType.type == MealEntity.DINNER_TYPE){
                val redSpannable = SpannableString(".")
                redSpannable.setSpan(ForegroundColorSpan(Color.GREEN), 0, 1, 0)
                builder.append(redSpannable)
            }else if(mealType.type == MealEntity.SNACK_AFTER_BREAKFAST_TYPE || mealType.type == MealEntity.SNACK_AFTERLUNCK_TYPE|| mealType.type == MealEntity.SNACK_AFTER_DINNER_TYPE){
                val redSpannable = SpannableString(".")
                redSpannable.setSpan(ForegroundColorSpan(Color.LTGRAY), 0, 1, 0)
                builder.append(redSpannable)
            }

        }
        /*mealPlans.forEach {
            //textView.setText(".")
            val redSpannable = SpannableString(".")
            redSpannable.setSpan(ForegroundColorSpan(Color.RED), 0, 1, 0)
            builder.append(redSpannable)
        }*/
        indicatorTextView.setText(builder,TextView.BufferType.SPANNABLE)
        indicatorTextView.visibility = View.VISIBLE

    }

    private fun setTotalCostOfTheMonth(){

        mFragmentCalendarBinding.totalCostConstraintLayout.visibility = View.GONE

        val adapater = mFragmentCalendarBinding.calendarViewPager.adapter as ViewPagerFragmentAdapter
        val calendarFragment = adapater.fragmentList[1] as Calendar
        calendarFragment.mCalendar?.let{ calendar->

            calendar.set(java.util.Calendar.DAY_OF_MONTH,1)

            val lastDayOfMonth =calendar.getActualMaximum(java.util.Calendar.DATE)
            val month = SimpleDateFormat("MM").format(calendar.time)
            val year = calendar.get(java.util.Calendar.YEAR)
            val firstDateOfMonth = "${year}-${month}-01"
            val lastDateOfMonth  = "${year}-${month}-${lastDayOfMonth}"

            mMealPlannerViewModel.mCoroutineScope.launch {
                val totalCostForTheMonth = mMealPlannerViewModel.getTotalCostInTheMonth(requireContext(),firstDateOfMonth,lastDateOfMonth)

                withContext(Main){
                    if(totalCostForTheMonth > 0.0){
                        mFragmentCalendarBinding.totalCostConstraintLayout.visibility = View.VISIBLE
                        mFragmentCalendarBinding.costTextView.setText("${NumberUtils.formatNumber(totalCostForTheMonth)}")
                    }

                }
            }


        }
    }

    val calendarRenderingListener  = object:Calendar.CalendarRenderingListener{
        override fun onDayRendering(dateString: String, dayHolder: ConstraintLayout) {
            dayHolder.setOnClickListener(calendarDayViewClickListener)
            val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView

            mMealPlannerViewModel.mCoroutineScope.launch {

                val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                withContext(Main){
                    showMealIndicator(textView,mealPlan)

                }
            }
            Log.e("dates","${dateString}")
        }

    }
    private val calendarDayViewClickListener = object:View.OnClickListener{
        override fun onClick(v: View?) {
            val dateString = v?.getTag()

            val intent = Intent(requireContext(), ViewMealOfTheDayActivity::class.java)
            intent.putExtra(ViewMealOfTheDayFragment.DATE_SELECTED_PARAM,dateString.toString())
            startActivity(intent)

        }

    }

    class ViewPagerFragmentAdapter(var fragmentList: ArrayList<Calendar>, fragmentManager: FragmentManager, lifecyle: Lifecycle) : FragmentStateAdapter(fragmentManager,lifecyle) {
         override fun onBindViewHolder(holder: FragmentViewHolder, position: Int, payloads: MutableList<Any>) {
            super.onBindViewHolder(holder, position, payloads)


        }
        override fun getItemCount(): Int {

            return fragmentList.size
        }

        override fun createFragment(position: Int): Calendar {
            val fragment = fragmentList[position]
            return fragment
        }

    }
}