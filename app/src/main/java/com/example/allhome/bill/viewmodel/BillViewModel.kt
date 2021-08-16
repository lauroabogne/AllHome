package com.example.allhome.bill.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.MealEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class BillViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("BillViewModel"))

    suspend fun addBills(context: Context, bills: ArrayList<BillEntity>):List<Long>{

        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.saveBills(bills)

    }

    suspend fun addBill(context: Context, bill: BillEntity):Long{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.saveBill(bill)
    }
    suspend fun getBillsInMonth(context:Context,yearMonth:String):List<BillEntity>{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.getBillsInMonth(yearMonth)
    }
}