package com.example.allhome.meal_planner.calendar

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.MealTypes
import com.example.allhome.databinding.CalendarBinding
import com.example.allhome.meal_planner.ViewMealOfTheDayActivity
import com.example.allhome.meal_planner.ViewMealOfTheDayFragment
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.utils.NumberUtils
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Calendar : Fragment() {

    //val mCoroutineScope = CoroutineScope(CoroutineName("MealPlannerCalendarLoaded"))
    private var param1: String? = null
    private var param2: String? = null
    var mCalendar:java.util.Calendar? = null
    val mWeekViews:ArrayList<View> = arrayListOf()
    lateinit var mCalendarBinding:CalendarBinding
    lateinit var mCurrentDateString:String

    var mCalendarRenderingListener:CalendarRenderingListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mCalendarBinding =  DataBindingUtil.inflate(inflater, R.layout.calendar, container, false)

        val currentDateCalendar = java.util.Calendar.getInstance();
        mCurrentDateString = SimpleDateFormat("yyyy-MM-dd").format(currentDateCalendar.time)

        generateData()
        return mCalendarBinding.root
    }
    fun generateData(){

        mCalendar?.let{
            it.set(java.util.Calendar.DAY_OF_MONTH,1)

            val numberOfWeeksInMonth = it.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH)
            val firstDayOfMonth: Int = it.get(java.util.Calendar.DAY_OF_WEEK)
            val totalDaysInMonth: Int = it.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val year = it.get(java.util.Calendar.YEAR)

            val monthName = SimpleDateFormat("MMMM").format(it.time)
            val numericMonth = SimpleDateFormat("MM").format(it.time)

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
                    val constraintLayout = mCalendarBinding.firstWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE

                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        val currentDateIndicator = dayHolder.findViewById<View>(R.id.divider)

                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        currentDateIndicator.visibility = View.INVISIBLE

                        if (x+1 < firstDayOfMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}

                        val dateString = "${year}-${numericMonth}-${dayWithZero}"
                        dayHolder.setTag(dateString)
                        showCurrentDateIndicator(dateString,currentDateIndicator)
                        mCalendarRenderingListener?.let {
                            it.onDayRendering(dateString,dayHolder)
                        }
                    }


                }else if(i == 2){
                    val constraintLayout = mCalendarBinding.secondWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        val currentDateIndicator = dayHolder.findViewById<View>(R.id.divider)

                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        currentDateIndicator.visibility = View.INVISIBLE

                        if (totalDays >= totalDaysInMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                        val dateString = "${year}-${numericMonth}-${dayWithZero}"
                       dayHolder.setTag(dateString)
                        showCurrentDateIndicator(dateString,currentDateIndicator)
                        mCalendarRenderingListener?.let {
                            it.onDayRendering(dateString,dayHolder)
                        }

                    }
                }else if(i == 3){
                    val constraintLayout = mCalendarBinding.thirdWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        val currentDateIndicator = dayHolder.findViewById<View>(R.id.divider)

                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        currentDateIndicator.visibility = View.INVISIBLE

                        if (totalDays >= totalDaysInMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")



                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                        val dateString = "${year}-${numericMonth}-${dayWithZero}"
                        dayHolder.setTag(dateString)
                        showCurrentDateIndicator(dateString,currentDateIndicator)
                        mCalendarRenderingListener?.let {
                            it.onDayRendering(dateString,dayHolder)
                        }

                    }
                }else if(i == 4){
                    val constraintLayout = mCalendarBinding.forthWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        val currentDateIndicator = dayHolder.findViewById<View>(R.id.divider)

                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        currentDateIndicator.visibility = View.INVISIBLE

                        if (totalDays >= totalDaysInMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")


                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                        val dateString = "${year}-${numericMonth}-${dayWithZero}"
                        dayHolder.setTag(dateString)
                        showCurrentDateIndicator(dateString,currentDateIndicator)
                        mCalendarRenderingListener?.let {
                            it.onDayRendering(dateString,dayHolder)
                        }

                    }
                }else if(i == 5){
                    val constraintLayout = mCalendarBinding.fifthWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        val currentDateIndicator = dayHolder.findViewById<View>(R.id.divider)

                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        currentDateIndicator.visibility = View.INVISIBLE

                        if (totalDays >= totalDaysInMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")

                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                        val dateString = "${year}-${numericMonth}-${dayWithZero}"
                        dayHolder.setTag(dateString)
                        showCurrentDateIndicator(dateString,currentDateIndicator)
                        mCalendarRenderingListener?.let {
                            it.onDayRendering(dateString,dayHolder)
                        }

                    }
                }else if(i == 6){
                    val constraintLayout = mCalendarBinding.sixthWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE

                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        val currentDateIndicator = dayHolder.findViewById<View>(R.id.divider)

                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        currentDateIndicator.visibility = View.INVISIBLE

                        if (totalDays >= totalDaysInMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")

                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                        val dateString = "${year}-${numericMonth}-${dayWithZero}"
                        showCurrentDateIndicator(dateString,currentDateIndicator)
                        dayHolder.setTag(dateString)
                        mCalendarRenderingListener?.let {
                            it.onDayRendering(dateString,dayHolder)
                        }
                    }
                }
            }
        }


    }
    private fun showCurrentDateIndicator(dateString:String,view:View){

        if(mCurrentDateString.equals(dateString)){
            view.visibility = View.VISIBLE
        }
    }


    /**
     * @todo be implemented on CalendarFragment.kt
     */
    /*private fun setTotalCostOfTheMonth(){

        mCalendarBinding.totalCostConstraintLayout.visibility = View.GONE

        mCalendar?.let{ calendar->

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
                        mCalendarBinding.totalCostConstraintLayout.visibility = View.VISIBLE
                        mCalendarBinding.costTextView.setText("${NumberUtils.formatNumber(totalCostForTheMonth)}")
                    }

                }
            }


        }
    }*/
    private fun setCircleBackground(view: View) {
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle))
        } else {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.calendar_circle)
        }
    }


   /* private val dayViewClickListener = object:View.OnClickListener{
        override fun onClick(v: View?) {
            val dateString = v?.getTag()

            val intent = Intent(requireContext(), ViewMealOfTheDayActivity::class.java)
            intent.putExtra(ViewMealOfTheDayFragment.DATE_SELECTED_PARAM,dateString.toString())
            startActivity(intent)

        }

    }*/
    companion object {
        @JvmStatic fun newInstance(param1: String, param2: String) =
            Calendar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    interface CalendarRenderingListener{
        fun onDayRendering(dateString:String,dayHolder:ConstraintLayout)
    }
}