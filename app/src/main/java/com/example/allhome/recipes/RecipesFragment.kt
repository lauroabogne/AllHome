package com.example.allhome.recipes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.allhome.R
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.AppSettingEntity
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeEntityWithTotalIngredient
import com.example.allhome.databinding.FragmentRecipesBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.databinding.RecipeItemGridBinding
import com.example.allhome.meal_planner.AddMealDialogFragment
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecipesFragment(val action:Int = NORMAL_RECIPE_VIEWING, val recipeSelectedListener: AddMealDialogFragment.RecipeSelectedListener? = null) : Fragment() {

    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    private lateinit var mFragmentRecipesBinding: FragmentRecipesBinding
    var mSelectedMenuItem:MenuItem? = null
    private var mSearchView:SearchView? = null

    var mAction = action
    var mSearchJob = Job()




    companion object{
        const val NO_FILTER = 0
        const val FILTER_BY_INFORMATION = 1
        const val FILTER_BY_INGREDIENTS = 2
        const val NORMAL_RECIPE_VIEWING = 1
        const val ADDING_MEAL_VIEWING = 2
    }

    private val openRecipeContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){
            val activityResultAction = activityResult.data?.getIntExtra(AddRecipeActivity.ACTION_TAG,0)
            if(activityResultAction == AddRecipeActivity.EDIT_ACTION){
                activityResult.data?.let {
                    it.getParcelableExtra<RecipeEntity>(ViewRecipeFragment.RECIPE_INTENT_TAG)?.let{recipeEntity->

                        if(mFragmentRecipesBinding.recipesRecyclerview.adapter is RecipesRecyclerviewViewAdapter){

                            val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapter
                            val recipeEntitiesWithTotalIngredient = adapter.mRecipeStepEntities

                            mRecipesFragmentViewModel.mCoroutineScope.launch {
                                val recipeEntityWithTotalIngredient = mRecipesFragmentViewModel.getRecipe(requireContext(),recipeEntity.uniqueId)
                                val index = recipeEntitiesWithTotalIngredient.indexOfFirst {
                                    it.recipeEntity.uniqueId == recipeEntityWithTotalIngredient.recipeEntity.uniqueId
                                }
                                if(index >=0){
                                    recipeEntitiesWithTotalIngredient.set(index,recipeEntityWithTotalIngredient)
                                }
                                withContext(Main){
                                    adapter.notifyItemChanged(index)
                                }

                            }
                        }else{

                            val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewGridAdapter
                            val recipeEntitiesWithTotalIngredient = adapter.mRecipeStepEntities

                            mRecipesFragmentViewModel.mCoroutineScope.launch {
                                val recipeEntityWithTotalIngredient = mRecipesFragmentViewModel.getRecipe(requireContext(),recipeEntity.uniqueId)
                                val index = recipeEntitiesWithTotalIngredient.indexOfFirst {
                                    it.recipeEntity.uniqueId == recipeEntityWithTotalIngredient.recipeEntity.uniqueId
                                }
                                if(index >=0){
                                    recipeEntitiesWithTotalIngredient.set(index,recipeEntityWithTotalIngredient)
                                }
                                withContext(Main){
                                    adapter.notifyItemChanged(index)
                                }

                            }

                        }

                    }

                }
            }else{
                val searchQuery = mSearchView?.query.toString()
                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val searchResult = getSearchItems(searchQuery)
                    withContext(Main){
                        setUpRecyclierView(searchResult)

                        mFragmentRecipesBinding.swipeRefresh.isRefreshing = false
                    }
                }
            }


        }
    }

    private val addRecipeContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){

            val searchQuery = mSearchView?.query.toString()
            mRecipesFragmentViewModel.mCoroutineScope.launch {
                val searchResult = getSearchItems(searchQuery)
                withContext(Main){
                    setUpRecyclierView(searchResult)
                    mFragmentRecipesBinding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }
    private val webBrowseRecipeContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){

            val searchQuery = mSearchView?.query.toString()
            mRecipesFragmentViewModel.mCoroutineScope.launch {
                val searchResult = getSearchItems(searchQuery)
                withContext(Main){
                    setUpRecyclierView(searchResult)
                    mFragmentRecipesBinding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().title = "Recipe"
        arguments?.let {

        }



        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        mFragmentRecipesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipes, container, false)

        mRecipesFragmentViewModel.mCoroutineScope.launch {
            // initialized recipe viewing
            mRecipesFragmentViewModel.getRecipeViewingSetting(requireContext())

            val searchResult = getSearchItems()
            withContext(Main){
                setUpRecyclierView(searchResult)
                setRecipeViewingIcon(mRecipesFragmentViewModel.mRecipeViewing,mFragmentRecipesBinding.toggleViewMoreButton)
            }
        }

        mFragmentRecipesBinding.addRecipeFab.setOnClickListener {

            val intent = Intent(requireContext(),AddRecipeActivity::class.java)
            addRecipeContract.launch(intent)

        }



        mFragmentRecipesBinding.swipeRefresh.setOnRefreshListener {
            val searchQuery = mSearchView?.query.toString()
            mRecipesFragmentViewModel.mCoroutineScope.launch {
                val searchResult = getSearchItems(searchQuery)
                withContext(Main){
                    setUpRecyclierView(searchResult)

                    mFragmentRecipesBinding.swipeRefresh.isRefreshing = false
                }
            }
        }

        if(action == ADDING_MEAL_VIEWING){
            mFragmentRecipesBinding.addRecipeFab.visibility = View.GONE
        }

        mFragmentRecipesBinding.toggleViewMoreButton.setOnClickListener {


            val searchQuery = mSearchView?.query.toString()
            val recipeViewing = if(mRecipesFragmentViewModel.mRecipeViewing == AppSettingEntity.RECIPE_LIST_VIEWING)  AppSettingEntity.RECIPE_GRID_VIEWING else AppSettingEntity.RECIPE_LIST_VIEWING

            mRecipesFragmentViewModel.mCoroutineScope.launch {

                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val currentDatetime: String = simpleDateFormat.format(Date())

                val appSettingEntity = mRecipesFragmentViewModel.getRecipeViewingSettingEntity(requireContext())

                appSettingEntity?.let {
                    //update
                    mRecipesFragmentViewModel.updateRecipeViewingAppSetting(requireContext(),recipeViewing,currentDatetime)

                }?:run{
                    //insert
                    var autoGeneratedUniqueId = UUID.randomUUID().toString()
                    val appSettingEntity = AppSettingEntity(
                        autoGeneratedUniqueId,
                        AppSettingEntity.RECIPE_COLUMN_NAME,
                        recipeViewing,
                        AppSettingEntity.NOT_DELETED_STATUS,
                        AppSettingEntity.NOT_UPLOADED,
                        currentDatetime,
                        currentDatetime
                    )
                    mRecipesFragmentViewModel.setRecipeViewingAppSetting(requireContext(),appSettingEntity)
                }
                mRecipesFragmentViewModel.getRecipeViewingSetting(requireContext())
                val searchResult = getSearchItems(searchQuery)
                withContext(Main){

                    setRecipeViewingIcon(recipeViewing,it)
                    setUpRecyclierView(searchResult)
                }
            }

        }

        mFragmentRecipesBinding.categoryFab.setOnClickListener {
            val recipeCategoryDialogFragment  = RecipeCategoryDialogFragment()
            recipeCategoryDialogFragment.setCategoryOnSelectListener(object:RecipeCategoryDialogFragment.OnSelectCategoryListener{
                override fun selected(recipeCategory: RecipeCategoryEntity) {
                    recipeCategoryDialogFragment.dismiss()
                    mRecipesFragmentViewModel.mSelectedCategory = recipeCategory
                    mFragmentRecipesBinding.searchIndicatorTextView.text = recipeCategory.name
                    //reset as no filter
                    mRecipesFragmentViewModel.mFilter = NO_FILTER
                    mSelectedMenuItem?.isChecked = true
                    mRecipesFragmentViewModel.mCoroutineScope.launch {

                        val searchResult = getSearchItems("")
                        withContext(Main){
                            setUpRecyclierView(searchResult)
                            mFragmentRecipesBinding.swipeRefresh.isRefreshing = false
                        }
                    }
                }
            })
            recipeCategoryDialogFragment.show(requireActivity().supportFragmentManager,"RecipeCategoryDialogFragment")
        }
        return mFragmentRecipesBinding.root
    }
    fun setRecipeViewingIcon(recipeViewing:String,extendedFloatingActionButton:View){
        if(recipeViewing == AppSettingEntity.RECIPE_LIST_VIEWING){
            (extendedFloatingActionButton as ExtendedFloatingActionButton).setIconResource(R.drawable.ic_baseline_grid_on_24)
        }else{

            (extendedFloatingActionButton as ExtendedFloatingActionButton).setIconResource(R.drawable.ic_baseline_view_list_24)
        }
    }
    fun setUpRecyclierView(recipeEntityWithTotalIngredients:List<RecipeEntityWithTotalIngredient>){
        val recipeViewing = mRecipesFragmentViewModel.mRecipeViewing

        if(recipeViewing == AppSettingEntity.RECIPE_GRID_VIEWING){
            val recipesRecyclerviewViewAdapter = RecipesRecyclerviewViewGridAdapter(arrayListOf(),this@RecipesFragment)
            mFragmentRecipesBinding.recipesRecyclerview.adapter = recipesRecyclerviewViewAdapter
            mFragmentRecipesBinding.recipesRecyclerview.layoutManager  = GridLayoutManager(requireContext(),2)
        }else{
            val recipesRecyclerviewViewAdapter = RecipesRecyclerviewViewAdapter(arrayListOf(),this@RecipesFragment)
            mFragmentRecipesBinding.recipesRecyclerview.adapter = recipesRecyclerviewViewAdapter
            mFragmentRecipesBinding.recipesRecyclerview.layoutManager  = LinearLayoutManager(requireContext())

        }

        if(mFragmentRecipesBinding.recipesRecyclerview.adapter is RecipesRecyclerviewViewAdapter){
            val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapter
            adapter.mRecipeStepEntities = recipeEntityWithTotalIngredients as ArrayList<RecipeEntityWithTotalIngredient>
            adapter.notifyDataSetChanged()

        }else{

            val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewGridAdapter
            adapter.mRecipeStepEntities = recipeEntityWithTotalIngredients as ArrayList<RecipeEntityWithTotalIngredient>
            adapter.notifyDataSetChanged()
        }

        if(recipeEntityWithTotalIngredients.isEmpty()){
            mFragmentRecipesBinding.noRecipeTextView.visibility = View.VISIBLE
        }else{
            mFragmentRecipesBinding.noRecipeTextView.visibility = View.GONE
        }
    }

    fun setUpToolbar(toolbar: Toolbar){

        toolbar.title = "Recipe"
        toolbar.inflateMenu(R.menu.view_all_recipe_menu)
        mSearchView = toolbar.menu.findItem(R.id.appBarSearch)?.actionView as SearchView
        mSearchView?.setOnQueryTextListener(searchViewListener)

        toolbar.setOnMenuItemClickListener(toolbarMenuItemClickListener)

    }

    private val toolbarMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        mSelectedMenuItem = item
        when (item?.itemId) {

            R.id.noFilterMenu -> {
                mRecipesFragmentViewModel.mFilter = NO_FILTER
                mSelectedMenuItem?.isChecked = true

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val searchResult = getSearchItems()

                    withContext(Main){
                        val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapter
                        adapter.mRecipeStepEntities = searchResult as ArrayList<RecipeEntityWithTotalIngredient>
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            R.id.recipeInformationFilterMenu -> {
                val filterByInformationDialogFragment  = FilterByInformationDialogFragment(mRecipesFragmentViewModel)
                filterByInformationDialogFragment.isCancelable = false
                filterByInformationDialogFragment.setRecipeInformationFilterListener(recipeInformationFilterListener)
                filterByInformationDialogFragment.show(requireActivity().supportFragmentManager,"IngredientDialogFragment")
            }
            R.id.recipeIngredientFilterMenu -> {
                val filterByIngredientsDialogFragment  =  FilterByIngredientsDialogFragment(mRecipesFragmentViewModel)
                filterByIngredientsDialogFragment.isCancelable = false
                filterByIngredientsDialogFragment.setRecipeIngredientFilterListener(recipeIngredientFilterListener)
                filterByIngredientsDialogFragment.show(requireActivity().supportFragmentManager,"FilterByIngredientsDialogFragment")
            }

        }
        true
    }
    private suspend fun loadAll(searchTerm: String): List<RecipeEntityWithTotalIngredient> {

        if(mRecipesFragmentViewModel.mSelectedCategory != null ){
            val categoryUniqueId = mRecipesFragmentViewModel.mSelectedCategory!!.uniqueId
            if(categoryUniqueId != "1"){
                val queryString = mRecipesFragmentViewModel.createQueryForAllProductWithCategorySelected()
                val simpleSqliteQuery = SimpleSQLiteQuery(queryString,arrayOf("%${searchTerm}%",categoryUniqueId))
                return AllHomeDatabase.getDatabase(requireContext()).getRecipeDAO().getRecipes(simpleSqliteQuery)
            }
        }

        return mRecipesFragmentViewModel.getRecipes(requireContext(), searchTerm)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.view_all_recipe_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        mSearchView = menu.findItem(R.id.appBarSearch)?.actionView as SearchView
        mSearchView?.setOnQueryTextListener(searchViewListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        mSelectedMenuItem = item
        when (item.itemId) {

            R.id.noFilterMenu -> {
                mRecipesFragmentViewModel.mFilter = NO_FILTER
                mSelectedMenuItem?.isChecked = true

                mRecipesFragmentViewModel.mCoroutineScope.launch {
                    val searchResult = getSearchItems()

                    withContext(Main){
                        setUpRecyclierView(searchResult)

                    }
                }
            }
            R.id.recipeInformationFilterMenu -> {
                val filterByInformationDialogFragment  = FilterByInformationDialogFragment(mRecipesFragmentViewModel)
                filterByInformationDialogFragment.isCancelable = false
                filterByInformationDialogFragment.setRecipeInformationFilterListener(recipeInformationFilterListener)
                filterByInformationDialogFragment.show(requireActivity().supportFragmentManager,"IngredientDialogFragment")
            }
            R.id.recipeIngredientFilterMenu -> {
                val filterByIngredientsDialogFragment  =  FilterByIngredientsDialogFragment(mRecipesFragmentViewModel)
                filterByIngredientsDialogFragment.isCancelable = false
                filterByIngredientsDialogFragment.setRecipeIngredientFilterListener(recipeIngredientFilterListener)
                filterByIngredientsDialogFragment.show(requireActivity().supportFragmentManager,"FilterByIngredientsDialogFragment")
            }
            R.id.browseRecipe->{

                val webBrowseRecipeActivity = Intent(view?.context, WebBrowseRecipeActivity::class.java)
                webBrowseRecipeContract.launch(webBrowseRecipeActivity)
            }

        }


        return true
    }

    suspend fun filterByInformations(searchQuery:String = ""):List<RecipeEntityWithTotalIngredient>{
        mRecipesFragmentViewModel.mFilter = FILTER_BY_INFORMATION
        val hasCostInput = mRecipesFragmentViewModel.mHasCostInput
        val hasServingInput = mRecipesFragmentViewModel.mHasServingInput
        val  hasHourOrMinuteInput = mRecipesFragmentViewModel.mHasHourOrMinuteInput
        val allRecipeCategoryUniqueId = "1"

        if(hasCostInput && hasServingInput &&  hasHourOrMinuteInput ) {
            //filter by cost, serving and total preparation and cook time
            val cost = mRecipesFragmentViewModel.mCostString.toDouble()
            val serving = mRecipesFragmentViewModel.mServingString.toInt()
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString, mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

            mRecipesFragmentViewModel.mSelectedCategory?.let {

                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByCostServingAndTotalPrepAndCookTimeWithCategory(
                        requireContext(), searchQuery,
                        mRecipesFragmentViewModel.mCostCondition, cost, mRecipesFragmentViewModel.mServingCondition, serving,
                        mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes,categoryUniqueId
                    )
                }
            }

            return mRecipesFragmentViewModel.filterByCostServingAndTotalPrepAndCookTime(
                requireContext(), searchQuery,
                mRecipesFragmentViewModel.mCostCondition, cost, mRecipesFragmentViewModel.mServingCondition, serving,
                mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes
            )

        }else if(hasCostInput && hasServingInput &&  !hasHourOrMinuteInput ) {
            // filter by cost and serving
            val cost = mRecipesFragmentViewModel.mCostString.toDouble()
            val serving = mRecipesFragmentViewModel.mServingString.toInt()

            mRecipesFragmentViewModel.mSelectedCategory?.let {

                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByCostAndServingWithCategorySelected(requireContext(), searchQuery, mRecipesFragmentViewModel.mCostCondition, cost, mRecipesFragmentViewModel.mServingCondition, serving, categoryUniqueId)
                }
            }

            return mRecipesFragmentViewModel.filterByCostAndServing(requireContext(), searchQuery, mRecipesFragmentViewModel.mCostCondition, cost, mRecipesFragmentViewModel.mServingCondition, serving)
        }else if(hasCostInput && !hasServingInput &&  hasHourOrMinuteInput ) {
            // filter by cost and total preparation + cook time

            val cost = mRecipesFragmentViewModel.mCostString.toDouble()
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString, mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

            mRecipesFragmentViewModel.mSelectedCategory?.let {

                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByCostAndTotalPrepAndCookTimeWithCategorySelected(requireContext(), searchQuery, mRecipesFragmentViewModel.mCostCondition, cost, mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes, categoryUniqueId)
                }
            }

            return mRecipesFragmentViewModel.filterByCostAndTotalPrepAndCookTime(requireContext(), searchQuery, mRecipesFragmentViewModel.mCostCondition, cost, mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes)

        }else if(!hasCostInput && hasServingInput &&  hasHourOrMinuteInput ) {
            // filter by serving and total preparation and cook time

            val serving = mRecipesFragmentViewModel.mServingString.toInt()
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString, mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

            mRecipesFragmentViewModel.mSelectedCategory?.let {

                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByServingAndTotalPrepAndCookTimeWithCategorySelected(requireContext(), searchQuery, mRecipesFragmentViewModel.mServingCondition, serving, mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes, categoryUniqueId)
                }
            }

            return mRecipesFragmentViewModel.filterByServingAndTotalPrepAndCookTime(requireContext(), searchQuery, mRecipesFragmentViewModel.mServingCondition, serving, mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes,)

        }else if(hasCostInput && !hasServingInput &&  !hasHourOrMinuteInput ) {
            // filter by cost
            val cost = mRecipesFragmentViewModel.mCostString.toDouble()
            mRecipesFragmentViewModel.mSelectedCategory?.let {


                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByCostAndCategory(requireContext(), searchQuery, mRecipesFragmentViewModel.mCostCondition, cost, categoryUniqueId)
                }
            }
            return mRecipesFragmentViewModel.filterByCost(requireContext(), searchQuery, mRecipesFragmentViewModel.mCostCondition, cost)

        }else if(!hasCostInput && hasServingInput &&  !hasHourOrMinuteInput ) {
            // filter by serving

            val serving = mRecipesFragmentViewModel.mServingString.toInt()

            mRecipesFragmentViewModel.mSelectedCategory?.let {

                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByServingWithCategorySelected(requireContext(), searchQuery, mRecipesFragmentViewModel.mServingCondition, serving, categoryUniqueId)
                }
            }
            return mRecipesFragmentViewModel.filterByServing(requireContext(), searchQuery, mRecipesFragmentViewModel.mServingCondition, serving)

        }else if(!hasCostInput && !hasServingInput &&  hasHourOrMinuteInput ) {
            // filter by total preparation and cook time
            val totalPrepAndCookTimeInMinutes = totalPrepAndCookTimeInMinutes(mRecipesFragmentViewModel.mPrepPlusCookHourString, mRecipesFragmentViewModel.mPrepPlusCookMinutesString)

            mRecipesFragmentViewModel.mSelectedCategory?.let {

                val categoryUniqueId = it.uniqueId;
                if (categoryUniqueId != allRecipeCategoryUniqueId) {
                    return mRecipesFragmentViewModel.filterByTotalPrepAndCookTimeWithCategorySelected(requireContext(), searchQuery, mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes, categoryUniqueId)
                }
            }

            return mRecipesFragmentViewModel.filterByTotalPrepAndCookTime(requireContext(), searchQuery, mRecipesFragmentViewModel.mPrepPlusCookTimeCondition, totalPrepAndCookTimeInMinutes)
        }
        return arrayListOf()
    }

    suspend fun filterByRecipeIngredients(searchTerm: String = "",ingredients:List<String>):List<RecipeEntityWithTotalIngredient>{

        val allRecipeCategoryUniqueId = "1"
        mRecipesFragmentViewModel.mSelectedCategory?.let {

            val categoryUniqueId = it.uniqueId;
            if (categoryUniqueId != allRecipeCategoryUniqueId) {
                return mRecipesFragmentViewModel.getRecipesByIngredientsWithCategorySelected(requireContext(),searchTerm,ingredients,categoryUniqueId)
            }
        }

        return mRecipesFragmentViewModel.getRecipesByIngredients(requireContext(),searchTerm,ingredients)
    }
    private fun totalPrepAndCookTimeInMinutes(prepPlusCookHourString: String, prepPlusCookMinutesString: String):Int{
        val prepAPlusCookHourInt = if(prepPlusCookHourString.isEmpty()) 0 else prepPlusCookHourString.toInt()
        val prepPlusCookMinutesString: Int = if(prepPlusCookMinutesString.isEmpty()) 0 else prepPlusCookMinutesString.toInt()
        val minutesInHour = 60
        return (prepAPlusCookHourInt * minutesInHour)+prepPlusCookMinutesString
    }
    private val  recipeInformationFilterListener = object:RecipeInformationFilterListener{
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

                    setUpRecyclierView(recipes)

                }
            }


        }

    }
    private val recipeIngredientFilterListener = object:RecipeIngredientFilterListener{
        override fun onIngredientFilterSet(ingredients: ArrayList<String>) {

            mRecipesFragmentViewModel.mFilter = FILTER_BY_INGREDIENTS
            mSelectedMenuItem?.isChecked = true
            mRecipesFragmentViewModel.mCoroutineScope.launch {
                val searchResult = getSearchItems("")


                withContext(Main){
                    setUpRecyclierView(searchResult)

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
                        setUpRecyclierView(searchResult)
                    }
                }
            }
            return true
        }
    }
    suspend fun getSearchItems(searchTerm:String = ""):List<RecipeEntityWithTotalIngredient>{
        if(mRecipesFragmentViewModel.mFilter == NO_FILTER){
            return loadAll(searchTerm)

        }else if(mRecipesFragmentViewModel.mFilter == FILTER_BY_INFORMATION){

            return filterByInformations(searchTerm)
        }else if(mRecipesFragmentViewModel.mFilter == FILTER_BY_INGREDIENTS){

            return filterByRecipeIngredients(searchTerm,mRecipesFragmentViewModel.mFilterIngredients)
        }

        return arrayListOf()

    }
    interface RecipeInformationFilterListener{
        fun filterConditions(costCondition:String,servingCondition:String,prepPlusCookTimeSelectedCondition:String)
        fun filters(costString:String,servingString:String,prepPlusCookHourString:String,prepPlusCookMinutesString:String)
        fun onFilterSet(hasCostInput:Boolean,hasServingInput:Boolean,hasHourOrMinuteInput:Boolean)
    }

    interface RecipeIngredientFilterListener{
        fun onIngredientFilterSet(ingredients:ArrayList<String>)
    }

    class RecipesRecyclerviewViewAdapter(var mRecipeStepEntities:ArrayList<RecipeEntityWithTotalIngredient>, val mRecipesFragment: RecipesFragment):
        RecyclerView.Adapter<RecipesRecyclerviewViewAdapter.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val recipeItemBinding = RecipeItemBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(recipeItemBinding, this)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val recipeEntity = mRecipeStepEntities[position]
            holder.recipeItemBinding.recipeEntityWithTotalIngredient = recipeEntity
            holder.recipeItemBinding.executePendingBindings()

        }
        override fun getItemCount(): Int {

            return mRecipeStepEntities.size
        }

        inner class  ItemViewHolder(var recipeItemBinding: RecipeItemBinding, val recipesRecyclerviewViewAdapater: RecipesRecyclerviewViewAdapter): RecyclerView.ViewHolder(recipeItemBinding.root),View.OnClickListener{

            init {
                recipeItemBinding.root.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                val recipeEntity = recipesRecyclerviewViewAdapater.mRecipeStepEntities[adapterPosition]

                if(mRecipesFragment.mAction == ADDING_MEAL_VIEWING){
                    mRecipesFragment.recipeSelectedListener?.onSelect(recipeEntity.recipeEntity)
                    return
                }
                val viewRecipeActivity = Intent(view?.context, ViewRecipeActivity::class.java)
                viewRecipeActivity.putExtra(ViewRecipeFragment.RECIPE_INTENT_TAG,recipeEntity.recipeEntity)
                mRecipesFragment.openRecipeContract.launch(viewRecipeActivity)
            }
        }
    }

    class RecipesRecyclerviewViewGridAdapter(var mRecipeStepEntities:ArrayList<RecipeEntityWithTotalIngredient>, val mRecipesFragment: RecipesFragment):
        RecyclerView.Adapter<RecipesRecyclerviewViewGridAdapter.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val recipeItemBinding = RecipeItemGridBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(recipeItemBinding, this)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val recipeEntity = mRecipeStepEntities[position]
            holder.recipeItemBinding.recipeEntityWithTotalIngredient = recipeEntity
            holder.recipeItemBinding.executePendingBindings()

        }
        override fun getItemCount(): Int {

            return mRecipeStepEntities.size
        }

        inner class  ItemViewHolder(var recipeItemBinding: RecipeItemGridBinding, val recipesRecyclerviewViewAdapater: RecipesRecyclerviewViewGridAdapter): RecyclerView.ViewHolder(recipeItemBinding.root),View.OnClickListener{

            init {
                recipeItemBinding.root.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                val recipeEntity = recipesRecyclerviewViewAdapater.mRecipeStepEntities[adapterPosition]

                if(mRecipesFragment.mAction == ADDING_MEAL_VIEWING){
                    mRecipesFragment.recipeSelectedListener?.onSelect(recipeEntity.recipeEntity)
                    return
                }
                val viewRecipeActivity = Intent(view?.context, ViewRecipeActivity::class.java)
                viewRecipeActivity.putExtra(ViewRecipeFragment.RECIPE_INTENT_TAG,recipeEntity.recipeEntity)
                mRecipesFragment.openRecipeContract.launch(viewRecipeActivity)
            }
        }
    }
}

