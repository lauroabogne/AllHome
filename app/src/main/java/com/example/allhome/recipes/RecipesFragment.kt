package com.example.allhome.recipes

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.databinding.FragmentRecipesBinding


class RecipesFragment : Fragment() {

    private lateinit var mFragmentRecipesBinding: FragmentRecipesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        mFragmentRecipesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipes, container, false)
        mFragmentRecipesBinding.fab.setOnClickListener {
            Toast.makeText(requireContext(),"Toast",Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(),AddRecipeActivity::class.java)
            startActivity(intent)
        }

        return mFragmentRecipesBinding.root
    }


}