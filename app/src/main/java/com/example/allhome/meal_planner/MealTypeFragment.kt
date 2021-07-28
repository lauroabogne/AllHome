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


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MealTypeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mFragmentMealTypeBinding:FragmentMealTypeBinding
    var mOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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

        @JvmStatic fun newInstance(param1: String, param2: String) =
            MealTypeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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