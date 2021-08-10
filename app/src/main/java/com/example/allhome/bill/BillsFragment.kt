package com.example.allhome.bill

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.grocerylist.GroceryListFragment
import com.example.allhome.grocerylist.GroceryListInformationActivity

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BillsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_bills, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_bill_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.addBillMenuItem->{
                Toast.makeText(requireContext(),"Add bill",Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), BillActivity::class.java)
                intent.putExtra(BillActivity.TITLE_TAG,"Create bill payment")
                requireContext().startActivity(intent)

            }
        }
        return true
    }
    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            BillsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}