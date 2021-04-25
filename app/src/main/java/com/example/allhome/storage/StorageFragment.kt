package com.example.allhome.storage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageEntityWithExtraInformation
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.databinding.FragmentGroceryListBinding
import com.example.allhome.databinding.FragmentStorageBinding
import com.example.allhome.databinding.PantrySimpleExpirationLayoutBinding
import com.example.allhome.databinding.StorageItemBinding
import com.example.allhome.storage.viewmodel.StorageViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StorageFragment : Fragment() {
    private lateinit var mStorageViewModel: StorageViewModel
    private lateinit var mDataBindingUtil: FragmentStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Storage"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)
        mDataBindingUtil = DataBindingUtil.inflate(inflater, R.layout.fragment_storage, container, false)
        mDataBindingUtil.lifecycleOwner = this


        mDataBindingUtil.fab.setOnClickListener{
            val createStorageActivity = Intent(this.context, CreateStorageActivity::class.java)
            startActivity(createStorageActivity)
        }


        val storageViewAdapter = StorageViewAdapter(this)
        mDataBindingUtil.storageStorageRecyclerview.adapter = storageViewAdapter

        mStorageViewModel.coroutineScope.launch {
            mStorageViewModel.getAllStorage(this@StorageFragment.requireContext())
            storageViewAdapter.storageEntities = mStorageViewModel.storageEntitiesWithExtraInformation
            withContext(Main){
                (mDataBindingUtil.storageStorageRecyclerview.adapter as StorageViewAdapter).notifyDataSetChanged()
            }
        }

        return mDataBindingUtil.root
    }


}

class StorageViewAdapter(val storageFragment:StorageFragment): RecyclerView.Adapter<StorageViewAdapter.ItemViewHolder>() {

    var storageEntities:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val storageItemBinding = StorageItemBinding.inflate(layoutInflater,parent,false)
        val itemViewHolder = ItemViewHolder(storageItemBinding)
        return itemViewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pantryItemExpirationEntity = storageEntities[position]
        holder.storageItemBinding.storageEntityWithExtraInformation = pantryItemExpirationEntity
        holder.storageItemBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        Log.e("THE_COUNT",storageEntities.size.toString())
        return storageEntities.size
    }
    inner class  ItemViewHolder(var storageItemBinding: StorageItemBinding): RecyclerView.ViewHolder(storageItemBinding.root),View.OnClickListener{
        init {

            storageItemBinding.storageItemParentLayout.setOnClickListener(this)
            storageItemBinding.moreActionImageView.setOnClickListener(this)

        }

        override fun onClick(view: View?) {

            when(view?.id){
                R.id.storageItemParentLayout->{

                    val storageEntity = storageEntities[adapterPosition].storageEntity
                    val storageActivity = Intent(view!!.context, StorageActivity::class.java)
                    storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG,storageEntity.name)
                    storageFragment.requireActivity().startActivity(storageActivity)
                }

                R.id.moreActionImageView->{

                    val popupMenu = PopupMenu(view.context, view)
                    popupMenu.menuInflater.inflate(R.menu.storage_item_menu, popupMenu.menu)
                    popupMenu.show()

                    popupMenu.setOnMenuItemClickListener{
                        val storageEntity = storageEntities[adapterPosition].storageEntity


                        when (it.itemId) {
                            R.id.viewInformationMenu->{
                                val createStorageActivity = Intent(view!!.context, CreateStorageActivity::class.java)
                                createStorageActivity.putExtra(CreateStorageActivity.ACTION_TAG,CreateStorageActivity.UPDATE_RECORD_ACTION)
                                createStorageActivity.putExtra(CreateStorageActivity.STORAGE_UNIQUE_ID_TAG,storageEntity.uniqueId)
                                storageFragment.requireActivity().startActivity(createStorageActivity)

                            }
                            R.id.viewItemsMenu->{

                                val storageActivity = Intent(view!!.context, StorageActivity::class.java)
                                storageActivity.putExtra(StorageActivity.STORAGE_EXTRA_DATA_TAG,storageEntity.name)
                                storageFragment.requireActivity().startActivity(storageActivity)
                            }
                            R.id.deleteStorageMenu->{

                            }
                        }
                        Toast.makeText(view.context,"Clicked",Toast.LENGTH_SHORT).show()
                        true
                    }
                }
            }

            //startActivity(pantryStorageActivity
        }


    }

}