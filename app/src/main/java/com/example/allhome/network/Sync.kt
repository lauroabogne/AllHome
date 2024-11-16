package com.example.allhome.network

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import androidx.room.withTransaction
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.SyncNotificationProgress
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.network.uploads.BillsPaymentsUpload
import com.example.allhome.network.uploads.BillsUpload
import com.example.allhome.network.uploads.ExpensesUpload
import com.example.allhome.network.uploads.GroceryListUpload
import com.example.allhome.utils.ImageUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Sync private constructor(private val context: Context) {

    // Static singleton instance
    companion object {
        @Volatile
        private var instance: Sync? = null

        fun getInstance(context: Context): Sync {
            return instance ?: synchronized(this) {
                instance ?: Sync(context).also { instance = it }
            }
        }
    }

    // Dependencies
    private val billsUpload: BillsUpload by lazy {
        val billDAO = (context as AllHomeBaseApplication).billDAO
        BillsUpload(RetrofitInstance.api, billDAO)
    }

    private val billPaymentsUpload: BillsPaymentsUpload by lazy {
        val billPaymentDAO = (context as AllHomeBaseApplication).billPaymentDAO
        BillsPaymentsUpload(RetrofitInstance.api, billPaymentDAO)
    }

    private val expensesUpload: ExpensesUpload by lazy {
        val expensesDAO = (context as AllHomeBaseApplication).expensesDAO
        ExpensesUpload(RetrofitInstance.api, expensesDAO)
    }
    private val groceryListUpload : GroceryListUpload by lazy{
        val groceryListDAO = (context as AllHomeBaseApplication).groceryListDAO
        val groceryItemDAO = (context as AllHomeBaseApplication).groceryItemDAO
        GroceryListUpload(RetrofitInstance.api, groceryListDAO, groceryItemDAO)
    }
    // List of tables to sync
    private val NEED_TO_SYNC = arrayOf("Bills","Bills Payments", "Expenses","Grocery List")

    // Notification instance
    private val syncNotification = SyncNotificationProgress(context)

    // Job to handle coroutine cancellation
    @Volatile
    private var syncJob: Job? = null

    // Start sync process
    fun startSync() {
        // Cancel any ongoing sync process
        syncJob?.cancel()

        // Initialize notification with overall progress
        syncNotification.showOverallProgressNotification(0, NEED_TO_SYNC.size)

        // Start a new sync process
        syncJob = CoroutineScope(Dispatchers.IO).launch {
            var overallProgress = 0
            val totalItemsToSync = NEED_TO_SYNC.size
            for ((index, table) in NEED_TO_SYNC.withIndex()) {
                when (table) {
                    "Bills" -> {


                        billsUpload();
                        delay(1000)
                        syncNotification.showDetailedProgressMessageNotification("Preparing to upload bills $index")



                    }
                    "Bills Payments" -> {

                        billPaymentsUpload()
                        delay(1000) // Simulate bill payments upload delay
                        syncNotification.showDetailedProgressMessageNotification("Preparing to upload bills payments")



                    }
                    // Add more cases for other tables as needed
                    "Expenses" -> {

                        expensesUpload()
                        delay(1000)
                        syncNotification.showDetailedProgressMessageNotification("Preparing to upload expenses")

                    }
                    "Grocery List" ->{
                        groceryListUpload()
                        delay(1000)
                        syncNotification.showDetailedProgressMessageNotification("Preparing to upload grocery list")
                    }
                }

                // Update overall progress
                overallProgress ++
                syncNotification.showOverallProgressNotification(overallProgress, totalItemsToSync)
            }

            // Once sync is complete
            syncNotification.completeSync()
        }
    }
    private suspend fun billsUpload(){

        val uniqueIdsToUpload =  billsUpload.getBillsToUpload()

        uniqueIdsToUpload.forEachIndexed() { index, billUniqueId ->
            val billEntity = billsUpload.getBillByUniqueId(billUniqueId)
            billEntity?.let {
                val syncResult = billsUpload.uploadBill(billEntity)
                if(syncResult.isSuccess){
                    // update as uploaded
                    billsUpload.updateBillAsUploaded(billUniqueId)
                    Log.e("Sync", "Bill uploaded: $billUniqueId ${syncResult.message}")
                }else{
                    // Create log
                    Log.e("Sync", "Failed to upload bill: $billUniqueId ${syncResult.errorMessage}")
                }
            }

            syncNotification.showDetailedProgressNotification("Bill upload ",index + 1 , uniqueIdsToUpload.size);
        }

    }
    private suspend fun billPaymentsUpload(){
        val uniqueIdsToUpload =  billPaymentsUpload.getBillsPaymentToUpload()

        uniqueIdsToUpload.forEachIndexed(){ index, billPaymentUniqueId ->
            val billPaymentEntity = billPaymentsUpload.getBillPaymentByUniqueId(billPaymentUniqueId)
            billPaymentEntity?.let {

                val imageURI = ImageUtil.getImageUriFromPath(context, ImageUtil.BILL_PAYMENT_IMAGES_FINAL_LOCATION, billPaymentEntity.imageName)
                val imageInputStream = if(imageURI != null) ImageUtil.getInputStreamFromUri(context, imageURI) else null
                val syncResult =  billPaymentsUpload.uploadBillPayments(billPaymentEntity, imageInputStream)

                if(syncResult.isSuccess){
                    // update as uploaded
                    billPaymentsUpload.updateBillPaymentAsUploaded(billPaymentUniqueId)

                    Log.e("Sync", "Bill Payment uploaded: $billPaymentUniqueId ${syncResult.message}")
                }else{
                    // Create log
                    Log.e("Sync", "Failed to upload Bill Payment: $billPaymentUniqueId ${syncResult.errorMessage}")
                }
            }

            syncNotification.showDetailedProgressNotification("Bill payments upload ",index + 1 , uniqueIdsToUpload.size);
            delay(3000)
        }

    }
    private suspend fun expensesUpload(){

        val uniqueIdsToUpload =  expensesUpload.getExpensesToUpload()

        uniqueIdsToUpload.forEachIndexed() { index, expenseUniqueId ->
            val expensesEntity = expensesUpload.getExpensesByUniqueId(expenseUniqueId)
            expensesEntity?.let {
                val syncResult = expensesUpload.uploadExpense(expensesEntity)
                Log.e("Sync", "Expense to upload: $syncResult")
                if(syncResult.isSuccess){
                    // update as uploaded
                    expensesUpload.updateExpensesAsUploaded(expenseUniqueId)

                }else{
                    // Create log
//                    Log.e("Sync", "Failed to upload bill: $billUniqueId ${syncResult.errorMessage}")
                }
            }
            delay(3000)
            syncNotification.showDetailedProgressNotification("Expense upload ",index + 1 , uniqueIdsToUpload.size);
        }
    }

    private suspend fun groceryListUpload(){

        val uniqueIdsToUpload =  groceryListUpload.getGroceryListUniqueIdToUpload()

        uniqueIdsToUpload.forEachIndexed(){index,uniqueId->
            val groceryList = groceryListUpload.getGroceryListByUniqueId(uniqueId)

            groceryList?.let {
                val groceryListItems = groceryListUpload.getGroceryListItems(uniqueId)
                val syncResult = groceryListUpload.uploadGroceryList(context,groceryList, groceryListItems)
                if(syncResult.isSuccess){

                  val groceryListUpdatedCount = groceryListUpload.updateGroceryListAsUploaded(uniqueId)
                  val groceryListItemsUpdatedCount = groceryListUpload.updateGroceryListItemsAsUploaded(uniqueId)
                }
            }
        }

//        val uniqueIdsToUpload =  expensesUpload.getExpensesToUpload()
//
//        uniqueIdsToUpload.forEachIndexed() { index, expenseUniqueId ->
//            val expensesEntity = expensesUpload.getExpensesByUniqueId(expenseUniqueId)
//            expensesEntity?.let {
//                val syncResult = expensesUpload.uploadExpense(expensesEntity)
//                Log.e("Sync", "Expense to upload: $syncResult")
//                if(syncResult.isSuccess){
//                    // update as uploaded
//                    expensesUpload.updateExpensesAsUploaded(expenseUniqueId)
//
//                }else{
//                    // Create log
////                    Log.e("Sync", "Failed to upload bill: $billUniqueId ${syncResult.errorMessage}")
//                }
//            }
//            delay(3000)
//            syncNotification.showDetailedProgressNotification("Expense upload ",index + 1 , uniqueIdsToUpload.size);
//        }
    }
}
