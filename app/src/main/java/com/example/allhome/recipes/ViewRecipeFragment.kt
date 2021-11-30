package com.example.allhome.recipes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentViewRecipeBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewRecipeFragment : Fragment() {

    private lateinit var mRecipeEntity:RecipeEntity
    private lateinit var mFragmentViewRecipeBinding:FragmentViewRecipeBinding
    lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel

    val mFragmentList = arrayListOf<Fragment>()
    var mGroceryListEdited =false

    private val editRecipeContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){
            mRecipesFragmentViewModel.mCoroutineScope.launch {
               mRecipeEntity = mRecipesFragmentViewModel.getRecipe(requireContext(),mRecipeEntity.uniqueId).recipeEntity
                withContext(Main){
                    mFragmentList.clear()
                    mFragmentList.add( ViewRecipeInformationFragment.newInstance(mRecipeEntity))
                    mFragmentList.add(ViewRecipeIngredientsFragment.newInstance(mRecipeEntity))
                    mFragmentList.add( ViewRecipeStepsFragment.newInstance(mRecipeEntity))

                    val adapter = ViewPagerFragmentAdapter(mFragmentList,requireActivity().supportFragmentManager,lifecycle)
                    mFragmentViewRecipeBinding.viewPager.adapter = adapter

                    mGroceryListEdited = true
                }
            }

        }
    }

    companion object {
        val TAG = "ViewRecipeFragment"
        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"
        @JvmStatic fun newInstance(recipeEntity: RecipeEntity) =
            ViewRecipeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)
                }
            }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            mRecipeEntity= it.getParcelable<RecipeEntity>(RECIPE_INTENT_TAG)!!
            mFragmentList.add( ViewRecipeInformationFragment.newInstance(mRecipeEntity))
            mFragmentList.add(ViewRecipeIngredientsFragment.newInstance(mRecipeEntity))
            mFragmentList.add( ViewRecipeStepsFragment.newInstance(mRecipeEntity))



        }

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentViewRecipeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_recipe, container, false)

        mFragmentViewRecipeBinding.customToolbar.title = mRecipeEntity.name
        mFragmentViewRecipeBinding.recipeEntity = mRecipeEntity

        mFragmentViewRecipeBinding.customToolbar.inflateMenu(R.menu.view_recipe_menu)
        mFragmentViewRecipeBinding.customToolbar.setNavigationOnClickListener {

            if(mGroceryListEdited){

                val result = Intent().putExtra(RECIPE_INTENT_TAG, mRecipeEntity)
                activity?.setResult(Activity.RESULT_OK,result)

            }

            activity?.finish()

        }
        mFragmentViewRecipeBinding.customToolbar.setOnMenuItemClickListener {


            when(it.itemId){
                R.id.deleteMenu->{
                   deleteRecipe()
                }
                R.id.editMenu->{
                    editRecipe()
                }
                R.id.addToGroceryListMenu->{
                    var ingredientDialogFragment = IngredientDialogFragment("Select ingredients",mRecipeEntity)
                    ingredientDialogFragment.show(requireActivity().supportFragmentManager,"IngredientDialogFragment")
                }
            }
            true
        }
        mFragmentViewRecipeBinding.viewRecipeTabLayout.addOnTabSelectedListener(onTabSelectedListener)


        val adapter = ViewPagerFragmentAdapter(mFragmentList,requireActivity().supportFragmentManager,lifecycle)
        mFragmentViewRecipeBinding.viewPager.adapter = adapter
        mFragmentViewRecipeBinding.viewPager.offscreenPageLimit = 3 // important. It render all 3 fragment


        mFragmentViewRecipeBinding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mFragmentViewRecipeBinding.viewRecipeTabLayout.selectTab(mFragmentViewRecipeBinding.viewRecipeTabLayout.getTabAt(position))
            }
        })


        return mFragmentViewRecipeBinding.root
    }
    fun deleteRecipe(){
        mRecipesFragmentViewModel.mCoroutineScope.launch {
            mRecipesFragmentViewModel.deleteRecipe(requireContext(),mRecipeEntity.uniqueId)
            withContext(Main){
                Toast.makeText(requireContext(),"DELETED",Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }
    fun editRecipe(){

        val viewRecipeInformationFragment:ViewRecipeInformationFragment = mFragmentList[0] as ViewRecipeInformationFragment
        val viewRecipeIngredientsFragment:ViewRecipeIngredientsFragment = mFragmentList[1] as ViewRecipeIngredientsFragment
        val viewRecipeStepsFragment:ViewRecipeStepsFragment = mFragmentList[2] as ViewRecipeStepsFragment

        mRecipesFragmentViewModel.mCoroutineScope.launch {

            val ingredients= mRecipesFragmentViewModel.getIngredients(requireContext(),mRecipeEntity.uniqueId)
            val steps = mRecipesFragmentViewModel.getSteps(requireContext(),mRecipeEntity.uniqueId)

            withContext(Main){
                val intent = Intent(requireContext(),AddRecipeActivity::class.java)
                intent.putExtra(AddRecipeActivity.ACTION_TAG,AddRecipeActivity.EDIT_ACTION)
                intent.putExtra(AddRecipeActivity.RECIPE_TAG,mRecipeEntity)
                editRecipeContract.launch(intent)

            }
        }



    }


    val onTabSelectedListener = object:TabLayout.OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab?) {

            mFragmentViewRecipeBinding.viewPager.currentItem = tab!!.position
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

    class ViewPagerFragmentAdapter(fragmentList: ArrayList<Fragment>, fragmentManager: FragmentManager, lifecyle: Lifecycle) : FragmentStateAdapter(fragmentManager,lifecyle) {
        private val fragmentList: ArrayList<Fragment> = fragmentList
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }

    }

}