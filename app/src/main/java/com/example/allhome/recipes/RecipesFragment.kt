package com.example.allhome.recipes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentRecipesBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RecipesFragment : Fragment() {

     lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    private lateinit var mFragmentRecipesBinding: FragmentRecipesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {

        }

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_all_recipe_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.noFilterMenu -> {

            }
            R.id.recipeInformationFilterMenu -> {
                val filterByInformationDialogFragment  = FilterByInformationDialogFragment(mRecipesFragmentViewModel)
                filterByInformationDialogFragment.isCancelable = false
                filterByInformationDialogFragment.setRecipeFilterSetter(recipeFilterSetter)
                filterByInformationDialogFragment.show(requireActivity().supportFragmentManager,"IngredientDialogFragment")
            }
            R.id.recipeIngredientFilterMenu -> {


            }

        }


        return true
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        mFragmentRecipesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipes, container, false)
        mFragmentRecipesBinding.fab.setOnClickListener {

            val intent = Intent(requireContext(),AddRecipeActivity::class.java)
            startActivity(intent)
        }


        val recipesRecyclerviewViewAdapater = RecipesRecyclerviewViewAdapater(arrayListOf(),this)
        mFragmentRecipesBinding.recipesRecyclerview.adapter = recipesRecyclerviewViewAdapater

        mRecipesFragmentViewModel.mCoroutineScope.launch {
            val recipes = mRecipesFragmentViewModel.getRecipes(requireContext())

            withContext(Main){
                val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                adapter.notifyDataSetChanged()
            }
        }


        return mFragmentRecipesBinding.root
    }
    private val  recipeFilterSetter = object:RecipeFilterSetter{
        override fun filterConditions(costCondition: String, servingCondition: String, prepPlusCookTimeSelectedCondition: String) {
            mRecipesFragmentViewModel.mCostCondition=costCondition
            mRecipesFragmentViewModel.mServingCondition=servingCondition
            mRecipesFragmentViewModel.mPrepPlusCookTimeCondition=prepPlusCookTimeSelectedCondition
        }

        override fun filters(costString: String, servingString: String, prepPlusCookHourString: String, prepPlusCookMinutesString: String) {

            mRecipesFragmentViewModel.mCostString = costString
            mRecipesFragmentViewModel.mServingString = servingString
            mRecipesFragmentViewModel.mPrepPlusCookHourString = prepPlusCookHourString
            mRecipesFragmentViewModel.mPrepPlusCookMinutesString = prepPlusCookMinutesString
        }

        override fun onFilterSet(hasCostInput:Boolean,hasServingInput:Boolean,hasHourOrMinuteInput:Boolean) {

            Toast.makeText(requireContext(),"Filtering",Toast.LENGTH_SHORT).show()
            mRecipesFragmentViewModel.mHasCostInput = hasCostInput
            mRecipesFragmentViewModel.mHasServingInput = hasServingInput
            mRecipesFragmentViewModel.mHasHourOrMinuteInput = hasHourOrMinuteInput
            mRecipesFragmentViewModel.mFiltering = true

            Log.e("onFilterSet",hasCostInput.toString()+" "+hasServingInput.toString()+" "+hasHourOrMinuteInput.toString())
            if(hasCostInput && hasServingInput &&  hasHourOrMinuteInput ){
                //filter by cost, serving and total preparation and cook time
                    Log.e("FILTER_BY","filterByCostServingAndTotalPrepAndCookTime")
                val cost =mRecipesFragmentViewModel.mCostString.toDouble()
                val serving = mRecipesFragmentViewModel.mServingString.toInt()
                val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByCostServingAndTotalPrepAndCookTime(requireContext(),
                        mRecipesFragmentViewModel.mCostCondition,cost,mRecipesFragmentViewModel.mServingCondition,serving,
                        mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }
            }else if(hasCostInput && hasServingInput &&  !hasHourOrMinuteInput ){
                // filter by cost and serving
                val cost =mRecipesFragmentViewModel.mCostString.toDouble()
                val serving = mRecipesFragmentViewModel.mServingString.toInt()

                Log.e("FILTER_BY","filterByCostAndServing")

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByCostAndServing(requireContext(), mRecipesFragmentViewModel.mCostCondition,cost,mRecipesFragmentViewModel.mServingCondition,serving)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }
            }else if(hasCostInput && !hasServingInput &&  hasHourOrMinuteInput ){
                // filter by cost and total preparation + cook time

                val cost = mRecipesFragmentViewModel.mCostString.toDouble()
                val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

                Log.e("FILTER_BY","filterByCostAndTotalPrepAndCookTime")

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByCostAndTotalPrepAndCookTime(requireContext(), mRecipesFragmentViewModel.mCostCondition,cost,mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes,)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }

            }else if(!hasCostInput && hasServingInput &&  hasHourOrMinuteInput ){
                // filter by serving and total preparation and cook time

                val serving = mRecipesFragmentViewModel.mServingString.toInt()
                val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

                Log.e("FILTER_BY","filterByServingAndTotalPrepAndCookTime")

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByServingAndTotalPrepAndCookTime(requireContext(), mRecipesFragmentViewModel.mServingCondition,serving,mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes,)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }

            }else if(hasCostInput && !hasServingInput &&  !hasHourOrMinuteInput ){
                // filter by cost
                val cost = mRecipesFragmentViewModel.mCostString.toDouble()

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByCost(requireContext(), mRecipesFragmentViewModel.mCostCondition,cost)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }

            }else if(!hasCostInput && hasServingInput &&  !hasHourOrMinuteInput ){
                // filter by serving

                val serving = mRecipesFragmentViewModel.mServingString.toInt()

                Log.e("FILTER_BY","filterByServing")

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByServing(requireContext(), mRecipesFragmentViewModel.mServingCondition,serving)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }
            }else if(!hasCostInput && !hasServingInput &&  hasHourOrMinuteInput ){
                // filter by total preparation and cook time
                val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

                Log.e("FILTER_BY","filterByTotalPrepAndCookTime")

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val recipes = mRecipesFragmentViewModel.filterByTotalPrepAndCookTime(requireContext(), mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
        fun totalPrepAndCookTimeInMinutes(prepPlusCookHourString: String, prepPlusCookMinutesString: String):Int{
            val prepAPlusCookHourInt = if(prepPlusCookHourString.isEmpty()) 0 else prepPlusCookHourString.toInt()
            val prepPlusCookMinutesString = if(prepPlusCookMinutesString.isEmpty()) 0 else prepPlusCookMinutesString.toInt()
            val minutesInHour = 60
            return (prepAPlusCookHourInt / minutesInHour)+prepPlusCookMinutesString
        }
    }

    interface RecipeFilterSetter{
        fun filterConditions(costCondition:String,servingCondition:String,prepPlusCookTimeSelectedCondition:String)
        fun filters(costString:String,servingString:String,prepPlusCookHourString:String,prepPlusCookMinutesString:String)
        fun onFilterSet(hasCostInput:Boolean,hasServingInput:Boolean,hasHourOrMinuteInput:Boolean)
    }


    class RecipesRecyclerviewViewAdapater(var mRecipeStepEntities:ArrayList<RecipeEntity>, val mRecipesFragment: RecipesFragment):
        RecyclerView.Adapter<RecipesRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutInflater = LayoutInflater.from(parent.context)
            val recipeItemBinding =  RecipeItemBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(recipeItemBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val recipeEntity = mRecipeStepEntities[position]
            holder.recipeItemBinding.recipeEntity = recipeEntity
            holder.recipeItemBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {

            return mRecipeStepEntities.size
        }


        inner class  ItemViewHolder(var recipeItemBinding: RecipeItemBinding, val recipesRecyclerviewViewAdapater: RecipesRecyclerviewViewAdapater): RecyclerView.ViewHolder(recipeItemBinding.root),View.OnClickListener{

            init {
                recipeItemBinding.root.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                Toast.makeText(view?.context,"Clicked",Toast.LENGTH_SHORT).show()
                val recipeEntity = recipesRecyclerviewViewAdapater.mRecipeStepEntities[adapterPosition]


                val viewRecipeActivity = Intent(view?.context, ViewRecipeActivity::class.java)
                viewRecipeActivity.putExtra(ViewRecipeFragment.RECIPE_INTENT_TAG,recipeEntity)
                view?.context?.startActivity(viewRecipeActivity)
            }


        }


    }

}

