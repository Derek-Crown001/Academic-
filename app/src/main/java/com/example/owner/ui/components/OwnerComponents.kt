package com.example.owner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppOwnerLicenseConfig
import com.example.data.model.SubscriptionTier

object OwnerThemeColors {
    val BackgroundDark = Color(0xFF0A0F1D)
    val SurfaceDark = Color(0xFF131D31)
    val CardDark = Color(0xFF1E293B)
    val CardBorder = Color(0xFF334155)
    val AmberPrimary = Color(0xFFD97706)
    val AmberLight = Color(0xFFFBBF24)
    val AmberDark = Color(0xFFB45309)
    val GoldAccent = Color(0xFFF59E0B)
    val EmeraldSuccess = Color(0xFF10B981)
    val EmeraldLight = Color(0xFF6EE7B7)
    val RoseDanger = Color(0xFFEF4444)
    val RoseLight = Color(0xFFFCA5A5)
    val SkyBlue = Color(0xFF0284C7)
    val SkyLight = Color(0xFF38BDF8)
    val IndigoAccent = Color(0xFF6366F1)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

@Composable
fun OwnerExecutiveHeader(
    title: String,
    subtitle: String,
    connectedSchoolName: String,
    isSchoolLocked: Boolean,
    onSwitchToSchoolApp: () -> Unit,
    onLockConsole: () -> Unit
) {
    Surface(
        color = OwnerThemeColors.SurfaceDark,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(OwnerThemeColors.AmberLight, OwnerThemeColors.AmberPrimary)
                                )
                            )
                            .border(1.5.dp, OwnerThemeColors.GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Master Owner Icon",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = OwnerThemeColors.TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = OwnerThemeColors.AmberPrimary.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.AmberLight)
                            ) {
                                Text(
                                    text = "SAAS MASTER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OwnerThemeColors.AmberLight,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = OwnerThemeColors.TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Switch to School App Client Button
                    Button(
                        onClick = onSwitchToSchoolApp,
                        colors = ButtonDefaults.buttonColors(containerColor = OwnerThemeColors.SkyBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("School App", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Lock Console button
                    IconButton(
                        onClick = onLockConsole,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Lock Console",
                            tint = OwnerThemeColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connected School Status Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OwnerThemeColors.CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sensors,
                            contentDescription = null,
                            tint = if (isSchoolLocked) OwnerThemeColors.RoseDanger else OwnerThemeColors.EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Target Client: ",
                            fontSize = 11.sp,
                            color = OwnerThemeColors.TextMuted
                        )
                        Text(
                            text = connectedSchoolName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = OwnerThemeColors.TextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSchoolLocked) OwnerThemeColors.RoseDanger.copy(alpha = 0.25f) else OwnerThemeColors.EmeraldSuccess.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = if (isSchoolLocked) "● LOCKED / RESTRICTED" else "● ONLINE & ACTIVE",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSchoolLocked) OwnerThemeColors.RoseLight else OwnerThemeColors.EmeraldLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerStatPill(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = OwnerThemeColors.CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, OwnerThemeColors.CardBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(text = label, fontSize = 10.sp, color = OwnerThemeColors.TextSecondary)
                Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OwnerThemeColors.TextPrimary)
            }
        }
    }
}
