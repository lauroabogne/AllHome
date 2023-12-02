package com.example.simplecalendar.calendar

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.meal_planner.calendar.CalendarFragment
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.meal_planner_v2.MonthDate
import com.example.allhome.meal_planner_v2.calendar.views.MonthView
import com.example.allhome.utils.NumberUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class CalendarPagerAdapter(var viewPagerItemArrayList: Array<MonthPagerItem>,var context: Context) :
    RecyclerView.Adapter<CalendarPagerAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMMM yyyy")
    private val dateFormat_ = SimpleDateFormat("yyyy-MM-dd")
    private var itemDateSelectedListener: ItemDateSelectedListener? = null
    private lateinit var mealPlannerViewModel: MealPlannerViewModel


    init {
        setupViewModel()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewPagerItem = viewPagerItemArrayList[position]
        val monthView = holder.monthView

        mealPlannerViewModel.mCoroutineScope.launch {

                val clonedCalendar:Calendar = Calendar.getInstance()
                clonedCalendar.set(Calendar.YEAR,viewPagerItem.calendar.get(Calendar.YEAR))
                clonedCalendar.set(Calendar.MONTH,viewPagerItem.calendar.get(Calendar.MONTH))

                clonedCalendar.set(Calendar.DAY_OF_MONTH, 1)
                val firstDayOfMonth = clonedCalendar.time

                // Get the last day of the month
                 clonedCalendar.set(Calendar.DAY_OF_MONTH, clonedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val lastDayOfMonth = clonedCalendar.time

                // Format the dates

                val formattedFirstDayOfMonth = dateFormat_.format(firstDayOfMonth)
                val formattedLastDayOfMonth = dateFormat_.format(lastDayOfMonth)

                val dates = createCalendarArray(viewPagerItem.calendar.get(Calendar.YEAR), viewPagerItem.calendar.get(Calendar.MONTH))

                val totalCostForTheMonth = mealPlannerViewModel.getTotalCostInTheMonth(context,formattedFirstDayOfMonth,formattedLastDayOfMonth)
                withContext(Dispatchers.Main){

                    monthView.setYearMonthAndDates( viewPagerItem,dates)
                    val formattedDate = dateFormat.format(viewPagerItem.calendar.time)
                    holder.monthView.postDelayed({
                        holder.dateTextView.text = formattedDate
                        holder.totalCostTextView.text = "Meal cost for month : ${NumberUtils.formatNumber(totalCostForTheMonth)}"
                    },100)


           }
        }

    }


    override fun getItemCount(): Int {

        return viewPagerItemArrayList.size
    }

    fun setItemDateSelectedListener(itemDateSelectedListener: ItemDateSelectedListener){
        this.itemDateSelectedListener = itemDateSelectedListener
    }

    private fun setupViewModel() {
        val activity = context as? AppCompatActivity
        activity?.let {
            val viewModelProvider = ViewModelProvider(it)
            mealPlannerViewModel = viewModelProvider.get(MealPlannerViewModel::class.java)
            // Use the mealPlannerViewModel instance as needed within the custom view
        }
    }
    private suspend fun createCalendarArray(year: Int, month: Int): Array<Array<MonthDate?>>? {


        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // Shift the first day to Monday
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if(MonthView.startOfWeek() == Calendar.SUNDAY){
            val daysToShift = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - 1
            calendar.add(Calendar.DAY_OF_MONTH, -daysToShift)
        }else{
            val daysToShift = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
            calendar.add(Calendar.DAY_OF_MONTH, -daysToShift)
        }

        // Create a 2D array to hold the calendar
        val calendarArray:Array<Array<MonthDate?>>? = Array(MonthView.numberOfColumns) { row ->
            Array(MonthView.numberOfRows) { col ->
                val date = calendar.time
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                // get meal types in the day
                val mealPlan = mealPlannerViewModel.getMealPlanForDay(context,SimpleDateFormat("yyyy-MM-dd").format(date.time))
                MonthDate(date, mealPlan)

            }
        }


        return calendarArray
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var monthView: MonthView
        var dateTextView:TextView
        var totalCostTextView:TextView

        init {
            monthView = itemView.findViewById(R.id.monthView)
            dateTextView = itemView.findViewById(R.id.dateTextView)
            totalCostTextView = itemView.findViewById(R.id.totalCostTextView)

            monthView.setOnDateSelectedListener(object:MonthView.OnDateSelectedListener{
                override fun dateSelected(date: Date, monthView: MonthView) {

                    viewPagerItemArrayList[adapterPosition].selectedDate = date
                    itemDateSelectedListener?.let{
                        it.onDateSelected(date,adapterPosition)
                    }
                }
            })
        }
    }
    interface ItemDateSelectedListener{
        fun onDateSelected (date: Date, position: Int)
    }
}