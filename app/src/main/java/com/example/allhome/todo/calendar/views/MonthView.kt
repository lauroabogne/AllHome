package com.example.allhome.todo.calendar.views


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.forEach
import com.example.allhome.R
import com.example.allhome.todo.calendar.MonthDate
import com.example.allhome.todo.calendar.TodoMonthPagerItem
import java.text.SimpleDateFormat
import java.util.*

class MonthView (context: Context, attrs: AttributeSet, defStyle: Int) : FrameLayout(context, attrs, defStyle){
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)


    private var horizontalOffset = 0
    private var weekDaysLetterHeight = 0
    private var dayWidth = 0f
    private var dayHeight = 0f

    private  var textPaint: Paint
    private  var todoTextPaint: Paint
    private  var circleStrokePaint: Paint
    private  var dates: Array<Array<MonthDate?>>? =  null
    private var textColorBlack = Color.BLACK
    private var textColorRed = Color.RED
    private var textColorGreen = Color.parseColor("#2E8B57")
    private var weekendTextColor = Color.RED
    private var textColorGray = Color.GRAY
    private var year = 2023
    private var month = Calendar.MAY
    private var onDrawFinishedListener: (() -> Unit)? = null

    private var onDateSelectedListener:OnDateSelectedListener? = null
    private var monthPagerItem: TodoMonthPagerItem? = null

    companion object {
        val numberOfRows = 7
        val numberOfColumns = 6
        private val WEEKENDSUNDAY = "SUN"
        private val WEEKENDSATURDAY = "SAT"
        private var days = listOf("MON", "TUE", "WED", "THU", "FRI", WEEKENDSATURDAY,WEEKENDSUNDAY)
        var startOfWeekDay = Calendar.SUNDAY

        public fun startOfWeek():Int{
            return Calendar.SUNDAY

        }

    }


    init {

        // Toast.makeText(this.context,"Init",Toast.LENGTH_SHORT).show()

        val normalTextSize = resources.getDimensionPixelSize(R.dimen.normal_text_size)
        weekDaysLetterHeight = normalTextSize * 2
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorBlack
            textSize = normalTextSize.toFloat()
            textAlign = Paint.Align.CENTER
        }

        val todoTextSize = resources.getDimensionPixelSize(R.dimen.todo_calendar_task_text_size)

        todoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColorBlack
            textSize = todoTextSize.toFloat()
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true

        }




        circleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.circle_stroke_width)
            color = getThemePrimaryColor()
        }
        startOfWeekDay = startOfWeek()
        days = if(startOfWeekDay == Calendar.SUNDAY) listOf(WEEKENDSUNDAY, "MON", "TUE", "WED", "THU", "FRI", WEEKENDSATURDAY) else listOf("MON", "TUE", "WED", "THU", "FRI", WEEKENDSATURDAY,WEEKENDSUNDAY)

        setOnDrawFinishedListener {
            removeAllViews()
            addClickableBackground()
            // set onDrawFinishedListener to avoid infinite loop of onDraw function
            onDrawFinishedListener = null
        }

    }

    fun getMonthPagerItem(): TodoMonthPagerItem? {
        return monthPagerItem
    }
    fun setYearMonthAndDates(monthPagerItem: TodoMonthPagerItem, monthDate: Array<Array<MonthDate?>>?) {

        this.monthPagerItem = monthPagerItem
        this.year = monthPagerItem.calendar.get(Calendar.YEAR)
        this.month = monthPagerItem.calendar.get(Calendar.MONTH)
        this.dates = monthDate


//        if (monthPagerItem.selectedDate == null) {
//            removeAllBackground()
//        }
        removeAllViews()
        addClickableBackground()
        invalidate()
        //removeAllViews()


    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        measureDaySize(canvas)
        drawWeekLetters(canvas)

        val currentDate = Date()
        val normalTextSize = resources.getDimensionPixelSize(R.dimen.normal_text_size)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = normalTextSize.toFloat()
            textAlign = Paint.Align.CENTER
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        for (y in 0 until numberOfColumns) {
            for (x in 0 until numberOfRows) {
                // Log.e("Calendar","Calendar ${y}} ${x}")

                dates?.let { date ->
                    val date = date[y][x]

                    //Log.e("Calendar","Calendar ${y}} ${x} ${date.toString()}")
                    val dayOfMonth = Calendar.getInstance().apply { time = date!!.date }.get(Calendar.DAY_OF_MONTH)
                    val monthOfYear = Calendar.getInstance().apply { time = date!!.date }.get(Calendar.MONTH)
                    val yearOfMonth = Calendar.getInstance().apply { time =date!!.date }.get(Calendar.YEAR)

                    val xPos = x * dayWidth + horizontalOffset
                    val yPos = y * dayHeight + weekDaysLetterHeight
                    val xPosCenter = xPos + dayWidth / 2

                    val doDateIsCurrentDate = doDateIsCurrentDate(date!!.date)
                    val doDateIsSaturdayOrSunday = doDateIsSaturdayOrSunday(date!!.date)
                    val isDateNotInSelectedMonth = doDateNotInSelectedMonth(date!!.date, year, month)

                    if(doDateIsCurrentDate){
                        canvas.drawCircle(xPosCenter, yPos + textPaint.textSize, textPaint.textSize * .8f, getCirclePaint())
                        textPaint.color = getContrastColor(getThemePrimaryColor())
                    }else{
                        if(doDateIsSaturdayOrSunday){
                            textPaint.color = if(isDateNotInSelectedMonth) weekendTextColor else adjustOpacity(weekendTextColor,0.3f)
                        }else{
                            textPaint.color = if(isDateNotInSelectedMonth)  textColorBlack else adjustOpacity(textColorBlack,0.3f)
                        }

                    }

                    val isPastDate = dateFormat.format(date.date) < dateFormat.format(currentDate)

                    drawNumberOfTaskText(xPos, yPos, canvas,date.todoCount,date.accomplishedTaskCount, date.overdueTaskCount,isDateNotInSelectedMonth,isPastDate)


                    canvas.drawText("$dayOfMonth", xPosCenter, yPos + textPaint.textSize + (textPaint.textSize * .4f) , textPaint)
                    onDrawFinishedListener?.invoke()
                }

            }
        }

    }
    // Register a callback function to be called when onDraw() has finished executing
    private fun setOnDrawFinishedListener(listener: () -> Unit) {
        this.onDrawFinishedListener = listener
    }

    fun setOnDateSelectedListener(onDateSelectedListener:OnDateSelectedListener){
        this.onDateSelectedListener = onDateSelectedListener
    }
    private fun createCalendarArray(year: Int, month: Int): Array<Array<Date?>> {
        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // Shift the first day to Monday
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if(startOfWeekDay == Calendar.SUNDAY){
            val daysToShift = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - 1
            calendar.add(Calendar.DAY_OF_MONTH, -daysToShift)
        }else{
            val daysToShift = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
            calendar.add(Calendar.DAY_OF_MONTH, -daysToShift)
        }



        // Create a 2D array to hold the calendar
        val calendarArray = Array(numberOfColumns) { row ->
            Array(numberOfRows) { col ->
                val date = calendar.time
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                date
            }
        }

        return calendarArray
    }


    private fun doDateIsSaturdayOrSunday(date: Date):Boolean {
        val dayOfWeek = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK)
        return (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)

    }
    private fun doDateIsCurrentDate(date: Date):Boolean {
        val currentDateCalendar = Calendar.getInstance() // get the current calendar
        // create a calendar object for your date
        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = date

        // compare yourDate to the current date
        return dateCalendar.get(Calendar.YEAR) == currentDateCalendar.get(Calendar.YEAR) &&
                dateCalendar.get(Calendar.MONTH) == currentDateCalendar.get(Calendar.MONTH) &&
                dateCalendar.get(Calendar.DAY_OF_MONTH) == currentDateCalendar.get(Calendar.DAY_OF_MONTH)

    }
    private fun doDateNotInSelectedMonth(date: Date, year: Int, month: Int): Boolean {
//        val selectedCalendarDate = Calendar.getInstance()
//        selectedCalendarDate.set(Calendar.YEAR, year) // set year to 2023
//        selectedCalendarDate.set(Calendar.MONTH, month) // set month to May (0-based index)

        val calendarParams = Calendar.getInstance()
        calendarParams.time = date
        return calendarParams.get(Calendar.MONTH) == month &&
                calendarParams.get(Calendar.YEAR) == year
    }
    private fun drawWeekLetters(canvas: Canvas) {
        for (i in 0 until 7) {
            val xPos = horizontalOffset + (i + 1) * dayWidth - dayWidth / 2
            var weekDayLetterPaint = textPaint
            val day = days[i]
            if(day == WEEKENDSATURDAY || day == WEEKENDSUNDAY){
                weekDayLetterPaint.color = weekendTextColor
            }else{
                weekDayLetterPaint.color = textColorBlack
            }
            canvas.drawText(day, xPos , weekDaysLetterHeight * .7f, weekDayLetterPaint)
        }
    }
    private fun getCirclePaint(): Paint {
        val curPaint = Paint(textPaint)
        var paintColor = getThemePrimaryColor()
        curPaint.color = paintColor
        return curPaint
    }
    private fun measureDaySize(canvas: Canvas) {
        dayWidth = (canvas.width) / numberOfRows.toFloat()
        dayHeight = (canvas.height - weekDaysLetterHeight) / numberOfColumns.toFloat()

    }
    private fun addClickableBackground(){
        for (y in 0 until numberOfColumns) {
            for (x in 0 until numberOfRows) {
                dates?.let{date->
                    val date = date[y][x]!!
                    val dayOfMonth = Calendar.getInstance().apply { time = date!!.date }.get(Calendar.DAY_OF_MONTH)
                    val monthOfYear = Calendar.getInstance().apply { time = date!!.date }.get(Calendar.MONTH)
                    val yearOfMonth = Calendar.getInstance().apply { time =date!!.date }.get(Calendar.YEAR)

                    val xPos = x * dayWidth + horizontalOffset
                    val yPos = y * dayHeight + weekDaysLetterHeight

                    LayoutInflater.from(context).inflate(R.layout.calendar_child_background,this,false).apply{
                        layoutParams.width = dayWidth.toInt()
                        layoutParams.height = dayHeight.toInt()
                        this.x = xPos
                        this.y = yPos
                        this.tag = date
                        addView(this)
                        setOnClickListener{


                            //removeAllBackground()
                            // it.background =  resources.getDrawable(R.drawable.selected_calendar_date_background,context.theme)
                            onDateSelectedListener?.dateSelected(date!!.date,this@MonthView)


                        }
                    }
                }

            }
        }
    }
    private fun removeAllBackground(){
        this.forEach {
            it.background = null
        }
    }
    private fun getContrastColor(color:Int): Int {
        val luminance = ColorUtils.calculateLuminance(color)
        val textColor = if (luminance > 0.5) {
            return Color.BLACK // use black text for light primary colors
        } else {
            return Color.WHITE // use white text for dark primary colors
        }
    }
    private fun adjustOpacity(color: Int, opacity: Float): Int {
        val alpha = (255 * opacity).toInt() // convert opacity to alpha value (0-255)
        return ColorUtils.setAlphaComponent(color, alpha)
    }
    private fun getThemePrimaryColor(): Int {
        val typedValue = TypedValue()
        val currentTheme = context.theme
        currentTheme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        return typedValue.data
    }
    private fun drawNumberOfTaskText(startX: Float, startY: Float, canvas: Canvas, todoCount: Int  = 0,accomplishedTaskCount:Int =0, overdueTaskCount:Int = 0, isDateNotInSelectedMonth:Boolean = false, isFastDate:Boolean = false){
        if(todoCount <=0){
            return
        }

        val startingXOfText = startX  + (dayWidth /  2 )
        val remainingTodoCount = todoCount - (accomplishedTaskCount + overdueTaskCount)


        if(isFastDate){

            if(accomplishedTaskCount >0 && overdueTaskCount > 0){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorGreen else adjustOpacity(textColorGreen,0.3f)
                canvas.drawText("$accomplishedTaskCount task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)

                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorRed else adjustOpacity(textColorRed,0.3f)
                canvas.drawText("$overdueTaskCount task", startingXOfText , startY + (dayHeight * .8f )+20, todoTextPaint)

            }else if(accomplishedTaskCount >0 && overdueTaskCount <= 0){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorGreen else adjustOpacity(textColorGreen,0.3f)
                canvas.drawText("$accomplishedTaskCount task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)
            }else if(accomplishedTaskCount <=0 && overdueTaskCount > 0){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorRed else adjustOpacity(textColorRed,0.3f)
                canvas.drawText("$overdueTaskCount task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)
            }

        }else{

            if(accomplishedTaskCount >0 && overdueTaskCount > 0 && remainingTodoCount > 0){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorGreen else adjustOpacity(textColorGreen,0.3f)
                canvas.drawText("$accomplishedTaskCount task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)

                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorRed else adjustOpacity(textColorRed,0.3f)
                canvas.drawText("${overdueTaskCount} task", startingXOfText , startY + (dayHeight * .8f )+20, todoTextPaint)

                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorBlack else adjustOpacity(textColorBlack,0.3f)
                canvas.drawText("${remainingTodoCount} task", startingXOfText , startY + (dayHeight * .8f )+40, todoTextPaint)

            }else if(accomplishedTaskCount >0 && ( todoCount - accomplishedTaskCount) <=0){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorGreen else adjustOpacity(textColorGreen,0.3f)
                canvas.drawText("$accomplishedTaskCount task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)
            }else if(accomplishedTaskCount >0 && (todoCount - accomplishedTaskCount) > 0 ){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorGreen else adjustOpacity(textColorGreen,0.3f)
                canvas.drawText("$accomplishedTaskCount task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)

                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorBlack else adjustOpacity(textColorBlack,0.3f)
                canvas.drawText("${todoCount - accomplishedTaskCount } task", startingXOfText , startY + (dayHeight * .8f )+20, todoTextPaint)
            }else if(accomplishedTaskCount <=0 && (todoCount - accomplishedTaskCount) > 0 && overdueTaskCount <=0){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorBlack else adjustOpacity(textColorBlack,0.3f)
                canvas.drawText("${todoCount} task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)
            }else if(remainingTodoCount > 0 && overdueTaskCount > 0 ){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorRed else adjustOpacity(textColorRed,0.3f)
                canvas.drawText("${overdueTaskCount} task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)

                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorBlack else adjustOpacity(textColorBlack,0.3f)
                canvas.drawText("${remainingTodoCount } task", startingXOfText , startY + (dayHeight * .8f ) + 20, todoTextPaint)

            }else if(remainingTodoCount <= 0 && overdueTaskCount > 0 ){
                todoTextPaint.color = if(isDateNotInSelectedMonth)  textColorRed else adjustOpacity(textColorRed,0.3f)
                canvas.drawText("${overdueTaskCount} task", startingXOfText , startY + (dayHeight * .8f ), todoTextPaint)


            }

        }


    }
    interface OnDateSelectedListener {
        fun dateSelected(date: Date,monthView:MonthView)
    }
}