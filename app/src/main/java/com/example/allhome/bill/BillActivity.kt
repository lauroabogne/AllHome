package com.example.allhome.bill

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.example.allhome.R
import com.example.allhome.meal_planner.ViewerActivity

class BillActivity : AppCompatActivity() {
    companion object{
        const val TITLE_TAG = "TITLE_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        intent.getStringExtra(TITLE_TAG)?.let{ titleParam->
            toolbar.title = titleParam
        }

        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener(toolbarNavigationClickListener)
    }
    val toolbarNavigationClickListener= object: View.OnClickListener{
        override fun onClick(v: View?) {
            this@BillActivity.finish()
        }

    }

}