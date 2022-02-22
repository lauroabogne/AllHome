package com.example.allhome.grocerylist

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.executor.GlideExecutor.UncaughtThrowableStrategy.LOG
import com.example.allhome.R
import com.example.allhome.databinding.FragmentBrowseItemImageBinding
import com.example.allhome.global_ui.ProgressDialogFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream



class BrowseItemImageFragment : Fragment() {

    private var mProgressDialogFragment: ProgressDialogFragment = ProgressDialogFragment()
    lateinit var mFragmentBrowseItemImageBinding:FragmentBrowseItemImageBinding

    private lateinit var mItemName:String
    private var mItemUnit:String? = null
    private var mPrice:Double = 0.0
    private var mImageAbsolutePath:String? = null

    companion object {
        //const val TEMP_IMAGE_NAME = "grocery_list_temp_img.png"
        const val ITEM_NAME_TAG = "item_name"
        const val ITEM_UNIT_TAG = "unit"
        const val ITEM_PRICE_TAG = "price"
        const val ITEM_IMAGE_NAME_TAG = "image_path"

        @JvmStatic fun newInstance(itemName: String,itemUnit:String,price:Double,imageName:String) =
            BrowseItemImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ITEM_NAME_TAG, itemName)
                    putString(ITEM_UNIT_TAG,itemUnit)
                    putDouble(ITEM_PRICE_TAG,price)
                    putString(ITEM_IMAGE_NAME_TAG,imageName)
                }
            }
    }
    private val copyBtnOnClick = View.OnClickListener {

        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        when(it.id){

            R.id.copyPriceBtn->{

                val price = clipboardManager.primaryClip?.getItemAt(0)?.text.toString().replace("[^\\d.]".toRegex(), "");
                try {
                    mPrice = price.toDouble()
                    Toast.makeText(requireContext(),"Price copy successfully.",Toast.LENGTH_SHORT).show()
                }catch (exception:Exception){
                    Toast.makeText(requireContext(),"Invalid price.",Toast.LENGTH_SHORT).show()
                }
            }
            R.id.copyUnitBtn->{
                mItemUnit = clipboardManager.primaryClip?.getItemAt(0)?.text.toString();
                Toast.makeText(requireContext(),"Unit copy successfully.",Toast.LENGTH_SHORT).show()
            }
            R.id.copyNameBtn->{
                mItemName = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()
                Toast.makeText(requireContext(),"Item name copy successfully.",Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mItemName = it.getString(ITEM_NAME_TAG).toString()
            mItemUnit = it.getString(ITEM_UNIT_TAG)
            mPrice = it.getDouble(ITEM_PRICE_TAG)

            it.getString(ITEM_IMAGE_NAME_TAG)?.let {imageName->
                val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
                //create directory if not exists
                if(!storageDir.exists()){
                    storageDir.mkdir()
                }
                mImageAbsolutePath = File(storageDir, imageName).absolutePath

            }


        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentBrowseItemImageBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_browse_item_image,container,false)
        mFragmentBrowseItemImageBinding.progressBar.max = 100

        val toolBar = mFragmentBrowseItemImageBinding.toolbar
        toolBar.title = "Search item"
        toolBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolBar.setNavigationOnClickListener {
            activity?.finish()
        }
        toolBar.inflateMenu(R.menu.search_grocery_item_online)
        toolBar.setOnMenuItemClickListener {


            val intent = Intent()
            intent.putExtra(ITEM_IMAGE_NAME_TAG, mImageAbsolutePath)
            intent.putExtra(ITEM_NAME_TAG, mItemName)
            intent.putExtra(ITEM_UNIT_TAG, mItemUnit)
            intent.putExtra(ITEM_PRICE_TAG, mPrice)

            requireActivity().setResult(RESULT_OK, intent)
            requireActivity().finish()


            true
        }

        mImageAbsolutePath?.let {
            displayImage(it)
        }

        setupViewView()
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                if(mFragmentBrowseItemImageBinding.webView.canGoBack()){
                    mFragmentBrowseItemImageBinding.webView.goBack()
                    return
                }
                activity?.finish()

            }

        })

        mFragmentBrowseItemImageBinding.copyPriceBtn.setOnClickListener(copyBtnOnClick)
        mFragmentBrowseItemImageBinding.copyUnitBtn.setOnClickListener(copyBtnOnClick)
        mFragmentBrowseItemImageBinding.copyNameBtn.setOnClickListener(copyBtnOnClick)

        return mFragmentBrowseItemImageBinding.root
    }

    private fun saveImage(imageUrl:String){
        mProgressDialogFragment.show(requireActivity().supportFragmentManager,"ProgressDialogFragment")
        mProgressDialogFragment.isCancelable = false;

        val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("GroceryListFragmentViewModel"))
        coroutineScope.launch {
            val okHttpClient = OkHttpClient()
            val call = okHttpClient.newCall(Request.Builder().url(imageUrl).get().build())
            val response: Response = call.execute()

            if (response.code == 200 || response.code == 201) {

                var inputStream: InputStream? = null
                try {
                    inputStream =response.body?.byteStream()

                    val buff = ByteArray(1024 * 4)
                    var downloaded: Long = 0
                    val target = response.body!!.contentLength()
                    val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
                    //create directory if not exists
                    if(!storageDir.exists()){
                        storageDir.mkdir()
                    }


                    //val actualImageFile = File(storageDir, TEMP_IMAGE_NAME)
                    val actualImageFile = File.createTempFile(
                        AddGroceryListItemFragment.IMAGE_TEMP_NAME, /* prefix */
                        ".${AddGroceryListItemFragment.IMAGE_NAME_SUFFIX}", /* suffix */
                        storageDir /* directory */
                    )
                    val output: OutputStream = FileOutputStream(actualImageFile)

                    while (target > downloaded) {
                        val readed = inputStream!!.read(buff)
                        if (readed == -1) {
                            break
                        }
                        output.write(buff, 0, readed)
                        //write buff
                        downloaded += readed
                        Log.e("PROGRESS","$downloaded / $target")

                    }

                    output.flush()
                    output.close()

                    withContext(Main){
                        doneSavingImage(actualImageFile.absolutePath)
                    }


                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveBitmap(itemImageBitmap:Bitmap){
        mProgressDialogFragment.show(requireActivity().supportFragmentManager,"ProgressDialogFragment")
        mProgressDialogFragment.isCancelable = false;

        try {
            val storageDir: File = requireContext().getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
            if(!storageDir.exists()){
                storageDir.mkdir()
            }

           val imageFile = File.createTempFile(
                AddGroceryListItemFragment.IMAGE_TEMP_NAME, /* prefix */
                ".${AddGroceryListItemFragment.IMAGE_NAME_SUFFIX}", /* suffix */
                storageDir /* directory */
            )
           // val imageFile =  File(storageDir,TEMP_IMAGE_NAME)
            val fileOutputStream = FileOutputStream(imageFile)
            itemImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            doneSavingImage(imageFile.absolutePath)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private fun doneSavingImage(fileNameAbsolutePath:String){
        mProgressDialogFragment.dismiss()

        Toast.makeText(requireContext(),fileNameAbsolutePath,Toast.LENGTH_SHORT).show()
//        val intent = Intent()
//        intent.putExtra(TEMP_IMAGE_NAME, fileName)
//        requireActivity().setResult(RESULT_OK, intent)
//        requireActivity().finish()


        mImageAbsolutePath = fileNameAbsolutePath
        displayImage(fileNameAbsolutePath)


    }
    fun displayImage(fileNameAbsolutePath:String){
        val imageUri = Uri.fromFile(File(fileNameAbsolutePath))
        mFragmentBrowseItemImageBinding.itemImageView.setImageURI(null)//set image url to null. The ImageView won't reload the image if you call setImageURI with the same URI
        mFragmentBrowseItemImageBinding.itemImageView.setImageURI(imageUri)
    }
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val webViewHitTestResult: WebView.HitTestResult =  mFragmentBrowseItemImageBinding.webView.hitTestResult


        if (webViewHitTestResult.type == WebView.HitTestResult.IMAGE_TYPE ) {
            menu.setHeaderTitle("Download Image...")
            menu.add(0, 1, 0, "Click to download").setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener{
                    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                        val downloadImageURL = webViewHitTestResult.extra

                        if (URLUtil.isValidUrl(downloadImageURL)) {

                            saveImage(downloadImageURL.toString())

                        } else {
                            //base 64
                            val possibleBase64Image = webViewHitTestResult.extra
                            val base64Word = "base64,"
                            val indexOfBase64Word: Int = possibleBase64Image!!.indexOf(base64Word)


                            if(indexOfBase64Word <=0 ){
                                Toast.makeText(requireContext(), "Sorry.. Something Went Wrong...", Toast.LENGTH_LONG).show()
                                return false
                            }

                            val base64StringImage = possibleBase64Image.removeRange(0,indexOfBase64Word + base64Word.length)
                            val imageBytes = Base64.decode(base64StringImage, Base64.DEFAULT)
                            val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            saveBitmap(bitmapImage)

                       }
                        return false
                    }
                })
        }
    }

    private val webChromeClient = object: WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            Log.e("PROGRESS","${newProgress}")
            mFragmentBrowseItemImageBinding.progressBar.progress = newProgress
           // mFragmentBrowseRecipeBinding.progressBar.progress = newProgress

            super.onProgressChanged(view, newProgress)
        }


    }

    private fun setupViewView(){
        registerForContextMenu(mFragmentBrowseItemImageBinding.webView)
        mFragmentBrowseItemImageBinding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                //mSearchView.setQuery(url,false)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.e("loaded","${url}")
                super.onPageFinished(view, url)
            }


        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFragmentBrowseItemImageBinding.webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true)
        }
        mFragmentBrowseItemImageBinding.webView.webChromeClient =webChromeClient
        mFragmentBrowseItemImageBinding.webView.settings.domStorageEnabled = true
        mFragmentBrowseItemImageBinding.webView.settings.javaScriptEnabled = true// disable the default zoom controls on the page
        mFragmentBrowseItemImageBinding.webView.apply {
            settings.javaScriptEnabled = true
            loadUrl("https://www.google.com/search?q=${mItemName}")//metadata ERROR SAVING


        }
    }

}