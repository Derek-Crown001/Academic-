package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Subscription tiers for school licensing.
 */
enum class SubscriptionTier {
    TRIAL,
    TERMLY,      // 1 Term (e.g. 120 Days)
    ANNUAL,      // 1 Academic Session (365 Days)
    LIFETIME,
    EXPIRED,
    SUSPENDED
}

/**
 * Types of official memos and invoices sent by the App Owner to school owners.
 */
enum class OwnerMemoType {
    PAYMENT_INVOICE,
    SUBSCRIPTION_RENEWAL,
    SUSPENSION_WARNING,
    GENERAL_ANNOUNCEMENT,
    MAINTENANCE_NOTICE
}

/**
 * Status of payment proofs submitted by school owners.
 */
enum class PaymentClaimStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * Master License and Feature Lock Configuration for a school.
 * Controls app-wide access and individual feature locks.
 */
@Entity(tableName = "app_owner_license_config")
data class AppOwnerLicenseConfig(
    @PrimaryKey val schoolCode: String = "SCH-KINGSWAY-01",
    val schoolName: String = "Kingsway Model International College",
    val isAppLocked: Boolean = false,
    val lockReason: String = "Your school's subscription has expired. Please contact the App Owner to renew your license.",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.ANNUAL,
    val subscriptionStartDateMillis: Long = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000), // started 15 days ago
    val subscriptionExpiryDateMillis: Long = System.currentTimeMillis() + (350L * 24 * 60 * 60 * 1000), // expires in 350 days
    val gracePeriodDays: Int = 7,
    val registeredOwnerEmail: String = "owner@acadamiatrack.io",
    val ownerContactPhone: String = "+234 803 123 4567",
    val ownerBankName: String = "Zenith Bank Plc",
    val ownerAccountNumber: String = "1012345678",
    val ownerAccountName: String = "AcademiaTrack Global Systems Ltd",
    val subscriptionFeePerTerm: Double = 150000.0,
    val subscriptionFeePerYear: Double = 400000.0,
    val currencySymbol: String = "₦",
    // Granular Feature-Level Locks
    val isCbtLocked: Boolean = false,
    val isAiAssistantLocked: Boolean = false,
    val isReportCardLocked: Boolean = false,
    val isTeacherAttendanceLocked: Boolean = false,
    val isStudentManagementLocked: Boolean = false,
    val isChatRoomsLocked: Boolean = false,
    val isParentPortalLocked: Boolean = false,
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Platform maintenance in progress. All operations temporarily suspended.",
    val maintenanceExpectedEnd: String = "In 2 hours",
    val activeLicenseKey: String = "ACAD-PREM-2026-X89K",
    val lastSyncTimestampMillis: Long = System.currentTimeMillis(),
    val principalName: String = "Dr. C. Adebayo",
    val schoolCity: String = "Lagos",
    val estimatedStudents: Int = 420,
    val estimatedTeachers: Int = 34
) {
    val daysRemaining: Int
        get() = maxOf(0, ((subscriptionExpiryDateMillis - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)).toInt())

    val isOverdue: Boolean
        get() = subscriptionExpiryDateMillis < System.currentTimeMillis() || isAppLocked

    val isDueSoon: Boolean
        get() = !isOverdue && daysRemaining <= 14

    val statusBadgeText: String
        get() = when {
            isMaintenanceMode -> "MAINTENANCE"
            isAppLocked -> "LOCKED"
            isOverdue -> "SUBSCRIPTION DUE"
            isDueSoon -> "DUE IN ${daysRemaining}d"
            subscriptionTier == SubscriptionTier.TRIAL -> "TRIAL (${daysRemaining}d)"
            else -> "ACTIVE (${daysRemaining}d)"
        }
}

/**
 * Official Memo / Invoice sent directly by App Owner to School Owners/Admins.
 */
@Entity(tableName = "app_owner_memos")
data class AppOwnerMemo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolCode: String = "SCH-KINGSWAY-01",
    val title: String,
    val memoBody: String,
    val memoType: OwnerMemoType = OwnerMemoType.PAYMENT_INVOICE,
    val amountDue: Double = 0.0,
    val currency: String = "₦",
    val dueDate: String = "",
    val paymentBank: String = "Zenith Bank Plc",
    val accountNumber: String = "1012345678",
    val accountName: String = "AcademiaTrack Global Systems Ltd",
    val isPaid: Boolean = false,
    val isDismissed: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * Payment proof / claim submitted by school owners for the App Owner to review and approve.
 */
@Entity(tableName = "app_owner_payment_claims")
data class AppOwnerPaymentClaim(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schoolCode: String = "SCH-KINGSWAY-01",
    val schoolName: String = "Kingsway Model International College",
    val amountPaid: Double,
    val paymentReference: String,
    val paymentDate: String,
    val payerName: String,
    val payerPhone: String,
    val notes: String = "",
    val requestedTier: SubscriptionTier = SubscriptionTier.ANNUAL,
    val status: PaymentClaimStatus = PaymentClaimStatus.PENDING,
    val submittedAtMillis: Long = System.currentTimeMillis(),
    val reviewedAtMillis: Long? = null,
    val reviewedBy: String? = null
)

/**
 * Cryptographic 16-character License Keys generated by the App Owner to grant instant access.
 */
@Entity(tableName = "app_owner_license_keys")
data class AppOwnerLicenseKey(
    @PrimaryKey val licenseKey: String, // e.g. "ACAD-ANNL-2026-9XK4"
    val tier: SubscriptionTier = SubscriptionTier.ANNUAL,
    val durationDays: Int = 365,
    val generatedAtMillis: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false,
    val usedBySchoolCode: String? = null,
    val usedAtMillis: Long? = null
)
