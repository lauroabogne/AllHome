package com.example.allhome.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.allhome.grocerylist.GroceryUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class ImageUtil {

    companion object {
        const val STORAGE_IMAGES_FINAL_LOCATION = "storage_images"
        const val STORAGE_ITEM_IMAGES_FINAL_LOCATION = "storage_item_images"
        const val RECIPE_IMAGES_FINAL_LOCATION = "recipe_images"
        const val BILL_PAYMENT_IMAGES_FINAL_LOCATION = "bill_payments"
        const val TEMPORARY_IMAGES_LOCATION = "temporary_images"
        const val IMAGE_TEMP_NAME = "temp_image"
        const val IMAGE_NAME_SUFFIX = "jpg"

        fun resizeImage(bitmap: Bitmap, targetMaxWidthOrHeight: Int): Bitmap {

            val height = bitmap.height
            val width = bitmap.width

            if (height <= targetMaxWidthOrHeight && width <= targetMaxWidthOrHeight) {
                //do nothing
                return bitmap
            }

            if (height > width) {
                //set height = targetMaxWidthOrHeight and compute value of width
                val computedWidth = computeWidth(height, width, targetMaxWidthOrHeight)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, computedWidth, targetMaxWidthOrHeight, false)
                return resizedBitmap
            } else {
                // set width = targetMaxWidthOrHeight and compute value of height
                val computedHeight = computeHeight(height, width, targetMaxWidthOrHeight)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetMaxWidthOrHeight, computedHeight, false)
                return resizedBitmap
            }

        }

        fun computeWidth(imageHeight: Int, imageWidth: Int, targetHeight: Int): Int {
            return (imageWidth * targetHeight) / imageHeight
        }

        fun computeHeight(imageHeight: Int, imageWidth: Int, targetWidth: Int): Int {
            return (imageHeight * targetWidth) / imageWidth
        }

        fun processImageForProperOrientation(originalBitmap: Bitmap, orginalBitmapPath: String): Bitmap? {
            var ei: ExifInterface? = null
            var rotatedBitmap: Bitmap? = null
            try {
                ei = ExifInterface(orginalBitmapPath)
                val orientation = ei!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(originalBitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(originalBitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(originalBitmap, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = originalBitmap
                    else -> rotatedBitmap = originalBitmap
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return rotatedBitmap
        }

        fun getFileSizeInMB(filePath: String):Double{

            var file  = File(filePath)
            var fileSize = file.length().toDouble()
            // see here https://www.gbmb.org/kb-to-mb
            var kb = fileSize * 0.0009765625 //binary conversion (1/1024)
            var mb = kb * 0.0009765625 //binary conversion
            var gb = mb * 0.0009765625 //binary conversion

            return mb
        }


        fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
                    matrix, true)
        }

        fun uriToBitmap(uri: Uri, context: Context): Bitmap {

            if(Build.VERSION.SDK_INT < 28) {
                return  MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                return ImageDecoder.decodeBitmap(source)

            }
        }

         fun saveImage(context:Context,imageUri: Uri, imageName: String,dirName:String):Boolean{
            val imageBitmap =  uriToBitmap(imageUri, context)
            val resizedImageBitmap = resizeImage(imageBitmap,1000)
            val storageDir: File = context.getExternalFilesDir(dirName)!!
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

        fun getImageUriFromPath(context: Context, storageDir:String,imageName:String): Uri? {

            val storageDir: File =  context.getExternalFilesDir(storageDir)!!
            if(!storageDir.exists()){
                return null
            }
            val imageFile  = File(storageDir, imageName)

            if(imageFile.exists() && imageFile.isFile){
                return Uri.fromFile(imageFile)
            }

            return null
        }

        fun deleteImageFile( uri:Uri) {
            val imageFile = File(uri.path)
            imageFile.delete()

        }
        fun deleteAllTemporaryImages(context: Context){
            val storageDir: File = context.getExternalFilesDir(GroceryUtil.TEMPORARY_IMAGES_LOCATION)!!
            storageDir.exists().apply {
                storageDir.deleteRecursively()
            }
        }

         fun saveImage(context:Context,image: Bitmap, storageDir:String,imageName:String): String? {
            var savedImagePath: String? = null
            val storageDir: File = context.getExternalFilesDir(storageDir)!!
            var success = true
            if (!storageDir.exists()) {
                success = storageDir.mkdirs()
            }
            if (success) {
                val imageFile = File(storageDir, imageName)
                savedImagePath = imageFile.getAbsolutePath()
                try {
                    val fOut: OutputStream = FileOutputStream(imageFile)
                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
            return savedImagePath
        }

    }

}