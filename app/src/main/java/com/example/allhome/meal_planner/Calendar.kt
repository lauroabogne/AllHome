package com.example.allhome.meal_planner

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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.databinding.CalendarBinding
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
    var mDate:Date? = null
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


        //mMainView.addView(inflater.inflate(R.layout.calendar_day_name_header,null,false))
        //mMainView.setBackgroundColor(Color.RED)
        //val linearLayoutCompat = TextView(requireContext())
        generateData()
        return mCalendarBinding.root
    }
    fun generateData(){

        val calendar = java.util.Calendar.getInstance()
        calendar.time = mDate
        calendar.set(java.util.Calendar.DAY_OF_MONTH,1)
        val numberOfWeeksInMonth = calendar.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH)
        val firstDayOfMonth: Int = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val totalDaysInMonth: Int = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)


        val monthName = SimpleDateFormat("MMMM").format(calendar.time)


        mCalendarBinding.dateTextView.setText("${monthName}")
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
                }

                if(hasVisibleView){
                    //mMainView.addView(view)
                    //mWeekViews.add(view)
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

                }
            }
        }

       /* mCalendarBinding.button.setText("${mDate}  ${numberOfWeeksInMonth} ${firstDayOfMonth}")
        mCalendarBinding.firstWeek.day1.dayTextView.setText("1")
        mCalendarBinding.firstWeek.day2.dayTextView.setText("2")
        mCalendarBinding.firstWeek.day3.dayTextView.setText("3")
        mCalendarBinding.firstWeek.day4.dayTextView.setText("4")
        mCalendarBinding.firstWeek.day5.dayTextView.setText("5")
        mCalendarBinding.firstWeek.day6.dayTextView.setText("6")
        mCalendarBinding.firstWeek.day7.dayTextView.setText("7")

        mCalendarBinding.secondWeek.day1.dayTextView.setText("1")
        mCalendarBinding.secondWeek.day2.dayTextView.setText("2")
        mCalendarBinding.secondWeek.day3.dayTextView.setText("3")
        mCalendarBinding.secondWeek.day4.dayTextView.setText("4")
        mCalendarBinding.secondWeek.day5.dayTextView.setText("5")
        mCalendarBinding.secondWeek.day6.dayTextView.setText("6")
        mCalendarBinding.secondWeek.day7.dayTextView.setText("7")

        mCalendarBinding.thirdWeek.day1.dayTextView.setText("1")
        mCalendarBinding.thirdWeek.day2.dayTextView.setText("2")
        mCalendarBinding.thirdWeek.day3.dayTextView.setText("3")
        mCalendarBinding.thirdWeek.day4.dayTextView.setText("4")
        mCalendarBinding.thirdWeek.day5.dayTextView.setText("5")
        mCalendarBinding.thirdWeek.day6.dayTextView.setText("6")
        mCalendarBinding.thirdWeek.day7.dayTextView.setText("7")

        mCalendarBinding.forthWeek.day1.dayTextView.setText("1")
        mCalendarBinding.forthWeek.day2.dayTextView.setText("2")
        mCalendarBinding.forthWeek.day3.dayTextView.setText("3")
        mCalendarBinding.forthWeek.day4.dayTextView.setText("4")
        mCalendarBinding.forthWeek.day5.dayTextView.setText("5")
        mCalendarBinding.forthWeek.day6.dayTextView.setText("6")
        mCalendarBinding.forthWeek.day7.dayTextView.setText("7")

        mCalendarBinding.fifthWeek.day1.dayTextView.setText("1")
        mCalendarBinding.fifthWeek.day2.dayTextView.setText("2")
        mCalendarBinding.fifthWeek.day3.dayTextView.setText("3")
        mCalendarBinding.fifthWeek.day4.dayTextView.setText("4")
        mCalendarBinding.fifthWeek.day5.dayTextView.setText("5")
        mCalendarBinding.fifthWeek.day6.dayTextView.setText("6")
        mCalendarBinding.fifthWeek.day7.dayTextView.setText("7")


        mCalendarBinding.sixthWeek.day1.dayTextView.setText("1")
        mCalendarBinding.sixthWeek.day2.dayTextView.setText("2")
        mCalendarBinding.sixthWeek.day3.dayTextView.setText("3")
        mCalendarBinding.sixthWeek.day4.dayTextView.setText("4")
        mCalendarBinding.sixthWeek.day5.dayTextView.setText("5")
        mCalendarBinding.sixthWeek.day6.dayTextView.setText("6")
        mCalendarBinding.sixthWeek.day7.dayTextView.setText("7")*/

       // mMainView.findViewById<Button>(R.id.button).setText("${mDate}  ${numberOfWeeksInMonth} ${firstDayOfMonth}")


    }
    fun generateData_backup(){

        /*val calendar = java.util.Calendar.getInstance()
        calendar.time = mDate
        calendar.set(java.util.Calendar.DAY_OF_MONTH,1)
        val numberOfWeeksInMonth = calendar.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH)
        val firstDayOfMonth: Int = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val totalDaysInMonth: Int = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)


        mMainView.findViewById<Button>(R.id.button).setText("${mDate}  ${numberOfWeeksInMonth} ${firstDayOfMonth}")

        mWeekViews.forEach {
            mMainView.removeView(it)
        }
        *//* val coroutineScope = CoroutineScope(CoroutineName("Fragment"))
         coroutineScope.launch {
             delay(500)
             withContext(Main){*//*
        var totalDays = 0
        for (i in 1..6){

            val view:ConstraintLayout = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_week,null,false) as ConstraintLayout

            if(i==1){
                var hasVisibleView = false
                for(x in 0..6){
                    val dayView = view.getChildAt(x)
                    if (x+1 < firstDayOfMonth) {
                        continue
                    }
                    hasVisibleView = true
                    totalDays++
                    dayView.visibility= View.VISIBLE
                    dayView.findViewById<TextView>(R.id.dayTextView).setText("${totalDays}")
                }
                if(hasVisibleView){
                    mMainView.addView(view)
                    mWeekViews.add(view)
                }

            }else{
                for(x in 0..6){
                    if (totalDays >= totalDaysInMonth) {
                        break
                    }
                    totalDays++
                    val dayView = view.getChildAt(x)
                    dayView.visibility= View.VISIBLE
                    dayView.findViewById<TextView>(R.id.dayTextView).setText("${totalDays}")
                }
                mMainView.addView(view)
                mWeekViews.add(view)
            }
            *//*    }
            }*//*
        }*/





    }
    private fun setCircleBackground(view: View) {
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle))
        } else {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle)
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