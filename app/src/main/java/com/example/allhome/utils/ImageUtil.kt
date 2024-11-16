package com.example.allhome.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.allhome.grocerylist.GroceryUtil
import com.github.chrisbanes.photoview.PhotoView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class ImageUtil {

    companion object {
        const val STORAGE_IMAGES_FINAL_LOCATION = "storage_images"
        const val STORAGE_ITEM_IMAGES_FINAL_LOCATION = "storage_item_images"
        const val RECIPE_IMAGES_FINAL_LOCATION = "recipe_images"
        const val BILL_PAYMENT_IMAGES_FINAL_LOCATION = "bill_payments"
        const val FINAL_IMAGES_LOCATION = "item_images"
        const val TEMPORARY_IMAGES_LOCATION = "temporary_images"
        const val IMAGE_TEMP_NAME = "temp_image"
        const val IMAGE_NAME_SUFFIX = "jpg"

         fun getProportionImageSize(targetMaxWidthOrHeight: Int, imageWidth:Int,imageHeight:Int): Map<String, Int> {
            if(imageWidth < targetMaxWidthOrHeight && imageHeight < targetMaxWidthOrHeight){
                return mapOf("width" to imageWidth,"height" to imageHeight)
            }
            if(imageWidth > imageHeight){
                val height = (imageHeight * targetMaxWidthOrHeight) / imageWidth
                return mapOf("width" to targetMaxWidthOrHeight,"height" to height)
            }else{
                val width = (imageWidth * targetMaxWidthOrHeight) / imageHeight
                return mapOf("width" to width,"height" to targetMaxWidthOrHeight)
            }

        }
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

        fun getInputStreamFromUri(context: Context, uri: Uri): InputStream? {
            return context.contentResolver.openInputStream(uri)
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
        fun doImageExist(imagePath:String): Boolean {
            return File(imagePath).exists()
        }

        fun zoomInImageFromThumb(thumbView: View, imageUri: Uri, mainLayout: View, imageContainer: View, imageHolder: PhotoView, animationDuration:Long) {

            imageHolder.setImageURI(imageUri)

            // Calculate the starting and ending bounds for the zoomed-in image.
            // This step involves lots of math. Yay, math.
            val startBoundsInt = Rect()
            val finalBoundsInt = Rect()
            val globalOffset = Point()

            // The start bounds are the global visible rectangle of the thumbnail,
            // and the final bounds are the global visible rectangle of the container
            // view. Also set the container view's offset as the origin for the
            // bounds, since that's the origin for the positioning animation
            // properties (X, Y).
            thumbView.getGlobalVisibleRect(startBoundsInt)
            mainLayout.getGlobalVisibleRect(finalBoundsInt, globalOffset)
            startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
            finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

            val startBounds = RectF(startBoundsInt)
            val finalBounds = RectF(finalBoundsInt)

            // Adjust the start bounds to be the same aspect ratio as the final
            // bounds using the "center crop" technique. This prevents undesirable
            // stretching during the animation. Also calculate the start scaling
            // factor (the end scaling factor is always 1.0).
            val startScale: Float
            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                // Extend start bounds horizontally
                startScale = startBounds.height() / finalBounds.height()
                val startWidth: Float = startScale * finalBounds.width()
                val deltaWidth: Float = (startWidth - startBounds.width()) / 2
                startBounds.left -= deltaWidth.toInt()
                startBounds.right += deltaWidth.toInt()
            } else {
                // Extend start bounds vertically
                startScale = startBounds.width() / finalBounds.width()
                val startHeight: Float = startScale * finalBounds.height()
                val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
                startBounds.top -= deltaHeight.toInt()
                startBounds.bottom += deltaHeight.toInt()
            }

            // Hide the thumbnail and show the zoomed-in view. When the animation
            // begins, it will position the zoomed-in view in the place of the
            // thumbnail.
            thumbView.alpha = 0f
            imageContainer.visibility = View.VISIBLE

            // Set the pivot point for SCALE_X and SCALE_Y transformations
            // to the top-left corner of the zoomed-in view (the default
            // is the center of the view).
            imageContainer.pivotX = 0f
            imageContainer.pivotY = 0f

            // Construct and run the parallel animation of the four translation and
            // scale properties (X, Y, SCALE_X, and SCALE_Y).
            var currentAnimator: AnimatorSet? = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        imageContainer,
                        View.X,
                        startBounds.left,
                        finalBounds.left
                    )
                ).apply {
                    with(ObjectAnimator.ofFloat(imageContainer, View.Y, startBounds.top, finalBounds.top))
                    with(ObjectAnimator.ofFloat(imageContainer, View.SCALE_X, startScale, 1f))
                    with(ObjectAnimator.ofFloat(imageContainer, View.SCALE_Y, startScale, 1f))
                }
                duration = animationDuration
                interpolator = DecelerateInterpolator()
            }

            currentAnimator?.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }
                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            currentAnimator?.start()

        }


        fun zoomOutImageFromThumb(thumbView: View, mainLayout:View,  imageContainer:View,  animationDuration:Long) {

            // If there's an animation in progress, cancel it
            // immediately and proceed with this one.
            // currentAnimator?.cancel()

            // val imageContainer = dataBindingUtil.imageContainerIncludedLayout.imageContainer

            // Calculate the starting and ending bounds for the zoomed-in image.
            // This step involves lots of math. Yay, math.
            val startBoundsInt = Rect()
            val finalBoundsInt = Rect()
            val globalOffset = Point()

            // The start bounds are the global visible rectangle of the thumbnail,
            // and the final bounds are the global visible rectangle of the container
            // view. Also set the container view's offset as the origin for the
            // bounds, since that's the origin for the positioning animation
            // properties (X, Y).
            thumbView.getGlobalVisibleRect(startBoundsInt)
            mainLayout.getGlobalVisibleRect(finalBoundsInt, globalOffset)
            startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
            finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

            val startBounds = RectF(startBoundsInt)
            val finalBounds = RectF(finalBoundsInt)

            // Adjust the start bounds to be the same aspect ratio as the final
            // bounds using the "center crop" technique. This prevents undesirable
            // stretching during the animation. Also calculate the start scaling
            // factor (the end scaling factor is always 1.0).
            val startScale: Float
            if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
                // Extend start bounds horizontally
                startScale = startBounds.height() / finalBounds.height()
                val startWidth: Float = startScale * finalBounds.width()
                val deltaWidth: Float = (startWidth - startBounds.width()) / 2
                startBounds.left -= deltaWidth.toInt()
                startBounds.right += deltaWidth.toInt()
            } else {
                // Extend start bounds vertically
                startScale = startBounds.width() / finalBounds.width()
                val startHeight: Float = startScale * finalBounds.height()
                val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
                startBounds.top -= deltaHeight.toInt()
                startBounds.bottom += deltaHeight.toInt()
            }


            // Upon clicking the zoomed-in image, it should zoom back down
            // to the original bounds and show the thumbnail instead of
            // the expanded image.


            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            var currentAnimator : AnimatorSet? = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(imageContainer, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(imageContainer, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(imageContainer, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(imageContainer, View.SCALE_Y, startScale))
                }
                duration = animationDuration
                interpolator = DecelerateInterpolator()
            }

            currentAnimator?.addListener(object:AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator) {
                    thumbView.alpha = 1f
                    imageContainer.visibility = View.GONE
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    imageContainer.visibility = View.GONE
                    currentAnimator = null
                }
            })
            currentAnimator?.start()
        }

    }

}