package com.example.allhome.utils

import android.text.InputFilter
import android.text.Spanned

class MinMaxInputFilter( var mIntMin: Int,var mIntMax: Int,var inputErrorListener: InputErrorListener? = null) : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(mIntMin, mIntMax, input)) return null
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        inputErrorListener?.let {
            it.onError()
        }
        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c >= a && c <= b else c >= b && c <= a
    }
    interface InputErrorListener{
        fun onError()
    }
}
