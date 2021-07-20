package com.example.allhome.meal_planner

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.databinding.FragmentViewMealOfTheDayBinding
import com.example.allhome.databinding.MealItemBinding
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.recipes.ViewRecipeActivity
import com.example.allhome.recipes.ViewRecipeFragment
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class ViewMealOfTheDayFragment : Fragment() {

    private var mStringDateSelected:String? = null
    private var mDateSelected: Date? = null

    lateinit var mMealPlannerViewModel:MealPlannerViewModel
    lateinit var mFragmentViewMealOfTheDayBinding:FragmentViewMealOfTheDayBinding

    companion object {

        const val DATE_SELECTED_PARAM = "DATE_SELECTED_PARAM"

        @JvmStatic fun newInstance(dateSelected: String) =
            ViewMealOfTheDayFragment().apply {
                arguments = Bundle().apply {
                    putString(DATE_SELECTED_PARAM, dateSelected)
                }
            }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMealPlannerViewModel = ViewModelProvider(this).get(MealPlannerViewModel::class.java)


        arguments?.let {
            mStringDateSelected = it.getString(DATE_SELECTED_PARAM)
            mDateSelected = SimpleDateFormat("yyyy-MM-dd").parse(mStringDateSelected)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentViewMealOfTheDayBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_meal_of_the_day, container, false)
        mFragmentViewMealOfTheDayBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        mFragmentViewMealOfTheDayBinding.toolbar.inflateMenu(R.menu.view_meal_of_the_day_menu)
        mDateSelected?.let {
            mFragmentViewMealOfTheDayBinding.toolbar.title = SimpleDateFormat("MMMM dd,yyyy").format(it)
        }

        mFragmentViewMealOfTheDayBinding.toolbar.setNavigationOnClickListener {
            activity?.finish()
        }
        mFragmentViewMealOfTheDayBinding.toolbar.setOnMenuItemClickListener(toolbarMenuItemClickListener)

        mealPlan()
        return mFragmentViewMealOfTheDayBinding.root
    }
    fun mealPlan(){

        val currentDate = SimpleDateFormat("yyyy-MM-dd").format(mDateSelected)

        mMealPlannerViewModel.mCoroutineScope.launch {

            val breakFastPlan = mMealPlannerViewModel.getMealByTypeAndDate(requireContext(),MealEntity.BREAKFAST_TYPE,currentDate)
            val snackAfterBreakFastPlan = mMealPlannerViewModel.getMealByTypeAndDate(requireContext(),MealEntity.SNACK_AFTER_BREAKFAST_TYPE,currentDate)
            val lunchFastPlan = mMealPlannerViewModel.getMealByTypeAndDate(requireContext(),MealEntity.LUNCK_TYPE,currentDate)
            val snackAfterLunchPlan = mMealPlannerViewModel.getMealByTypeAndDate(requireContext(),MealEntity.SNACK_AFTERLUNCK_TYPE,currentDate)
            val dinnerPlan = mMealPlannerViewModel.getMealByTypeAndDate(requireContext(),MealEntity.DINNER_TYPE,currentDate)
            val snackAfterDinnerPlan = mMealPlannerViewModel.getMealByTypeAndDate(requireContext(),MealEntity.SNACK_AFTER_DINNER_TYPE,currentDate)


            withContext(Main){

                setupBreakFastMealPlan(breakFastPlan)
                setupSnackAfterBreakFastMealPlan(snackAfterBreakFastPlan)
                setupLunchMealPlan(lunchFastPlan)
                setupSnackAfterLunchMealPlan(snackAfterLunchPlan)
                setupDinnerMealPlan(dinnerPlan)
                setupSnackAfterDinnerMealPlan(snackAfterDinnerPlan)




            }
        }

    }
    fun setupBreakFastMealPlan(mealEntities:List<MealEntity>){
        var breakFastMealPlanItemsAdapter = MealPlanItemsAdapter(this@ViewMealOfTheDayFragment)
        breakFastMealPlanItemsAdapter.mMealEntities = mealEntities as ArrayList<MealEntity>
        var rcyclerview = mFragmentViewMealOfTheDayBinding.breakFastsRecyclerview
        rcyclerview.adapter = breakFastMealPlanItemsAdapter
        rcyclerview.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        breakFastMealPlanItemsAdapter.notifyDataSetChanged()
    }
    fun setupSnackAfterBreakFastMealPlan(mealEntities:List<MealEntity>){

        if(mealEntities.isEmpty()){
            mFragmentViewMealOfTheDayBinding.snackAfterBreakfastFastsLinearLayout.visibility = View.GONE
            return
        }
        var mealPlanItemsAdapter = MealPlanItemsAdapter(this@ViewMealOfTheDayFragment)
        mealPlanItemsAdapter.mMealEntities = mealEntities as ArrayList<MealEntity>
        var rcyclerview = mFragmentViewMealOfTheDayBinding.snackAfterBreakfastFastsRecyclerview
        rcyclerview.adapter = mealPlanItemsAdapter
        rcyclerview.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        mealPlanItemsAdapter.notifyDataSetChanged()
    }
    fun setupLunchMealPlan(mealEntities:List<MealEntity>){
        var mealPlanItemsAdapter = MealPlanItemsAdapter(this@ViewMealOfTheDayFragment)
        mealPlanItemsAdapter.mMealEntities = mealEntities as ArrayList<MealEntity>
        var rcyclerview = mFragmentViewMealOfTheDayBinding.lunchRecyclerview
        rcyclerview.adapter = mealPlanItemsAdapter
        rcyclerview.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        mealPlanItemsAdapter.notifyDataSetChanged()
    }

    fun setupSnackAfterLunchMealPlan(mealEntities:List<MealEntity>){
        if(mealEntities.isEmpty()){
            mFragmentViewMealOfTheDayBinding.snackAfterLunchLinearLayout.visibility = View.GONE
            return
        }
        var mealPlanItemsAdapter = MealPlanItemsAdapter(this@ViewMealOfTheDayFragment)
        mealPlanItemsAdapter.mMealEntities = mealEntities as ArrayList<MealEntity>
        var rcyclerview = mFragmentViewMealOfTheDayBinding.snackAfterLunchRecyclerview
        rcyclerview.adapter = mealPlanItemsAdapter
        rcyclerview.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        mealPlanItemsAdapter.notifyDataSetChanged()
    }
    fun setupDinnerMealPlan(mealEntities:List<MealEntity>){
        var mealPlanItemsAdapter = MealPlanItemsAdapter(this@ViewMealOfTheDayFragment)
        mealPlanItemsAdapter.mMealEntities = mealEntities as ArrayList<MealEntity>
        var rcyclerview = mFragmentViewMealOfTheDayBinding.dinnerLunchRecyclerview
        rcyclerview.adapter = mealPlanItemsAdapter
        rcyclerview.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        mealPlanItemsAdapter.notifyDataSetChanged()
    }

    fun setupSnackAfterDinnerMealPlan(mealEntities:List<MealEntity>){
        if(mealEntities.isEmpty()){
            mFragmentViewMealOfTheDayBinding.snackAfterDinnerLinearLayout.visibility = View.GONE
            return
        }
        var mealPlanItemsAdapter = MealPlanItemsAdapter(this@ViewMealOfTheDayFragment)
        mealPlanItemsAdapter.mMealEntities = mealEntities as ArrayList<MealEntity>
        var rcyclerview = mFragmentViewMealOfTheDayBinding.snackAfterDinnerRecyclerview
        rcyclerview.adapter = mealPlanItemsAdapter
        rcyclerview.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        mealPlanItemsAdapter.notifyDataSetChanged()
    }


    val toolbarMenuItemClickListener = object:Toolbar.OnMenuItemClickListener{
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.addMeal->{
                    var dialog = AddMealDialogFragment(mDateSelected!!)
                    dialog.mDialogDettachedListener  = dialogDettachedListener
                    dialog.show(childFragmentManager,"AddMealDialogFragment")

                }
                R.id.addToGroceryList->{
                    Toast.makeText(requireContext(),"addToGroceryList",Toast.LENGTH_SHORT).show()


                }
            }
           return true
        }
    }

    val dialogDettachedListener = object:AddMealDialogFragment.DialogDettachedListener{
        override fun onDialogDettached() {
            mealPlan()
        }

    }

    class MealPlanItemsAdapter(val viewMealOfTheDayFragment: ViewMealOfTheDayFragment): RecyclerView.Adapter<MealPlanItemsAdapter.ItemViewHolder>() {

        var mMealEntities:ArrayList<MealEntity> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val mealItemBinding = MealItemBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(mealItemBinding)
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val mealEntity = mMealEntities[position]
            holder.setOnClickListener()
            holder.setText(mealEntity.name)

        }

        override fun getItemCount(): Int {

            return mMealEntities.size
        }
        inner class  ItemViewHolder(val mealItemBinding: MealItemBinding): RecyclerView.ViewHolder(mealItemBinding.root),View.OnClickListener{

            fun setText(text:String){
                mealItemBinding.textView.setText(text)
            }
            fun setOnClickListener(){
                mealItemBinding.textView.setOnClickListener(this)
                mealItemBinding.moreActionImageBtn.setOnClickListener(this)
            }

            override fun onClick(v: View?) {

                when(v!!.id){
                    R.id.textView ->{
                        val mealEntity = mMealEntities[adapterPosition]

                        if(mealEntity.kind == MealEntity.RECIPE_KIND){
                            val recipeUniqueId = mealEntity.recipeUniqueId
                            viewMealOfTheDayFragment.mMealPlannerViewModel.mCoroutineScope.launch {
                                val recipe = viewMealOfTheDayFragment.mMealPlannerViewModel.getRecipe(v.context,recipeUniqueId)
                                withContext(Main){
                                    recipe?.let{

                                        val viewRecipeActivity = Intent(v.context, ViewRecipeActivity::class.java)
                                        viewRecipeActivity.putExtra(ViewRecipeFragment.RECIPE_INTENT_TAG,it)
                                        v.context.startActivity(viewRecipeActivity)
                                    }
                                }
                            }

                        }else if(mealEntity.kind == MealEntity.QUICK_RECIPE_KIND){

                            val viewerIntent = Intent(v.context,ViewerActivity::class.java)
                            viewerIntent.putExtra(ViewerActivity.TITLE_TAG,"View quick recipe")
                            v.context.startActivity(viewerIntent)

                        }


                    }
                    R.id.moreActionImageBtn->{
                      Toast.makeText(v.context,"Delete",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }

    }

}


