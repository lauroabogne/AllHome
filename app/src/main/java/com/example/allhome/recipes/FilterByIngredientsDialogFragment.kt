package com.example.allhome.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.databinding.AddIngredientItemBinding
import com.example.allhome.databinding.AddIngredientItemForSeachingBinding
import com.example.allhome.databinding.FilterByIngredientsDialogFragmentBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.example.allhome.storage.StorageAddItemActivity
import com.example.allhome.storage.StorageItemAutoSuggestCustomAdapter
import com.example.allhome.utils.MinMaxInputFilter
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FilterByIngredientsDialogFragment(var mRecipesFragmentViewModel: RecipesFragmentViewModel): DialogFragment() {

    lateinit var mFilterByIngredientsDialogFragmentBinding:FilterByIngredientsDialogFragmentBinding
    var mRecipeIngredientFilterListener: RecipesFragment.RecipeIngredientFilterListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)





        /*val addIngredientRecyclerviewViewAdapater = AddIngredientRecyclerviewViewAdapater(mRecipesFragmentViewModel.mFilterIngredients)
        mFilterByIngredientsDialogFragmentBinding.addIngredientRecyclerview.adapter = addIngredientRecyclerviewViewAdapater*/


    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mFilterByIngredientsDialogFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.filter_by_ingredients_dialog_fragment,null,false)

        val addIngredientRecyclerviewViewAdapater = AddIngredientRecyclerviewViewAdapater(arrayListOf())
        addIngredientRecyclerviewViewAdapater.mIngredients = mRecipesFragmentViewModel.mFilterIngredients
        mFilterByIngredientsDialogFragmentBinding.addIngredientRecyclerview.adapter = addIngredientRecyclerviewViewAdapater

        mFilterByIngredientsDialogFragmentBinding.addIngredientBtn.setOnClickListener(addIngredientBtnOnClick)

        val storageItemAutoSuggestCustomAdapter = StringAutoSuggestCustomAdapter(this, arrayListOf())
        mFilterByIngredientsDialogFragmentBinding.ingredientTextInput.threshold = 0
        mFilterByIngredientsDialogFragmentBinding.ingredientTextInput.setAdapter(storageItemAutoSuggestCustomAdapter)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)

        alertDialogBuilder.setView(mFilterByIngredientsDialogFragmentBinding.root)
        alertDialogBuilder.setPositiveButton("Search", DialogInterface.OnClickListener { dialog, which ->
            mRecipeIngredientFilterListener?.onIngredientFilterSet(mRecipesFragmentViewModel.mFilterIngredients)
        })
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })

        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return alertDialog
    }
    fun setRecipeIngredientFilterListener(recipeIngredientFilterListener: RecipesFragment.RecipeIngredientFilterListener){
        mRecipeIngredientFilterListener = recipeIngredientFilterListener
    }
    val addIngredientBtnOnClick = object:View.OnClickListener {
        override fun onClick(v: View?) {


            val addIngredientRecyclerviewViewAdapater =  mFilterByIngredientsDialogFragmentBinding.addIngredientRecyclerview.adapter as AddIngredientRecyclerviewViewAdapater
            val ingredient = mFilterByIngredientsDialogFragmentBinding.ingredientTextInput.text.toString().trim()
            mFilterByIngredientsDialogFragmentBinding.ingredientTextInput.setText("")

            if(ingredient.length <=0){
                Toast.makeText(requireContext(),"Enter ingredient",Toast.LENGTH_SHORT).show()
                return
            }


            mRecipesFragmentViewModel.mFilterIngredients.add(ingredient)
            addIngredientRecyclerviewViewAdapater.notifyDataSetChanged()



        }

    }
    /**
     *
     */
    class AddIngredientRecyclerviewViewAdapater(var mIngredients:ArrayList<String>): RecyclerView.Adapter<AddIngredientRecyclerviewViewAdapater.ItemViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {


            val layoutInflater = LayoutInflater.from(parent.context)
            val addIngredientItemForSeachingBinding = AddIngredientItemForSeachingBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(addIngredientItemForSeachingBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val ingredient = mIngredients[position]

            holder.addIngredientItemForSeachingBinding.ingredient = ingredient
            holder.addIngredientItemForSeachingBinding.executePendingBindings()
            holder.setTextWatcher()
        }

        override fun getItemCount(): Int {


            return mIngredients.size
        }
        fun itemDropped(viewHolderParams: RecyclerView.ViewHolder){

            var viewHolder  = viewHolderParams as AddIngredientRecyclerviewViewAdapater.ItemViewHolder
            viewHolder.addIngredientItemForSeachingBinding.root.setBackgroundColor(Color.WHITE)

        }

        fun removeItem(position: Int){

            mIngredients.removeAt(position)
            notifyItemRemoved(position)



        }

        inner class  ItemViewHolder(var addIngredientItemForSeachingBinding: AddIngredientItemForSeachingBinding, val addIngredientRecyclerviewViewAdapater: AddIngredientRecyclerviewViewAdapater): RecyclerView.ViewHolder(addIngredientItemForSeachingBinding.root), View.OnClickListener{


            init {
                addIngredientItemForSeachingBinding.removeBtn.setOnClickListener(this)
            }
            fun setTextWatcher(){
               /* addIngredientItemForSeachingBinding.ingredientEditTextText.addTextChangedListener{
                    val ingredient =  addIngredientRecyclerviewViewAdapater.mIngredients[adapterPosition]
                    ingredient.name = it.toString()
                    addIngredientRecyclerviewViewAdapater.mIngredients.set(adapterPosition,ingredient)
                }*/
            }
            override fun onClick(view: View?) {

                when(view?.id){
                    R.id.removeBtn->{

                        addIngredientRecyclerviewViewAdapater.removeItem(adapterPosition)
                    }
                }

            }

        }


    }
    /**
     * Item unit auto suggest adapter
     */
    class StringAutoSuggestCustomAdapter(filterByIngredientsDialogFragment: FilterByIngredientsDialogFragment, var stringParams: List<String>):
        ArrayAdapter<String>(filterByIngredientsDialogFragment.requireContext(), 0, stringParams) {
        private var stringAutoSuggest: List<String>? = null


        init{
            stringAutoSuggest = ArrayList(stringParams)
        }

        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(Dispatchers.IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        var arrayList = arrayListOf<String>()

                        arrayList = filterByIngredientsDialogFragment.mRecipesFragmentViewModel.getIngredientForAutoSuggest(filterByIngredientsDialogFragment.requireContext(),searchTerm) as ArrayList<String>
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
            var textView: TextView? = null
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
}