package com.example.allhome.network

import android.content.Context
import android.util.Log
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.SyncNotificationProgress
import com.example.allhome.network.uploads.BillsUpload
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

    // List of tables to sync
    private val NEED_TO_SYNC = arrayOf("Bills", "Expenses")

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
                        syncNotification.showOverallProgressNotification(overallProgress, NEED_TO_SYNC.size)
                        syncNotification.showDetailedProgressMessageNotification("Preparing to upload bills")
                        delay(5000)
                        billsUpload();
                    }
                    "Expenses" -> {
                        // Handle expense upload if needed
                        // You can add detailed progress update here if needed
                        syncNotification.showOverallProgressNotification(overallProgress, NEED_TO_SYNC.size)
                        delay(1000) // Simulate expense upload delay
                    }
                }
                overallProgress = index + 1
                // Update overall progress
                syncNotification.showOverallProgressNotification(overallProgress, totalItemsToSync)
            }

            // Once sync is complete
            syncNotification.completeSync()
        }
    }
    private suspend fun billsUpload(){

        val uniqueIdsToUpload =  billsUpload.getBillsToUpload()

       // Log.e("uniqueIdsToUpload", uniqueIdsToUpload.size.toString())
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

            syncNotification.showDetailedProgressNotification("Bill upload : ",index + 1 , uniqueIdsToUpload.size);
        }

//        val perItemTotalItemToSync = 10
//        for (x in 1..uniqueIdsToUpload.size) {
//            billsUpload.uploadBills() // Adjust as necessary
//            // Update detailed progress
//            syncNotification.showDetailedProgressNotification("Bill upload : ",x , perItemTotalItemToSync);
//
//            delay(1000) // Adjust delay as necessary
//        }
    }
}
