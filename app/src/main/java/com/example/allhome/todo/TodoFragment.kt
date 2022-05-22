package com.example.allhome.todo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.bill.AddPaymentFragment
import com.example.allhome.bill.BillActivity
import com.example.allhome.databinding.FragmentGroceryListBinding
import com.example.allhome.databinding.FragmentTodoBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TodoFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    private val addTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){

        }
    }

    lateinit var mFragmentTodoBinding:FragmentTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
        requireActivity().title = "To Do List"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        mFragmentTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_todo,null,false)
        mFragmentTodoBinding.fab.setOnClickListener {
            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
            addTodoListResultContract.launch(intent)
        }

        return mFragmentTodoBinding.root
    }

    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            TodoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}