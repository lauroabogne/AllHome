package com.example.allhome.recipes

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentViewRecipeBinding
import com.google.android.material.tabs.TabLayout


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ViewRecipeFragment : Fragment() {

    private lateinit var mRecipeEntity:RecipeEntity

    private lateinit var mFragmentViewRecipeBinding:FragmentViewRecipeBinding
    val mFragmentList = arrayListOf<Fragment>()

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
        arguments?.let {
            mRecipeEntity= it.getParcelable<RecipeEntity>(RECIPE_INTENT_TAG)!!
            mFragmentList.add( ViewRecipeInformationFragment.newInstance(mRecipeEntity))
            mFragmentList.add(ViewRecipeIngredientsFragment.newInstance(mRecipeEntity))
            mFragmentList.add( ViewRecipeStepsFragment.newInstance(mRecipeEntity))

            Log.e(TAG,mRecipeEntity.toString())

        }

        setHasOptionsMenu(true)



    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentViewRecipeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_recipe, container, false)

        mFragmentViewRecipeBinding.customToolbar.inflateMenu(R.menu.view_recipe_menu)
        mFragmentViewRecipeBinding.customToolbar.setNavigationOnClickListener {
            Toast.makeText(requireContext(),"Clicked backed",Toast.LENGTH_SHORT).show()
        }
        mFragmentViewRecipeBinding.viewRecipeTabLayout.addOnTabSelectedListener(onTabSelectedListener)


        val adapter = ViewPagerFragmentAdapter(mFragmentList,requireActivity().supportFragmentManager,lifecycle)
        mFragmentViewRecipeBinding.viewPager.adapter = adapter

        mFragmentViewRecipeBinding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mFragmentViewRecipeBinding.viewRecipeTabLayout.selectTab(mFragmentViewRecipeBinding.viewRecipeTabLayout.getTabAt(position))
            }
        })

        return mFragmentViewRecipeBinding.root
    }


    val onTabSelectedListener = object:TabLayout.OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab?) {

            mFragmentViewRecipeBinding.viewPager.setCurrentItem(tab!!.position)
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