package com.example.allhome.recipes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.databinding.AddStepItemBinding
import com.example.allhome.databinding.FragmentRecipesBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.example.allhome.storage.CreateStorageActivity
import com.example.allhome.storage.StorageFragment
import com.example.allhome.storage.StorageViewAdapter
import com.example.allhome.storage.viewmodel.StorageViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RecipesFragment : Fragment() {

     lateinit var mRecipesFragmentViewModel: RecipesFragmentViewModel
    private lateinit var mFragmentRecipesBinding: FragmentRecipesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        mRecipesFragmentViewModel = ViewModelProvider(this).get(RecipesFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        mFragmentRecipesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipes, container, false)
        mFragmentRecipesBinding.fab.setOnClickListener {

            val intent = Intent(requireContext(),AddRecipeActivity::class.java)
            startActivity(intent)
        }


        val recipesRecyclerviewViewAdapater = RecipesRecyclerviewViewAdapater(arrayListOf(),this)
        mFragmentRecipesBinding.recipesRecyclerview.adapter = recipesRecyclerviewViewAdapater

        mRecipesFragmentViewModel.mCoroutineScope.launch {
            val recipes = mRecipesFragmentViewModel.getRecipes(requireContext())

            withContext(Main){
                val adapter = mFragmentRecipesBinding.recipesRecyclerview.adapter as RecipesRecyclerviewViewAdapater
                adapter.mRecipeStepEntities = recipes as ArrayList<RecipeEntity>
                adapter.notifyDataSetChanged()
            }
        }


        return mFragmentRecipesBinding.root
    }

    class RecipesRecyclerviewViewAdapater(var mRecipeStepEntities:ArrayList<RecipeEntity>, val mRecipesFragment: RecipesFragment):
        RecyclerView.Adapter<RecipesRecyclerviewViewAdapater.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {



            val layoutInflater = LayoutInflater.from(parent.context)
            val recipeItemBinding =  RecipeItemBinding.inflate(layoutInflater, parent, false)
            val itemViewHolder = ItemViewHolder(recipeItemBinding, this)

            return itemViewHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val recipeEntity = mRecipeStepEntities[position]
            holder.recipeItemBinding.recipeEntity = recipeEntity
            holder.recipeItemBinding.executePendingBindings()

        }

        override fun getItemCount(): Int {

            return mRecipeStepEntities.size
        }


        inner class  ItemViewHolder(var recipeItemBinding: RecipeItemBinding, val recipesRecyclerviewViewAdapater: RecipesRecyclerviewViewAdapater): RecyclerView.ViewHolder(recipeItemBinding.root),View.OnClickListener{

            init {
                recipeItemBinding.root.setOnClickListener(this)
            }
            override fun onClick(view: View?) {
                Toast.makeText(view?.context,"Clicked",Toast.LENGTH_SHORT).show()
                val intent = Intent()

                val viewRecipeActivity = Intent(view?.context, ViewRecipeActivity::class.java)
                view?.context?.startActivity(viewRecipeActivity)
            }


        }


    }

}

