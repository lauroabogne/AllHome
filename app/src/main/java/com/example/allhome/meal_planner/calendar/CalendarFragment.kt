package com.example.allhome.meal_planner.calendar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.R
import com.example.allhome.databinding.FragmentCalendarBinding
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CalendarFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mFragmentCalendarBinding: FragmentCalendarBinding

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentCalendarBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)

        val dates = generate3Fragment()
        val indexOfCurrentDate = getIndexOfCurrentDate(dates)
        val adapter = ViewPagerFragmentAdapter(dates, requireActivity().supportFragmentManager, lifecycle)

        mFragmentCalendarBinding.calendarViewPager.adapter = adapter
        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(indexOfCurrentDate,false)
        mFragmentCalendarBinding.calendarViewPager.offscreenPageLimit = 2

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
        lastMonthCalendarFragment.mCalendar = lastMonthCalendar
        dates.add(lastMonthCalendarFragment)


        val currentMonthCalendar = java.util.Calendar.getInstance()
        val currentMonthFragment  = Calendar.newInstance("","")
        currentMonthFragment.mCalendar = currentMonthCalendar
        dates.add(currentMonthFragment)

        val nextMonthCalendar = java.util.Calendar.getInstance()
        nextMonthCalendar.add(java.util.Calendar.MONTH,1)
        val nextMonthCalendarFragment   = Calendar.newInstance("","")
        nextMonthCalendarFragment.mCalendar = nextMonthCalendar
        dates.add(nextMonthCalendarFragment)


        return dates
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