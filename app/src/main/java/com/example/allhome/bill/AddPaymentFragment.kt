package com.example.allhome.bill

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.data.entities.BillPaymentEntity
import com.example.allhome.databinding.FragmentAddPaymentBinding
import com.example.allhome.storage.StorageAddItemActivity
import com.example.allhome.utils.ImageUtil
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM2 = "param2"


class AddPaymentFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    var mSelectedPaymentDateCalendar = Calendar.getInstance()
    private lateinit var mBillEntityWithTotalPayment:BillEntityWithTotalPayment
    private var mBillPaymentEntity:BillPaymentEntity? = null
    private var mAction:Int = ADD_ACTION
    lateinit var mFragmentAddPaymentBinding:FragmentAddPaymentBinding
    lateinit var mBillViewModel:BillViewModel
    var mTotalPayment:Double = 0.0
    var mTempPhotoFileForAddingImage: File? = null

    var mPreviousImageUri: Uri? = null
    var mNewImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        arguments?.let {
            mBillEntityWithTotalPayment = it.getParcelable(ARG_BILL_ENTITY)!!
            mBillPaymentEntity = it.getParcelable<BillPaymentEntity>(ARG_PAYMENT_ENTITY)
            mAction = it.getInt(ARG_ACTION, ADD_ACTION)


        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentAddPaymentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_payment,null,false)
        mFragmentAddPaymentBinding.billEntityWithTotalPayment = mBillEntityWithTotalPayment
        mFragmentAddPaymentBinding.billPaymentEntity = mBillPaymentEntity
        mFragmentAddPaymentBinding.action = mAction
        mFragmentAddPaymentBinding.paymentDateImageView.setOnClickListener(paymentDateOnClickListener)
        mFragmentAddPaymentBinding.billPaymentAddImageBtn.setOnClickListener(addPaymentImageOnClickListener)

        initUI()
        return mFragmentAddPaymentBinding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_bill_payment_menu, menu)

        Log.e("ACTION","${mAction}")

        if(mAction == ADD_ACTION){
            menu.findItem(R.id.updateBilPaymentMenu).setVisible(false)
            menu.findItem(R.id.saveBillPaymentMenu).setVisible(true)
        }else{
            menu.findItem(R.id.updateBilPaymentMenu).setVisible(true)
            menu.findItem(R.id.saveBillPaymentMenu).setVisible(false)
        }

        super.onCreateOptionsMenu(menu, inflater)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.saveBillPaymentMenu->{
                savePayment()
            }
            R.id.updateBilPaymentMenu->{
                updatePayment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

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

            Toast.makeText(requireContext(),"Croped",Toast.LENGTH_SHORT).show()
            val result = CropImage.getActivityResult(data)
            mNewImageUri= result.uri
            mFragmentAddPaymentBinding.itemImageView.setImageURI(result.uri)



        }

    }
    private fun initUI(){

        if(mAction == ADD_ACTION){
            val paymentDateString = SimpleDateFormat("MMMM dd,yyyy").format(mSelectedPaymentDateCalendar.time)
            mFragmentAddPaymentBinding.paymentDateTextInputEditText.setText(paymentDateString)
        }else{
            mSelectedPaymentDateCalendar = Calendar.getInstance()
            mSelectedPaymentDateCalendar.time =  SimpleDateFormat("yyyy-MM-dd").parse(mBillPaymentEntity!!.paymentDate)

            val paymentDateString = SimpleDateFormat("MMMM dd,yyyy").format(mSelectedPaymentDateCalendar.time)
            mFragmentAddPaymentBinding.paymentDateTextInputEditText.setText(paymentDateString)

        }

    }
    private fun savePayment(){
        val paymentString = mFragmentAddPaymentBinding.paymentAmountTextInputEditText.text.toString()
        val paymentNoteString = mFragmentAddPaymentBinding.billPaymentNoteTextInputEditText.text.toString()
        val paymentDouble = if(paymentString.length > 0) paymentString.toDouble() else 0.0

        if(paymentDouble <=0){
            Toast.makeText(requireContext(),"Please add payment.",Toast.LENGTH_SHORT).show()
            return
        }

        var paymentUniqueId = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        mBillEntityWithTotalPayment?.let {billEntityWithTotalPayment->
            val billEntity = BillPaymentEntity(
                uniqueId = paymentUniqueId,
                billUniqueId = billEntityWithTotalPayment.billEntity.uniqueId,
                billGroupUniqueId = billEntityWithTotalPayment.billEntity.groupUniqueId,
                paymentAmount = paymentDouble,
                paymentDate = SimpleDateFormat("yyyy-MM-dd").format(mSelectedPaymentDateCalendar.time),
                paymentNote = paymentNoteString,
                imageName = "",
                status = BillPaymentEntity.NOT_DELETED_STATUS,
                uploaded = BillPaymentEntity.NOT_UPLOADED,
                created = currentDatetime,
                modified = currentDatetime
            )
            mBillViewModel.mCoroutineScope.launch {
                val id = mBillViewModel.saveBillPayment(requireContext(),billEntity)

                withContext(Main){
                    Toast.makeText(requireContext(),"Payment save successfully",Toast.LENGTH_SHORT).show()
                    activity?.finish()
                }
            }
        }


    }
    fun updatePayment(){

        val paymentString = mFragmentAddPaymentBinding.paymentAmountTextInputEditText.text.toString()
        val paymentNoteString = mFragmentAddPaymentBinding.billPaymentNoteTextInputEditText.text.toString()
        val paymentDouble = if(paymentString.length > 0) paymentString.toDouble() else 0.0

        if(paymentDouble <=0){
            Toast.makeText(requireContext(),"Please add payment.",Toast.LENGTH_SHORT).show()
            return
        }

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        val paymentDate = SimpleDateFormat("yyyy-MM-dd").format(mSelectedPaymentDateCalendar.time)
        mBillViewModel.mCoroutineScope.launch {
            val id = mBillViewModel.updatePayment(requireContext(),mBillPaymentEntity!!.uniqueId,paymentDouble,paymentDate,paymentNoteString,"",currentDatetime)
            withContext(Main){
                Toast.makeText(requireContext(),"Payment update successfully",Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }


    }
    fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val paymentDateString = SimpleDateFormat("MMMM dd,yyyy").format(date)

            mFragmentAddPaymentBinding.paymentDateTextInputEditText.setText(paymentDateString)
            mSelectedPaymentDateCalendar.time = date


        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }

    fun showIntentChooser(){
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

        startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE)



    }
    private fun lauchImageCropper(uri: Uri){

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1000, 1000)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(requireContext(),this)
    }
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
    val paymentDateOnClickListener = object:View.OnClickListener{
        override fun onClick(view: View?) {
            showCalendar()
        }
    }
    val addPaymentImageOnClickListener = object:View.OnClickListener{
        override fun onClick(view: View?) {
            showIntentChooser()
        }

    }

    companion object {
        const val REQUEST_PICK_IMAGE = 4
        const val ARG_BILL_ENTITY = "ARG_BILL_ENTITY"
        const val ARG_PAYMENT_ENTITY = "ARG_PAYMENT_ENTITY"
        const val ARG_ACTION = "ACTION"
        const val ADD_ACTION = 0
        const val EDIT_ACTION = 1
        @JvmStatic fun newInstance(billEntity: BillEntityWithTotalPayment, action: Int,paymentEntity:BillPaymentEntity?) =
            AddPaymentFragment().apply {


                Log.e("THE PAYMENT aa",paymentEntity.toString())
                arguments = Bundle().apply {
                    putParcelable(ARG_BILL_ENTITY, billEntity)
                    putParcelable(ARG_PAYMENT_ENTITY,paymentEntity)
                    putInt(ARG_ACTION,action)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}