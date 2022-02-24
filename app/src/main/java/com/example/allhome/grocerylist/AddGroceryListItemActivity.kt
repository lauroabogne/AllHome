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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grocery_list_item)

        val action = intent.getIntExtra(AddGroceryListItemFragment.GROCERY_LIST_ACTION_EXTRA_DATA_TAG, AddGroceryListItemFragment.ADD_NEW_RECORD_ACTION)

        if(action == AddGroceryListItemFragment.ADD_NEW_RECORD_ACTION || action == AddGroceryListItemFragment.UPDATE_RECORD_ACTION){

            val groceryListUniqueId = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)
            val groceryListItemId = intent.getIntExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, 0)
            val groceryListItemIndex = intent.getIntExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, -1)
            val addGroceryListItemFragment = AddGroceryListItemFragment.newInstance(groceryListUniqueId,groceryListItemId,action,groceryListItemIndex)
            supportFragmentManager.beginTransaction()
                .replace(R.id.recipeFragmentContainer,addGroceryListItemFragment)
                .commit()

        }else if(action == AddGroceryListItemFragment.ADD_NEW_RECORD_FROM_BROWSER){
            val groceryListUniqueId = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)
            val itemName = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_NAME_TAG)!!
            val itemUnit = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_UNIT_TAG)!!
            val itemPrice = intent.getDoubleExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_PRICE_TAG,0.0)
            val quantity = intent.getDoubleExtra(AddGroceryListItemFragment.ITEM_QUANTITY_TAG,0.0)
            val category = intent.getStringExtra(AddGroceryListItemFragment.ITEM_CATEGORY)!!
            val notes = intent.getStringExtra(AddGroceryListItemFragment.ITEM_NOTES)!!

            val itemImageName = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_IMAGE_NAME_TAG)!!
            val addGroceryListItemFragment = AddGroceryListItemFragment.newInstance(groceryListUniqueId, action,itemName,itemUnit,itemPrice,quantity,category,notes,itemImageName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.recipeFragmentContainer,addGroceryListItemFragment)
                .commit()



        }


    }



}

