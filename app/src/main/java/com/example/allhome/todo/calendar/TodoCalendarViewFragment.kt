package com.example.allhome.todo.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.MainActivity
import com.example.allhome.R
import com.example.allhome.databinding.FragmentTodoCalendarViewBinding
import com.example.allhome.todo.TodoFragment
import com.example.allhome.todo.TodoFragmentContainerActivity
import com.example.allhome.todo.viewmodel.TodoCalendarViewFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoCalendarViewFragmentViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class TodoCalendarViewFragment : Fragment(),TodoFragment.TodoFragmentCommunication {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

   // private val numberOfMonths: Int = 151
    var todoCalendarViewPager: ViewPager2? = null



    lateinit var mFragmentTodoCalendarViewBinding:FragmentTodoCalendarViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.title = "To Do List"

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

                val todoMonthPagerItem = (todoCalendarViewPager!!.adapter as TodoCalendarPagerAdapter).viewPagerItemArrayList[position]
                todoMonthPagerItem.selectedDate = date
               // displayTaskInListView(position)
                showListOfTodo(date)


            }

        })

        todoCalendarViewPager!!.adapter = calendarPagerAdapter
        todoCalendarViewPager!!.clipToPadding = false
        todoCalendarViewPager!!.clipChildren = false
        todoCalendarViewPager!!.offscreenPageLimit = OFF_SCREEN_PAGE_LIMIT


        todoCalendarViewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                todoCalendarViewPager!!.postDelayed({
                    //displayTaskInListView(position)
                    val todoMonthPagerItem = (todoCalendarViewPager!!.adapter as TodoCalendarPagerAdapter).viewPagerItemArrayList[position]
                    showListOfTodo(todoMonthPagerItem.selectedDate)
                },500)
            }
        })
        todoCalendarViewPager!!.setCurrentItem(DEFAULT_POSITION, false)

        return mFragmentTodoCalendarViewBinding.root
    }

    private fun showListOfTodo(date: Date){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date.time)


        childFragmentManager
            .beginTransaction()
            .replace(R.id.listViewTaskContainer,  TodoFragment.newInstance(TodoFragment.MAIN_ACTIVITY, formattedDate,TodoFragment.CALENDAR_VIEW))
            .commit();
    }

    private fun generateMonthPagerItems(): Array<TodoMonthPagerItem> {

        val calendar = Calendar.getInstance()
        // subtract 75 months to get the starting month
        calendar.add(Calendar.MONTH, -(NUMBER_OF_MONTHS/2))


        val monthPagerItems = Array(NUMBER_OF_MONTHS) { i ->
            calendar.add(Calendar.MONTH, 1)
            val month = calendar.clone() as Calendar
            TodoMonthPagerItem(month,month.time)

        }



        return monthPagerItems

    }
    companion object {

        const val  NUMBER_OF_MONTHS = 151
        const val OFF_SCREEN_PAGE_LIMIT = 1
        const val DEFAULT_POSITION = 74
        @JvmStatic fun newInstance(param1: String, param2: String) =
            TodoCalendarViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDataChanged() {
        todoCalendarViewPager!!.adapter?.notifyDataSetChanged()

    }


}