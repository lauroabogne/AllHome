package com.example.allhome.todo.custom_binding_adapter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.allhome.R
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import org.joda.time.DateTime
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
fun todoDueDateFormatter(textView:TextView,dueDatetime:String){
    if(dueDatetime == "0000-00-00 00:00:00"){
        textView.visibility = View.GONE
        return
    }
    textView.visibility = View.VISIBLE
    if(dueDatetime.contains("00:00:00")){

        val dueDateDateTime = DateTime.parse(dueDatetime.replace("00:00:00","").trim(), DateTimeFormat.forPattern("yyyy-MM-dd"))
        val dueDateFormattedString = SimpleDateFormat("MMM dd, y").format(dueDateDateTime.toDate())
        textView.text = dueDateFormattedString

        return
    }
    val dueDateDateTime = DateTime.parse(dueDatetime, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
    val dueDateFormattedString = SimpleDateFormat("MMM dd, y h:mm:ss a").format(dueDateDateTime.toDate())
    textView.text = dueDateFormattedString

}