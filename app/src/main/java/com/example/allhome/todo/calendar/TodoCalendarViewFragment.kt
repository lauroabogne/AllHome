package com.example.allhome.todo.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.databinding.FragmentTodoCalendarViewBinding
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.todo.TodoFragment
import com.example.allhome.todo.TodoFragmentContainerActivity
import com.example.allhome.todo.viewmodel.TodoCalendarViewFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoCalendarViewFragmentViewModelFactory
import com.example.allhome.todo.viewmodel.TodoFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoFragmentViewModelFactory
import com.example.simplecalendar.calendar.CalendarPagerAdapter
import com.example.simplecalendar.calendar.MonthPagerItem
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class TodoCalendarViewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val numberOfMonths: Int = 151
    var todoCalendarViewPager: ViewPager2? = null



    lateinit var mFragmentTodoCalendarViewBinding:FragmentTodoCalendarViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    private val mTodoCalendarViewFragmentViewModel:TodoCalendarViewFragmentViewModel by viewModels{
        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        val logsDAO = (context?.applicationContext as AllHomeBaseApplication).logsDAO
        TodoCalendarViewFragmentViewModelFactory(todosDAO,logsDAO)
    }


    private val viewTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){
            todoCalendarViewPager!!.adapter?.notifyDataSetChanged()
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mFragmentTodoCalendarViewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_todo_calendar_view, container, false)
        todoCalendarViewPager = mFragmentTodoCalendarViewBinding.todoCalendarViewPager
        val monthPagerItems = generateMonthPagerItems()
        val calendarPagerAdapter = TodoCalendarPagerAdapter(monthPagerItems,requireContext(),mTodoCalendarViewFragmentViewModel)
        calendarPagerAdapter.setItemDateSelectedListener(object: TodoCalendarPagerAdapter.ItemDateSelectedListener{
            override fun onDateSelected(date: Date, position: Int) {

                val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
                intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG, TodoFragmentContainerActivity.VIEW_TODO_LIST_FRAGMENT)
                intent.putExtra(TodoFragment.SELECTED_DATE_TAG,SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
                viewTodoListResultContract.launch(intent)

            }

        })

        todoCalendarViewPager!!.adapter = calendarPagerAdapter
        todoCalendarViewPager!!.clipToPadding = false
        todoCalendarViewPager!!.clipChildren = false
        todoCalendarViewPager!!.offscreenPageLimit = 1
        todoCalendarViewPager!!.setCurrentItem(74, false)

        todoCalendarViewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.e("Position selected","${position} ${ todoCalendarViewPager!!.currentItem}")
                val monthView: TodoMonthPagerItem = monthPagerItems.get(position)
                Log.e("Position selected","${ todoCalendarViewPager!!.getChildAt(0)}")
            }
        })


        return mFragmentTodoCalendarViewBinding.root
    }

    private fun generateMonthPagerItems(): Array<TodoMonthPagerItem> {

        val calendar = Calendar.getInstance()
        // subtract 75 months to get the starting month
        calendar.add(Calendar.MONTH, -(numberOfMonths/2))


        val monthPagerItems = Array(numberOfMonths) { i ->
            calendar.add(Calendar.MONTH, 1)
            val month = calendar.clone() as Calendar
            TodoMonthPagerItem(month)

        }



        return monthPagerItems

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TodoCalendarViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
            TodoCalendarViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}