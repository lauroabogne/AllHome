package com.example.allhome.meal_planner

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.StorageEntityWithExtraInformation
import com.example.allhome.databinding.FragmentViewMealOfTheDayBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.recipes.RecipesFragment
import com.example.allhome.storage.StorageFragment
import com.example.allhome.storage.StorageUtil
import com.example.allhome.utils.ImageUtil
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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

                Log.e("snackAfterLunchPlan",snackAfterLunchPlan.toString())
                Log.e("dinnerPlan",dinnerPlan.toString())
                Log.e("snackAfterDinnerPlan",snackAfterDinnerPlan.toString())
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
                    Toast.makeText(requireContext(),"addToGrocer dasf asdf yList",Toast.LENGTH_SHORT).show()
                    var dialog = AddMealDialogFragment(mDateSelected!!)
                    dialog.show(childFragmentManager,"AddMealDialogFragment")

                }
                R.id.addToGroceryList->{
                    Toast.makeText(requireContext(),"addToGroceryList",Toast.LENGTH_SHORT).show()


                }
            }
           return true
        }
    }


    class MealPlanItemsAdapter(val viewMealOfTheDayFragment: ViewMealOfTheDayFragment): RecyclerView.Adapter<MealPlanItemsAdapter.ItemViewHolder>() {

        var mMealEntities:ArrayList<MealEntity> = arrayListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)

            val textView: TextView = layoutInflater.inflate(android.R.layout.simple_list_item_1,null,false) as TextView
            val itemViewHolder = ItemViewHolder(textView)
            return itemViewHolder
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val mealEntity = mMealEntities[position]
            holder.textView.setText("${mealEntity.name}")

        }

        override fun getItemCount(): Int {

            return mMealEntities.size
        }
        inner class  ItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView){


        }

    }

}


