package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChamberSystemStatus
import com.example.model.ConnectionStatus
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatusCard(
    systemStatus: ChamberSystemStatus,
    connectionStatus: ConnectionStatus,
    isSimulationMode: Boolean,
    onTogglePower: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSystemOn = systemStatus == ChamberSystemStatus.ON

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val systemStatusColor by animateColorAsState(
        targetValue = if (isSystemOn) TelemetryGreen else TelemetryRed,
        label = "systemStatusColor"
    )

    val connectionColor = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> TelemetryGreen
        ConnectionStatus.CONNECTING -> TelemetryAmber
        ConnectionStatus.DISCONNECTED -> TelemetryRed
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .testTag("status_card"),
        color = DarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: System status title and indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (isSystemOn) systemStatusColor.copy(alpha = pulseAlpha) else systemStatusColor,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHAMBER SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                // Right: Firebase live badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = connectionColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = connectionColor.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("firebase_connection_badge")
                ) {
                    Icon(
                        imageVector = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> Icons.Default.CloudDone
                            ConnectionStatus.CONNECTING -> Icons.Default.CloudSync
                            ConnectionStatus.DISCONNECTED -> Icons.Default.CloudOff
                        },
                        contentDescription = "Connection icon",
                        tint = connectionColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> "FIREBASE LIVE"
                            ConnectionStatus.CONNECTING -> "CONNECTING..."
                            ConnectionStatus.DISCONNECTED -> "OFFLINE"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = connectionColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (systemStatus) {
                                ChamberSystemStatus.ON -> "ACTIVE / RUNNING"
                                ChamberSystemStatus.OFF -> "STANDBY / OFF"
                                ChamberSystemStatus.UNKNOWN -> "INITIALIZING"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isSystemOn) "Sensors streaming live telemetry" else "System cooling & monitoring suspended",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                // Toggle Power Button
                ElevatedButton(
                    onClick = onTogglePower,
                    modifier = Modifier.testTag("power_toggle_button"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isSystemOn) TelemetryGreen.copy(alpha = 0.2f) else TelemetryRed.copy(alpha = 0.2f),
                        contentColor = if (isSystemOn) TelemetryGreen else TelemetryRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Power",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSystemOn) "TURN OFF" else "TURN ON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isSimulationMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = TelemetryAmber.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = TelemetryAmber.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Simulation Icon",
                        tint = TelemetryAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAFETY SIMULATION MODE: Generating virtual chamber telemetry for testing",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TelemetryAmber
                    )
                }
            }
        }
    }
}
