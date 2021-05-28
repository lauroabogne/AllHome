package com.example.allhome.recipes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.allhome.R
import com.example.allhome.databinding.ActivityAddRecipeBinding
import com.example.allhome.recipes.viewmodel.AddRecipeIngredientsFragmentModel
import com.example.allhome.storage.StorageActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*


class AddRecipeActivity : AppCompatActivity() {


    lateinit var mActivityAddRecipeBinding:ActivityAddRecipeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Create recipe"

        mActivityAddRecipeBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_recipe)
        val fragmentList = arrayListOf(
            AddRecipeInformationFragment(),AddRecipeIngredientsFragment(),AddRecipeStepsFragment()
        )
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

        val adapter = ViewPagerFragmentAdapter(fragmentList,supportFragmentManager,lifecycle)
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
        }

        return super.onOptionsItemSelected(item)
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