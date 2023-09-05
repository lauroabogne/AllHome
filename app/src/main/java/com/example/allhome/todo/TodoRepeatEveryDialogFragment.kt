package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import com.example.allhome.R

class TodoRepeatEveryDialogFragment() : DialogFragment() {

    private var mOnItemClickListener: OnItemClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.todo_repeat_every_dialog, null)

       val  mListView = dialogView.findViewById<ListView>(R.id.optionList)
        val options = requireContext().resources.getStringArray(R.array.tod_repeat_month_options)


        val adapter = ArrayAdapter(requireContext(), R.layout.textview_with_ripple_effect, options)
        mListView?.adapter = adapter


        builder.setView(dialogView)
            .setTitle("Repeat every")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()


        mListView.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = options[position]
            // Perform action based on selected option
            // For example: Toast.makeText(requireContext(), "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
            mOnItemClickListener?.onItemClick(selectedOption)
            dialog.dismiss()
        }

        return dialog
    }
    fun setOnItemClickListener(OnItemClickListener:OnItemClickListener){
        mOnItemClickListener = OnItemClickListener
    }

    interface OnItemClickListener{
        fun onItemClick(selectedOption:String)
    }
 }
