package com.example.allhome.bill.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
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
    suspend fun getBillsInMonth(context:Context,yearMonth:String):List<BillEntityWithTotalPayment>{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.getBillsInMonth(yearMonth)
    }
    suspend fun saveBillPayment(context:Context,billPaymentEntity: BillPaymentEntity):Long{

        val billPaymentDAO = AllHomeDatabase.getDatabase(context).getBillPaymentDAO()
        return billPaymentDAO.saveBillPayment(billPaymentEntity)

    }
    suspend fun getTotalPayment(context:Context,billUniqueId:String):Double{
        val billPaymentDAO = AllHomeDatabase.getDatabase(context).getBillPaymentDAO()
        return billPaymentDAO.getTotalPayment(billUniqueId)

    }

    suspend fun getPayments(context:Context,billUniqueId:String):List<BillPaymentEntity>{
        val billPaymentDAO = AllHomeDatabase.getDatabase(context).getBillPaymentDAO()
        return billPaymentDAO.getBillPayments(billUniqueId)

    }

    suspend fun updatePayment(context:Context,paymentUniqueId: String,paymentAmount:Double,paymentDate:String,
                              paymentNote:String,imageName:String,modified:String):Int{
        val billPaymentDAO = AllHomeDatabase.getDatabase(context).getBillPaymentDAO()
        return billPaymentDAO.updatePayment(paymentUniqueId,paymentAmount,paymentDate,paymentNote,imageName,modified)
    }
}