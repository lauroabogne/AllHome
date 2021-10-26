package com.example.allhome.storage

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageEntityValues
import com.example.allhome.databinding.ActivityCreateStorageBinding
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.storage.viewmodel.StorageViewModel
import com.example.allhome.utils.ImageUtil
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

class CreateStorageActivity : AppCompatActivity() {

    lateinit var mStorageViewModel: StorageViewModel
    lateinit var mActivityCreateStorageBinding: ActivityCreateStorageBinding
    var mAction = ADD_NEW_RECORD_ACTION
    var mTempPhotoFileForAddingImage: File? = null
    companion object{
        val STORAGE_UNIQUE_ID_TAG = "STORAGE_UNIQUE_ID_TAG"
        val ACTION_TAG = "ACTION_TAG"
        val ADD_NEW_RECORD_ACTION = 1
        val UPDATE_RECORD_ACTION = 2
        val REQUEST_PICK_IMAGE = 4

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAction = intent.getIntExtra(CreateStorageActivity.ACTION_TAG, ADD_NEW_RECORD_ACTION)

        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        if(mAction == UPDATE_RECORD_ACTION){

            val storageUniqueId: String = intent.getStringExtra(STORAGE_UNIQUE_ID_TAG)!!
            mStorageViewModel.coroutineScope.launch {
                val storageEntity:StorageEntity =  mStorageViewModel.getStorage(this@CreateStorageActivity,storageUniqueId)
                val imageName = storageEntity.imageName
                val imageURI = StorageUtil.getImageUriFromPath(this@CreateStorageActivity, ImageUtil.STORAGE_IMAGES_FINAL_LOCATION, imageName)
                mStorageViewModel.storagePreviousImageUri = imageURI

            }

        }
        mActivityCreateStorageBinding = DataBindingUtil.setContentView<ActivityCreateStorageBinding>(this,R.layout.activity_create_storage).apply {
            lifecycleOwner = this@CreateStorageActivity
            storageViewModel = mStorageViewModel
        }

        mActivityCreateStorageBinding.storageItemAddImageBtn.setOnClickListener{

            ImageUtil.deleteAllTemporaryImages(this)

            showIntentChooser()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if(mAction == UPDATE_RECORD_ACTION){
            menu?.findItem(R.id.saveStorageMenu)?.isVisible = false

        }else{
            menu?.findItem(R.id.updateStorageMenu)?.isVisible = false
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_storage_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.create_storage_menu, menu)
//        return true
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == StorageAddItemActivity.REQUEST_PICK_IMAGE){
            data?.data?.let{
                lauchImageCropper(it)
            }

            mTempPhotoFileForAddingImage?.let{
                val fileUri = Uri.fromFile(mTempPhotoFileForAddingImage) as Uri
                lauchImageCropper(fileUri)
            }
        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            val result = CropImage.getActivityResult(data)
            mStorageViewModel.storageNewImageUri = result.uri
            mActivityCreateStorageBinding.itemImageView.setImageURI(result.uri)



        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.saveStorageMenu -> {

                saveStorage()

            }
            R.id.updateStorageMenu -> {
                updateStorage()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun saveStorage(){

        var storageUniqueId = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val storageName = mActivityCreateStorageBinding.storageNameTextinput.text.toString()
        val storageDescription = mActivityCreateStorageBinding.storageDescriptionTextinput.text.toString()

        val imageName =storageUniqueId+"_"+storageName+"."+ImageUtil.IMAGE_NAME_SUFFIX

        val storageEntity = StorageEntity(
            uniqueId = storageUniqueId,
            name = storageName,
            description = storageDescription,
            imageName = imageName,
            itemStatus = StorageEntityValues.NOT_DELETED_STATUS,
            created = currentDatetime,
            modified = currentDatetime
        )

        mStorageViewModel.coroutineScope.launch {
            val id = mStorageViewModel.addStorage(this@CreateStorageActivity,storageEntity)

            mStorageViewModel.storageNewImageUri?.let {
                saveImage(it,imageName)
            }



            withContext(Main){

                if(id >= 1){
                    Toast.makeText(this@CreateStorageActivity,"Storage save successfully",Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra(StorageFragment.STORAGE_ENTITY_TAG,storageEntity)
                    setResult(RESULT_OK,resultIntent)
                    finish()

                }else{
                    Toast.makeText(this@CreateStorageActivity,"Failed to save storage",Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
    fun updateStorage(){

        var storageUniqueId = mStorageViewModel.storageEntity!!.uniqueId
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val storageName = mActivityCreateStorageBinding.storageNameTextinput.text.toString()
        val storageDescription = mActivityCreateStorageBinding.storageDescriptionTextinput.text.toString()

        var imageName = mStorageViewModel.storageNewImageUri?.let {
                            UUID.randomUUID().toString()+"_"+storageName+"."+ImageUtil.IMAGE_NAME_SUFFIX
                        }?:run {
                            mStorageViewModel.storageEntity!!.imageName
                        }


        mStorageViewModel.coroutineScope.launch {

           val affectedRowCount =  mStorageViewModel.updateStorage(this@CreateStorageActivity,storageUniqueId,storageName,storageDescription,imageName,currentDatetime)

            if(affectedRowCount > 0){
                    mStorageViewModel.storageNewImageUri?.let {
                        mStorageViewModel.storagePreviousImageUri?.let {
                            StorageUtil.deleteImageFile(it)
                        }
                        saveImage(it,imageName)
                    }
            }



            withContext(Main){

                if(affectedRowCount >  0){
                    Toast.makeText(this@CreateStorageActivity,"Storage updated successfully",Toast.LENGTH_SHORT).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra(StorageFragment.STORAGE_ENTITY_TAG,mStorageViewModel.storageEntity)
                    setResult(RESULT_OK,resultIntent)
                    finish()

                    return@withContext
                }


            }
        }

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
        startActivityForResult(chooserIntent, StorageAddItemActivity.REQUEST_PICK_IMAGE)



    }
    private fun saveImage(imageUri: Uri, imageName: String):Boolean{
        val imageBitmap = uriToBitmap(imageUri, this)
        val resizedImageBitmap = ImageUtil.resizeImage(imageBitmap,1000)
        val storageDir: File = getExternalFilesDir(ImageUtil.STORAGE_IMAGES_FINAL_LOCATION)!!
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
        val storageDir: File = getExternalFilesDir(ImageUtil.TEMPORARY_IMAGES_LOCATION)!!

        if(!storageDir.exists()){
            storageDir.mkdir()
        }

        return File.createTempFile(
            ImageUtil.IMAGE_TEMP_NAME, /* prefix */
            ".${ImageUtil.IMAGE_NAME_SUFFIX}", /* suffix */
            storageDir /* directory */
        )
    }
    private fun lauchImageCropper(uri: Uri){

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }
}