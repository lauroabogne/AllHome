package com.example.allhome.todo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.allhome.R

class TodoFragmentContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_fragment_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer,CreateEditTodoFragment.newInstance("",""))
                commit()
            }
        }

    }
}