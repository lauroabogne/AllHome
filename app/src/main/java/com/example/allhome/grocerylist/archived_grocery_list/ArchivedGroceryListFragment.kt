package com.example.allhome.grocerylist.archived_grocery_list

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.MainActivity
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryListEntityValues
import com.example.allhome.data.entities.GroceryListWithItemCount
import com.example.allhome.databinding.FragmentTrashGroceryListBinding
import com.example.allhome.databinding.TrashGroceryListItemBinding
import com.example.allhome.grocerylist.*
import com.example.allhome.grocerylist.viewmodel.ArchivedGroceryListFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class TrashGroceryListFragment : Fragment() {

    private lateinit var mDataBindingUtil: FragmentTrashGroceryListBinding
    private lateinit var mGroceryListFragmentViewModel: ArchivedGroceryListFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        requireActivity().title = "Archived Grocery List"

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mGroceryListFragmentViewModel = ViewModelProvider(this).get(ArchivedGroceryListFragmentViewModel::class.java)
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_trash_grocery_list, container, false)
        mDataBindingUtil.apply {

        }

        val groceryListRecyclerViewAdapter = TrashGroceryListRecyclerViewAdapter(this, mGroceryListFragmentViewModel)
        mDataBindingUtil.trashGroceryListRecyclerview.adapter = groceryListRecyclerViewAdapter

        mGroceryListFragmentViewModel.coroutineScope.launch {
            mGroceryListFragmentViewModel.getArchivedGroceryLists(requireContext())
            withContext(Dispatchers.Main) {

                groceryListRecyclerViewAdapter.mGroceryListWithItemCount = mGroceryListFragmentViewModel.groceryLists
                groceryListRecyclerViewAdapter.notifyDataSetChanged()
                hideOrShowGroceryNoListTextView()
            }
        }
        return mDataBindingUtil.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.grocery_list_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.grocery_list_menu->{

                if(activity is MainActivity){
                    (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                        replace(R.id.home_fragment_container,GroceryListFragment())
                        commit()
                    }
                }

            }
            R.id.grocery_list_archived_menu->{

                if(activity is MainActivity){
                    (activity as MainActivity).supportFragmentManager.beginTransaction().apply {
                        replace(R.id.home_fragment_container,TrashGroceryListFragment())
                        commit()
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun hideOrShowGroceryNoListTextView(){
        if(mGroceryListFragmentViewModel.groceryLists.isEmpty()){
            mDataBindingUtil.noArchivedListTextView.visibility = View.VISIBLE
        }else{
            mDataBindingUtil.noArchivedListTextView.visibility = View.GONE
        }
    }
}

/**
 *
 */
class TrashGroceryListRecyclerViewAdapter(val trashGroceryListFragment: TrashGroceryListFragment, val trashGroceryListFragmentViewModel: ArchivedGroceryListFragmentViewModel) : RecyclerView.Adapter<TrashGroceryListRecyclerViewAdapter.ItemViewHolder>() {
    var mGroceryListWithItemCount: List<GroceryListWithItemCount> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)



        val groceryListItemBinding = TrashGroceryListItemBinding.inflate(layoutInflater, parent, false)
        val itemViewHolder = ItemViewHolder(groceryListItemBinding)

        return itemViewHolder

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val groceryItemEntity = mGroceryListWithItemCount[position]
        holder.groceryListItemBinding.groceryListWithCount= groceryItemEntity
        holder.groceryListItemBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mGroceryListWithItemCount.size
    }

    /**
     *
     */
    inner  class ItemViewHolder(var groceryListItemBinding: TrashGroceryListItemBinding) : RecyclerView.ViewHolder(groceryListItemBinding.root), View.OnClickListener {

        init{
            groceryListItemBinding.root.setOnClickListener(this)
            groceryListItemBinding.moreActionImageView.setOnClickListener(this)
            groceryListItemBinding.itemCountAndBoughtTextView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {

            if(view?.id == R.id.trash_grocery_item_list_parent_layout){

                val groceryListWithCount  = groceryListItemBinding.groceryListWithCount
                val intent = Intent(trashGroceryListFragment.requireContext(), ArchivedSingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListWithCount?.groceryListEntity?.autoGeneratedUniqueId)
                trashGroceryListFragment.startActivityForResult(intent, GroceryListFragment.OPEN_SINGLE_GROCERY_LIST_REQUEST)
            }else if(view?.id == R.id.itemCountAndBoughtTextView || view?.id == R.id.moreActionImageView){

                val popupMenu = PopupMenu(trashGroceryListFragment.requireContext(), groceryListItemBinding.moreActionImageView)
                popupMenu.menuInflater.inflate(R.menu.trash_grocery_list_action, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {

                    when (it.itemId) {
                        R.id.trash_grocery_list_restore_menu -> {

                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val currentDatetime: String = simpleDateFormat.format(Date())
                            val autoGeneratedUniqueId  = groceryListItemBinding.groceryListWithCount!!.groceryListEntity.autoGeneratedUniqueId
                           // GroceryListEntityValues
                            //context: Context,autoGeneratedUniqueId:String,datetime:String,status:Int
                            trashGroceryListFragmentViewModel.coroutineScope.launch {
                                trashGroceryListFragmentViewModel.restoreArchivedGroceryList(trashGroceryListFragment.requireContext(),autoGeneratedUniqueId,currentDatetime,GroceryListEntityValues.ACTIVE_STATUS)
                                trashGroceryListFragmentViewModel.groceryLists.removeAt(adapterPosition)

                                withContext(Dispatchers.Main) {

                                    notifyItemRemoved(adapterPosition)
                                    trashGroceryListFragment.hideOrShowGroceryNoListTextView()
                                    Toast.makeText(trashGroceryListFragment.requireContext(),"Grocery list restored successfully.",Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                        R.id.trash_grocery_list_create_copy_menu -> {

                            val oldAutoGeneratedUniqueId  = groceryListItemBinding.groceryListWithCount!!.groceryListEntity.autoGeneratedUniqueId

                            val groceryListNameInputDialog = GroceryListFragment.CustomDialog(trashGroceryListFragment.requireContext())

                            groceryListNameInputDialog.setButtonClickListener(View.OnClickListener {

                                val groceryListName = groceryListNameInputDialog.groceryListName()
                                if(groceryListName.trim().length <=0){
                                    Toast.makeText(trashGroceryListFragment.requireContext(),"Please enter grocery list name",Toast.LENGTH_SHORT).show()
                                    return@OnClickListener

                                }

                                trashGroceryListFragmentViewModel.coroutineScope.launch {

                                    val groceryListWithItemCount = trashGroceryListFragmentViewModel.copy(trashGroceryListFragment.requireContext(), oldAutoGeneratedUniqueId, groceryListName)

                                    withContext(Main){
                                        Toast.makeText(trashGroceryListFragment.requireContext(),"Trash list successfully copy to your list.",Toast.LENGTH_SHORT).show()
                                        groceryListNameInputDialog.mAlertDialog.dismiss()
                                    }
                                }


                            })
                            groceryListNameInputDialog.createPositiveButton("Continue")
                            groceryListNameInputDialog.show()

                        }
                        R.id.trash_grocery_list_delete_permanently_menu -> {

                            val autoGeneratedUniqueId  = groceryListItemBinding.groceryListWithCount!!.groceryListEntity.autoGeneratedUniqueId
                            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            val currentDatetime: String = simpleDateFormat.format(Date())

                            trashGroceryListFragmentViewModel.coroutineScope.launch {

                                trashGroceryListFragmentViewModel.deleteGroceryListPermanently(trashGroceryListFragment.requireContext(),autoGeneratedUniqueId,currentDatetime,GroceryListEntityValues.PERMANENTLY_DELETED_STATUS)
                                trashGroceryListFragmentViewModel.groceryLists.removeAt(adapterPosition)
                                withContext(Main){
                                    notifyItemRemoved(adapterPosition)
                                    Toast.makeText(trashGroceryListFragment.requireContext(),"Grocery list deleted permanently.",Toast.LENGTH_SHORT).show()
                                }
                            }


                        }
                    }
                    true
                })
                popupMenu.show()
            }



        }


    }

}
