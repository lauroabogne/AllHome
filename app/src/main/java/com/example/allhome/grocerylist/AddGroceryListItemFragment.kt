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
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityForAutoSuggest
import com.example.allhome.data.entities.GroceryItemEntityValues
import com.example.allhome.data.entities.GroceryListEntityValues
import com.example.allhome.databinding.FragmentAddGroceryListItemBinding
import com.example.allhome.databinding.FragmentAddRecipeStepsBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import com.example.allhome.grocerylist.viewmodel_factory.GroceryListViewModelFactory
import com.example.allhome.utils.ImageUtil
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddGroceryListItemFragment : Fragment() {


    lateinit var mDataBinding: FragmentAddGroceryListItemBinding
    private lateinit var mGroceryListViewModel: GroceryListViewModel
    var groceryListUniqueId: String = ""
    var action = ADD_NEW_RECORD_ACTION
    var groceryListItemId = 0
    var groceryListItemIndex = -1
    var tempPhotoFileForAddingImage: File? = null
    var imageChanged = false

    companion object {

        const val IMAGE_TEMP_NAME = "temp_image"
        const val IMAGE_NAME_SUFFIX = "jpg"
        const val ADD_NEW_RECORD_ACTION = 1
        const val UPDATE_RECORD_ACTION = 2
        const val REQUEST_PICK_IMAGE = 4
        const val ADD_NEW_RECORD_FROM_BROWSER = 5


        const val GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG = "GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG"
        const val GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG"
        const val GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG = "GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG"
        const val GROCERY_LIST_ACTION_EXTRA_DATA_TAG = "GROCERY_LIST_ACTION_EXTRA_DATA_TAG"

        @JvmStatic fun newInstance(groceryListUniqueId: String?, groceryListItemId: Int,action:Int,groceryListItemIndex:Int) =
            AddGroceryListItemFragment().apply {
                arguments = Bundle().apply {
                    Log.e("THE_ACTION","${action} ${groceryListItemId}")
                    putString(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
                    putInt(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG,groceryListItemId)
                    putInt(GROCERY_LIST_ACTION_EXTRA_DATA_TAG,action)
                    putInt(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG,groceryListItemIndex)
                }
            }


    }

    private val openBrowseImageContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        activityResult.data?.let {

            val itemName = it.getStringExtra(BrowseItemImageFragment.ITEM_NAME_TAG)!!
            val itemUnit = it.getStringExtra(BrowseItemImageFragment.ITEM_UNIT_TAG)!!
            val price = it.getDoubleExtra(BrowseItemImageFragment.ITEM_PRICE_TAG,0.0)
            val tempImagePath = it.getStringExtra(BrowseItemImageFragment.ITEM_IMAGE_NAME_TAG)

            mGroceryListViewModel.selectedGroceryItem?.itemName = itemName
            mGroceryListViewModel.selectedGroceryItem?.unit = itemUnit
            mGroceryListViewModel.selectedGroceryItem?.pricePerUnit = price


            if(tempImagePath != null){
                val imageUri = Uri.fromFile(File(tempImagePath))

                mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  imageUri
                mDataBinding.itemImageview.setImageURI(null)//set image url to null. The ImageView won't reload the image if you call setImageURI with the same URI
                mDataBinding.itemImageview.setImageURI(imageUri)
            }
            mDataBinding.invalidateAll()





//            val intent = Intent()
//            intent.putExtra(BrowseItemImageFragment.TEMP_IMAGE_NAME, mImageAbsolutePath)
//            intent.putExtra(, mItemName)
//            intent.putExtra(BrowseItemImageFragment.ITEM_UNIT_TAG, mItemUnit)
//            intent.putExtra(BrowseItemImageFragment.ITEM_PRICE_TAG, mPrice)
//
//            it.getStringExtra(BrowseItemImageFragment.TEMP_IMAGE_NAME)?.let {imagePath->
//
//                Toast.makeText(requireContext(),"Has data ${imagePath}",Toast.LENGTH_SHORT).show()
//                val imageUri = Uri.fromFile(File(imagePath))
//                mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  imageUri
//                mDataBinding.itemImageview.setImageURI(null)//set image url to null. The ImageView won't reload the image if you call setImageURI with the same URI
//                mDataBinding.itemImageview.setImageURI(imageUri)
//
//            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

            groceryListItemId = it.getInt(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, 0)
            action = it.getInt(GROCERY_LIST_ACTION_EXTRA_DATA_TAG, ADD_NEW_RECORD_ACTION)
            groceryListItemIndex = it.getInt(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, -1)

            it.getString(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {groceryListUniqueIdParam->
                groceryListUniqueId = groceryListUniqueIdParam
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        setHasOptionsMenu(true)

        val initGroceryItemEntity = GroceryItemEntity("", 0, "", 0.0, "", 0.0, "",
            "", "", 0,itemStatus = GroceryItemEntityValues.ACTIVE_STATUS,
            datetimeCreated = "",datetimeModified = "")
        val addGroceryListItemActivityViewModelFactory = GroceryListViewModelFactory(null, initGroceryItemEntity)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(GroceryListViewModel::class.java)


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
        }

        val toolBar = mDataBinding.toolbar
        toolBar.title = "New item"
        toolBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolBar.setNavigationOnClickListener {
            activity?.finish()
        }
        toolBar.menu.let { menu->
            if(action == AddGroceryListItemFragment.ADD_NEW_RECORD_ACTION){
                menu?.findItem(R.id.add_item)?.isVisible = true

            }else if(action == AddGroceryListItemFragment.UPDATE_RECORD_ACTION){
                menu?.findItem(R.id.update_item)?.isVisible = true
            }
        }
        toolBar.setOnMenuItemClickListener {item->
            when (item.itemId) {
                android.R.id.home -> {
                    activity?.finish()
                }
                R.id.add_item -> {
                    addRecord()
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
                Log.e("IMAGE", "HAS IMAGE")
            } else {
                Log.e("IMAGE", "NO IMAGE")
            }

            mDataBinding.invalidateAll()
        }

        mDataBinding.browseItemImageBtn.setOnClickListener{

            val itemName = mDataBinding.itemNameTextinput.text.toString()
            val price = if(mDataBinding.pricePerUnitTextinput.text.toString().trim().isEmpty())  0.0 else mDataBinding.pricePerUnitTextinput.text.toString().toDouble()
            val unit = mDataBinding.pricePerUnitTextinput.text.toString()
            val path = mGroceryListViewModel?.selectedGroceryItemEntityCurrentImageUri?.path
            val imageName = if(path !=null) File(path).name else ""


            val browseItemActivity = Intent(requireContext(),BrowserItemImageActivity::class.java)
            browseItemActivity.putExtra(BrowseItemImageFragment.ITEM_NAME_TAG,itemName)
            browseItemActivity.putExtra(BrowseItemImageFragment.ITEM_PRICE_TAG,price)
            browseItemActivity.putExtra(BrowseItemImageFragment.ITEM_UNIT_TAG,unit)
            browseItemActivity.putExtra(BrowseItemImageFragment.ITEM_IMAGE_NAME_TAG,imageName)

            openBrowseImageContract.launch(browseItemActivity)
        }
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
                lauchImageCropper(it)
            }

            tempPhotoFileForAddingImage?.let{
                val fileUri = Uri.fromFile(tempPhotoFileForAddingImage) as Uri
                lauchImageCropper(fileUri)
            }
        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val result = CropImage.getActivityResult(data)

            mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  result.uri

            mDataBinding.itemImageview.setImageURI(result.uri)



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

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val groceryItemEntity = GroceryItemEntity(
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

            withContext(Dispatchers.Main){


                activity?.let{
                    it.setResult(AppCompatActivity.RESULT_OK)
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
            mGroceryListViewModel.updateGroceryItem(requireContext(), itemName, doubleQuantity, unit, doublePricePerUnit, category, notes, imageName, groceryListItemId,currentDatetime)


            withContext(Dispatchers.Main){

                val intent = Intent()
                intent.putExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, groceryListItemIndex)
                intent.putExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, groceryListItemId)
                activity?.let {
                    it.setResult(AppCompatActivity.RESULT_OK, intent)
                    it.finish()
                }

            }
        }
    }
    private fun lauchImageCropper(uri: Uri){

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            //.setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(requireContext(), this);
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
        Log.e("width and height", "$imageWidth  $imageHeight")
        Log.e("proportion","$imageWidthAndHeight")

        val resizedImageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageWidthAndHeight["width"]!!, imageWidthAndHeight["height"]!!, false)
        val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.FINAL_IMAGES_LOCATION)!!

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
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(Dispatchers.IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntitiesForAutoSuggest(searchTerm,groceryListUniqueId)
                        results.apply {
                            results.values = arrayList
                            results.count = arrayList.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
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
    class UnitAutoSuggestCustomAdapter(context: Context, var groceryItemEntityUnitsParams: List<String>):
        ArrayAdapter<String>(context, 0, groceryItemEntityUnitsParams) {
        private var groceryItemEntityUnits: List<String>? = null
        init{
            groceryItemEntityUnits = ArrayList(groceryItemEntityUnitsParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(Dispatchers.IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntityUnits(searchTerm)
                        results.apply {
                            results.values = arrayList
                            results.count = arrayList.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
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
    class CategoryAutoSuggestCustomAdapter(context: Context, var groceryItemEntityCategoriesParams: List<String>):
        ArrayAdapter<String>(context, 0, groceryItemEntityCategoriesParams) {
        private var groceryItemEntityCategories: List<String>? = null
        init{
            groceryItemEntityCategories = ArrayList(groceryItemEntityCategoriesParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(Dispatchers.IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val arrayList = AllHomeDatabase.getDatabase(context).groceryItemDAO().getGroceryItemEntityCategories(searchTerm)
                        results.apply {
                            results.values = arrayList
                            results.count = arrayList.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
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