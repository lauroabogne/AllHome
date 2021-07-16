package com.example.allhome.meal_planner

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
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
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.AddMealDialogFragmentBinding
import com.example.allhome.recipes.RecipesFragment


class AddMealDialogFragment(): DialogFragment() {

    lateinit var mlayoutBinding: AddMealDialogFragmentBinding
    var mSelectedFragment:Fragment? = null

    val mSelectedMealTypes = arrayListOf<String>()
    lateinit var mCurrentFragment:Fragment
    var mMealKind = MealEntity.NO_KIND
    var mMealType  = MealEntity.NO_TYPE

    var mSelectedRecipeEntity:RecipeEntity? = null
    var mQuickRecipeName = ""
    var mQuckRecipeCost = 0.0


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

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun loadFragment(fragment:Fragment){
        childFragmentManager.beginTransaction().replace(R.id.fragmentContainer,fragment).commit()

    }
    fun showNextButton(){
        mlayoutBinding.nextBtn.visibility = View.VISIBLE
    }
    fun hideNextButton(){
        mlayoutBinding.nextBtn.visibility = View.GONE
    }
    val nextBtnClickListener = object:View.OnClickListener{
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
                mQuckRecipeCost = quickrecipeCostDouble


                val mealTypeFragment = MealTypeFragment.newInstance("","")
                mealTypeFragment.setOnCheckedChangeListener(mealTypeFragmentOnCheckedChangeListener)
                mCurrentFragment = mealTypeFragment
                loadFragment(mealTypeFragment)
            }else if(mCurrentFragment is MealTypeFragment){

                Log.e("MEAL_TYPE",mSelectedMealTypes.toString())
                Toast.makeText(requireContext(),"Saving",Toast.LENGTH_SHORT).show()
            }
        }

    }
    val toolbarNavigationClickListener= object:View.OnClickListener{
        override fun onClick(v: View?) {
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

                    val quickRecipeFragment = QuickRecipeFragment.newInstance(mQuickRecipeName,mQuckRecipeCost)
                    mCurrentFragment = quickRecipeFragment
                    loadFragment(quickRecipeFragment)


                    showNextButton()



                }else{
                    val recipeFragment = RecipesFragment(RecipesFragment.ADDING_MEAL_VIEWING,recipeSelectedListener)
                    mCurrentFragment = recipeFragment
                    loadFragment(recipeFragment)
                    hideNextButton()

                }
            }

        }

    }
    val addMealOptionFragmentSelectionListener =  object:AddMealOptionFragmentSelectionListener{
        override fun onSelect(viewId: Int) {
            when(viewId){
                R.id.recipeButton->{

                    mMealKind = MealEntity.RECIPE_KIND
                    val recipeFragment = RecipesFragment(RecipesFragment.ADDING_MEAL_VIEWING,recipeSelectedListener)
                    mCurrentFragment = recipeFragment
                    loadFragment(recipeFragment)
                    hideNextButton()
                }
                R.id.quickRecipeButton->{

                    mMealKind = MealEntity.QUICK_RECIPE_KIND
                    val quickRecipeFragment = QuickRecipeFragment.newInstance(mQuickRecipeName,mQuckRecipeCost)
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

            val mealTypeFragment = MealTypeFragment.newInstance("","")
            mealTypeFragment.mOnCheckedChangeListener = mealTypeFragmentOnCheckedChangeListener
            mCurrentFragment = mealTypeFragment
            loadFragment(mealTypeFragment)
            showNextButton()

        }

    }
    val mealTypeFragmentOnCheckedChangeListener =  object : CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

            buttonView?.let{
                Toast.makeText(requireContext(),"Changed",Toast.LENGTH_SHORT).show()
                val text = it.text.toString()
                if(isChecked){
                    mSelectedMealTypes.add(text)
                }else{
                    mSelectedMealTypes.remove(text)
                }
            }
        }

    }

    interface AddMealOptionFragmentSelectionListener{
        fun onSelect(viewId:Int)
    }
    interface RecipeSelectedListener{
        fun onSelect(recipe:RecipeEntity)
    }
}