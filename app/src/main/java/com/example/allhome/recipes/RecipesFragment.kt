package com.example.allhome.recipes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
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
import com.example.allhome.storage.StoragePerItemRecyclerviewViewAdapater
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main


class RecipesFragment : Fragment() {

    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    private lateinit var mFragmentRecipesBinding: FragmentRecipesBinding
    var mSelectedMenuItem:MenuItem? = null

    var mSearchJob = Job()

    companion object{
        const val NO_FILTER = 0
        const val FILTER_BY_INFORMATION = 1
        const val FILTER_BY_INGREDIENTS = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {

        }

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        mFragmentRecipesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipes, container, false)
        mFragmentRecipesBinding.fab.setOnClickListener {

            val intent = Intent(requireContext(),AddRecipeActivity::class.java)
            startActivity(intent)
        }


        val recipesRecyclerviewViewAdapater = RecipesRecyclerviewViewAdapater(arrayListOf(),this)
        mFragmentRecipesBinding.recipesRecyclerview.adapter = recipesRecyclerviewViewAdapater
        loadAll()



        return mFragmentRecipesBinding.root
    }
    private fun loadAll(){
        mRecipesFragmentViewModel.mCoroutineScope.launch {
            val recipes = mRecipesFragmentViewModel.getRecipes(requireContext())

            withContext(Main){
                val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                adapter.notifyDataSetChanged()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_all_recipe_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val searchView = menu.findItem(R.id.appBarSearch)?.actionView as SearchView
        searchView?.setOnQueryTextListener(searchViewListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        mSelectedMenuItem = item
        when (item.itemId) {

            R.id.noFilterMenu -> {
                mRecipesFragmentViewModel.mFilter = NO_FILTER
                mSelectedMenuItem?.isChecked = true

                loadAll()
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

    suspend fun filterByInformations(searchQuery:String = ""):List<RecipeEntity>{
        mRecipesFragmentViewModel.mFilter = FILTER_BY_INFORMATION
        val hasCostInput = mRecipesFragmentViewModel.mHasCostInput
        val hasServingInput = mRecipesFragmentViewModel.mHasServingInput
        val  hasHourOrMinuteInput = mRecipesFragmentViewModel.mHasHourOrMinuteInput


        if(hasCostInput && hasServingInput &&  hasHourOrMinuteInput ){
            //filter by cost, serving and total preparation and cook time
            Log.e("SEARCHING","Searching here")
            val cost =mRecipesFragmentViewModel.mCostString.toDouble()
            val serving = mRecipesFragmentViewModel.mServingString.toInt()
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

             val recipes = mRecipesFragmentViewModel.filterByCostServingAndTotalPrepAndCookTime(requireContext(),searchQuery,
                    mRecipesFragmentViewModel.mCostCondition,cost,mRecipesFragmentViewModel.mServingCondition,serving,
                    mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes)

                    return recipes


        }else if(hasCostInput && hasServingInput &&  !hasHourOrMinuteInput ){
            // filter by cost and serving
            val cost =mRecipesFragmentViewModel.mCostString.toDouble()
            val serving = mRecipesFragmentViewModel.mServingString.toInt()
            val recipes = mRecipesFragmentViewModel.filterByCostAndServing(requireContext(), mRecipesFragmentViewModel.mCostCondition,cost,mRecipesFragmentViewModel.mServingCondition,serving)

            return recipes
        }else if(hasCostInput && !hasServingInput &&  hasHourOrMinuteInput ){
            // filter by cost and total preparation + cook time

            val cost = mRecipesFragmentViewModel.mCostString.toDouble()
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)
            val recipes = mRecipesFragmentViewModel.filterByCostAndTotalPrepAndCookTime(requireContext(), mRecipesFragmentViewModel.mCostCondition,cost,mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes,)
            return recipes

        }else if(!hasCostInput && hasServingInput &&  hasHourOrMinuteInput ){
            // filter by serving and total preparation and cook time

            val serving = mRecipesFragmentViewModel.mServingString.toInt()
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

            val recipes = mRecipesFragmentViewModel.filterByServingAndTotalPrepAndCookTime(requireContext(), mRecipesFragmentViewModel.mServingCondition,serving,mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes,)

            return recipes

        }else if(hasCostInput && !hasServingInput &&  !hasHourOrMinuteInput ){
            // filter by cost
            val cost = mRecipesFragmentViewModel.mCostString.toDouble()
            val recipes = mRecipesFragmentViewModel.filterByCost(requireContext(), mRecipesFragmentViewModel.mCostCondition,cost)
            return recipes

        }else if(!hasCostInput && hasServingInput &&  !hasHourOrMinuteInput ){
            // filter by serving

            val serving = mRecipesFragmentViewModel.mServingString.toInt()
            val recipes = mRecipesFragmentViewModel.filterByServing(requireContext(), mRecipesFragmentViewModel.mServingCondition,serving)
            return recipes

        }else if(!hasCostInput && !hasServingInput &&  hasHourOrMinuteInput ){
            // filter by total preparation and cook time
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString,mRecipesFragmentViewModel.mPrepPlusCookMinutesString)
            val recipes = mRecipesFragmentViewModel.filterByTotalPrepAndCookTime(requireContext(), mRecipesFragmentViewModel.mPrepPlusCookTimeCondition,totalPrepAndCookTimeInMinutes)

            return recipes
        }
        return arrayListOf<RecipeEntity>()
    }

    fun totalPrepAndCookTimeInMinutes(prepPlusCookHourString: String, prepPlusCookMinutesString: String):Int{
        val prepAPlusCookHourInt = if(prepPlusCookHourString.isEmpty()) 0 else prepPlusCookHourString.toInt()
        val prepPlusCookMinutesString = if(prepPlusCookMinutesString.isEmpty()) 0 else prepPlusCookMinutesString.toInt()
        val minutesInHour = 60
        return (prepAPlusCookHourInt / minutesInHour)+prepPlusCookMinutesString
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

            mSelectedMenuItem?.isChecked = true

            mRecipesFragmentViewModel.mFilter = FILTER_BY_INFORMATION
            mRecipesFragmentViewModel.mHasCostInput = hasCostInput
            mRecipesFragmentViewModel.mHasServingInput = hasServingInput
            mRecipesFragmentViewModel.mHasHourOrMinuteInput = hasHourOrMinuteInput
            mRecipesFragmentViewModel.mFiltering = true

            mRecipesFragmentViewModel.mCoroutineScope.launch {
                val recipes =  filterByInformations()
                withContext(Main){
                    val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                    adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                    adapter.notifyDataSetChanged()
                }
            }


        }

    }
    private val searchViewListener = object:SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(query: String?): Boolean {
            query?.let {

                if(mSearchJob == null){
                    mSearchJob = Job()
                }else{
                    mSearchJob.cancel()
                    mSearchJob = Job()
                }

                CoroutineScope(Dispatchers.IO +mSearchJob).launch {
                    val searchResult = getSearchItems(it)
                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                        adapter.mRecipeStepEntities = searchResult as ArrayList<RecipeEntity>
                        adapter.notifyDataSetChanged()
                    }
                }


            }

            return true
        }

    }
    suspend fun getSearchItems(searchTerm:String):List<RecipeEntity>{
        if(mRecipesFragmentViewModel.mFilter == NO_FILTER){

        }else if(mRecipesFragmentViewModel.mFilter == FILTER_BY_INFORMATION){
            Log.e("TEST","SEARCHING")
            return filterByInformations(searchTerm)
        }else if(mRecipesFragmentViewModel.mFilter == FILTER_BY_INGREDIENTS){

        }

        return arrayListOf()

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

