package com.example.allhome.bill

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.example.allhome.R
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.meal_planner.ViewerActivity

class BillActivity : AppCompatActivity() {
    companion object{
        const val TITLE_TAG = "TITLE_TAG"
        const val WHAT_FRAGMENT = "WHAT_FRAGMENT"
        const val ADD_BILL_FRAGMENT = 0
        const val ADD_BILL_PAYMENT_FRAGMENT = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        intent.getStringExtra(TITLE_TAG)?.let{ titleParam->
            toolbar.title = titleParam
        }

        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener(toolbarNavigationClickListener)

        val whatFragment = intent.getIntExtra(WHAT_FRAGMENT, ADD_BILL_FRAGMENT)

        when(whatFragment){
            ADD_BILL_FRAGMENT->{
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container,AddBillFragment.newInstance("",""))
                    .commit()
            }
            ADD_BILL_PAYMENT_FRAGMENT->{
                val billEntity = intent.getParcelableExtra<BillEntity>(AddPaymentFragment.ARG_BILL_ENTITY)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container,AddPaymentFragment.newInstance(billEntity!!,""))
                    .commit()
            }
        }




    }
    val toolbarNavigationClickListener= object: View.OnClickListener{
        override fun onClick(v: View?) {
            this@BillActivity.finish()
        }

    }

}