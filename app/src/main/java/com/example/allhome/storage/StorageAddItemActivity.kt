package com.example.allhome.storage

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.databinding.ActivityStorageAddItemBinding
import com.example.allhome.databinding.StorageExpirationLayoutBinding
import com.example.allhome.storage.viewmodel.StorageAddItemViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class StorageAddItemActivity : AppCompatActivity() {
    internal lateinit var mStorageAddItemViewModel: StorageAddItemViewModel
    private lateinit var mActivityPantryAddItemBinding: ActivityStorageAddItemBinding
    var mAction = ADD_NEW_RECORD_ACTION
    var mStorageName:String? = null
    var mStorageItemUniqueId:String? = null
    var mStorageEntity:StorageEntity? = null
    var mStorageItemName:String? = null
    var mTempPhotoFileForAddingImage: File? = null

    companion object {
        val STORAGE_ITEM_UNIQUE_ID_TAG = "STORAGE_ITEM_UNIQUE_ID_TAG"
        val STORAGE_ITEM_NAME_TAG = "PANTRY_ITEM_NAME_TAG"
        val STORAGE_NAME_TAG = "STORAGE_NAME_TAG"
        val STORAGE_TAG = "STORAGE_TAG"
        val ACTION_TAG = "ACTION_TAG"
        val ADD_NEW_RECORD_ACTION = 1
        val UPDATE_RECORD_ACTION = 2
        val REQUEST_PICK_IMAGE = 4

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Add Storage Item"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        mStorageAddItemViewModel = ViewModelProvider(this).get(StorageAddItemViewModel::class.java)
        intent.getParcelableExtra<StorageEntity>(STORAGE_TAG)?.let{
            mStorageEntity = it
        }?:run {
            Toast.makeText(this@StorageAddItemActivity, "Storage require", Toast.LENGTH_SHORT).show()
            finish()
        }
        intent.getStringExtra(STORAGE_NAME_TAG)?.let {
            mStorageName = it
            mStorageAddItemViewModel.storageName = mStorageName

        }?:run {
            Toast.makeText(this@StorageAddItemActivity, "Storage require", Toast.LENGTH_SHORT).show()
            finish()
        }

        intent.getIntExtra(ACTION_TAG, ADD_NEW_RECORD_ACTION).let {

            mAction = it
            if(mAction == UPDATE_RECORD_ACTION){

                mStorageItemUniqueId = intent.getStringExtra(STORAGE_ITEM_UNIQUE_ID_TAG)
                mStorageItemName = intent.getStringExtra(STORAGE_ITEM_NAME_TAG)


                mStorageAddItemViewModel.coroutineScope.launch {
                    mStorageAddItemViewModel.setStorageItemAndExpirations(this@StorageAddItemActivity, mStorageItemUniqueId!!, mStorageItemName!!)

                    val imageName = mStorageAddItemViewModel.storageItemEntity?.imageName
                    val imageURI = StorageUtil.getStorageItemImageUriFromPath(this@StorageAddItemActivity, imageName!!)
                    mStorageAddItemViewModel.previousImageUri = imageURI


                }
            }
        }

        mActivityPantryAddItemBinding =  DataBindingUtil.setContentView(this, R.layout.activity_storage_add_item)
        mActivityPantryAddItemBinding.lifecycleOwner = this
        mActivityPantryAddItemBinding.pantryAddItemViewModel = mStorageAddItemViewModel


        val pantryItemsExpirationRecyclerViewAdapter = PantryItemRecyclerViewAdapter(this)
        if(mAction == UPDATE_RECORD_ACTION){
            pantryItemsExpirationRecyclerViewAdapter.storageItemExpirationsEntity = mStorageAddItemViewModel.storageItemExpirationsEntity
        }
        mActivityPantryAddItemBinding.pantryItemExpirationRecyclerview.adapter = pantryItemsExpirationRecyclerViewAdapter

        mActivityPantryAddItemBinding.pantryAddExpirationBtn.setOnClickListener {
            showCalendar()
        }
        mActivityPantryAddItemBinding.storageItemAddImageBtn.setOnClickListener {
            showIntentChooser()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pantry_add_item_menu, menu)
        if(mAction == ADD_NEW_RECORD_ACTION){

            menu?.findItem(R.id.update_pantry_item_menu)?.setVisible(false)
            menu?.findItem(R.id.save_pantry_item_menu)?.setVisible(true)

        }else{
            menu?.findItem(R.id.update_pantry_item_menu)?.setVisible(true)
            menu?.findItem(R.id.save_pantry_item_menu)?.setVisible(false)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
            R.id.save_pantry_item_menu -> {
                saveNewRecord()
            }
            R.id.update_pantry_item_menu -> {
                updateRecord()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_PICK_IMAGE){
            data?.data?.let{
                lauchImageCropper(it)
            }

            mTempPhotoFileForAddingImage?.let{
                val fileUri = Uri.fromFile(mTempPhotoFileForAddingImage) as Uri
                lauchImageCropper(fileUri)
            }
        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val result = CropImage.getActivityResult(data)
            mStorageAddItemViewModel.newImageUri = result.uri
            mActivityPantryAddItemBinding.itemImageView.setImageURI(result.uri)
            //itemImageView
            //mGroceryListViewModel.selectedGroceryItemEntityNewImageUri =  result.uri
            //dataBindingUtil.itemImageview.setImageURI(result.uri)


        }
    }
    private fun lauchImageCropper(uri: Uri){

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }
    fun saveNewRecord(){
        val storageItem = mActivityPantryAddItemBinding.pantryItemTextinput.text.toString().trim()
        val storageItemQuantity = mActivityPantryAddItemBinding.pantryItemQuantityTextinput.text.toString().trim()
        val storageItemUnit = mActivityPantryAddItemBinding.pantryItemUnitTextinput.text.toString().trim()
        val storageItemStockWeightCheckedId = mActivityPantryAddItemBinding.pantryItemStockWeightRadiogroup.checkedRadioButtonId
        val storageItemNotes = mActivityPantryAddItemBinding.pantryItemNotesTextinput.text.toString().trim()
        val storageCategory = mActivityPantryAddItemBinding.categoryItemTextinput.text.toString().trim()

        val pantryItemStockWeightIntValue = StorageUtil.stockWeightIntegerIdToIntegerValue(storageItemStockWeightCheckedId)
        val quantityIntValue = if (storageItemQuantity.length <=0)  StorageItemEntityValues.NO_QUANTITY_INPUT.toDouble()  else storageItemQuantity.toDouble()

        if(storageItem.length <=0){
            Toast.makeText(this, "Please provide pantry item name", Toast.LENGTH_SHORT).show()
            return
        }


        var itemUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val imageName =itemUniqueID+"_"+storageItem+"."+StorageUtil.IMAGE_NAME_SUFFIX




        val pantryItemEntity = StorageItemEntity(
            uniqueId = itemUniqueID,
            storageUniqueId = mStorageEntity!!.uniqueId,
            name = storageItem,
            quantity = quantityIntValue,
            unit = storageItemUnit,
            stockWeight = pantryItemStockWeightIntValue,
            category = storageCategory,
            storage = mStorageName!!,
            notes = storageItemNotes,
            imageName = imageName,
            itemStatus = StorageItemEntityValues.NOT_DELETED_STATUS,
            created = currentDatetime,
            modified = currentDatetime
        )



        mStorageAddItemViewModel.coroutineScope.launch {
            var savedSuccessfully = true
            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageAddItemActivity);

            try{
                mStorageAddItemViewModel.newImageUri?.let {
                    saveImage(it, imageName)
                }

                allHomeDatabase.withTransaction {
                    val storageItemId = mStorageAddItemViewModel.saveStorageItemEntity(this@StorageAddItemActivity, pantryItemEntity)

                    if(storageItemId <= 0){
                        throw Exception("Failed to save")
                    }
                    for(storageItemExpirationEntity in mStorageAddItemViewModel.storageItemExpirationsEntity){
                        val storageItemExpirationUniqueID = UUID.randomUUID().toString()

                        storageItemExpirationEntity.uniqueId = storageItemExpirationUniqueID
                        storageItemExpirationEntity.storageItemUniqueId = itemUniqueID
                        storageItemExpirationEntity.created = currentDatetime
                        storageItemExpirationEntity.storage = mStorageName!!
                        storageItemExpirationEntity.storageItemName = pantryItemEntity.name
                        storageItemExpirationEntity.created = currentDatetime
                        storageItemExpirationEntity.modified = currentDatetime

                        val storageItemExpirationId = mStorageAddItemViewModel.saveStorageItemExpirationEntity(this@StorageAddItemActivity, storageItemExpirationEntity)

                        if(storageItemExpirationId <= 0){
                            throw Exception("Failed to save")
                        }
                    }

                }
            }catch (ex: java.lang.Exception){
                savedSuccessfully = false
            }

            withContext(Main){
                if(savedSuccessfully){

                    val resultIntent = Intent()
                    resultIntent.putExtra(StorageActivity.STORAGE_ITEM_UNIQUE_ID_TAG,itemUniqueID)
                    setResult(RESULT_OK,resultIntent)
                    finish()

                }else{
                    Toast.makeText(this@StorageAddItemActivity, "Failed to save pantry item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun updateRecord(){
        val storageItem = mActivityPantryAddItemBinding.pantryItemTextinput.text.toString().trim()
        val storageItemQuantity = mActivityPantryAddItemBinding.pantryItemQuantityTextinput.text.toString().trim()
        val storageItemUnit = mActivityPantryAddItemBinding.pantryItemUnitTextinput.text.toString().trim()
        val storageItemStockWeightCheckedId = mActivityPantryAddItemBinding.pantryItemStockWeightRadiogroup.checkedRadioButtonId
        val storageItemNotes = mActivityPantryAddItemBinding.pantryItemNotesTextinput.text.toString().trim()
        val storageItemCategory = mActivityPantryAddItemBinding.categoryItemTextinput.text.toString().trim()

        val storageItemStockWeightIntValue = StorageUtil.stockWeightIntegerIdToIntegerValue(storageItemStockWeightCheckedId)
        val quantityIntValue = if (storageItemQuantity.length <=0)  StorageItemEntityValues.NO_QUANTITY_INPUT.toDouble()  else storageItemQuantity.toDouble()

        if(storageItem.length <=0){
            Toast.makeText(this, "Please provide pantry item name", Toast.LENGTH_SHORT).show()
            return
        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        var  imageName:String = ""

        mStorageAddItemViewModel.coroutineScope.launch {
            var savedSuccessfully = true
            val allHomeDatabase = AllHomeDatabase.getDatabase(this@StorageAddItemActivity);

            mStorageAddItemViewModel.newImageUri?.let {
                mStorageAddItemViewModel.previousImageUri?.let {
                    StorageUtil.deleteImageFile(it)
                }
                var itemUniqueID = UUID.randomUUID().toString()
                imageName = itemUniqueID+"_"+storageItem+"."+StorageUtil.IMAGE_NAME_SUFFIX
                saveImage(mStorageAddItemViewModel.newImageUri!!, imageName)

            }?:run{

                imageName = mStorageAddItemViewModel.storageItemEntity!!.imageName
            }

            try{
                allHomeDatabase.withTransaction {
                    val affectedRowCount = mStorageAddItemViewModel.updateStorageItemEntity(
                        this@StorageAddItemActivity, storageItem, quantityIntValue, storageItemUnit, storageItemCategory, storageItemStockWeightIntValue,
                        mStorageName!!, storageItemNotes, imageName, currentDatetime, mStorageItemUniqueId!!
                    )

                    if(affectedRowCount <=0){
                        throw Exception("Failed to update record")
                    }

                    for(storageItemExpirationEntity in mStorageAddItemViewModel.storageItemExpirationsEntity){
                        val storageItemExpirationUniqueID = UUID.randomUUID().toString()

                        storageItemExpirationEntity.uniqueId = storageItemExpirationUniqueID
                        storageItemExpirationEntity.storageItemUniqueId = mStorageItemUniqueId!!
                        storageItemExpirationEntity.created = currentDatetime
                        storageItemExpirationEntity.storage = mStorageName!!
                        storageItemExpirationEntity.storageItemName = storageItem

                        val storageItemExpirationId = mStorageAddItemViewModel.saveStorageItemExpirationEntity(this@StorageAddItemActivity, storageItemExpirationEntity)

                        if(storageItemExpirationId <= 0){
                            throw Exception("Failed to update expiration")
                        }
                    }

                }
            }catch (ex: java.lang.Exception){
                Log.e("error", ex.message.toString())
                savedSuccessfully = false
            }

            withContext(Main){
                if(savedSuccessfully){
                    Toast.makeText(this@StorageAddItemActivity, "Item updated", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra(StorageActivity.STORAGE_ITEM_UNIQUE_ID_TAG,mStorageItemUniqueId)
                    setResult(RESULT_OK,resultIntent)
                    finish()

                }else{
                    Toast.makeText(this@StorageAddItemActivity, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun imageName(storageItemName: String):String?{

        var itemUniqueID = UUID.randomUUID().toString()
        return itemUniqueID+"_"+storageItemName+"."+StorageUtil.IMAGE_NAME_SUFFIX

    }
    fun showIntentChooser(){
        // Determine Uri of camera image to save.

        // create temporary file
        mTempPhotoFileForAddingImage = createImageFile()
        var photoURI = FileProvider.getUriForFile(this, "com.example.allhome.fileprovider", mTempPhotoFileForAddingImage!!)

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
    private fun saveImage(imageUri: Uri, imageName: String):Boolean{
        val imageBitmap = uriToBitmap(imageUri!!, this)
        val resizedImageBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 500, false)
        val storageDir: File = getExternalFilesDir(StorageUtil.STORAGE_ITEM_IMAGES_FINAL_LOCATION)!!
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
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File = getExternalFilesDir(StorageUtil.TEMPORARY_IMAGES_LOCATION)!!

        if(!storageDir.exists()){
            storageDir.mkdir()
        }

        return File.createTempFile(
            StorageUtil.IMAGE_TEMP_NAME, /* prefix */
            ".${StorageUtil.IMAGE_NAME_SUFFIX}", /* suffix */
            storageDir /* directory */
        )
    }
    /**
     * expirationDateIndex is equal to -1 for adding new expiration
     */
    fun showCalendar(expirationDateIndex: Int = -1){

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val stringDate= SimpleDateFormat("yyyy-MM-dd").format(date)

            var uniqueID = UUID.randomUUID().toString()

            mStorageAddItemViewModel.coroutineScope.launch {
                val pantryItemExpirationEntity = StorageItemExpirationEntity(
                    uniqueId = uniqueID,
                    storageItemName = "",
                    storageItemUniqueId = "",
                    expirationDate = stringDate,
                    deleted = StorageItemEntityValues.NOT_DELETED_STATUS,
                    created = "",
                    modified = "",
                    storage = mStorageName!!
                )

                if(expirationDateIndex <= -1){
                    mStorageAddItemViewModel.storageItemExpirationsEntity.add(pantryItemExpirationEntity)
                }else{
                    mStorageAddItemViewModel.storageItemExpirationsEntity.set(expirationDateIndex, pantryItemExpirationEntity)
                }



                withContext(Main){
                    val pantryItemRecyclerViewAdapter = mActivityPantryAddItemBinding.pantryItemExpirationRecyclerview.adapter as PantryItemRecyclerViewAdapter
                    pantryItemRecyclerViewAdapter.storageItemExpirationsEntity = mStorageAddItemViewModel.storageItemExpirationsEntity
                    pantryItemRecyclerViewAdapter.notifyDataSetChanged()
                }
                Log.e("Expiration count", mStorageAddItemViewModel.storageItemExpirationsEntity.size.toString())

            }

            Log.e("expiration", stringDate)
        }

        val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        datePickerDialog.show()

    }
}

/**
 *
 */
class PantryItemRecyclerViewAdapter(val storageAddItemActivity: StorageAddItemActivity): RecyclerView.Adapter<PantryItemRecyclerViewAdapter.ItemViewHolder>() {

    var storageItemExpirationsEntity:List<StorageItemExpirationEntity> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val pantryExpirationLayoutBinding = StorageExpirationLayoutBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(pantryExpirationLayoutBinding)
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationsEntity = storageItemExpirationsEntity[position]
        holder.pantryExpirationLayoutBinding.pantryItemExpirationEntity = pantryItemExpirationsEntity
        holder.pantryExpirationLayoutBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {

        return storageItemExpirationsEntity.size

    }

    inner class  ItemViewHolder(var pantryExpirationLayoutBinding: StorageExpirationLayoutBinding): RecyclerView.ViewHolder(pantryExpirationLayoutBinding.root),View.OnClickListener{

        init{
            pantryExpirationLayoutBinding.deleteExpirationDateBtn.setOnClickListener(this)
            pantryExpirationLayoutBinding.editExpirationDateBtn.setOnClickListener(this)

        }
        override fun onClick(view: View?) {
            when(view!!.id){
                R.id.deleteExpirationDateBtn -> {
                    storageAddItemActivity.mStorageAddItemViewModel.storageItemExpirationsEntity.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                }
                R.id.editExpirationDateBtn -> {

                    storageAddItemActivity.showCalendar(adapterPosition)

                }
            }
        }

    }
}