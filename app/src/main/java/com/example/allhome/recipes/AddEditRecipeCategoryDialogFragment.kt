package com.example.allhome.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.databinding.AddEditRecipeCategoryDialogFragmentLayoutBinding
import com.example.allhome.recipes.viewmodel.RecipeCategoryViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddEditRecipeCategoryDialogFragment(val mRecipeCategoryViewModel: RecipeCategoryViewModel): DialogFragment() {

    companion object{
        const val FAILED = 0
        const val SUCCESS = 1
    }

    var mAddingRecipeCategoryListener:AddingRecipeCategoryListener? = null
    lateinit var mAddEditRecipeCategoryDialogFragmentLayoutBinding:AddEditRecipeCategoryDialogFragmentLayoutBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val inflater = LayoutInflater.from(requireContext())
        mAddEditRecipeCategoryDialogFragmentLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.add_edit_recipe_category_dialog_fragment_layout,null,false)
        mAddEditRecipeCategoryDialogFragmentLayoutBinding.toolbar.title = "Add Category"

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(mAddEditRecipeCategoryDialogFragmentLayoutBinding.root)


        alertDialogBuilder.setPositiveButton("Save", DialogInterface.OnClickListener { dialog, which ->
            saveRecipeCategory()
        })
        alertDialogBuilder.setNegativeButton("Close", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })

        val alertDialog = alertDialogBuilder.create()


        return alertDialog
    }

    fun saveRecipeCategory(){
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        var itemUniqueID = UUID.randomUUID().toString()
        val recipeCategory = mAddEditRecipeCategoryDialogFragmentLayoutBinding.categoryTextInputEditText.text.toString().trim()
        val recipeCategoryEntity = RecipeCategoryEntity(
            uniqueId = itemUniqueID,
            name = recipeCategory,
            status=RecipeCategoryEntity.NOT_DELETED_STATUS,
            uploaded =RecipeCategoryEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime

        )

        mRecipeCategoryViewModel.mCoroutineScope.launch {
            val id = mRecipeCategoryViewModel.add(requireContext(),recipeCategoryEntity)

            withContext(Main){
                mAddingRecipeCategoryListener?.let {
                    if(id > 0){
                        it.onSave(SUCCESS,itemUniqueID)
                    }else{
                        it.onSave(FAILED,null)
                    }
                }

            }
        }


    }

    fun setAddingRecipeCategoryListener(addingRecipeCategoryListener:AddingRecipeCategoryListener){
        mAddingRecipeCategoryListener = addingRecipeCategoryListener
    }
    interface AddingRecipeCategoryListener{
        fun onSave(status:Int,categoryUniqueId:String?)
    }
}