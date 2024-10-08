package com.example.allhome.todo.custom_binding_adapter

import android.content.Context
import android.media.MicrophoneInfo.Coordinate3F
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.BindingAdapter
import com.example.allhome.R
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("todoShowHideRepeatIcon")
fun todoShowHideRepeatIcon(imageButton: ImageButton,todoWithSubTaskCount: TodosWithSubTaskCount){

    if(todoWithSubTaskCount.totalSubTaskCount > 0 ){
        imageButton.visibility = View.VISIBLE
    }else{
        imageButton.visibility = View.GONE
    }

}
@BindingAdapter("todoShowHideNotificationIcon")
fun todoShowHideNotificationIcon(imageButton: ImageButton,todoWithSubTaskCount: TodosWithSubTaskCount){
    if(todoWithSubTaskCount.todoEntity.notifyEveryType.lowercase() == imageButton.context.getString(R.string.grocery_notification_none).lowercase()){
        imageButton.visibility = View.GONE
    }else{
        imageButton.visibility = View.VISIBLE
    }
}
@BindingAdapter("todoShowHideSubTaskIcon")
fun todoShowHideSubTaskIcon(imageButton: ImageButton,todoWithSubTaskCount: TodosWithSubTaskCount){

    if(todoWithSubTaskCount.totalSubTaskCount <= 0 ){
        imageButton.visibility = View.GONE
    }else{
        imageButton.visibility = View.VISIBLE
    }

}
@BindingAdapter("todoShowHideDueDateIcon")
fun todoShowHideDueDateIcon(imageButton: ImageButton,todoWithSubTaskCount: TodosWithSubTaskCount){
    if(todoWithSubTaskCount.todoEntity.dueDate == "0000-00-00 00:00:00"){
        imageButton.visibility = View.GONE

    }else{
        imageButton.visibility = View.VISIBLE
    }
}
@BindingAdapter("todoDueDateFormatter")
fun todoDueDateFormatter(textView:TextView,dueDatetime:String?){
    if(dueDatetime == "0000-00-00 00:00:00" || dueDatetime == null){
        textView.visibility = View.GONE
        return
    }
    textView.visibility = View.VISIBLE
    if(dueDatetime.contains("00:00:00")){

        val dueDateDateTime = DateTime.parse(dueDatetime.replace("00:00:00","").trim(), DateTimeFormat.forPattern("yyyy-MM-dd"))
        val dueDateFormattedString = SimpleDateFormat("MMM dd, y").format(dueDateDateTime.toDate())

        //textView.text = "Due date : $dueDateFormattedString"
        textView.text =  setDueDatetimeLabel(dueDatetime)

        return
    }
    textView.text =  setDueDateLabel(dueDatetime)

}
fun setDueDateLabel(dueDatetime:String?):String {
    val dueDateDateTime = LocalDateTime.parse(dueDatetime, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
    val dueDateFormattedString = SimpleDateFormat("MMM dd, y h:mm:ss a").format(dueDateDateTime.toDate())
    val dueDateDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(dueDateDateTime.toDate()))
    val currentDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(DateTime.now().toDate()))
    val tomorrowDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(DateTime.now().plusDays(1).toDate()))
    val yesterdayDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(DateTime.now().minusDays(1).toDate()))

    return if (dueDateDate.isEqual(currentDate)) {
        "Today at ${SimpleDateFormat("h:mm:ss a").format(dueDateDateTime.toDate())}"
    }else if(dueDateDate.isEqual(tomorrowDate)){
        "Tomorrow at ${SimpleDateFormat("h:mm:ss a").format(dueDateDateTime.toDate())}"
    }else if(dueDateDate.isEqual(yesterdayDate)){
        "Yesterday at ${SimpleDateFormat("h:mm:ss a").format(dueDateDateTime.toDate())}"
    }  else {
        // The due date is not equal to the current date
        "$dueDateFormattedString"
    }
}
fun setDueDatetimeLabel(dueDatetime:String?):String{
    val dueDateDateTime = DateTime.parse(dueDatetime?.replace("00:00:00","")?.trim(), DateTimeFormat.forPattern("yyyy-MM-dd"))
    val dueDateDate =  LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(dueDateDateTime.toDate()))
    val dueDateFormattedString = SimpleDateFormat("MMM dd, y").format(dueDateDateTime.toDate())
    val currentDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(DateTime.now().toDate()))
    val tomorrowDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(DateTime.now().plusDays(1).toDate()))
    val yesterdayDate = LocalDate.parse(SimpleDateFormat("yyyy-MM-dd").format(DateTime.now().minusDays(1).toDate()))

    return if (dueDateDate.isEqual(currentDate)) {
        "Due date : Today"
    }else if(dueDateDate.isEqual(tomorrowDate)){
        "Due date : Tomorrow"
    }else if(dueDateDate.isEqual(yesterdayDate)){
        "Due date : Yesterday"
    }  else {
        // The due date is not equal to the current date
        "Due date : $dueDateFormattedString"
    }

}
@BindingAdapter("todoDescription")
fun todoDescription(textview:TextView,todoEntity:TodoEntity?){
    if(todoEntity == null){
        return
    }
    if(todoEntity.description.trim().isEmpty()){
        textview.visibility = View.GONE
        return
    }
    textview.visibility = View.VISIBLE
    textview.text = "${todoEntity.description}"
}
@BindingAdapter("todoRepeatUntil")
fun todoRepeatUntil(textView:TextView,repeatUntilDate:String?){
    if(repeatUntilDate == "0000-00-00 00:00:00" || repeatUntilDate == null){
        textView.visibility = View.GONE

        (textView.parent as ConstraintLayout).findViewById<TextView>(R.id.repeatUntilLabelTextView).visibility = View.GONE
        return
    }

    textView.visibility = View.VISIBLE
    (textView.parent as ConstraintLayout).findViewById<TextView>(R.id.repeatUntilLabelTextView).visibility = View.VISIBLE
    if(repeatUntilDate.contains("00:00:00")){
        val dueDateDateTime = DateTime.parse(repeatUntilDate.replace("00:00:00","").trim(), DateTimeFormat.forPattern("yyyy-MM-dd"))
        val dueDateFormattedString = SimpleDateFormat("MMM dd, y").format(dueDateDateTime.toDate())

        textView.text = "$dueDateFormattedString"
        return
    }
    val dueDateDateTime = DateTime.parse(repeatUntilDate, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
    val dueDateFormattedString = SimpleDateFormat("MMM dd, y h:mm:ss a").format(dueDateDateTime.toDate())
    textView.text = "$dueDateFormattedString"

}
@BindingAdapter("todoRepeatEvery")
fun todoRepeatEvery(textview:TextView,todoEntity:TodoEntity?){
    if(todoEntity == null){
        return
    }
    if(todoEntity.repeatEveryType == textview.context.getString(R.string.none)){
        textview.visibility = View.GONE
        (textview.parent as ConstraintLayout).findViewById<TextView>(R.id.repeatEveryLabelTextView).visibility = View.GONE
        return
    }

    textview.visibility = View.VISIBLE
    if(textview.context.resources.getString(R.string.day) == todoEntity.repeatEveryType){
        textview.text = "${todoEntity.repeatEvery} ${todoEntity.repeatEveryType} ${generateDayRepeatString(textview.context,todoEntity)}"
    }else{
        textview.text = "${todoEntity.repeatEvery} ${todoEntity.repeatEveryType}"
    }

}
fun generateDayRepeatString(context: Context, todoEntity: TodoEntity):String{

    val selectedDays = arrayListOf<String>()
    if (todoEntity.isSetInMonday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.monday_abbr))
    }
    if (todoEntity.isSetInTuesday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.tuesday_abbr))
    }
    if (todoEntity.isSetInWednesday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.wednesday_abbr))
    }
    if (todoEntity.isSetInThursday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.thursday_abbr))
    }
    if (todoEntity.isSetInFriday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.friday_abbr))
    }
    if (todoEntity.isSetInSaturday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.saturday_abbr))
    }
    if (todoEntity.isSetInSunday == TodoEntity.SET) {
        selectedDays.add(context.getString(R.string.sunday_abbr))
    }

    if(selectedDays.isEmpty()){
        return ""
    }

    return "(${selectedDays.joinToString(", ")})"
}
@BindingAdapter("todoNotifyBefore")
fun todoNotifyBefore(textview:TextView,todoEntity: TodoEntity?){
    if(todoEntity == null){
        return
    }

    textview.visibility = View.VISIBLE
    (textview.parent as ConstraintLayout).findViewById<TextView>(R.id.notifyLabelTextView).visibility = View.VISIBLE

    val notifyAt = todoEntity.notifyAt
    when(todoEntity.notifyEveryType){ //mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
        textview.context.getString(R.string.none)->{
            textview.text = ""
            textview.visibility = View.GONE
            (textview.parent as ConstraintLayout).findViewById<TextView>(R.id.notifyLabelTextView).visibility = View.GONE
        }
        textview.context.getString(R.string.grocery_notification_same_day_and_time)->{
            textview.text = "Same day and time of due date"
        }
        textview.context.getString(R.string.grocery_notification_minute_before)->{
            textview.text = "$notifyAt minutes before due date"
        }
        textview.context.getString(R.string.grocery_notification_hour_before)->{
            textview.text = "$notifyAt hour before due date"
        }
        textview.context.getString(R.string.grocery_notification_day_before)->{
            textview.text = "$notifyAt day before due date"
        }
    }
    todoEntity.notifyEveryType

}

@BindingAdapter("capitalizeFirstLetter")
fun capitalizeFirstLetter(textView: TextView,todoName:String ?) {

    todoName?.let {
        val capitalizedText = if (it.isNotEmpty()) {
            it.substring(0, 1).uppercase(Locale.getDefault()) + it.substring(1)
        } else {
            it
        }
        textView.text = capitalizedText
    }
}