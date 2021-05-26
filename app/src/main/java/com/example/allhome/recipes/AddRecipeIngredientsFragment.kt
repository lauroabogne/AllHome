package com.example.allhome.recipes

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.databinding.AddIngredientItemBinding
import com.example.allhome.databinding.FragmentAddRecipeIngredientsBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.concurrent.TimeUnit


class AddRecipeIngredientsFragment : Fragment() {

    private lateinit var mDataBindingUtil: FragmentAddRecipeIngredientsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_ingredients, container, false)
        mDataBindingUtil.fab.setOnClickListener {
            Toast.makeText(requireContext(), "Test fab", Toast.LENGTH_SHORT).show()

            val addIngredientRecyclerviewViewAdapater =  mDataBindingUtil.addIngredientRecyclerview.adapter as AddIngredientRecyclerviewViewAdapater

            val elemetSize = addIngredientRecyclerviewViewAdapater.mIngredients.size
            addIngredientRecyclerviewViewAdapater.mIngredients.add(elemetSize, "")
            addIngredientRecyclerviewViewAdapater.notifyItemInserted(elemetSize)

            mDataBindingUtil.addIngredientRecyclerview.scrollToPosition(elemetSize );
            mDataBindingUtil.fab.isClickable = false

            CoroutineScope(IO).launch {
                delay(500)
                withContext(Dispatchers.Main) {
                    mDataBindingUtil.fab.isClickable = true
                    Log.i("TAG", "this will be called after 3 seconds")
                    val holder = mDataBindingUtil.addIngredientRecyclerview.findViewHolderForLayoutPosition(elemetSize) as AddIngredientRecyclerviewViewAdapater.ItemViewHolder

                    holder.addIngredientItemBinding.ingredientEditTextText.requestFocus()
                    showSoftKeyboard(holder.addIngredientItemBinding.ingredientEditTextText)
                    //showSoftKeyboard()
                    Log.e("THE", holder.toString())
                }
            }



        }


        val addIngredientRecyclerviewViewAdapater = AddIngredientRecyclerviewViewAdapater(this)

        mDataBindingUtil.addIngredientRecyclerview.adapter = addIngredientRecyclerviewViewAdapater
        return mDataBindingUtil.root
    }


    /*private fun showSoftKeyboard(view: View){
        val inputMethodManager = activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0
        )
    }*/
    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }



}

class AddIngredientRecyclerviewViewAdapater(val addRecipeIngredientsFragment: AddRecipeIngredientsFragment): RecyclerView.Adapter<AddIngredientRecyclerviewViewAdapater.ItemViewHolder>() {


    var mIngredients:ArrayList<String>  = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {


        val layoutInflater = LayoutInflater.from(parent.context)
        val pantryItemLayoutBinding = AddIngredientItemBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(pantryItemLayoutBinding, this)

        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val ingredient = mIngredients[position]
       // holder.storageItemLayoutBinding.storageItemWithExpirationsAndStorages = storageEntitiesWithExpirationsAndStorages
        holder.addIngredientItemBinding.executePendingBindings()
        holder.setText(ingredient)
        holder.setTextWatcher()

    }

    override fun getItemCount(): Int {

        return mIngredients.size
    }

    inner class  ItemViewHolder(var addIngredientItemBinding: AddIngredientItemBinding, val addIngredientRecyclerviewViewAdapater: AddIngredientRecyclerviewViewAdapater): RecyclerView.ViewHolder(addIngredientItemBinding.root),View.OnClickListener{


        fun setText(text:String){

            addIngredientItemBinding.ingredientEditTextText.setText(text)
        }
        fun setTextWatcher(){
            addIngredientItemBinding.ingredientEditTextText.addTextChangedListener{

                addIngredientRecyclerviewViewAdapater.mIngredients.set(adapterPosition,it.toString())
            }
        }
        override fun onClick(view: View?) {


        }

    }


}