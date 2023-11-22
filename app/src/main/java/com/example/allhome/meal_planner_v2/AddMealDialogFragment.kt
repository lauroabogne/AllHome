package com.example.allhome.meal_planner_v2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.AddMealDialogFragmentBinding
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.recipes.RecipesFragment
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class AddMealDialogFragment(private val mDate: Date, private val singleMealType:String?): DialogFragment() {

    lateinit var mMealPlannerViewModel:MealPlannerViewModel
    lateinit var mlayoutBinding: AddMealDialogFragmentBinding
    lateinit var mCurrentFragment:Fragment

    var mSelectedFragment:Fragment? = null
    val mSelectedMealTypes = arrayListOf<String>()
    var mMealKind = MealEntity.NO_KIND
    var mMealType  = MealEntity.NO_TYPE

    var mSelectedRecipeEntity:RecipeEntity? = null
    var mQuickRecipeName = ""
    var mRecipeCost = 0.0
    var mDialogDettachedListener:DialogDettachedListener? = null
    init {
        singleMealType?.let {
            mSelectedMealTypes.add(singleMealType)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMealPlannerViewModel = ViewModelProvider(this).get(MealPlannerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflater = LayoutInflater.from(requireContext())
        mlayoutBinding = DataBindingUtil.inflate(inflater, R.layout.add_meal_dialog_fragment,null,false)

        mlayoutBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        mlayoutBinding.toolbar.setNavigationOnClickListener(toolbarNavigationClickListener)

        val addMealOptionFragment = AddMealOptionFragment.newInstance("","")
        addMealOptionFragment.mAddMealOptionFragmentSelectionListener = addMealOptionFragmentSelectionListener
        mCurrentFragment = addMealOptionFragment

        loadFragment(addMealOptionFragment)

        mlayoutBinding.nextBtn.setOnClickListener(nextBtnClickListener)


        return mlayoutBinding.root
    }
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val inflater = LayoutInflater.from(requireContext())
//        mlayoutBinding = DataBindingUtil.inflate(inflater, R.layout.add_meal_dialog_fragment,null,true)
//        mlayoutBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
//        mlayoutBinding.toolbar.setNavigationOnClickListener(toolbarNavigationClickListener)
//
//
//
//        mlayoutBinding.nextBtn.setOnClickListener(nextBtnClickListener)
//
//        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
//        alertDialogBuilder.setView(mlayoutBinding.root)
//        val alertDialog = alertDialogBuilder.create()
//
//        return alertDialog
//
//    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDetach() {
        super.onDetach()
        mDialogDettachedListener?.let{
            it.onDialogDettached()
        }
    }
    fun loadFragment(fragment:Fragment){
        childFragmentManager.beginTransaction().replace(R.id.fragmentContainer,fragment).commit()
        setTitle(fragment)

    }
    fun setTitle(fragment: Fragment){
        when (fragment) {
            is AddMealOptionFragment -> {
                mlayoutBinding.toolbar.title = "Add meal"
            }
            is RecipesFragment -> {
                mlayoutBinding.toolbar.title = "Recipe"
            }
            is MealTypeFragment -> {
                mlayoutBinding.toolbar.title = "Select type"
            }
            is MealRecipeCostFragment->{
                mlayoutBinding.toolbar.title = "Select recipe"
            }
        }
    }
    fun showNextButton(){
        mlayoutBinding.nextBtn.visibility = View.VISIBLE
    }
    fun hideNextButton(){
        mlayoutBinding.nextBtn.visibility = View.GONE
    }
    private val nextBtnClickListener = object:View.OnClickListener{
        override fun onClick(v: View?) {
            if(mCurrentFragment is QuickRecipeFragment){

                val mFragmentQuickRecipeBinding = (mCurrentFragment as QuickRecipeFragment).mFragmentQuickRecipeBinding
                val quickRecipe = mFragmentQuickRecipeBinding.quickRecipeTextInput.text.toString().trim()
                val quickrecipeCostString = mFragmentQuickRecipeBinding.quickRecipeCostTextInput.text.toString().trim()
                val quickrecipeCostDouble:Double = if(quickrecipeCostString.isEmpty()) 0.0 else quickrecipeCostString.toDouble()

                if(quickRecipe.isEmpty()){
                    Toast.makeText(requireContext(),"Please enter quick recipe name.",Toast.LENGTH_SHORT).show()
                    return
                }

                mQuickRecipeName = quickRecipe
                mRecipeCost = quickrecipeCostDouble


                val mealTypeFragment = MealTypeFragment.newInstance(mSelectedMealTypes.toTypedArray())
                mealTypeFragment.setOnCheckedChangeListener(mealTypeFragmentOnCheckedChangeListener)
                mCurrentFragment = mealTypeFragment
                loadFragment(mealTypeFragment)
            }else if(mCurrentFragment is MealTypeFragment){

                if(mSelectedMealTypes.isEmpty()){
                    Toast.makeText(requireContext(),"Please select meal type.",Toast.LENGTH_SHORT).show()
                    return
                }

                try{
                    saveData()
                }catch (e:Exception){
                    Log.e("ex",e.toString())
                }

            }else if(mCurrentFragment is MealRecipeCostFragment){

                mRecipeCost =  (mCurrentFragment as MealRecipeCostFragment).getCost()

                val mealTypeFragment = MealTypeFragment.newInstance(mSelectedMealTypes.toTypedArray())
                mealTypeFragment.mOnCheckedChangeListener = mealTypeFragmentOnCheckedChangeListener
                mCurrentFragment = mealTypeFragment
                mlayoutBinding.toolbar.menu.clear()

                mlayoutBinding.nextBtn.text = "Save"
                loadFragment(mealTypeFragment)




            }
        }

    }
    private fun convertStringMealTypeStringToInt(mealTypeString:String):Int{


        if(mealTypeString == requireContext().getString(R.string.breakfast)){
            return MealEntity.BREAKFAST_TYPE
        }else if(mealTypeString == requireContext().getString(R.string.snack_after_breakfast)){
            return MealEntity.SNACK_AFTER_BREAKFAST_TYPE
        }else if(mealTypeString == requireContext().getString(R.string.lunch)){
            return MealEntity.LUNCK_TYPE
        }else if(mealTypeString == requireContext().getString(R.string.snack_after_lunch)){
            return MealEntity.SNACK_AFTERLUNCK_TYPE
        }else if(mealTypeString == requireContext().getString(R.string.dinner)){
            return MealEntity.DINNER_TYPE
        }else if(mealTypeString == requireContext().getString(R.string.snack_after_dinner)){
            return MealEntity.SNACK_AFTER_DINNER_TYPE
        }

        return MealEntity.BREAKFAST_TYPE
    }

    fun saveData(){

        val selectedDate = SimpleDateFormat("yyyy-MM-dd").format(mDate)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


        if(mMealKind == MealEntity.RECIPE_KIND){

            mMealPlannerViewModel.mCoroutineScope.launch {

                mSelectedMealTypes.forEach {mealTypeString->
                    mSelectedRecipeEntity?.let {recipe->

                        val uniqueId = UUID.randomUUID().toString()
                        val mealEntity = MealEntity(
                            uniqueId,
                            recipe.uniqueId,
                            recipe.name,
                            selectedDate,
                            convertStringMealTypeStringToInt(mealTypeString),
                            MealEntity.RECIPE_KIND,
                            //recipe.estimatedCost,
                            mRecipeCost,
                            MealEntity.NOT_UPLOADED,
                            MealEntity.NOT_DELETED,
                            currentDatetime,
                            currentDatetime
                        )
                        val id = mMealPlannerViewModel.saveMealPlan(requireContext(),mealEntity)

                    }
                }
                withContext(Main){
                    Toast.makeText(requireContext(),"Meal plan saved successfully.",Toast.LENGTH_SHORT).show()
                    this@AddMealDialogFragment.dismiss()
                }
            }
        }else if(mMealKind == MealEntity.QUICK_RECIPE_KIND){

            mMealPlannerViewModel.mCoroutineScope.launch {
                mSelectedMealTypes.forEach {mealTypeString->
                    val uniqueId = UUID.randomUUID().toString()
                    val mealEntity = MealEntity(
                        uniqueId,
                        "",
                        mQuickRecipeName,
                        selectedDate,
                        convertStringMealTypeStringToInt(mealTypeString),
                        MealEntity.QUICK_RECIPE_KIND,
                        mRecipeCost,
                        MealEntity.NOT_UPLOADED,
                        MealEntity.NOT_DELETED,
                        currentDatetime,
                        currentDatetime
                    )
                    val id = mMealPlannerViewModel.saveMealPlan(requireContext(),mealEntity)



                }
                withContext(Main){
                    Toast.makeText(requireContext(),"Meal plan saved successfully.",Toast.LENGTH_SHORT).show()
                    this@AddMealDialogFragment.dismiss()
                }
            }
        }
    }
    private val toolbarNavigationClickListener= View.OnClickListener {
        mlayoutBinding.toolbar.menu.clear()

        if(mCurrentFragment is AddMealOptionFragment){
            this@AddMealDialogFragment.dismiss()
        }else if(mCurrentFragment is RecipesFragment || mCurrentFragment is QuickRecipeFragment){

            val addMealOptionFragment = AddMealOptionFragment.newInstance("","")
            addMealOptionFragment.mAddMealOptionFragmentSelectionListener = addMealOptionFragmentSelectionListener
            mCurrentFragment = addMealOptionFragment
            loadFragment(addMealOptionFragment)
            hideNextButton()

        }else if(mCurrentFragment is MealTypeFragment){



            if(mMealKind == MealEntity.QUICK_RECIPE_KIND){

                val quickRecipeFragment = QuickRecipeFragment.newInstance(mQuickRecipeName,mRecipeCost)
                mCurrentFragment = quickRecipeFragment
                loadFragment(quickRecipeFragment)
                showNextButton()

            }else{
                val recipeFragment = RecipesFragment(RecipesFragment.ADDING_MEAL_VIEWING,recipeSelectedListener)
                recipeFragment.setUpToolbar(mlayoutBinding.toolbar)
                mCurrentFragment = recipeFragment
                loadFragment(recipeFragment)
                hideNextButton()

            }
        }else if(mCurrentFragment is MealRecipeCostFragment){
            val recipeFragment = RecipesFragment(RecipesFragment.ADDING_MEAL_VIEWING,recipeSelectedListener)
            recipeFragment.setUpToolbar(mlayoutBinding.toolbar)
            mCurrentFragment = recipeFragment
            loadFragment(recipeFragment)
            hideNextButton()

        }
    }
    private val addMealOptionFragmentSelectionListener =  object:AddMealOptionFragmentSelectionListener{
        override fun onSelect(viewId: Int) {
            when(viewId){
                R.id.recipeButton->{

                    mMealKind = MealEntity.RECIPE_KIND
                    val recipeFragment = RecipesFragment(RecipesFragment.ADDING_MEAL_VIEWING,recipeSelectedListener)
                    recipeFragment.setUpToolbar(mlayoutBinding.toolbar)

                    mCurrentFragment = recipeFragment
                    loadFragment(recipeFragment)
                    hideNextButton()
                }
                R.id.quickRecipeButton->{

                    mMealKind = MealEntity.QUICK_RECIPE_KIND
                    val quickRecipeFragment = QuickRecipeFragment.newInstance(mQuickRecipeName,mRecipeCost)
                    mCurrentFragment = quickRecipeFragment
                    loadFragment(quickRecipeFragment)
                    showNextButton()
                }
            }


        }
    }
    val recipeSelectedListener = object :RecipeSelectedListener{
        override fun onSelect(recipe: RecipeEntity) {

            mSelectedRecipeEntity = recipe

//            val mealTypeFragment = MealTypeFragment.newInstance(mSelectedMealTypes.toTypedArray())
//            mealTypeFragment.mOnCheckedChangeListener = mealTypeFragmentOnCheckedChangeListener
//            mCurrentFragment = mealTypeFragment
//
//            mlayoutBinding.toolbar.menu.clear()
//
//            loadFragment(mealTypeFragment)


            val mealRecipeCostFragment = MealRecipeCostFragment.newInstance(recipe.name)
            mCurrentFragment = mealRecipeCostFragment
            loadFragment(mealRecipeCostFragment)
            mlayoutBinding.toolbar.menu.clear()

            mlayoutBinding.nextBtn.text = "Next"
            showNextButton()

        }

    }
    val mealTypeFragmentOnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        buttonView?.let{
            val text = it.text.toString()
            if(isChecked){
                mSelectedMealTypes.add(text)
            }else{
                mSelectedMealTypes.remove(text)
            }
        }
    }

    interface AddMealOptionFragmentSelectionListener{
        fun onSelect(viewId:Int)
    }
    interface RecipeSelectedListener{
        fun onSelect(recipe:RecipeEntity)
    }
    interface DialogDettachedListener{
        fun onDialogDettached()
    }
}