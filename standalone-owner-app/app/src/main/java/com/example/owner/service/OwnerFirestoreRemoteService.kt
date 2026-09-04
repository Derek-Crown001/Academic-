package com.example.owner.service

import android.util.Log
import com.example.data.model.AppOwnerLicenseConfig
import com.example.data.model.SubscriptionTier
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Data representation of a School Instance status stored in Firestore.
 */
data class SchoolRemoteInstanceStatus(
    val schoolId: String = "",
    val schoolName: String = "",
    val isAccountLocked: Boolean = false,
    val lockReason: String = "",
    val lockedAtMillis: Long = 0L,
    val lockedBy: String = "Platform Master",
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val maintenanceExpectedEnd: String = "",
    val maintenanceEngagedAtMillis: Long = 0L,
    val maintenanceEngagedBy: String = "Platform Master",
    val subscriptionTier: String = SubscriptionTier.ANNUAL.name,
    val subscriptionExpiryDateMillis: Long = 0L,
    val activeLicenseKey: String = "",
    val granularLocks: Map<String, Boolean> = emptyMap(),
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromSnapshot(doc: DocumentSnapshot): SchoolRemoteInstanceStatus? {
            if (!doc.exists()) return null
            return try {
                @Suppress("UNCHECKED_CAST")
                val locks = (doc.get("granularLocks") as? Map<String, Boolean>) ?: emptyMap()
                SchoolRemoteInstanceStatus(
                    schoolId = doc.id,
                    schoolName = doc.getString("schoolName") ?: doc.id,
                    isAccountLocked = doc.getBoolean("isAccountLocked") ?: doc.getBoolean("isAppLocked") ?: false,
                    lockReason = doc.getString("lockReason") ?: doc.getString("accountLockReason") ?: "",
                    lockedAtMillis = doc.getLong("lockedAtMillis") ?: 0L,
                    lockedBy = doc.getString("lockedBy") ?: "Platform Master",
                    isMaintenanceMode = doc.getBoolean("isMaintenanceMode") ?: false,
                    maintenanceMessage = doc.getString("maintenanceMessage") ?: "",
                    maintenanceExpectedEnd = doc.getString("maintenanceExpectedEnd") ?: "",
                    maintenanceEngagedAtMillis = doc.getLong("maintenanceEngagedAtMillis") ?: 0L,
                    maintenanceEngagedBy = doc.getString("maintenanceEngagedBy") ?: "Platform Master",
                    subscriptionTier = doc.getString("subscriptionTier") ?: SubscriptionTier.ANNUAL.name,
                    subscriptionExpiryDateMillis = doc.getLong("subscriptionExpiryDateMillis") ?: 0L,
                    activeLicenseKey = doc.getString("activeLicenseKey") ?: "",
                    granularLocks = locks,
                    lastUpdatedMillis = doc.getLong("lastUpdatedMillis") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.w("OwnerFirestoreService", "Error mapping document ${doc.id}: ${e.message}")
                null
            }
        }
    }
}

/**
 * Service in the Owner module that directly interfaces with Cloud Firestore
 * to toggle 'maintenance mode' or 'account lock' for specific school instances
 * identified by their unique ID (e.g. schoolCode / tenantId).
 */
class OwnerFirestoreRemoteService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "OwnerFirestoreService"
        const val COLLECTION_SCHOOL_INSTANCES = "school_instances"
        const val COLLECTION_SYSTEM_AUDIT_LOGS = "platform_owner_audit_logs"

        @Volatile
        private var INSTANCE: OwnerFirestoreRemoteService? = null

        fun getInstance(): OwnerFirestoreRemoteService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    OwnerFirestoreRemoteService(FirebaseFirestore.getInstance()).also { INSTANCE = it }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed initializing FirebaseFirestore instance: ${e.message}")
                    OwnerFirestoreRemoteService().also { INSTANCE = it }
                }
            }
        }
    }

    /**
     * Toggles 'account lock' for a specific school instance identified by its unique ID.
     * When locked, the school app's lock-gate / paywall triggers immediately in real time.
     *
     * @param schoolId Unique identifier of the school instance (e.g. "SCH-KINGSWAY-01")
     * @param isLocked True to lock the account; false to unlock and restore access.
     * @param reason The explanatory text displayed to school administrators upon lockout.
     * @param lockedBy The master operator who invoked this action.
     */
    suspend fun toggleAccountLock(
        schoolId: String,
        isLocked: Boolean,
        reason: String = "Account access suspended by Platform Owner.",
        lockedBy: String = "Platform Master"
    ): Result<Boolean> {
        val cleanId = schoolId.trim().ifEmpty { "DEFAULT-SCHOOL" }
        return try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "schoolId" to cleanId,
                "isAccountLocked" to isLocked,
                "isAppLocked" to isLocked,
                "lockReason" to reason,
                "lockedAtMillis" to if (isLocked) now else 0L,
                "lockedBy" to lockedBy,
                "lastUpdatedMillis" to now
            )

            firestore.collection(COLLECTION_SCHOOL_INSTANCES)
                .document(cleanId)
                .set(updates, SetOptions.merge())
                .await()

            // Record audit log entry in Firestore
            logAuditAction(
                action = if (isLocked) "ACCOUNT_LOCKED" else "ACCOUNT_UNLOCKED",
                schoolId = cleanId,
                details = "Status: $isLocked, Reason: $reason, Operator: $lockedBy"
            )

            Log.d(TAG, "Successfully toggled account lock for school $cleanId to $isLocked")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling account lock for school $cleanId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Toggles 'maintenance mode' for a specific school instance identified by its unique ID.
     * Suspends school user access and displays the custom maintenance advisory notice.
     *
     * @param schoolId Unique identifier of the school instance
     * @param isMaintenance True to activate maintenance mode; false to restore normal operation.
     * @param message Custom maintenance message displayed on school screens.
     * @param expectedEnd Human-readable expected completion time (e.g. "Today at 4:00 PM").
     * @param engagedBy The administrator / operator engaging the mode.
     */
    suspend fun toggleMaintenanceMode(
        schoolId: String,
        isMaintenance: Boolean,
        message: String = "Platform maintenance in progress. All operations temporarily suspended.",
        expectedEnd: String = "In 2 hours",
        engagedBy: String = "Platform Master"
    ): Result<Boolean> {
        val cleanId = schoolId.trim().ifEmpty { "DEFAULT-SCHOOL" }
        return try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "schoolId" to cleanId,
                "isMaintenanceMode" to isMaintenance,
                "maintenanceMessage" to message,
                "maintenanceExpectedEnd" to expectedEnd,
                "maintenanceEngagedAtMillis" to if (isMaintenance) now else 0L,
                "maintenanceEngagedBy" to engagedBy,
                "lastUpdatedMillis" to now
            )

            firestore.collection(COLLECTION_SCHOOL_INSTANCES)
                .document(cleanId)
                .set(updates, SetOptions.merge())
                .await()

            // Record audit log entry in Firestore
            logAuditAction(
                action = if (isMaintenance) "MAINTENANCE_ENGAGED" else "MAINTENANCE_DISENGAGED",
                schoolId = cleanId,
                details = "Maintenance: $isMaintenance, Message: $message, ExpectedEnd: $expectedEnd"
            )

            Log.d(TAG, "Successfully toggled maintenance mode for school $cleanId to $isMaintenance")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling maintenance mode for school $cleanId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates granular feature locks for a specific school instance in Firestore.
     */
    suspend fun updateGranularFeatureLocks(
        schoolId: String,
        locks: Map<String, Boolean>
    ): Result<Boolean> {
        val cleanId = schoolId.trim().ifEmpty { "DEFAULT-SCHOOL" }
        return try {
            val updates = mapOf(
                "granularLocks" to locks,
                "lastUpdatedMillis" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_SCHOOL_INSTANCES)
                .document(cleanId)
                .set(updates, SetOptions.merge())
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating feature locks for $cleanId: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes a complete local AppOwnerLicenseConfig object to Firestore for the given school.
     */
    suspend fun syncSchoolInstanceConfig(config: AppOwnerLicenseConfig): Result<Boolean> {
        return try {
            val granularLocks = mapOf(
                "isCbtLocked" to config.isCbtLocked,
                "isAiAssistantLocked" to config.isAiAssistantLocked,
                "isReportCardLocked" to config.isReportCardLocked,
                "isTeacherAttendanceLocked" to config.isTeacherAttendanceLocked,
                "isStudentManagementLocked" to config.isStudentManagementLocked,
                "isChatRoomsLocked" to config.isChatRoomsLocked,
                "isParentPortalLocked" to config.isParentPortalLocked
            )

            val payload = mapOf(
                "schoolId" to config.schoolCode,
                "schoolName" to config.schoolName,
                "isAccountLocked" to config.isAppLocked,
                "isAppLocked" to config.isAppLocked,
                "lockReason" to config.lockReason,
                "isMaintenanceMode" to config.isMaintenanceMode,
                "maintenanceMessage" to config.maintenanceMessage,
                "maintenanceExpectedEnd" to config.maintenanceExpectedEnd,
                "subscriptionTier" to config.subscriptionTier.name,
                "subscriptionStartDateMillis" to config.subscriptionStartDateMillis,
                "subscriptionExpiryDateMillis" to config.subscriptionExpiryDateMillis,
                "gracePeriodDays" to config.gracePeriodDays,
                "activeLicenseKey" to config.activeLicenseKey,
                "ownerBankName" to config.ownerBankName,
                "ownerAccountNumber" to config.ownerAccountNumber,
                "ownerAccountName" to config.ownerAccountName,
                "subscriptionFeePerTerm" to config.subscriptionFeePerTerm,
                "subscriptionFeePerYear" to config.subscriptionFeePerYear,
                "currencySymbol" to config.currencySymbol,
                "principalName" to config.principalName,
                "schoolCity" to config.schoolCity,
                "estimatedStudents" to config.estimatedStudents,
                "estimatedTeachers" to config.estimatedTeachers,
                "granularLocks" to granularLocks,
                "lastUpdatedMillis" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_SCHOOL_INSTANCES)
                .document(config.schoolCode)
                .set(payload, SetOptions.merge())
                .await()

            Log.d(TAG, "Synced full configuration for school ${config.schoolCode} to Firestore")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing full config for ${config.schoolCode}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Batched operation: Toggles emergency global maintenance mode across a list of school IDs.
     */
    suspend fun batchToggleGlobalMaintenance(
        isMaintenance: Boolean,
        message: String,
        schoolIds: List<String>
    ): Result<Int> {
        return try {
            var updatedCount = 0
            val batch = firestore.batch()
            val now = System.currentTimeMillis()

            for (schoolId in schoolIds) {
                val docRef = firestore.collection(COLLECTION_SCHOOL_INSTANCES).document(schoolId)
                val updates = mapOf(
                    "isMaintenanceMode" to isMaintenance,
                    "maintenanceMessage" to message,
                    "maintenanceEngagedAtMillis" to if (isMaintenance) now else 0L,
                    "lastUpdatedMillis" to now
                )
                batch.set(docRef, updates, SetOptions.merge())
                updatedCount++
            }

            batch.commit().await()
            logAuditAction(
                action = "BATCH_GLOBAL_MAINTENANCE",
                schoolId = "ALL_TENANTS",
                details = "Count: $updatedCount, Status: $isMaintenance, Message: $message"
            )
            Result.success(updatedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing batch maintenance: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time flow of a single School Instance status from Firestore.
     */
    fun observeSchoolRemoteStatus(schoolId: String): Flow<SchoolRemoteInstanceStatus?> = callbackFlow {
        val cleanId = schoolId.trim().ifEmpty { "DEFAULT-SCHOOL" }
        val listener = firestore.collection(COLLECTION_SCHOOL_INSTANCES)
            .document(cleanId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for $cleanId: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val status = SchoolRemoteInstanceStatus.fromSnapshot(snapshot)
                    trySend(status)
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Real-time flow of all School Instances from Firestore.
     */
    fun observeAllRemoteSchools(): Flow<List<SchoolRemoteInstanceStatus>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_SCHOOL_INSTANCES)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for all schools: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { SchoolRemoteInstanceStatus.fromSnapshot(it) }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    private suspend fun logAuditAction(action: String, schoolId: String, details: String) {
        try {
            val auditEntry = mapOf(
                "action" to action,
                "schoolId" to schoolId,
                "details" to details,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_SYSTEM_AUDIT_LOGS)
                .add(auditEntry)
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Audit log write failed: ${e.message}")
        }
    }
}
