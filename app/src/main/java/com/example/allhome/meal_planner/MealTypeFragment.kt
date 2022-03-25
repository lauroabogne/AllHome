package com.example.allhome.meal_planner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.databinding.FragmentMealTypeBinding
import com.example.allhome.databinding.MealTypeTextviewBinding


const val SELECTED_MEAL_TYPE_TAG = "SELECTED_MEAL_TYPE_TAG"

class MealTypeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mSelectedMealTypes:Array<String>? = null
    lateinit var mFragmentMealTypeBinding:FragmentMealTypeBinding
    var mOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            mSelectedMealTypes = it.getStringArray(SELECTED_MEAL_TYPE_TAG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentMealTypeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_meal_type, container, false)

        val mealTypeRecyclerviewViewAdapater = MealTypeRecyclerviewViewAdapater(this)
        mFragmentMealTypeBinding.mealTypeRecyclerview.adapter  = mealTypeRecyclerviewViewAdapater

        val dividerItemDecoration = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentMealTypeBinding.mealTypeRecyclerview.addItemDecoration(dividerItemDecoration)

        return mFragmentMealTypeBinding.root
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: CompoundButton.OnCheckedChangeListener){
        mOnCheckedChangeListener = onCheckedChangeListener
    }

    companion object {

        @JvmStatic fun newInstance(selectedMealTypes: Array<String>) =
            MealTypeFragment().apply {
                arguments = Bundle().apply {

                    putStringArray(SELECTED_MEAL_TYPE_TAG,selectedMealTypes)
                }
            }
    }
    class MealTypeRecyclerviewViewAdapater(val mealTypeFragment:MealTypeFragment):RecyclerView.Adapter<MealTypeRecyclerviewViewAdapater.ItemViewHolder>(){


        val mMealType = mealTypeFragment.requireContext().resources.getStringArray(R.array.meal_type)


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val mealTypeTextviewBinding = MealTypeTextviewBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(mealTypeTextviewBinding)
            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val mealType = mMealType[position]

            val indexOfMealType:Int = if(mealTypeFragment.mSelectedMealTypes == null) -1 else mealTypeFragment.mSelectedMealTypes!!.indexOf(mealType)
            holder.mealTypeTextviewBinding.checkBox.isChecked = indexOfMealType >= 0
            holder.mealTypeTextviewBinding.checkBox.text = mealType
            holder.setCheckboxClickListener()
        }

        override fun getItemCount(): Int {
           return mMealType.size
        }

        inner class ItemViewHolder(val mealTypeTextviewBinding:MealTypeTextviewBinding):RecyclerView.ViewHolder(mealTypeTextviewBinding.root){

            fun setCheckboxClickListener(){
                mealTypeTextviewBinding.checkBox.setOnCheckedChangeListener(mealTypeFragment.mOnCheckedChangeListener)

            }
        }
    }
}