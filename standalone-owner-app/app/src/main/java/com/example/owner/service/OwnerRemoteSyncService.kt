package com.example.owner.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

/**
 * Android Background Service in the Owner module that manages asynchronous operations
 * and Firestore remote dispatching for toggling 'maintenance mode' or 'account lock'
 * for specific school instances identified by unique ID.
 */
class OwnerRemoteSyncService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val firestoreService = OwnerFirestoreRemoteService.getInstance()

    companion object {
        private const val TAG = "OwnerRemoteSyncService"

        const val ACTION_TOGGLE_ACCOUNT_LOCK = "com.example.owner.action.TOGGLE_ACCOUNT_LOCK"
        const val ACTION_TOGGLE_MAINTENANCE = "com.example.owner.action.TOGGLE_MAINTENANCE"
        const val ACTION_SYNC_SCHOOL_INSTANCE = "com.example.owner.action.SYNC_SCHOOL_INSTANCE"
        const val ACTION_BROADCAST_GLOBAL_MAINTENANCE = "com.example.owner.action.BROADCAST_GLOBAL_MAINTENANCE"

        const val EXTRA_SCHOOL_ID = "com.example.owner.extra.SCHOOL_ID"
        const val EXTRA_IS_LOCKED = "com.example.owner.extra.IS_LOCKED"
        const val EXTRA_LOCK_REASON = "com.example.owner.extra.LOCK_REASON"
        const val EXTRA_IS_MAINTENANCE = "com.example.owner.extra.IS_MAINTENANCE"
        const val EXTRA_MAINTENANCE_MESSAGE = "com.example.owner.extra.MAINTENANCE_MESSAGE"
        const val EXTRA_EXPECTED_END = "com.example.owner.extra.EXPECTED_END"
        const val EXTRA_OPERATOR_NAME = "com.example.owner.extra.OPERATOR_NAME"
        const val EXTRA_SCHOOL_ID_LIST = "com.example.owner.extra.SCHOOL_ID_LIST"

        /**
         * Helper to trigger remote account lock toggle via Service Intent.
         */
        fun toggleAccountLock(
            context: Context,
            schoolId: String,
            isLocked: Boolean,
            reason: String = "Account access suspended by Platform Owner.",
            operatorName: String = "Platform Master"
        ) {
            val intent = Intent(context, OwnerRemoteSyncService::class.java).apply {
                action = ACTION_TOGGLE_ACCOUNT_LOCK
                putExtra(EXTRA_SCHOOL_ID, schoolId)
                putExtra(EXTRA_IS_LOCKED, isLocked)
                putExtra(EXTRA_LOCK_REASON, reason)
                putExtra(EXTRA_OPERATOR_NAME, operatorName)
            }
            context.startService(intent)
        }

        /**
         * Helper to trigger remote maintenance mode toggle via Service Intent.
         */
        fun toggleMaintenance(
            context: Context,
            schoolId: String,
            isMaintenance: Boolean,
            message: String = "Platform maintenance in progress. All operations temporarily suspended.",
            expectedEnd: String = "In 2 hours",
            operatorName: String = "Platform Master"
        ) {
            val intent = Intent(context, OwnerRemoteSyncService::class.java).apply {
                action = ACTION_TOGGLE_MAINTENANCE
                putExtra(EXTRA_SCHOOL_ID, schoolId)
                putExtra(EXTRA_IS_MAINTENANCE, isMaintenance)
                putExtra(EXTRA_MAINTENANCE_MESSAGE, message)
                putExtra(EXTRA_EXPECTED_END, expectedEnd)
                putExtra(EXTRA_OPERATOR_NAME, operatorName)
            }
            context.startService(intent)
        }

        /**
         * Helper to broadcast global maintenance mode to a list of school instances.
         */
        fun broadcastGlobalMaintenance(
            context: Context,
            isMaintenance: Boolean,
            message: String,
            schoolIds: ArrayList<String>
        ) {
            val intent = Intent(context, OwnerRemoteSyncService::class.java).apply {
                action = ACTION_BROADCAST_GLOBAL_MAINTENANCE
                putExtra(EXTRA_IS_MAINTENANCE, isMaintenance)
                putExtra(EXTRA_MAINTENANCE_MESSAGE, message)
                putStringArrayListExtra(EXTRA_SCHOOL_ID_LIST, schoolIds)
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val action = intent.action
        val schoolId = intent.getStringExtra(EXTRA_SCHOOL_ID) ?: ""

        serviceScope.launch {
            try {
                when (action) {
                    ACTION_TOGGLE_ACCOUNT_LOCK -> {
                        val isLocked = intent.getBooleanExtra(EXTRA_IS_LOCKED, false)
                        val reason = intent.getStringExtra(EXTRA_LOCK_REASON) ?: "Account access suspended by Platform Owner."
                        val operator = intent.getStringExtra(EXTRA_OPERATOR_NAME) ?: "Platform Master"

                        Log.i(TAG, "Executing remote account lock toggle: school=$schoolId, locked=$isLocked")
                        firestoreService.toggleAccountLock(
                            schoolId = schoolId,
                            isLocked = isLocked,
                            reason = reason,
                            lockedBy = operator
                        )
                    }

                    ACTION_TOGGLE_MAINTENANCE -> {
                        val isMaintenance = intent.getBooleanExtra(EXTRA_IS_MAINTENANCE, false)
                        val message = intent.getStringExtra(EXTRA_MAINTENANCE_MESSAGE)
                            ?: "Platform maintenance in progress. All operations temporarily suspended."
                        val expectedEnd = intent.getStringExtra(EXTRA_EXPECTED_END) ?: "In 2 hours"
                        val operator = intent.getStringExtra(EXTRA_OPERATOR_NAME) ?: "Platform Master"

                        Log.i(TAG, "Executing remote maintenance mode toggle: school=$schoolId, maintenance=$isMaintenance")
                        firestoreService.toggleMaintenanceMode(
                            schoolId = schoolId,
                            isMaintenance = isMaintenance,
                            message = message,
                            expectedEnd = expectedEnd,
                            engagedBy = operator
                        )
                    }

                    ACTION_BROADCAST_GLOBAL_MAINTENANCE -> {
                        val isMaintenance = intent.getBooleanExtra(EXTRA_IS_MAINTENANCE, false)
                        val message = intent.getStringExtra(EXTRA_MAINTENANCE_MESSAGE)
                            ?: "Emergency Platform Maintenance engaged."
                        val schoolIds = intent.getStringArrayListExtra(EXTRA_SCHOOL_ID_LIST) ?: arrayListOf()

                        Log.i(TAG, "Executing batch global maintenance broadcast across ${schoolIds.size} schools")
                        firestoreService.batchToggleGlobalMaintenance(
                            isMaintenance = isMaintenance,
                            message = message,
                            schoolIds = schoolIds
                        )
                    }

                    else -> {
                        Log.w(TAG, "Unhandled service action: $action")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception running service action $action: ${e.message}", e)
            } finally {
                stopSelf(startId)
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "OwnerRemoteSyncService destroyed and jobs cancelled.")
    }
}
