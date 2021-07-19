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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Calendar : Fragment() {

    val mCoroutineScope = CoroutineScope(CoroutineName("MealPlannerCalendarLoaded"))
    private var param1: String? = null
    private var param2: String? = null
    var mCalendar:java.util.Calendar? = null
    lateinit var mMealPlannerViewModel:MealPlannerViewModel


    val mWeekViews:ArrayList<View> = arrayListOf()


    lateinit var mCalendarBinding:CalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mMealPlannerViewModel = ViewModelProvider(this).get(MealPlannerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mCalendarBinding =  DataBindingUtil.inflate(inflater, R.layout.calendar, container, false)
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
                        textView.text = ""
                        textView.visibility = View.INVISIBLE
                        if (x+1 < firstDayOfMonth) {
                            dayHolder.visibility = View.INVISIBLE
                            continue
                        }
                        totalDays++
                        dayHolder.visibility = View.VISIBLE
                        (dayHolder.getChildAt(0) as TextView).setText("${totalDays}")

                        dayHolder.setOnClickListener(dayViewClickListener)
                        val dayWithZero = if(totalDays <=9) "0${totalDays}" else{ totalDays}
                        dayHolder.setTag("${year}-${numericMonth}-${dayWithZero}")

                        mCoroutineScope.launch {

                            val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                            withContext(Main){
                                showIndicator(textView,mealPlan)

                            }
                        }
                    }


                }else if(i == 2){
                    val constraintLayout = mCalendarBinding.secondWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        textView.text = ""
                        textView.visibility = View.INVISIBLE

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

                        mCoroutineScope.launch {

                            val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                            withContext(Main){
                                showIndicator(textView,mealPlan)

                            }
                        }

                    }
                }else if(i == 3){
                    val constraintLayout = mCalendarBinding.thirdWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        textView.text = ""
                        textView.visibility = View.INVISIBLE

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

                        mCoroutineScope.launch {

                            val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                            withContext(Main){
                                showIndicator(textView,mealPlan)

                            }
                        }

                    }
                }else if(i == 4){
                    val constraintLayout = mCalendarBinding.forthWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        textView.text = ""
                        textView.visibility = View.INVISIBLE

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

                        mCoroutineScope.launch {

                            val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                            withContext(Main){
                                showIndicator(textView,mealPlan)

                            }
                        }

                    }
                }else if(i == 5){
                    val constraintLayout = mCalendarBinding.fifthWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        textView.text = ""
                        textView.visibility = View.INVISIBLE

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

                        mCoroutineScope.launch {

                            val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                            withContext(Main){
                                showIndicator(textView,mealPlan)

                            }
                        }

                    }
                }else if(i == 6){
                    val constraintLayout = mCalendarBinding.sixthWeek.root as ConstraintLayout
                    constraintLayout.visibility = View.VISIBLE
                    for(x in 0..6){

                        val dayHolder = constraintLayout.getChildAt(x) as ConstraintLayout
                        val textView = dayHolder.findViewById<View>(R.id.indicatorTextView) as TextView
                        textView.text = ""
                        textView.visibility = View.INVISIBLE

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

                        mCoroutineScope.launch {

                            val mealPlan = mMealPlannerViewModel.getMealPlanForDay(requireContext(),dayHolder.tag.toString())
                            withContext(Main){
                                showIndicator(textView,mealPlan)

                            }
                        }
                    }
                }
            }
        }


    }
    fun showIndicator(indicatorTextView:TextView,mealPlans:List<MealTypes>){

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
            val dateString = v?.getTag()

            val intent = Intent(requireContext(), ViewMealOfTheDayActivity::class.java)
            intent.putExtra(ViewMealOfTheDayFragment.DATE_SELECTED_PARAM,dateString.toString())
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