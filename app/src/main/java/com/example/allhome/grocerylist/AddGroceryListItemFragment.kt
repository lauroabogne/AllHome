package com.example.allhome.grocerylist

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityForAutoSuggest
import com.example.allhome.data.entities.GroceryItemEntityValues
import com.example.allhome.data.entities.GroceryListEntityValues
import com.example.allhome.databinding.FragmentAddGroceryListItemBinding
import com.example.allhome.global_ui.CustomConfirmationDialog
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.grocerylist.viewmodel_factory.GroceryListViewModelFactory
import com.example.allhome.utils.ImageUtil
import com.canhub.cropper.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddGroceryListItemFragment : Fragment() {

    private val TAG = "AddGroceryListItemFragment"
    lateinit var mDataBinding: FragmentAddGroceryListItemBinding
    private lateinit var mGroceryListViewModel: GroceryListViewModel
    var groceryListUniqueId: String = ""
    var action = ADD_NEW_RECORD_ACTION
    var groceryListItemId = ""
    var groceryListItemIndex = -1
    var tempPhotoFileForAddingImage: File? = null
    var imageChanged = false


    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
//            val uriContent = result.uriContent
//            val uriFilePath = result.getUriFilePath(requireContext()) // optional usage
//
//            val result = CropImage.getActivityResult(data)
            //mNewImageUri= result.uriContent
            //mFragmentAddPaymentBinding.itemImageView.setImageURI(result.uriContent)

            val result = result.uriContent
            val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
            if(!storageDir.exists()){
                storageDir.mkdir()
            }
            val itemBitmap = ImageUtil.uriToBitmap(result!!,requireContext())
            val uri = ImageUtil.saveImage(requireContext(),itemBitmap,GroceryUtil.TEMPORARY_IMAGES_LOCATION,"$IMAGE_TEMP_NAME.$IMAGE_NAME_SUFFIX")?.let {
                Uri.fromFile(File(it))
            }
            mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  uri
            mDataBinding.itemImageview.setImageURI(uri)

        } else {
            // An error occurred.
            val exception = result.error
            Toast.makeText(requireContext(), exception.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    private val toolbarNavigationOnClickListener = View.OnClickListener{
        if(action == ADD_NEW_RECORD_FROM_BROWSER){
            val intent = Intent()
            mGroceryListViewModel.selectedGroceryItem?.let {
                intent.putExtra(GROCERY_LIST_ITEM_NAME_TAG, it.itemName)
                intent.putExtra(GROCERY_LIST_ITEM_UNIT_TAG, it.unit)
                intent.putExtra(GROCERY_LIST_ITEM_PRICE_TAG, it.pricePerUnit)
                intent.putExtra(ITEM_QUANTITY_TAG, it.quantity)
                intent.putExtra(ITEM_CATEGORY, it.category)
                intent.putExtra(ITEM_NOTES, it.notes)
                intent.putExtra(GROCERY_LIST_ITEM_IMAGE_NAME_TAG, it.imageName)
                intent.putExtra(GROCERY_LIST_HAS_ADDED_ITEM_TAG, NO_ADDED_ITEM)

                activity?.setResult(Activity.RESULT_OK, intent)
            }
        }
        activity?.finish()
    }
    private val browseItemImageBtnOnClick = View.OnClickListener {
        if(action == ADD_NEW_RECORD_ACTION){
            val itemName = mDataBinding.itemNameTextinput.text.toString()
            val price = if(mDataBinding.pricePerUnitTextinput.text.toString().trim().isEmpty())  0.0 else mDataBinding.pricePerUnitTextinput.text.toString().toDouble()
            val unit = mDataBinding.pricePerUnitTextinput.text.toString()
            val quantity: Double = mDataBinding.itemQuantityTextinput.text?.let{ quantity-> if(quantity.trim().isEmpty()) 0.0 else quantity.toString().toDouble() }?:run{0.0}
            val category =mDataBinding.itemCategoryTextinput.text.toString()
            val note = mDataBinding.notesTextinput.text.toString()
            val path = mGroceryListViewModel?.selectedGroceryItemEntityCurrentImageUri?.path
            val imageName = if(path !=null) File(path).name else ""

            val intent = Intent()
            intent.putExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
            intent.putExtra(GROCERY_LIST_ITEM_NAME_TAG,itemName)
            intent.putExtra(GROCERY_LIST_ITEM_PRICE_TAG,price)
            intent.putExtra(GROCERY_LIST_ITEM_UNIT_TAG,unit)
            intent.putExtra(IMAGE_TEMP_NAME,imageName)
            intent.putExtra(ITEM_QUANTITY_TAG,quantity)
            intent.putExtra(ITEM_CATEGORY,category)
            intent.putExtra(ITEM_NOTES,note)
            intent.putExtra(GROCERY_LIST_ACTION_EXTRA_DATA_TAG, ADD_NEW_RECORD_FROM_BROWSER)

            activity?.let {fragmentActivity->
                fragmentActivity.setResult(AppCompatActivity.RESULT_OK, intent)
                fragmentActivity.finish()
            }
        }else if(action == UPDATE_RECORD_ACTION){

            val itemName = mDataBinding.itemNameTextinput.text.toString()
            val price = if(mDataBinding.pricePerUnitTextinput.text.toString().trim().isEmpty())  0.0 else mDataBinding.pricePerUnitTextinput.text.toString().toDouble()
            val unit = mDataBinding.unitTextinput.text.toString()
            val quantity: Double = mDataBinding.itemQuantityTextinput.text?.let{ quantity-> if(quantity.trim().isEmpty()) 0.0 else quantity.toString().toDouble() }?:run{0.0}
            val category =mDataBinding.itemCategoryTextinput.text.toString()
            val note = mDataBinding.notesTextinput.text.toString()

            val path = mGroceryListViewModel?.selectedGroceryItemEntityNewImageUri?.let {
                it.path
            }?:run{
                mGroceryListViewModel?.selectedGroceryItemEntityCurrentImageUri?.path
            }
            val imageName = if(path !=null) File(path).name else ""

            val intent = Intent()
            intent.putExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
            intent.putExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG,groceryListItemId)
            intent.putExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG,groceryListItemIndex)
            intent.putExtra(GROCERY_LIST_ITEM_NAME_TAG,itemName)
            intent.putExtra(GROCERY_LIST_ITEM_PRICE_TAG,price)
            intent.putExtra(GROCERY_LIST_ITEM_UNIT_TAG,unit)
            intent.putExtra(IMAGE_TEMP_NAME,imageName)
            intent.putExtra(ITEM_QUANTITY_TAG,quantity)
            intent.putExtra(ITEM_CATEGORY,category)
            intent.putExtra(ITEM_NOTES,note)
            intent.putExtra(GROCERY_LIST_ACTION_EXTRA_DATA_TAG, UPDATE_RECORD_FROM_BROWSER)

            activity?.let {fragmentActivity->
                fragmentActivity.setResult(AppCompatActivity.RESULT_OK, intent)
                fragmentActivity.finish()
            }


        }
    }
    companion object {

        const val IMAGE_TEMP_NAME = "temp_image"
        const val IMAGE_NAME_SUFFIX = "jpg"
        const val ADD_NEW_RECORD_ACTION = 1
        const val UPDATE_RECORD_ACTION = 2
        const val REQUEST_PICK_IMAGE = 4
        const val ADD_NEW_RECORD_FROM_BROWSER = 5
        const val UPDATE_RECORD_FROM_BROWSER = 6


        const val NO_ADDED_ITEM = 0
        const val ADDED_ITEM = 1
        const val UPDATED_ITEM = 2

        const val GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG = "GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG"
        const val GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG"
        const val GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG"
        const val GROCERY_LIST_ACTION_EXTRA_DATA_TAG = "GROCERY_LIST_ACTION_EXTRA_DATA_TAG"
        const val GROCERY_LIST_HAS_ADDED_ITEM_TAG = "GROCERY_LIST_HAS_ADDED_ITEM_TAG"
        const val GROCERY_LIST_HAS_UPDATED_ITEM_TAG = "GROCERY_LIST_HAS_UPDATED_ITEM_TAG"

        // user for adding item from browser
        const val GROCERY_LIST_ITEM_NAME_TAG = "item_name"
        const val GROCERY_LIST_ITEM_UNIT_TAG = "unit"
        const val GROCERY_LIST_ITEM_PRICE_TAG = "price"
        const val ITEM_QUANTITY_TAG = "quantity"
        const val ITEM_CATEGORY = "category"
        const val ITEM_NOTES = "notes"


        const val GROCERY_LIST_ITEM_IMAGE_NAME_TAG = "image_path"

        @JvmStatic fun newInstance(groceryListUniqueId: String?, groceryListItemId: String?,action:Int,groceryListItemIndex:Int) =
            AddGroceryListItemFragment().apply {
                arguments = Bundle().apply {
                    Log.e("THE_ACTION","${action} ${groceryListItemId}")
                    putString(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
                    putString(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG,groceryListItemId)
                    putInt(GROCERY_LIST_ACTION_EXTRA_DATA_TAG,action)
                    putInt(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG,groceryListItemIndex)
                }
            }
        @JvmStatic fun newInstance(groceryListUniqueId: String?, action:Int,itemName:String,itemUnit:String,itemPrice:Double,quantity:Double,category: String,notes:String,itemImageName:String) =
            AddGroceryListItemFragment().apply {
                arguments = Bundle().apply {

                    putString(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
                    putInt(GROCERY_LIST_ACTION_EXTRA_DATA_TAG,action)
                    putString(GROCERY_LIST_ITEM_NAME_TAG,itemName)
                    putString(GROCERY_LIST_ITEM_UNIT_TAG,itemUnit)
                    putDouble(GROCERY_LIST_ITEM_PRICE_TAG,itemPrice)
                    putDouble(ITEM_QUANTITY_TAG,quantity)
                    putString(ITEM_CATEGORY,category)
                    putString(ITEM_NOTES,notes)
                    putString(GROCERY_LIST_ITEM_IMAGE_NAME_TAG,itemImageName)

                }
            }
        @JvmStatic fun newInstance(groceryListUniqueId: String?,groceryItemId:String,groceryItemIndex:Int, action:Int,itemName:String,itemUnit:String,itemPrice:Double,quantity:Double,category: String,notes:String,itemImageName:String) =
            AddGroceryListItemFragment().apply {
                arguments = Bundle().apply {
                    putString(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
                    putInt(GROCERY_LIST_ACTION_EXTRA_DATA_TAG,action)
                    putString(GROCERY_LIST_ITEM_NAME_TAG,itemName)
                    putString(GROCERY_LIST_ITEM_UNIT_TAG,itemUnit)
                    putDouble(GROCERY_LIST_ITEM_PRICE_TAG,itemPrice)
                    putDouble(ITEM_QUANTITY_TAG,quantity)
                    putString(ITEM_CATEGORY,category)
                    putString(ITEM_NOTES,notes)
                    putString(GROCERY_LIST_ITEM_IMAGE_NAME_TAG,itemImageName)
                    putString(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG,groceryItemId)
                    putInt(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG,groceryItemIndex)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initGroceryItemEntity =  GroceryItemEntity(uniqueId = "",groceryListUniqueId="", sequence = 0, itemName = "", quantity = 0.0, unit = "", pricePerUnit = 0.0, category = "", notes = "", imageName = "", itemStatus = GroceryItemEntityValues.ACTIVE_STATUS,
            datetimeCreated = "",datetimeModified = "" )



        arguments?.let {
            action = it.getInt(GROCERY_LIST_ACTION_EXTRA_DATA_TAG, ADD_NEW_RECORD_ACTION)

            groceryListItemId = it.getString(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG)?.let { groceryListItemId->groceryListItemId }?:run{UUID.randomUUID().toString()}
            groceryListItemIndex = it.getInt(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, -1)

            it.getString(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {groceryListUniqueIdParam->
                groceryListUniqueId = groceryListUniqueIdParam
            }

            if(action == ADD_NEW_RECORD_FROM_BROWSER){
                initGroceryItemEntity.itemName = it.getString(GROCERY_LIST_ITEM_NAME_TAG).toString()
                initGroceryItemEntity.unit = it.getString(GROCERY_LIST_ITEM_UNIT_TAG).toString()
                initGroceryItemEntity.pricePerUnit = it.getDouble(GROCERY_LIST_ITEM_PRICE_TAG)
                initGroceryItemEntity.imageName = it.getString(GROCERY_LIST_ITEM_IMAGE_NAME_TAG).toString()
                initGroceryItemEntity.quantity = it.getDouble(ITEM_QUANTITY_TAG)
                initGroceryItemEntity.category = it.getString(ITEM_CATEGORY).toString()
                initGroceryItemEntity.notes = it.getString(ITEM_NOTES).toString()


            }else if(action == UPDATE_RECORD_FROM_BROWSER){
                initGroceryItemEntity.itemName = it.getString(GROCERY_LIST_ITEM_NAME_TAG).toString()
                initGroceryItemEntity.unit = it.getString(GROCERY_LIST_ITEM_UNIT_TAG).toString()
                initGroceryItemEntity.pricePerUnit = it.getDouble(GROCERY_LIST_ITEM_PRICE_TAG)
                initGroceryItemEntity.imageName = it.getString(GROCERY_LIST_ITEM_IMAGE_NAME_TAG).toString()
                initGroceryItemEntity.quantity = it.getDouble(ITEM_QUANTITY_TAG)
                initGroceryItemEntity.category = it.getString(ITEM_CATEGORY).toString()
                initGroceryItemEntity.notes = it.getString(ITEM_NOTES).toString()
            }
        }
        val addGroceryListItemActivityViewModelFactory = GroceryListViewModelFactory(null, initGroceryItemEntity)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(GroceryListViewModel::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        //Bind data
        mDataBinding = DataBindingUtil.inflate<FragmentAddGroceryListItemBinding?>(inflater, R.layout.fragment_add_grocery_list_item, container, false).apply {
            this.lifecycleOwner = this@AddGroceryListItemFragment
            this.groceryListViewModel = mGroceryListViewModel
        }

        if(action == UPDATE_RECORD_ACTION){

            CoroutineScope(Dispatchers.IO).launch {
                mGroceryListViewModel.getGroceryListItem(requireContext(), groceryListItemId, groceryListUniqueId)
                mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri = GroceryUtil.getImageFromPath(requireContext(), mGroceryListViewModel.selectedGroceryItem!!.imageName)
                withContext(Main){
                    mDataBinding.invalidateAll()
                }
            }
        }else if(action == UPDATE_RECORD_FROM_BROWSER){
            mDataBinding.browseItemImageBtn.visibility = View.GONE

            val uri = getImageUriForUpdatingRecordFromBrowser(mGroceryListViewModel.selectedGroceryItem!!.imageName)
            mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri = uri
            mGroceryListViewModel.selectedGroceryItemEntityNewImageUri = uri
            mDataBinding.invalidateAll()

        }else if(action == ADD_NEW_RECORD_FROM_BROWSER){
            mDataBinding.browseItemImageBtn.visibility = View.GONE
            val uri = ImageUtil.getImageUriFromPath(requireContext(), GroceryUtil.TEMPORARY_IMAGES_LOCATION,mGroceryListViewModel.selectedGroceryItem!!.imageName)
            mGroceryListViewModel.selectedGroceryItemEntityNewImageUri = uri
            mDataBinding.invalidateAll()

        }

        val toolBar = mDataBinding.toolbar
        toolBar.title = "New item"
        toolBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolBar.setNavigationOnClickListener(toolbarNavigationOnClickListener)
        toolBar.menu.let { menu->
            if(action == ADD_NEW_RECORD_ACTION || action == ADD_NEW_RECORD_FROM_BROWSER){
                menu?.findItem(R.id.add_item)?.isVisible = true
            }else if(action == UPDATE_RECORD_ACTION || action == UPDATE_RECORD_FROM_BROWSER){
                menu?.findItem(R.id.update_item)?.isVisible = true
            }
        }
        toolBar.setOnMenuItemClickListener {item->
            when (item.itemId) {
                R.id.add_item -> {

                    checkIfItemExists()
                    //addRecord()
                }
                R.id.update_item -> {
                    updateRecord()
                }
            }
            true
        }

        val itemNameAutoSuggestCustomAdapter = ItemNameAutoSuggestCustomAdapter(requireContext(), arrayListOf())
        mDataBinding.itemNameTextinput.threshold = 0
        mDataBinding.itemNameTextinput.setAdapter(itemNameAutoSuggestCustomAdapter)
        mDataBinding.itemNameTextinput.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            val groceryItemEntityForAutoSuggest: GroceryItemEntityForAutoSuggest = parent.getItemAtPosition(position) as GroceryItemEntityForAutoSuggest
            val groceryItemEntity: GroceryItemEntity = groceryItemEntityForAutoSuggest.groceryItemEntity
            mDataBinding.groceryListViewModel?.selectedGroceryItem = groceryItemEntity
            // set 1 as default value
            mDataBinding.groceryListViewModel?.selectedGroceryItem!!.quantity = 1.0

            val imageUri = GroceryUtil.getImageFromPath(requireContext(), groceryItemEntity.imageName)


            if (imageUri != null) {
                mDataBinding.groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri = imageUri
                mDataBinding.groceryListViewModel?.selectedGroceryItemEntityNewImageUri = imageUri

            }
            mDataBinding.invalidateAll()
        }

        mDataBinding.browseItemImageBtn.setOnClickListener(browseItemImageBtnOnClick)
        mDataBinding.addImgBtn.setOnClickListener{
            showIntentChooser()
        }
        val itemUnitAutoSuggestCustomAdapter = UnitAutoSuggestCustomAdapter(requireContext(), arrayListOf())
        mDataBinding.unitTextinput.threshold = 0
        mDataBinding.unitTextinput.setAdapter(itemUnitAutoSuggestCustomAdapter)

        val itemCategoryAutoSuggestCustomAdapter = CategoryAutoSuggestCustomAdapter(requireContext(), arrayListOf())
        mDataBinding.itemCategoryTextinput.threshold = 0
        mDataBinding.itemCategoryTextinput.setAdapter(itemCategoryAutoSuggestCustomAdapter)

        return mDataBinding.root
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_PICK_IMAGE){
            data?.data?.let{
                launchImageCropper(it)
            }
            tempPhotoFileForAddingImage?.let{
                val fileUri = Uri.fromFile(tempPhotoFileForAddingImage) as Uri
                launchImageCropper(fileUri)
            }
        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
//            val result = CropImage.getActivityResult(data)
//            val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
//            if(!storageDir.exists()){
//                storageDir.mkdir()
//            }
//            val itemBitmap = ImageUtil.uriToBitmap(result.uri,requireContext())
//            val uri = ImageUtil.saveImage(requireContext(),itemBitmap,GroceryUtil.TEMPORARY_IMAGES_LOCATION,"$IMAGE_TEMP_NAME.$IMAGE_NAME_SUFFIX")?.let {
//                Uri.fromFile(File(it))
//            }
//            mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  uri
//            mDataBinding.itemImageview.setImageURI(uri)



        }
    }
    private fun checkIfItemExists(){
        val itemName: String = mDataBinding.itemNameTextinput.text.toString().trim()
        val unit: String = mDataBinding.unitTextinput.text.toString().trim()

        if (itemName.trim().isEmpty()) {
            val customConfirmationDialog =CustomConfirmationDialog(requireContext())
            customConfirmationDialog.setCustomMessage("Please provide name.")
            customConfirmationDialog.show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val mGroceryItemEntity = mGroceryListViewModel.getGroceryItem(requireContext(),groceryListUniqueId,itemName,unit,GroceryItemEntityValues.ACTIVE_STATUS)
            withContext(Main){
                mGroceryItemEntity?.let {
                    Toast.makeText(requireContext(),"Update",Toast.LENGTH_SHORT).show()

                    val message = unit?.let {
                        if(it.isEmpty()){
                            "$itemName already exists."
                        }else{
                            "$unit of $itemName already exists."
                        }
                    }
                    val customConfirmationDialog =CustomConfirmationDialog(requireContext())
                    customConfirmationDialog.setCancelable(false)
                    customConfirmationDialog.setCustomMessage("$message")
                    customConfirmationDialog.createNegativeButton("Close")
                    customConfirmationDialog.show()

                }?:run{
                    addRecord()
                }
            }
        }


    }
    private fun addRecord() {
        val itemName: String = mDataBinding.itemNameTextinput.text.toString().trim()
        val quantityString = mDataBinding.itemQuantityTextinput.text.toString().trim()
        val unit: String = mDataBinding.unitTextinput.text.toString().trim()
        val pricePerUnitString = mDataBinding.pricePerUnitTextinput.text.toString() .trim()
        val category = mDataBinding.itemCategoryTextinput.text.toString().trim()
        val notes: String = mDataBinding.notesTextinput.text.toString().trim()

        if (itemName.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please provide name", Toast.LENGTH_SHORT).show()
            return
        }

        val doubleQuantity = if(quantityString.trim().isNotEmpty()) quantityString.toDouble() else 0.0
        val doublePricePerUnit = if(pricePerUnitString.trim().isNotEmpty()) pricePerUnitString.toDouble() else 0.0

        var imageName = ""

        mGroceryListViewModel.selectedGroceryItemEntityNewImageUri?.let{
            imageName = groceryListUniqueId+"_"+itemName+"."+ AddGroceryListItemFragment.IMAGE_NAME_SUFFIX
            val imageSavedSucessfully = saveImage(it, imageName)
            if(!imageSavedSucessfully){
                Toast.makeText(requireContext(), "Failed to save image.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val autoGeneratedUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


        val groceryItemEntity = GroceryItemEntity(
            uniqueId = autoGeneratedUniqueID,
            groceryListUniqueId = groceryListUniqueId, sequence = 1, itemName = itemName, quantity = doubleQuantity,
            unit = unit, pricePerUnit = doublePricePerUnit, category = category, notes = notes, imageName = imageName, bought = 0,itemStatus = GroceryItemEntityValues.ACTIVE_STATUS,
            datetimeCreated = currentDatetime,datetimeModified = currentDatetime
        )
        // hide soft keyboard

//        val view: View = activity.findViewById(android.R.id.content)
//        val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)

        CoroutineScope(Dispatchers.IO).launch {

            mGroceryListViewModel.addGroceryListItem(requireContext(),groceryItemEntity)
            mGroceryListViewModel.updateGroceryListAsNotUploaded(requireContext(),groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)

            withContext(Main){

                activity?.let{
                    val intent = Intent()
                    intent.putExtra(GROCERY_LIST_HAS_ADDED_ITEM_TAG, ADDED_ITEM)
                    it.setResult(AppCompatActivity.RESULT_OK,intent)
                    it.finish()
                }
            }
        }

    }
    private fun updateRecord(){
        val itemName: String = mDataBinding.itemNameTextinput.text.toString().trim()
        val quantityString = mDataBinding.itemQuantityTextinput.text.toString()
        val unit: String = mDataBinding.unitTextinput.text.toString().trim()
        val pricePerUnitString = mDataBinding.pricePerUnitTextinput.text.toString().trim()
        val category = mDataBinding.itemCategoryTextinput.text.toString().trim()
        val notes: String = mDataBinding.notesTextinput.text.toString().trim()


        if (itemName.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please provide name", Toast.LENGTH_SHORT).show()
            return
        }
        val doubleQuantity = if(quantityString.trim().isNotEmpty()) quantityString.toDouble() else 0.0
        val doublePricePerUnit = if(pricePerUnitString.trim().isNotEmpty()) pricePerUnitString.toDouble() else 0.0

        // hide soft keyboard
//        val view: View = findViewById(android.R.id.content)
//        val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)

        val imageName =  groceryListUniqueId+"_"+itemName+"."+ AddGroceryListItemFragment.IMAGE_NAME_SUFFIX

        if(mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri != null && mGroceryListViewModel.selectedGroceryItemEntityNewImageUri !=null ){
            //delete old image and save new image
            if(GroceryUtil.doImageFromPathExists(requireContext(), mGroceryListViewModel.selectedGroceryItem!!.imageName)){
                GroceryUtil.deleteImageFile(requireContext(), mGroceryListViewModel.selectedGroceryItem!!.imageName)
            }
            val imageSavedSucessfully = saveImage(mGroceryListViewModel.selectedGroceryItemEntityNewImageUri!!, imageName)
            if(!imageSavedSucessfully){
                Toast.makeText(requireContext(), "Failed to save image 1.", Toast.LENGTH_SHORT).show()
                return
            }

        }else if(mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri == null && mGroceryListViewModel.selectedGroceryItemEntityNewImageUri !=null ){
            //save image
            val imageSavedSucessfully = saveImage(mGroceryListViewModel.selectedGroceryItemEntityNewImageUri!!, imageName)
            if(!imageSavedSucessfully){
                Toast.makeText(requireContext(), "Failed to save image 2.", Toast.LENGTH_SHORT).show()
                return
            }
        }else if(mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri != null && mGroceryListViewModel.selectedGroceryItemEntityNewImageUri ==null ){
            //rename image

            if(GroceryUtil.doImageFromPathExists(requireContext(), mGroceryListViewModel.selectedGroceryItem!!.imageName)){
                GroceryUtil.renameImageFile(requireContext(), mGroceryListViewModel.selectedGroceryItem!!.imageName, imageName)
            }
        }



        CoroutineScope(Dispatchers.IO).launch {

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            mGroceryListViewModel.updateGroceryListAsNotUploaded(requireContext(),groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)
            mGroceryListViewModel.updateGroceryItem(requireContext(), itemName, doubleQuantity, unit, doublePricePerUnit, category, notes, imageName, groceryListItemId,currentDatetime, GroceryItemEntity.NOT_YET_UPLOADED)


            withContext(Dispatchers.Main){

                val intent = Intent()

                intent.putExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
                intent.putExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, groceryListItemIndex)
                intent.putExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, groceryListItemId)
                activity?.let {
                    intent.putExtra(GROCERY_LIST_HAS_UPDATED_ITEM_TAG, UPDATED_ITEM)
                    it.setResult(AppCompatActivity.RESULT_OK,intent)
                    it.finish()
                }

            }
        }
    }
    private fun getImageUriForUpdatingRecordFromBrowser(imageName:String):Uri?{
        return imageName?.let { imageName->
            var imageUri:Uri? = null
            val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
            //create directory if not exists
            if(!storageDir.exists()){
                storageDir.mkdir()
            }
            val imageFileFromTempStorage = File(storageDir, imageName)
            if(imageFileFromTempStorage.exists()){
                imageUri = ImageUtil.getImageUriFromPath(requireContext(), imageFileFromTempStorage.parentFile.name,mGroceryListViewModel.selectedGroceryItem!!.imageName)

            }else{
                val finalStorageDir: File = requireContext().getExternalFilesDir(GroceryUtil.GROCERY_ITEM_IMAGES_LOCATION)!!
                if(!finalStorageDir.exists()){
                    storageDir.mkdir()
                }
                val imageFileFromFinalStorage = File(finalStorageDir, imageName)
                if(imageFileFromFinalStorage.exists()){
                    imageUri = ImageUtil.getImageUriFromPath(requireContext(), imageFileFromFinalStorage.parentFile.name,mGroceryListViewModel.selectedGroceryItem!!.imageName)
                }
            }

            imageUri

        }
    }
    private fun launchImageCropper(uri: Uri){

//        CropImage.activity(uri)
//            .setGuidelines(CropImageView.Guidelines.ON)
//            //.setAspectRatio(500, 500)
//            .setCropShape(CropImageView.CropShape.RECTANGLE)
//            .start(requireContext(), this);

        cropImage.launch(
            options(uri = uri) {
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1000, 1000)
                setCropShape(CropImageView.CropShape.RECTANGLE)
            }
        )
    }
    private fun showIntentChooser(){
        // Determine Uri of camera image to save.

        // create temporary file
        tempPhotoFileForAddingImage = createImageFile()
        var photoURI = FileProvider.getUriForFile(requireContext(), "com.example.allhome.fileprovider", tempPhotoFileForAddingImage!!)

        // Camera.
        val imageIntents: MutableList<Intent> = java.util.ArrayList()
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val packageManager = requireContext().packageManager
        val listCam = packageManager.queryIntentActivities(captureIntent, 0)
        for (res in listCam) {
            val packageName = res.activityInfo.packageName
            val intent = Intent(captureIntent)
            intent.component = ComponentName(packageName, res.activityInfo.name)
            intent.setPackage(packageName)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            imageIntents.add(intent)
        }

        // all intent for picking image. eg Gallery app
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageIntent.type = "image/*"
        val pickImageResolver = packageManager.queryIntentActivities(pickImageIntent, 0)
        for (res in pickImageResolver) {
            val intent = Intent(pickImageIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.setPackage(res.activityInfo.packageName)
            imageIntents.add(intent)
        }

        val finalIntent = Intent()
        finalIntent.type = "image/*"
        finalIntent.action = Intent.ACTION_GET_CONTENT

        // Chooser of filesystem options.
        val chooserIntent = Intent.createChooser(finalIntent, "Select Source")
        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, imageIntents.toTypedArray<Parcelable>())
        startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE)

    }
    private fun saveImage(imageUri: Uri, imageName: String):Boolean{
        val imageBitmap = uriToBitmap(imageUri, requireContext())

        val imageWidth = imageBitmap.width
        val imageHeight = imageBitmap.height
        val targetMaxWidthOrHeight = 800

        val imageWidthAndHeight = ImageUtil.getProportionImageSize(targetMaxWidthOrHeight, imageWidth,imageHeight)
        val resizedImageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageWidthAndHeight["width"]!!, imageWidthAndHeight["height"]!!, false)
        val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.GROCERY_ITEM_IMAGES_LOCATION)!!

        if(!storageDir.exists()){
            storageDir.mkdir()
        }
        val file  = File(storageDir, imageName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            resizedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()

            return false
        }
    }
    private fun uriToBitmap(uri: Uri, context: Context): Bitmap {

        if(Build.VERSION.SDK_INT < 28) {
            return  MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

        } else {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            return ImageDecoder.decodeBitmap(source)

        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!

        if(!storageDir.exists()){
            storageDir.mkdir()
        }

        //return File(storageDir,BrowseItemImageFragment.TEMP_IMAGE_NAME)
        return File.createTempFile(
            IMAGE_TEMP_NAME, /* prefix */
            ".${IMAGE_NAME_SUFFIX}", /* suffix */
            storageDir /* directory */
        )
    }
    /**
     * Item name auto suggest adapter
     */
    inner class ItemNameAutoSuggestCustomAdapter(context: Context, var groceryItemEntitiesParams: List<GroceryItemEntityForAutoSuggest>): ArrayAdapter<GroceryItemEntityForAutoSuggest>(context, 0, groceryItemEntitiesParams) {
        private var groceryItemEntities: List<GroceryItemEntityForAutoSuggest>? = null
        init{
            groceryItemEntities = ArrayList(groceryItemEntitiesParams)


        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val results = FilterResults()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val searchTerm = if (constraint == null) "" else constraint.toString()
                    val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntitiesForAutoSuggest(searchTerm, groceryListUniqueId)
                    withContext(Dispatchers.Main) {
                        results.values = arrayList
                        results.count = arrayList.size
                        publishResults(constraint, results)
                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                val res:List<GroceryItemEntityForAutoSuggest> = results.values as ArrayList<GroceryItemEntityForAutoSuggest>
                addAll(res)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as GroceryItemEntityForAutoSuggest).groceryItemEntity.itemName
            }
        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var textView: TextView? = null
            if(convertView == null){
                textView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                textView = convertView as TextView?
            }
            val groceryItemEntity = getItem(position)
            textView!!.text = groceryItemEntity?.groceryItemEntity?.itemName

            if(groceryItemEntity?.itemInListCount!! > 0){
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                textView.setTextColor(Color.GRAY)

            }else{
                textView.paintFlags = 0
                textView.setTextColor(Color.parseColor("#000000"))
            }




            return textView
        }
    }
    /**
     * Item unit auto suggest adapter
     */
    inner class UnitAutoSuggestCustomAdapter(context: Context, var groceryItemEntityUnitsParams: List<String>):
        ArrayAdapter<String>(context, 0, groceryItemEntityUnitsParams) {
        private var groceryItemEntityUnits: List<String>? = null
        init{
            groceryItemEntityUnits = ArrayList(groceryItemEntityUnitsParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val results = FilterResults()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val searchTerm = if (constraint == null) "" else constraint.toString()
                    val suggestion = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntityUnits(searchTerm)
                    withContext(Dispatchers.Main) {
                        results.values = suggestion
                        results.count = suggestion.size
                        publishResults(constraint, results)
                    }
                }
                return results


            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                val res:List<String> = results.values as ArrayList<String>
                addAll(res)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue as CharSequence
            }
        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var textView: TextView? = null
            if(convertView == null){
                textView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                textView = convertView as TextView?
            }
            val unit = getItem(position)
            textView!!.text = unit
            return textView
        }
    }


    /**
     * Item category auto suggest adapter
     */
    inner class CategoryAutoSuggestCustomAdapter(context: Context, var groceryItemEntityCategoriesParams: List<String>):
        ArrayAdapter<String>(context, 0, groceryItemEntityCategoriesParams) {
        private var groceryItemEntityCategories: List<String>? = null
        init{
            groceryItemEntityCategories = ArrayList(groceryItemEntityCategoriesParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {

                val results = FilterResults()
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val searchTerm = if (constraint == null) "" else constraint.toString()
                    val suggestion = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntityCategories(searchTerm)
                    withContext(Dispatchers.Main) {
                        results.values = suggestion
                        results.count = suggestion.size
                        publishResults(constraint, results)
                    }
                }
                return results


            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                val res:List<String> = results.values as ArrayList<String>
                addAll(res)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue as CharSequence
            }
        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var textView: TextView? = null
            if(convertView == null){
                textView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                textView = convertView as TextView?
            }
            val category = getItem(position)
            textView!!.text = category
            return textView
        }
    }



}