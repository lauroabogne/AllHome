package com.example.allhome.grocerylist

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.allhome.R
import java.io.File
import java.util.*

class TestMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)
    }

    private fun openImageIntent() {

        // Determine Uri of camera image to save.
        val root = File(Environment.getExternalStorageDirectory().toString() + File.separator + "MyDir" + File.separator)
        root.mkdirs()
        val fname = ""
        val sdImageMainDirectory = File(root, fname)
        val outputFileUri = Uri.fromFile(sdImageMainDirectory)

        // Camera.
        val cameraIntents: MutableList<Intent> = ArrayList()
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = packageManager
        val listCam = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val packageName = res.activityInfo.packageName
            val intent = Intent(captureIntent)
            intent.component = ComponentName(packageName, res.activityInfo.name)
            intent.setPackage(packageName)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            cameraIntents.add(intent)
        }
        // Filesystem.
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT

        // Chooser of filesystem options.
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Source")

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toTypedArray<Parcelable>())
        startActivityForResult(chooserIntent, 123)
    }
}