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
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.ActivityAddGroceryListItemBinding
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

    private lateinit var dataBindingUtil: ActivityAddGroceryListItemBinding
    private lateinit var mGroceryListViewModel: GroceryListViewModel
    var groceryListUniqueId: String = ""
    var action = ADD_NEW_RECORD_ACTION
    var groceryListItemId = 0
    var groceryListItemIndex = -1
    var tempPhotoFileForAddingImage: File? = null
   // var croppedImageUri:Uri? = null
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

    private val openBrowseImageContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        activityResult.data?.let {
            it.getStringExtra(BrowseItemImageFragment.TEMP_IMAGE_NAME)?.let {imagePath->
                Toast.makeText(this@AddGroceryListItemActivity,"Has data ${imagePath}",Toast.LENGTH_SHORT).show()
                val imageUri = Uri.fromFile(File(imagePath))
                mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  imageUri
                dataBindingUtil.itemImageview.setImageURI(null)//set image url to null. The ImageView won't reload the image if you call setImageURI with the same URI
                dataBindingUtil.itemImageview.setImageURI(imageUri)


            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_grocery_list_item)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        intent.getStringExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
            groceryListUniqueId = it
        }

        groceryListItemId = intent.getIntExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, 0)
        action = intent.getIntExtra(GROCERY_LIST_ACTION_EXTRA_DATA_TAG, ADD_NEW_RECORD_ACTION)
        groceryListItemIndex = intent.getIntExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, -1)


        val initGroceryItemEntity = GroceryItemEntity("", 0, "", 0.0, "", 0.0, "",
            "", "", 0,itemStatus = GroceryItemEntityValues.ACTIVE_STATUS,
             datetimeCreated = "",datetimeModified = "")
        // create AddGroceryListItemActivityViewModel using AddGroceryListItemActivityViewModelFactory
        val addGroceryListItemActivityViewModelFactory = GroceryListViewModelFactory(null, initGroceryItemEntity)
        mGroceryListViewModel = ViewModelProvider(this, addGroceryListItemActivityViewModelFactory).get(GroceryListViewModel::class.java)

        if(action == UPDATE_RECORD_ACTION){
            CoroutineScope(IO).launch {
                mGroceryListViewModel.getGroceryListItem(this@AddGroceryListItemActivity, groceryListItemId, groceryListUniqueId)

                mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri = GroceryUtil.getImageFromPath(this@AddGroceryListItemActivity, mGroceryListViewModel.selectedGroceryItem!!.imageName)

            }
        }
        //Bind data
        dataBindingUtil = DataBindingUtil.setContentView<ActivityAddGroceryListItemBinding>(this, R.layout.activity_add_grocery_list_item).apply {
            this.lifecycleOwner = this@AddGroceryListItemActivity
            this.groceryListViewModel = mGroceryListViewModel

        }

        /*dataBindingUtil.addImgBtn.setOnClickListener(View.OnClickListener {
            CoroutineScope(IO).launch {

                mGroceryListViewModel.getGroceryListItem(this@AddGroceryListItemActivity, groceryListItemId, groceryListUniqueId)
                dataBindingUtil.invalidateAll()

            }
        })*/

        val itemNameAutoSuggestCustomAdapter = ItemNameAutoSuggestCustomAdapter(this, arrayListOf())
        dataBindingUtil.itemNameTextinput.threshold = 0
        dataBindingUtil.itemNameTextinput.setAdapter(itemNameAutoSuggestCustomAdapter)
        dataBindingUtil.itemNameTextinput.onItemClickListener =  OnItemClickListener { parent, view, position, id ->

            val groceryItemEntityForAutoSuggest:GroceryItemEntityForAutoSuggest = parent.getItemAtPosition(position) as GroceryItemEntityForAutoSuggest
            val groceryItemEntity:GroceryItemEntity = groceryItemEntityForAutoSuggest.groceryItemEntity
            dataBindingUtil.groceryListViewModel?.selectedGroceryItem= groceryItemEntity
            // set 1 as default value
            dataBindingUtil.groceryListViewModel?.selectedGroceryItem!!.quantity = 1.0

            val imageUri = GroceryUtil.getImageFromPath(this, groceryItemEntity.imageName)

            Log.e("IMAGE_Path","HAS IMAGE "+groceryItemEntity.imageName)

            if(imageUri !=null){
                dataBindingUtil.groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri = imageUri
                dataBindingUtil.groceryListViewModel?.selectedGroceryItemEntityNewImageUri = imageUri
                Log.e("IMAGE","HAS IMAGE")
            }else{
                Log.e("IMAGE","NO IMAGE")
            }

            dataBindingUtil.invalidateAll()
        }

        dataBindingUtil.browseItemImageBtn.setOnClickListener{


//            val viewRecipeActivity = Intent(view?.context, ViewRecipeActivity::class.java)
//            viewRecipeActivity.putExtra(ViewRecipeFragment.RECIPE_INTENT_TAG,recipeEntity.recipeEntity)
//            mRecipesFragment.openRecipeContract.launch(viewRecipeActivity)


            val itemName = dataBindingUtil.itemNameTextinput.text.toString()
            if(itemName.trim().isEmpty()){
                Toast.makeText(this@AddGroceryListItemActivity,"Input item name first.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val browseItemActivity = Intent(this@AddGroceryListItemActivity,BrowserItemImageActivity::class.java)
            browseItemActivity.putExtra(BrowseItemImageFragment.ARG_ITEM_NAME,itemName)
            openBrowseImageContract.launch(browseItemActivity)


        }

        dataBindingUtil.addImgBtn.setOnClickListener{

            showIntentChooser()

            if(true){
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            val customView = LayoutInflater.from(this).inflate(R.layout.select_image_options, null)
            builder.setView(customView)

            val customDialog = builder.create()
            customView.findViewById<View>(R.id.cameraTextView).setOnClickListener{
                customDialog.dismiss()

                ImageUtil.deleteAllTemporaryImages(this)

                dispatchTakePictureIntent()
            }
            customView.findViewById<View>(R.id.pickFromGalleryTextView).setOnClickListener{
                customDialog.dismiss()

                ImageUtil.deleteAllTemporaryImages(this)

                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                gallery.type = "image/*"
                gallery.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(gallery, REQUEST_PICK_IMAGE)
            }

            customDialog.show()
        }

        val itemUnitAutoSuggestCustomAdapter = UnitAutoSuggestCustomAdapter(this, arrayListOf())
        dataBindingUtil.unitTextinput.threshold = 0
        dataBindingUtil.unitTextinput.setAdapter(itemUnitAutoSuggestCustomAdapter)

        val itemCategoryAutoSuggestCustomAdapter = CategoryAutoSuggestCustomAdapter(this, arrayListOf())
        dataBindingUtil.itemCategoryTextinput.threshold = 0
        dataBindingUtil.itemCategoryTextinput.setAdapter(itemCategoryAutoSuggestCustomAdapter)
    }

   private fun showIntentChooser(){
       // Determine Uri of camera image to save.

       // create temporary file
       tempPhotoFileForAddingImage = createImageFile()
       var photoURI = FileProvider.getUriForFile(this, "com.example.allhome.fileprovider", tempPhotoFileForAddingImage!!)

       // Camera.
       val imageIntents: MutableList<Intent> = java.util.ArrayList()
       val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
       val packageManager = packageManager
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
        }/*
        Log.e("RESULT_CODE",requestCode.toString())
       if(requestCode == SingleGroceryListActivity.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

            if(tempPhotoFileForAddingImage !=null){
                val fileUri = Uri.fromFile(tempPhotoFileForAddingImage) as Uri
                lauchImageCropper(fileUri)
            }


        }else if(requestCode == SingleGroceryListActivity.REQUEST_PICK_IMAGE && resultCode == RESULT_OK){

            lauchImageCropper(data?.data!!)

        }*/else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val result = CropImage.getActivityResult(data)

           mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  result.uri
           //dataBindingUtil.invalidateAll()
           dataBindingUtil.itemImageview.setImageURI(result.uri)
           //croppedImageUri = result.uri
           //dataBindingUtil.itemImageview.setImageURI(croppedImageUri)
           //imageChanged = true


        }
    }
    private fun addRecord() {


        val itemName: String = dataBindingUtil.itemNameTextinput.text.toString().trim()
        val quantityString = dataBindingUtil.itemQuantityTextinput.text.toString().trim()
        val unit: String = dataBindingUtil.unitTextinput.text.toString().trim()
        val pricePerUnitString = dataBindingUtil.pricePerUnitTextinput.text.toString() .trim()
         val category = dataBindingUtil.itemCategoryTextinput.text.toString().trim()
        val notes: String = dataBindingUtil.notesTextinput.text.toString().trim()

        if (itemName.trim().isEmpty()) {
            Toast.makeText(this, "Please provide name", Toast.LENGTH_SHORT).show()
            return
        }

         val doubleQuantity = if(quantityString.trim().isNotEmpty()) quantityString.toDouble() else 0.0
         val doublePricePerUnit = if(pricePerUnitString.trim().isNotEmpty()) pricePerUnitString.toDouble() else 0.0


        var imageName = ""

        mGroceryListViewModel.selectedGroceryItemEntityNewImageUri?.let{
            imageName = groceryListUniqueId+"_"+itemName+"."+ IMAGE_NAME_SUFFIX
            val imageSavedSucessfully = saveImage(it, imageName)
            if(!imageSavedSucessfully){
                Toast.makeText(this@AddGroceryListItemActivity, "Failed to save image.", Toast.LENGTH_SHORT).show()
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
        val view: View = findViewById(android.R.id.content)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        CoroutineScope(IO).launch {

             mGroceryListViewModel.addGroceryListItem(this@AddGroceryListItemActivity,groceryItemEntity)
             mGroceryListViewModel.updateGroceryListAsNotUploaded(this@AddGroceryListItemActivity,groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)

            // updateGroceryListAsNotUploaded(autoGeneratedUniqueId:String,datetimeStatusUpdated:String,uploadedStatus:Int)

             withContext(Main){

                    setResult(RESULT_OK)
                    finish()
             }
         }


    }
    private fun updateRecord(){
        val itemName: String = dataBindingUtil.itemNameTextinput.text.toString().trim()
        val quantityString = dataBindingUtil.itemQuantityTextinput.text.toString()
        val unit: String = dataBindingUtil.unitTextinput.text.toString().trim()
        val pricePerUnitString = dataBindingUtil.pricePerUnitTextinput.text.toString().trim()
        val category = dataBindingUtil.itemCategoryTextinput.text.toString().trim()
        val notes: String = dataBindingUtil.notesTextinput.text.toString().trim()


        if (itemName.trim().isEmpty()) {
            Toast.makeText(this, "Please provide name", Toast.LENGTH_SHORT).show()
            return
        }
        val doubleQuantity = if(quantityString.trim().isNotEmpty()) quantityString.toDouble() else 0.0
        val doublePricePerUnit = if(pricePerUnitString.trim().isNotEmpty()) pricePerUnitString.toDouble() else 0.0

        // hide soft keyboard
        val view: View = findViewById(android.R.id.content)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        val imageName =  groceryListUniqueId+"_"+itemName+"."+ IMAGE_NAME_SUFFIX

        if(mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri != null && mGroceryListViewModel.selectedGroceryItemEntityNewImageUri !=null ){
            //delete old image and save new image
            if(GroceryUtil.doImageFromPathExists(this, mGroceryListViewModel.selectedGroceryItem!!.imageName)){
                GroceryUtil.deleteImageFile(this, mGroceryListViewModel.selectedGroceryItem!!.imageName)
            }
            val imageSavedSucessfully = saveImage(mGroceryListViewModel.selectedGroceryItemEntityNewImageUri!!, imageName)
            if(!imageSavedSucessfully){
                Toast.makeText(this@AddGroceryListItemActivity, "Failed to save image 1.", Toast.LENGTH_SHORT).show()
                return
            }

        }else if(mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri == null && mGroceryListViewModel.selectedGroceryItemEntityNewImageUri !=null ){
            //save image
            val imageSavedSucessfully = saveImage(mGroceryListViewModel.selectedGroceryItemEntityNewImageUri!!, imageName)
            if(!imageSavedSucessfully){
                Toast.makeText(this@AddGroceryListItemActivity, "Failed to save image 2.", Toast.LENGTH_SHORT).show()
                return
            }
        }else if(mGroceryListViewModel.selectedGroceryItemEntityCurrentImageUri != null && mGroceryListViewModel.selectedGroceryItemEntityNewImageUri ==null ){
            //rename image

            if(GroceryUtil.doImageFromPathExists(this, mGroceryListViewModel.selectedGroceryItem!!.imageName)){
                GroceryUtil.renameImageFile(this, mGroceryListViewModel.selectedGroceryItem!!.imageName, imageName)
            }
        }



        CoroutineScope(IO).launch {

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())
            mGroceryListViewModel.updateGroceryListAsNotUploaded(this@AddGroceryListItemActivity,groceryListUniqueId,currentDatetime, GroceryListEntityValues.NOT_YET_UPLOADED)
            mGroceryListViewModel.updateGroceryItem(this@AddGroceryListItemActivity, itemName, doubleQuantity, unit, doublePricePerUnit, category, notes, imageName, groceryListItemId,currentDatetime)


            withContext(Main){

                val intent = Intent()
                intent.putExtra(GROCERY_LIST_ITEM_INDEX_EXTRA_DATA_TAG, groceryListItemIndex)
                intent.putExtra(GROCERY_LIST_ITEM_ID_EXTRA_DATA_TAG, groceryListItemId)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun lauchImageCropper(uri: Uri){

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            //.setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }
    private fun dispatchTakePictureIntent() {

        // create temporary file
        /*tempPhotoFileForAddingImage = createImageFile()

        if(tempPhotoFileForAddingImage != null){
            *//**
             * com.example.resizeimage.fileprovider, see this in manifest
             *//*
            var photoURI = FileProvider.getUriForFile(
                this,
                "com.example.allhome.fileprovider",
                tempPhotoFileForAddingImage!!
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }*/

    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!

        if(!storageDir.exists()){
            storageDir.mkdir()
        }

        return File.createTempFile(
            IMAGE_TEMP_NAME, /* prefix */
            ".${IMAGE_NAME_SUFFIX}", /* suffix */
            storageDir /* directory */
        )
    }
    private fun saveImage(imageUri: Uri, imageName: String):Boolean{
        val imageBitmap = uriToBitmap(imageUri, this)

        val imageWidth = imageBitmap.width
        val imageHeight = imageBitmap.height
        val targetMaxWidthOrHeight = 800

        val imageWidthAndHeight = ImageUtil.getProportionImageSize(targetMaxWidthOrHeight, imageWidth,imageHeight)
        Log.e("width and height", "$imageWidth  $imageHeight")
        Log.e("proportion","$imageWidthAndHeight")

        val resizedImageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageWidthAndHeight["width"]!!, imageWidthAndHeight["height"]!!, false)
        val storageDir: File = getExternalFilesDir(GroceryUtil.FINAL_IMAGES_LOCATION)!!

        Log.e("Test","Test data")
//        if(true){
//            return false
//        }
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
            return  MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        } else {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_grocery_item, menu)
        if(action == ADD_NEW_RECORD_ACTION){
            menu?.findItem(R.id.add_item)?.isVisible = true

        }else if(action == UPDATE_RECORD_ACTION){
            menu?.findItem(R.id.update_item)?.isVisible = true
        }
        return super.onCreateOptionsMenu(menu)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.add_grocery_item, menu)
//        if(action == ADD_NEW_RECORD_ACTION){
//            menu?.findItem(R.id.add_item)?.isVisible = true
//
//        }else if(action == UPDATE_RECORD_ACTION){
//            menu?.findItem(R.id.update_item)?.isVisible = true
//        }
//        return super.onCreateOptionsMenu(menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.add_item -> {
                addRecord()
            }
            R.id.update_item -> {


                updateRecord()
            }
        }

        return super.onOptionsItemSelected(item)
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
                    searchJob = launch(IO) {
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
            var textView:TextView? = null
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
                    searchJob = launch(IO) {
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
            var textView:TextView? = null
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
                    searchJob = launch(IO) {
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
            var textView:TextView? = null
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

