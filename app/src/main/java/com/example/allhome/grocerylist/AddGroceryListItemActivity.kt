package com.example.allhome.grocerylist

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.grocerylist.viewmodel_factory.GroceryListViewModelFactory
import com.example.allhome.recipes.RecipesFragment
import com.example.allhome.recipes.ViewRecipeActivity
import com.example.allhome.recipes.ViewRecipeFragment
import com.example.allhome.utils.ImageUtil
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddGroceryListItemActivity : AppCompatActivity() {

    private lateinit var mGroceryListViewModel: GroceryListViewModel
    var groceryListUniqueId: String = ""
    var action = ADD_NEW_RECORD_ACTION
    var groceryListItemId = 0
    var groceryListItemIndex = -1
    var tempPhotoFileForAddingImage: File? = null
    var imageChanged = false

    companion object {

        val IMAGE_TEMP_NAME = "temp_image"
        val IMAGE_NAME_SUFFIX = "jpg"
        val ADD_NEW_RECORD_ACTION = 1
        val UPDATE_RECORD_ACTION = 2
        val REQUEST_PICK_IMAGE = 4

        val GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG = "GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG"
        val GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG"
        val GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG"
        val GROCERY_LIST_ACTION_EXTRA_DATA_TAG = "GROCERY_LIST_ACTION_EXTRA_DATA_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grocery_list_item)


        val addGroceryListItemFragment = AddGroceryListItemFragment.newInstance("","")
        supportFragmentManager.beginTransaction()
            .replace(R.id.recipeFragmentContainer,addGroceryListItemFragment)
            .commit()
    }



}

