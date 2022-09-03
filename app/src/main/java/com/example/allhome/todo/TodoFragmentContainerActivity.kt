package com.example.allhome.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.allhome.R
import com.example.allhome.storage.StorageAddItemActivity

class TodoFragmentContainerActivity : AppCompatActivity() {

    companion object {
        const val FRAGMENT_NAME_TAG = "FRAGMENT_NAME_TAG"
        const val CREATE_TODO_FRAGMENT = "CREATE_TODO_FRAGMENT"
        const val VIEW_TODO_FRAGMENT = "VIEW_TODO_FRAGMENT"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_fragment_container)

        val fragmentName = intent.getStringExtra(FRAGMENT_NAME_TAG)

        if (savedInstanceState == null) {
            if(fragmentName == CREATE_TODO_FRAGMENT ){

                intent.getStringExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG)?.let {

                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentContainer,CreateEditTodoFragment.newInstance(it))
                        commit()
                    }
                }?:run{
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.fragmentContainer,CreateEditTodoFragment.newInstance())
                        commit()
                    }
                }

            }else if(fragmentName == VIEW_TODO_FRAGMENT ){
                val todoUniqueId = intent.getStringExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG)!!

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragmentContainer,ViewTodoFragment.newInstance(todoUniqueId))
                    commit()
                }
            }

        }

    }


}