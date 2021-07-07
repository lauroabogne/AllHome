package com.example.allhome.meal_planner

import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
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
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    // TODO: Rename and change types of parameters
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

        val dates = getMonthsAndYearFragment()
        val indexOfCurrentDate = getIndexOfCurrentDate(dates)
        val adapter = ViewPagerFragmentAdapter(dates, requireActivity().supportFragmentManager, lifecycle)

        mFragmentCalendarBinding.calendarViewPager.adapter = adapter
        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(indexOfCurrentDate,false)
        mFragmentCalendarBinding.calendarViewPager.offscreenPageLimit = 2

        mFragmentCalendarBinding.calendarViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

               /* if(position + 1 == adapter.fragmentList.size){
                    val next3YearsCalendar = java.util.Calendar.getInstance()
                    next3YearsCalendar.time = adapter.fragmentList[position].mDate
                    next3YearsCalendar.add(java.util.Calendar.MONTH, 1)
                    val calendar = Calendar.newInstance("","")
                    calendar.mDate = next3YearsCalendar.time
                    adapter.fragmentList.add(calendar)
                    adapter.notifyDataSetChanged()
                }*/

                //if(position == 1){

                //}

               /* if(true){
                    return
                }*/
                if(position == 0){
                    //Handler().postDelayed({
                        //doSomethingHere()
                        val firstFragment = adapter.fragmentList[0]
                        firstFragment.mDate = addMonth(firstFragment.mDate!!,-1)
                        firstFragment.generateData()

                        val secondFragment = adapter.fragmentList[1]
                        secondFragment.mDate = addMonth(secondFragment.mDate!!,-1)
                        secondFragment.generateData()

                        val thirdFragment = adapter.fragmentList[2]
                        thirdFragment.mDate = addMonth(thirdFragment.mDate!!,-1)
                        thirdFragment.generateData()

                        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(1,false)
                    //}, 1000)
                }
                if(position == 2){
                   // Handler().postDelayed({
                        //doSomethingHere()

                        val firstFragment = adapter.fragmentList[0]
                        firstFragment.mDate = addMonth(firstFragment.mDate!!,1)
                        firstFragment.generateData()

                        val secondFragment = adapter.fragmentList[1]
                        secondFragment.mDate = addMonth(secondFragment.mDate!!,1)
                        secondFragment.generateData()

                        val thirdFragment = adapter.fragmentList[2]
                        thirdFragment.mDate = addMonth(thirdFragment.mDate!!,1)
                        thirdFragment.generateData()

                        mFragmentCalendarBinding.calendarViewPager.setCurrentItem(1,false)
                   // }, 1000)
                }

            }
        })
        return mFragmentCalendarBinding.root
    }

    fun addMonth(date:Date,month:Int):Date{
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.add(java.util.Calendar.MONTH,month)
        return calendar.time
    }

    fun getIndexOfCurrentDate(calendars:ArrayList<Calendar>):Int{
        val currenDateCalendar = java.util.Calendar.getInstance()
        return calendars.indexOfFirst {
            var fragmentCalendar = java.util.Calendar.getInstance()
            fragmentCalendar.time = it.mDate
            currenDateCalendar.get(java.util.Calendar.YEAR) == fragmentCalendar.get(java.util.Calendar.YEAR)&& currenDateCalendar.get(java.util.Calendar.MONTH) == fragmentCalendar.get(java.util.Calendar.MONTH)

        }

    }
    fun getMonthsAndYearFragment():ArrayList<Calendar>{

        val last3YearsCalendar = java.util.Calendar.getInstance()
        last3YearsCalendar.add(java.util.Calendar.MONTH,-1)

        val next3YearsCalendar = java.util.Calendar.getInstance()
        next3YearsCalendar.add(java.util.Calendar.MONTH,2)
        val dates = arrayListOf<Calendar>()
        while (last3YearsCalendar.before(next3YearsCalendar)){
            last3YearsCalendar.add(java.util.Calendar.MONTH, 1)
            val calendar = Calendar.newInstance("","")
            calendar.mDate = last3YearsCalendar.getTime()
            dates.add(calendar)
        }
        return dates
    }

    fun testFragment():ArrayList<Fragment>{

        var fragments = arrayListOf<Fragment>()
        for(i in 1..10){
            val calendar = Calendar.newInstance("","")
            calendar.mDate
            fragments.add(Calendar.newInstance("",""))
        }

        return fragments
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