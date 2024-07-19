package com.example.allhome.bill.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.allhome.bill.BillsFragment
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import java.util.logging.Handler
import kotlin.collections.ArrayList

class BillViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("BillViewModel"))
    var mTotalAmountDue:Double = 0.0
    var mTotalAmountPaid:Double = 0.0
    var mTotalAmountOverdue:Double = 0.0

    var mStartingCalendar: Calendar = Calendar.getInstance()
    var mEndingCalendar: Calendar = Calendar.getInstance()

    var mVIEWING = BillsFragment.MONTH_VIEWING

     fun addBills(context: Context, bills: ArrayList<BillEntity>):List<Long>{

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
    suspend fun getBillsByDateRange(context:Context,startDate:String,endDate:String):List<BillEntityWithTotalPayment>{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        Log.e("DATES","${startDate} ${endDate}")
        return billDAO.getBillsByDateRange(startDate,endDate)
    }

    suspend fun getBillWithTotalPayment(context:Context,uniqueId:String):BillEntityWithTotalPayment{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.getBillWithTotalPayment(uniqueId)
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

    suspend fun getRecordCountByGroupId(context:Context,groupUniqueId:String):Int{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.getRecordCountByGroupId(groupUniqueId)
    }
    suspend fun updateSelectedAndFutureBillAsDeleted(context: Context,groupUniqueId:String,selectedBillDueDate:String):Int{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.updateSelectedAndFutureBillAsDeleted(groupUniqueId,selectedBillDueDate)
    }
    suspend fun updateSelectedBillAsDeleted(context:Context,billUniqueId:String):Int{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.updateSelectedBillAsDeleted(billUniqueId)
    }
    suspend fun updatePaymentAsDeleted(context:Context,uniqueId:String):Int{
        val billPaymentDAO = AllHomeDatabase.getDatabase(context).getBillPaymentDAO()
        return billPaymentDAO.updatePaymentAsDeleted(uniqueId)

    }
    suspend fun getTotalAmountDue(context:Context,startDate:String,endDate:String):Double{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.getTotalAmountDue(startDate,endDate)
    }
    suspend fun getTotalPaymentAmount(context:Context,startDate:String,endDate:String):Double{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()

        return billDAO.getTotalPaymentAmount(startDate,endDate)
    }
    suspend fun getCategory(context:Context,name:String):BillCategoryEntity{
        val billCategoryDAO = AllHomeDatabase.getDatabase(context).getBillCategoryDAO()
        return billCategoryDAO.getCategory(name)

    }
    suspend fun saveBillCategory(context: Context,billCategory:BillCategoryEntity): Long{
        val billCategoryDAO = AllHomeDatabase.getDatabase(context).getBillCategoryDAO()
        return billCategoryDAO.saveCategory(billCategory)
    }
    suspend fun getOverdueAmount(context:Context,startDate:String,endDate:String):Double{
        val billDAO = AllHomeDatabase.getDatabase(context).getBillItemDAO()
        return billDAO.getTotalOverdue(startDate,endDate)
    }


}