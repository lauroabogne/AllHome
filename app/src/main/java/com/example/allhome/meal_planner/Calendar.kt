package com.example.allhome.meal_planner

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.allhome.R
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Calendar : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var mDate:Date? = null
    lateinit var mMainView:LinearLayoutCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mMainView = inflater.inflate(R.layout.calendar, container, false) as LinearLayoutCompat
        mMainView.addView(inflater.inflate(R.layout.calendar_day_name_header,null,false))
        //mMainView.setBackgroundColor(Color.RED)
        //val linearLayoutCompat = TextView(requireContext())

        generateData()
        return mMainView
    }
    fun generateData(){
        val calendar2 = java.util.Calendar.getInstance()
        calendar2.time = mDate
        calendar2.set(java.util.Calendar.DAY_OF_MONTH,1)

        val calendar = java.util.Calendar.getInstance()
        calendar.time = mDate

        /*Log.e("date",mDate.toString())

        val startDayOfMonth = android.text.format.DateFormat.format("EEEE", calendar2)*/
        val numberOfWeeksInMonth = calendar.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH)
        /*val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)*/

       /* val firstDateOfMonth = calendar.getActualMinimum(java.util.Calendar.DATE)
        val lastDateOfMonth = calendar.getActualMaximum(java.util.Calendar.DATE)*/
        val firstDayOfMonth: Int = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val totalDaysInMonth: Int = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

        mMainView.findViewById<Button>(R.id.button).setText("${mDate}  ${numberOfWeeksInMonth} ${firstDayOfMonth}")
        //Log.e("DATA",requireContext().toString())
       // Log.e("WEEKS","${numberOfWeeksInMonth} ${numberOfWeeksInMonth}")
        var totalDays = 0
        for (i in 1..6){

            val view:ConstraintLayout = LayoutInflater.from(requireContext()).inflate(R.layout.calendar_week,null,false) as ConstraintLayout

            if(i==1){
                var hasVisibleView = false
                for(x in 0..6){
                    val dayView = view.getChildAt(x)
                    if (x < firstDayOfMonth) {
                        continue
                    }
                    hasVisibleView = true
                    totalDays++
                    dayView.visibility= View.VISIBLE
                    dayView.findViewById<TextView>(R.id.dayTextView).setText("${totalDays}")
                }
                if(hasVisibleView){
                    mMainView.addView(view)
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