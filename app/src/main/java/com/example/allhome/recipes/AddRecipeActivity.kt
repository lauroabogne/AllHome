package com.example.allhome.recipes

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.databinding.ActivityAddRecipeBinding
import com.example.allhome.recipes.viewmodel.AddRecipeActivityViewModel
import com.example.allhome.recipes.viewmodel.AddRecipeInformationFragmentViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch


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


            intent.getParcelableExtra<RecipeEntity>(RECIPE_TAG)?.let {
                mFragmentList.add( AddRecipeInformationFragment.newInstanceForEditing(it))
                mFragmentList.add( AddRecipeIngredientsFragment.newInstanceForEditing(it))
                mFragmentList.add( AddRecipeStepsFragment.newInstanceForEditing(it))
            }


        }else{
            mFragmentList.add( AddRecipeInformationFragment.newInstanceForAdd())
            mFragmentList.add( AddRecipeIngredientsFragment.newInstanceForAdd())
            mFragmentList.add( AddRecipeStepsFragment.newInstanceForAdd())
        }


        mActivityAddRecipeBinding.addRecipeTabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mActivityAddRecipeBinding.viewPager2.setCurrentItem(tab!!.position)
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
        mActivityAddRecipeBinding.viewPager2.isUserInputEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_recipe_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.saveRecipe->{
                Toast.makeText(this,"save recipe",Toast.LENGTH_SHORT).show()
                checkDataForSaving()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun checkDataForSaving(){

       val addRecipeInformationFragment =  mFragmentList[0] as AddRecipeInformationFragment
       val addRecipeIngredientsFragment =  mFragmentList[1] as AddRecipeIngredientsFragment
       val addRecipeStepsFragment =  mFragmentList[2] as AddRecipeStepsFragment

        val recipeEntity: RecipeEntity? = addRecipeInformationFragment.getRecipeInformation()
        val ingredients = addRecipeIngredientsFragment.getIngredents()
        val steps = addRecipeStepsFragment.getSteps()
        recipeEntity?.let{
            assignedSomeValueToIngredients(recipeEntity,ingredients)
            assignedSomeValueToSteps(recipeEntity,steps)

            mAddRecipeActivityViewModel.mCoroutineScope.launch {
                    mAddRecipeActivityViewModel.saveRecipe(this@AddRecipeActivity,recipeEntity,ingredients,steps)
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