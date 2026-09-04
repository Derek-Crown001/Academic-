package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppOwnerLicenseConfig
import com.example.data.model.AppOwnerMemo
import com.example.data.model.OwnerMemoType
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.AcademicRose
import com.example.ui.theme.PrimaryLight
import com.example.ui.viewmodel.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Notice banner displayed at the top of the school admin/staff dashboard
 * when the App Owner broadcasts an official memo or invoice.
 */
@Composable
fun AppOwnerMemoBanner(
    memo: AppOwnerMemo,
    onPayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isInvoice = memo.memoType == OwnerMemoType.PAYMENT_INVOICE || memo.memoType == OwnerMemoType.SUBSCRIPTION_RENEWAL
    val isUrgent = memo.memoType == OwnerMemoType.SUSPENSION_WARNING
    val bannerColor = if (isUrgent) Color(0xFFEF4444) else if (isInvoice) Color(0xFFD97706) else Color(0xFF2563EB)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bannerColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, bannerColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(bannerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInvoice) Icons.Rounded.ReceiptLong else if (isUrgent) Icons.Rounded.Warning else Icons.Rounded.Campaign,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "OFFICIAL APP OWNER NOTICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = bannerColor,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = memo.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = memo.memoBody,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (memo.amountDue > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Due:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${memo.currency}${String.format(Locale.US, "%,.2f", memo.amountDue)}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = bannerColor
                            )
                        }

                        if (memo.dueDate.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Due Date:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(memo.dueDate, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Text(
                            "App Owner Bank Account: ${memo.paymentBank} • ${memo.accountNumber} (${memo.accountName})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isInvoice || memo.amountDue > 0) {
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = bannerColor),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Payment Reference", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Screen overlay or card shown when a specific feature or whole app is locked by the App Owner.
 */
@Composable
fun LockedFeaturePaywallCard(
    featureName: String,
    lockReason: String,
    config: AppOwnerLicenseConfig,
    onRedeemKeyClick: () -> Unit,
    onSubmitProofClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, AcademicRose.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lock Icon with glowing background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(AcademicRose.copy(alpha = 0.25f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AcademicRose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Locked Feature",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$featureName is Locked",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Restricted by App Platform Owner",
                    fontSize = 12.sp,
                    color = AcademicRose,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Lock Reason
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AcademicRose.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AcademicRose.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = lockReason.ifBlank { "This feature requires an active school license subscription. Please contact the App Owner to unlock access." },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }

            // App Owner Payment Info
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "APP OWNER PAYMENT & RENEWAL DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bank Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(config.ownerBankName, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Account Number:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(config.ownerAccountNumber, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryLight)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(config.ownerAccountNumber))
                                    copiedText = true
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy Account Number",
                                    tint = PrimaryLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Account Name:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(config.ownerAccountName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Annual License Fee:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${config.currencySymbol}${String.format(Locale.US, "%,.2f", config.subscriptionFeePerYear)}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Owner Support / Phone:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(config.ownerContactPhone, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRedeemKeyClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Activation License Key", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSubmitProofClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Bank Transfer Claim", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Dialog for school owners to enter a 16-character License Key generated by the App Owner.
 */
@Composable
fun RedeemLicenseKeyDialog(
    onDismiss: () -> Unit,
    onRedeem: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Rounded.Key, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(32.dp))
        },
        title = {
            Text("Activate School License", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter the 16-character License Activation Key provided by the App Owner (e.g. ACAD-ANNL-2026-XXXX).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it.uppercase() },
                    label = { Text("16-Digit License Key") },
                    placeholder = { Text("ACAD-ANNL-2026-X7K9") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.VpnKey, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRedeem(keyInput) },
                enabled = keyInput.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Activate & Unlock", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for school admins to submit payment references directly to the App Owner.
 */
@Composable
fun SubmitPaymentProofDialog(
    config: AppOwnerLicenseConfig,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, ref: String, payerName: String, phone: String, notes: String, tier: SubscriptionTier) -> Unit
) {
    var amountInput by remember { mutableStateOf(config.subscriptionFeePerYear.toInt().toString()) }
    var referenceInput by remember { mutableStateOf("") }
    var payerNameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf(SubscriptionTier.ANNUAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Submit Payment Claim", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Provide bank transfer details. The App Owner will verify and activate your school's subscription.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount Paid (${config.currencySymbol})") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referenceInput,
                    onValueChange = { referenceInput = it },
                    label = { Text("Bank Transaction Reference / Session ID") },
                    placeholder = { Text("e.g. 000013248920194") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payerNameInput,
                    onValueChange = { payerNameInput = it },
                    label = { Text("Payer / School Representative Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone Number for WhatsApp / SMS Alert") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Additional Remarks (Optional)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    onSubmit(amt, referenceInput, payerNameInput, phoneInput, notesInput, selectedTier)
                },
                enabled = referenceInput.isNotBlank() && payerNameInput.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Proof", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
