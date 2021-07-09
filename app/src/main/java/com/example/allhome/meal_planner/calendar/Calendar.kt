package com.example.allhome.meal_planner.calendar

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.databinding.CalendarBinding
import com.example.allhome.meal_planner.ViewMealOfTheDayActivity
import com.example.allhome.recipes.AddRecipeActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Calendar : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mCalendar:java.util.Calendar
    //var mDate:Date? = null


    val mWeekViews:ArrayList<View> = arrayListOf()


    lateinit var mCalendarBinding:CalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mCalendarBinding =  DataBindingUtil.inflate(inflater, R.layout.calendar, container, false)

        generateData()
        return mCalendarBinding.root
    }
    fun generateData(){

        val calendar = mCalendar
        calendar.set(java.util.Calendar.DAY_OF_MONTH,1)

        val numberOfWeeksInMonth = calendar.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH)
        val firstDayOfMonth: Int = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val totalDaysInMonth: Int = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val year = calendar.get(java.util.Calendar.YEAR)

        val monthName = SimpleDateFormat("MMMM").format(calendar.time)
        val numericMonth = SimpleDateFormat("MM").format(calendar.time)

        mCalendarBinding.dateTextView.setText("${monthName} ${year}")
        mCalendarBinding.firstWeek.root.visibility = View.GONE
        mCalendarBinding.secondWeek.root.visibility = View.GONE
        mCalendarBinding.thirdWeek.root.visibility = View.GONE
        mCalendarBinding.forthWeek.root.visibility = View.GONE
        mCalendarBinding.fifthWeek.root.visibility = View.GONE
        mCalendarBinding.sixthWeek.root.visibility = View.GONE

        var totalDays = 0
        for (i in 1..numberOfWeeksInMonth){
            if(i==1){
                var hasVisibleView = false
                val constraintLayout = mCalendarBinding.firstWeek.root as ConstraintLayout
                constraintLayout.visibility = View.VISIBLE
                for(x in 0..6){

                    val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                    if (x+1 < firstDayOfMonth) {
                        dayHolder.visibility = View.INVISIBLE
                        continue
                    }
                    hasVisibleView = true
                    totalDays++
                    dayHolder.visibility = View.VISIBLE
                    (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                    dayHolder.setOnClickListener(dayViewClickListener)
                    val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                    dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")

                }


            }else if(i == 2){
                val constraintLayout = mCalendarBinding.secondWeek.root as ConstraintLayout
                constraintLayout.visibility = View.VISIBLE
                for(x in 0..6){

                    val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                    if (totalDays >= totalDaysInMonth) {
                        dayHolder.visibility = View.INVISIBLE
                        continue
                    }
                    totalDays++
                    dayHolder.visibility = View.VISIBLE
                    (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")

                    dayHolder.setOnClickListener(dayViewClickListener)
                    val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                    dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")


                }
            }else if(i == 3){
                val constraintLayout = mCalendarBinding.thirdWeek.root as ConstraintLayout
                constraintLayout.visibility = View.VISIBLE
                for(x in 0..6){

                    val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                    if (totalDays >= totalDaysInMonth) {
                        dayHolder.visibility = View.INVISIBLE
                        continue
                    }
                    totalDays++
                    dayHolder.visibility = View.VISIBLE
                    (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                    dayHolder.setOnClickListener(dayViewClickListener)
                    val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                    dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")


                }
            }else if(i == 4){
                val constraintLayout = mCalendarBinding.forthWeek.root as ConstraintLayout
                constraintLayout.visibility = View.VISIBLE
                for(x in 0..6){

                    val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                    if (totalDays >= totalDaysInMonth) {
                        dayHolder.visibility = View.INVISIBLE
                        continue
                    }
                    totalDays++
                    dayHolder.visibility = View.VISIBLE
                    (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                    dayHolder.setOnClickListener(dayViewClickListener)
                    val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                    dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")

                }
            }else if(i == 5){
                val constraintLayout = mCalendarBinding.fifthWeek.root as ConstraintLayout
                constraintLayout.visibility = View.VISIBLE
                for(x in 0..6){

                    val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                    if (totalDays >= totalDaysInMonth) {
                        dayHolder.visibility = View.INVISIBLE
                        continue
                    }
                    totalDays++
                    dayHolder.visibility = View.VISIBLE
                    (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                    dayHolder.setOnClickListener(dayViewClickListener)
                    val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                    dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")

                }
            }else if(i == 6){
                val constraintLayout = mCalendarBinding.sixthWeek.root as ConstraintLayout
                constraintLayout.visibility = View.VISIBLE
                for(x in 0..6){

                    val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                    if (totalDays >= totalDaysInMonth) {
                        dayHolder.visibility = View.INVISIBLE
                        continue
                    }
                    totalDays++
                    dayHolder.visibility = View.VISIBLE
                    (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                    dayHolder.setOnClickListener(dayViewClickListener)
                    val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                    dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")

                }
            }
        }

    }

    private fun setCircleBackground(view: View) {
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle))
        } else {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle)
        }
    }


    val dayViewClickListener = object:View.OnClickListener{
        override fun onClick(v: View?) {
            val tag = v?.getTag()

            val intent = Intent(requireContext(), ViewMealOfTheDayActivity::class.java)
            startActivity(intent)

        }

    }
    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            Calendar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}