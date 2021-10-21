package com.example.allhome.recipes

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentAddRecipeInformationBinding
import com.example.allhome.databinding.HourAndTimeInputBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.recipes.viewmodel.AddRecipeInformationFragmentViewModel
import com.example.allhome.storage.StorageAddItemActivity
import com.example.allhome.storage.StorageUtil
import com.example.allhome.utils.ImageUtil
import com.example.allhome.utils.MinMaxInputFilter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddRecipeInformationFragment : Fragment() {


    private lateinit var mAddRecipeInformationFragmentViewModel: AddRecipeInformationFragmentViewModel

    lateinit var mDataBinding:FragmentAddRecipeInformationBinding
    var mRecipeEntity:RecipeEntity? = null
    var mAction = ADD_ACTION
    var mTempPhotoFileForAddingImage: File? = null

    companion object{
        const val ADD_ACTION = 0
        const val ADD_FROM_BROWSER_ACTION =1
        const val EDIT_ACTION = 2
        val REQUEST_PICK_IMAGE = 3
         val DIFICULTY_OPTIONS = arrayOf(
            "",
            "Easy",
            "Medium",
            "Hard",
        )

        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"

        @JvmStatic fun newInstanceForEditing(recipeEntity: RecipeEntity) =
            AddRecipeInformationFragment().apply {
                mAction = EDIT_ACTION
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)
                }

            }
        @JvmStatic fun newInstanceForAddingRecipeFromBrowser(recipeEntity: RecipeEntity) =
            AddRecipeInformationFragment().apply {
                mAction = ADD_FROM_BROWSER_ACTION
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)



                }

            }
        @JvmStatic fun newInstanceForAdd() =
            AddRecipeInformationFragment().apply {

            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAddRecipeInformationFragmentViewModel = ViewModelProvider(this).get(AddRecipeInformationFragmentViewModel::class.java)

        arguments?.let {
            if(mAction == EDIT_ACTION || mAction == ADD_FROM_BROWSER_ACTION){
                mRecipeEntity = it.getParcelable(RECIPE_INTENT_TAG)!!
                mRecipeEntity?.let{
                    mAddRecipeInformationFragmentViewModel.mTempCookTimeHour = it.cookingHours
                    mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes = it.cookingMinutes
                    mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour = it.preparationHour
                    mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes = it.preparationMinutes

                }

            }

        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_information, container, false)
        mDataBinding.recipeEntity = mRecipeEntity

        mDataBinding.difficultyTextInputEditText.setOnClickListener {
           showDifficultyPopup()
        }
        mDataBinding.preparationTextInputEditText.setOnClickListener {
            showPrepationTimePopup()
        }
        mDataBinding.cookTimeTextInputEditText.setOnClickListener {
            showCookingTimePopup()
        }
        mDataBinding.recipeAddImageBtn.setOnClickListener{

            ImageUtil.deleteAllTemporaryImages(requireContext())
            showIntentChooser()
        }

        if(mAction == ADD_FROM_BROWSER_ACTION ){

            mRecipeEntity?.imageName?.let {


                if(it.trim().length <=0){
                    return@let
                }

                val imageUri = ImageUtil.getImageUriFromPath(requireContext(),ImageUtil.TEMPORARY_IMAGES_LOCATION,"${ImageUtil.IMAGE_TEMP_NAME}.${ImageUtil.IMAGE_NAME_SUFFIX}")

                imageUri?.let{
                    mAddRecipeInformationFragmentViewModel.newImageUri = it
                    mDataBinding.itemImageView.setImageURI(it)
                }
            }


        }

        return mDataBinding.root
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
            mAddRecipeInformationFragmentViewModel.newImageUri = result.uri
            mDataBinding.itemImageView.setImageURI(result.uri)



        }
    }
    private fun lauchImageCropper(uri: Uri){

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(requireContext(),this)
    }
    fun showDifficultyPopup(){

        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Select difficulty")
            .setSingleChoiceItems(DIFICULTY_OPTIONS, 0, null)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            val checkedPosition = alertDialog.listView.checkedItemPosition
            mDataBinding.difficultyTextInputEditText.setText(DIFICULTY_OPTIONS[checkedPosition])
            alertDialog.dismiss()
        }
    }
    fun showPrepationTimePopup(){

        val hourAndTimeInputBinding:HourAndTimeInputBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.hour_and_time_input, null, false)

        hourAndTimeInputBinding.hourEditext.filters = arrayOf(MinMaxInputFilter( 1 , 168 ))
        hourAndTimeInputBinding.minutesEditext.filters = arrayOf(MinMaxInputFilter( 1 , 59 ))

        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Preperation time")
            .setView(hourAndTimeInputBinding.root)
            .setPositiveButton("Done",null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hour = hourAndTimeInputBinding.hourEditext.text.toString()
            val minutes = hourAndTimeInputBinding.minutesEditext.text.toString()

            val hourDisplay  = if(hour.isEmpty()) "" else "${hour} hrs"
            val minutesDisplay  = if(minutes.isEmpty()) "" else "${minutes} min"

            if(hourDisplay.isNotEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${hourDisplay}  ${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour = hour.toInt()
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isNotEmpty() && minutesDisplay.isEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${hourDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour = hour.toInt()
            }

            alertDialog.dismiss()
        }

    }
    fun showCookingTimePopup(){
        val hourAndTimeInputBinding:HourAndTimeInputBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.hour_and_time_input, null, false)
        hourAndTimeInputBinding.hourEditext.filters = arrayOf(MinMaxInputFilter( 1 , 168 ))
        hourAndTimeInputBinding.minutesEditext.filters = arrayOf(MinMaxInputFilter( 1 , 59 ))
        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Cooking time")
            .setView(hourAndTimeInputBinding.root)
            .setPositiveButton("Done", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hour = hourAndTimeInputBinding.hourEditext.text.toString()
            val minutes = hourAndTimeInputBinding.minutesEditext.text.toString()

            val hourDisplay  = if(hour.isEmpty()) "" else "${hour} hrs"
            val minutesDisplay  = if(minutes.isEmpty()) "" else "${minutes} min"

            if(hourDisplay.isNotEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${hourDisplay}  ${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempCookTimeHour = hour.toInt()
                mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isNotEmpty() && minutesDisplay.isEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${hourDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempCookTimeHour = hour.toInt()
            }
            alertDialog.dismiss()
        }
    }
    fun getRecipeInformation():RecipeEntity?{

        val name = mDataBinding.recipeNameTextInputEditText.text.toString()
        val serving = mDataBinding.servingTextInputEditText.text.toString().ifEmpty { "0" }
        val difficulty = mDataBinding.difficultyTextInputEditText.text.toString()
        val preperationHour = mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour
        val preperationMinute = mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes
        val cookTimeHour = mAddRecipeInformationFragmentViewModel.mTempCookTimeHour
        val cookTimeMinute = mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes
        val category = mDataBinding.categoryTimeTextInputEditText.text.toString()
        val estimatedCost = mDataBinding.estimatedCostTextInputEditText.text.toString().ifEmpty { "0" }

        val description = mDataBinding.descriptionTextInputEditText.text.toString()


        if(name.isEmpty()){
            showErroPopup("Recipe name must not empty.")
            return null
        }


        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        if(mAction == EDIT_ACTION){
            val recipeInformation  = RecipeEntity(
                uniqueId = mRecipeEntity!!.uniqueId,
                name= name,
                serving = serving.toInt(),
                difficulty = generateDifficultyInteger(difficulty),
                preparationHour=preperationHour,
                preparationMinutes = preperationMinute,
                cookingHours =cookTimeHour,
                cookingMinutes =cookTimeMinute,
                category=category,
                estimatedCost = estimatedCost.toDouble(),
                description = description,
                imageName = "",
                "",
                status = RecipeEntity.NOT_DELETED_STATUS,
                uploaded = RecipeEntity.NOT_UPLOADED,
                created = mRecipeEntity!!.created,
                modified = currentDatetime
            )

            return recipeInformation
        }else{

            var itemUniqueID = UUID.randomUUID().toString()
            val recipeInformation  = RecipeEntity(
                uniqueId = itemUniqueID,
                name= name,
                serving = serving.toInt(),
                difficulty = generateDifficultyInteger(difficulty),
                preparationHour=preperationHour,
                preparationMinutes = preperationMinute,
                cookingHours =cookTimeHour,
                cookingMinutes =cookTimeMinute,
                category=category,
                estimatedCost = estimatedCost.toDouble(),
                description = description,
                imageName = "",
                "",
                status = RecipeEntity.NOT_DELETED_STATUS,
                uploaded = RecipeEntity.NOT_UPLOADED,
                created = currentDatetime,
                modified = currentDatetime
            )

            return recipeInformation
        }


    }
    fun getRecipeImageURI():Uri?{
        return mAddRecipeInformationFragmentViewModel.newImageUri
    }
    fun showErroPopup(message:String){
        var dialog = CustomMessageDialogFragment(null,message,true)
        dialog.show(requireActivity().supportFragmentManager,"CustomMessageDialogFragment")
    }
    fun generateDifficultyInteger(difficultyString:String):Int{

        if(difficultyString.length <=0){
            return RecipeEntity.DIFFICULTY_NONE
        }else if(difficultyString.equals(DIFICULTY_OPTIONS[1])){
            return RecipeEntity.DIFFICULTY_EASY
        }else if(difficultyString.equals(DIFICULTY_OPTIONS[2])){
            return RecipeEntity.DIFFICULTY_MEDIUM
        }else if(difficultyString.equals(DIFICULTY_OPTIONS[3])){
            return RecipeEntity.DIFFICULTY_HARD
        }else{
            return RecipeEntity.DIFFICULTY_NONE
        }


    }

    private fun showIntentChooser(){
        // Determine Uri of camera image to save.

        // create temporary file
        mTempPhotoFileForAddingImage = createImageFile()
        var photoURI = FileProvider.getUriForFile(requireContext(), "com.example.allhome.fileprovider", mTempPhotoFileForAddingImage!!)

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
        startActivityForResult(chooserIntent,REQUEST_PICK_IMAGE)



    }

    @Throws(IOException::class)
    private fun createImageFile(): File {

        val storageDir: File = requireContext().getExternalFilesDir(ImageUtil.TEMPORARY_IMAGES_LOCATION)!!

        if(!storageDir.exists()){
            storageDir.mkdir()
        }

        return File.createTempFile(
            ImageUtil.IMAGE_TEMP_NAME, /* prefix */
            ".${ImageUtil.IMAGE_NAME_SUFFIX}", /* suffix */
            storageDir /* directory */
        )
    }


}

