package com.example.allhome.recipes

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.allhome.R
import com.example.allhome.bill.BillsFragment
import com.example.allhome.data.entities.*
import com.example.allhome.databinding.ActivityAddRecipeBinding
import com.example.allhome.recipes.viewmodel.AddRecipeActivityViewModel
import com.example.allhome.recipes.viewmodel.AddRecipeInformationFragmentViewModel
import com.example.allhome.utils.ImageUtil
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddRecipeActivity : AppCompatActivity() {


    lateinit var mAddRecipeActivityViewModel:AddRecipeActivityViewModel
    lateinit var mActivityAddRecipeBinding:ActivityAddRecipeBinding
    val mAddRecipeInformationFragment = AddRecipeInformationFragment()
    val mAddRecipeIngredientsFragment = AddRecipeIngredientsFragment()
    val mAddRecipeStepsFragment = AddRecipeStepsFragment()


    val mFragmentList = arrayListOf<Fragment>()
    var mAction = ADD_ACTION


    companion object{
        const val TAG = "AddRecipeActivity"
        const val ADD_ACTION = 0
        const val EDIT_ACTION = 1
        const val ADD_FROM_BROWSER_ACTION = 2
        const val ACTION_TAG = "ACTION_TAG"
        const val RECIPE_TAG = "RECIPE_TAG"
        const val INGREDIENTS_TAG = "INGREDIENTS_TAG"
        const val STEPS_TAG = "STEPS_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Create recipe"
        mAddRecipeActivityViewModel = ViewModelProvider(this).get(AddRecipeActivityViewModel::class.java)
        mActivityAddRecipeBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe)

       mAction = intent.getIntExtra(ACTION_TAG, ADD_ACTION)

        if(mAction == EDIT_ACTION){

            title = "Editing recipe"
            intent.getParcelableExtra<RecipeEntity>(RECIPE_TAG)?.let {

                mFragmentList.add( AddRecipeInformationFragment.newInstanceForEditing(it))
                mFragmentList.add( AddRecipeIngredientsFragment.newInstanceForEditing(it))
                mFragmentList.add( AddRecipeStepsFragment.newInstanceForEditing(it))
            }


        }else if(mAction == ADD_FROM_BROWSER_ACTION){
            intent.getParcelableExtra<RecipeEntity>(RECIPE_TAG)?.let {

                mFragmentList.add( AddRecipeInformationFragment.newInstanceForAddingRecipeFromBrowser(it))


            }
            intent.getParcelableArrayListExtra<IngredientEntity>(INGREDIENTS_TAG)?.let{

                mFragmentList.add( AddRecipeIngredientsFragment.newInstanceForAddingRecipeFromBrowser(it))
            }
            intent.getParcelableArrayListExtra<RecipeStepEntity>(STEPS_TAG)?.let{
                mFragmentList.add( AddRecipeStepsFragment.newInstanceForForAddingRecipeFromBrowser(it))
            }
        }else{
            mFragmentList.add( AddRecipeInformationFragment.newInstanceForAdd())
            mFragmentList.add( AddRecipeIngredientsFragment.newInstanceForAdd())
            mFragmentList.add( AddRecipeStepsFragment.newInstanceForAdd())
        }


        mActivityAddRecipeBinding.addRecipeTabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mActivityAddRecipeBinding.viewPager2.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        mActivityAddRecipeBinding.viewPager2.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mActivityAddRecipeBinding.addRecipeTabLayout.selectTab(mActivityAddRecipeBinding.addRecipeTabLayout.getTabAt(position))
            }
        })

        val adapter = ViewPagerFragmentAdapter(mFragmentList,supportFragmentManager,lifecycle)
        mActivityAddRecipeBinding.viewPager2.adapter = adapter
        mActivityAddRecipeBinding.viewPager2.offscreenPageLimit = 3 // important. It render all 3 fragment
        mActivityAddRecipeBinding.viewPager2.isUserInputEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
       menuInflater.inflate(R.menu.add_recipe_menu, menu)
        if(mAction == EDIT_ACTION){
            menu?.findItem(R.id.saveRecipe)?.isVisible = false
            menu?.findItem(R.id.browseRecipe)?.isVisible = false
            menu?.findItem(R.id.updateRecipe)?.isVisible = true
        }
        return super.onCreateOptionsMenu(menu)
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.add_recipe_menu, menu)
//        if(mAction == EDIT_ACTION){
//            menu?.findItem(R.id.saveRecipe)?.isVisible = false
//            menu?.findItem(R.id.browseRecipe)?.isVisible = false
//            menu?.findItem(R.id.updateRecipe)?.isVisible = true
//        }
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.saveRecipe->{
                checkDataForSaving()
            }
            R.id.updateRecipe->{

                checkingForUpdate()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun checkDataForSaving(){

        val addRecipeInformationFragment =  mFragmentList[0] as AddRecipeInformationFragment
        val addRecipeIngredientsFragment =  mFragmentList[1] as AddRecipeIngredientsFragment
        val addRecipeStepsFragment =  mFragmentList[2] as AddRecipeStepsFragment

        val recipeEntity: RecipeEntity? = addRecipeInformationFragment.getRecipeInformation()
        val recipeCategories = addRecipeInformationFragment.getRecipeCategories()

        val recipeImageUri =addRecipeInformationFragment.getRecipeImageURI()
        val ingredients = addRecipeIngredientsFragment.getIngredents()
        val steps = addRecipeStepsFragment.getSteps()

        recipeEntity?.let{

            val recipeCategoryAssignmentEntities = createRecipeCategoryAssignmentEntities(it.uniqueId,recipeCategories)

            assignedSomeValueToIngredients(recipeEntity,ingredients)
            assignedSomeValueToSteps(recipeEntity,steps)

            mAddRecipeActivityViewModel.mCoroutineScope.launch {

                    mAddRecipeActivityViewModel.saveRecipe(this@AddRecipeActivity,recipeEntity,ingredients,steps,recipeCategoryAssignmentEntities)
                    recipeImageUri?.let {
                        ImageUtil.saveImage(this@AddRecipeActivity,recipeImageUri,"${recipeEntity.uniqueId}.${ImageUtil.IMAGE_NAME_SUFFIX}",ImageUtil.RECIPE_IMAGES_FINAL_LOCATION)
                    }
                withContext(Main){
                    Toast.makeText(this@AddRecipeActivity,"Recipe saved successfully",Toast.LENGTH_SHORT).show()
                    this@AddRecipeActivity.finish()
                }

            }

        }

    }
    fun checkingForUpdate(){
        val addRecipeInformationFragment =  mFragmentList[0] as AddRecipeInformationFragment
        val addRecipeIngredientsFragment =  mFragmentList[1] as AddRecipeIngredientsFragment
        val addRecipeStepsFragment =  mFragmentList[2] as AddRecipeStepsFragment


        val recipeEntity: RecipeEntity? = addRecipeInformationFragment.getRecipeInformation()
        val recipeCategories = addRecipeInformationFragment.getRecipeCategories()
        val recipeImageUri =addRecipeInformationFragment.getRecipeImageURI()
        val ingredients = addRecipeIngredientsFragment.getIngredents()
        val steps = addRecipeStepsFragment.getSteps()


        recipeEntity?.let {

            val recipeCategoryAssignmentEntities = createRecipeCategoryAssignmentEntities(it.uniqueId,recipeCategories)
            assignedSomeValueToIngredients(recipeEntity,ingredients)
            assignedSomeValueToSteps(recipeEntity,steps)

            mAddRecipeActivityViewModel.mCoroutineScope.launch {
                mAddRecipeActivityViewModel.updateRecipe(this@AddRecipeActivity,recipeEntity,ingredients,steps,recipeCategoryAssignmentEntities)
                recipeImageUri?.let {
                    ImageUtil.saveImage(this@AddRecipeActivity,recipeImageUri,"${recipeEntity.uniqueId}.${ImageUtil.IMAGE_NAME_SUFFIX}",ImageUtil.RECIPE_IMAGES_FINAL_LOCATION)
                }
                withContext(Main){

                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }


    }
    fun assignedSomeValueToIngredients(recipeEntity:RecipeEntity,ingredients:ArrayList<IngredientEntity>){
        ingredients.forEach {
            it.recipeUniqueId = recipeEntity.uniqueId
        }
    }
    fun assignedSomeValueToSteps(recipeEntity:RecipeEntity,steps:ArrayList<RecipeStepEntity>){
        steps.forEach {
            it.recipeUniqueId = recipeEntity.uniqueId
        }

    }

    fun createRecipeCategoryAssignmentEntities(recipeUniqueId:String,selectedCategories:ArrayList<RecipeCategoryEntity>): ArrayList<RecipeCategoryAssignmentEntity> {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        val recipeCategoryAssignmentEntities = arrayListOf<RecipeCategoryAssignmentEntity>()
            selectedCategories.forEach {

                var itemUniqueID = UUID.randomUUID().toString()

                val recipeCategoryAssignmentEntity = RecipeCategoryAssignmentEntity(
                    uniqueId = itemUniqueID,
                    recipeCategoryUniqueId = it.uniqueId,
                    recipeUniqueId = recipeUniqueId,
                    status = RecipeCategoryAssignmentEntity.NOT_DELETED_STATUS,
                    uploaded = RecipeCategoryAssignmentEntity.NOT_UPLOADED,
                    created = currentDatetime,
                    modified = currentDatetime
                )

                recipeCategoryAssignmentEntities.add(recipeCategoryAssignmentEntity)
            }

        return recipeCategoryAssignmentEntities



    }
}

class ViewPagerFragmentAdapter(fragmentList: ArrayList<Fragment>,  fragmentManager: FragmentManager,lifecyle:Lifecycle) : FragmentStateAdapter(fragmentManager,lifecyle) {
    private val fragmentList: ArrayList<Fragment> = fragmentList
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}