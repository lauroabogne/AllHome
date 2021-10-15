package com.example.allhome.recipes.util

import android.R.attr.x
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast


class JavascriptBridgeNBackup {
    @JavascriptInterface fun showData(dataFromJavascript: String) {
        Log.e("action","${dataFromJavascript}")
    }

}