package com.example.allhome.storage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.databinding.ActivityCreateStorageBinding
import com.example.allhome.databinding.ActivityStorageBinding
import com.example.allhome.storage.viewmodel.StorageViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CreateStorageActivity : AppCompatActivity() {

    lateinit var mStorageViewModel: StorageViewModel
    lateinit var mActivityCreateStorageBinding: ActivityCreateStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mStorageViewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        mActivityCreateStorageBinding = DataBindingUtil.setContentView<ActivityCreateStorageBinding>(this,R.layout.activity_create_storage).apply {
            lifecycleOwner = this@CreateStorageActivity
            storageViewModel = mStorageViewModel
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_storage_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.saveStorageMenu -> {

                saveStorage()

            }
            R.id.updateStorageMenu -> {

            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun saveStorage(){

        var storageUniqueId = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val storageName = mActivityCreateStorageBinding.storageNameTextinput.text.toString()
        val storageDescription = mActivityCreateStorageBinding.storageDescriptionTextinput.text.toString()

        val storageEntity = StorageEntity(
            uniqueId = storageUniqueId,
            name = storageName,
            description = storageDescription,
            imageName = "",
            created = currentDatetime,
            modified = currentDatetime
        )

        mStorageViewModel.coroutineScope.launch {
            val id = mStorageViewModel.addStorage(this@CreateStorageActivity,storageEntity)

            withContext(Main){

                if(id >= 1){
                    Toast.makeText(this@CreateStorageActivity,"Storage save successfully",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@CreateStorageActivity,"Failed to save storage",Toast.LENGTH_SHORT).show()
                }

            }
        }

    }
}