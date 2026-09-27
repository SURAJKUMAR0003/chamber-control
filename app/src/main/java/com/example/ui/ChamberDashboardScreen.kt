package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ConnectionStatus
import com.example.ui.components.AlertBanner
import com.example.ui.components.GaugeCard
import com.example.ui.components.GaugeType
import com.example.ui.components.SettingsDialog
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusCard
import com.example.ui.components.TrendChart
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamberDashboardScreen(
    viewModel: ChamberDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val systemStatus by viewModel.systemStatus.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val currentReading by viewModel.currentReading.collectAsStateWithLifecycle()
    val historyReadings by viewModel.historyReadings.collectAsStateWithLifecycle()
    val hasReceivedInitialData by viewModel.hasReceivedInitialData.collectAsStateWithLifecycle()
    val isSimulationMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()
    val thresholds by viewModel.thresholds.collectAsStateWithLifecycle()
    val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()
    val lastUpdatedText by viewModel.lastUpdatedText.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val selectedTimeWindow by viewModel.selectedTimeWindowMinutes.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val databaseUrl by viewModel.databaseUrl.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (connectionStatus == ConnectionStatus.CONNECTED) TelemetryGreen else TelemetryAmber,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Full Pack Chamber Monitor",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "ESP32 IoT Realtime Telemetry",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openSettings() },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = TelemetryCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth > 640.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Alert Banner (Warning when threshold crossed)
                AlertBanner(
                    alert = activeAlert,
                    onDismiss = { viewModel.dismissCurrentAlert(it) },
                    onOpenSettings = { viewModel.openSettings() }
                )

                // 2. Last updated timestamp pill & quick status bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(DarkCard, RoundedCornerShape(8.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("last_updated_chip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TelemetryCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = lastUpdatedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    if (!isSimulationMode && !hasReceivedInitialData) {
                        OutlinedButton(
                            onClick = { viewModel.toggleSimulationMode(true) },
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("enable_simulation_quick_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TelemetryAmber),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(TelemetryAmber.copy(alpha = 0.5f))),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate ESP32 Data", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. Graceful Empty / Initial Loading State banner if no data received yet
                if (!hasReceivedInitialData && !isSimulationMode) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .testTag("awaiting_data_banner"),
                        color = Color(0xFF161F30)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TelemetryCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Connecting to Firebase Realtime Database...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Listening to /chamber path. Ensure your ESP32 has Wi-Fi and is publishing sensor readings.",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                // Error message if any
                if (errorMessage != null && !isSimulationMode) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, TelemetryRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        color = Color(0xFF261014)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error",
                                tint = TelemetryRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.sp,
                                color = TelemetryRed
                            )
                        }
                    }
                }

                // 4. System Status Card (ON/OFF and Firebase Live)
                StatusCard(
                    systemStatus = systemStatus,
                    connectionStatus = connectionStatus,
                    isSimulationMode = isSimulationMode,
                    onTogglePower = { viewModel.toggleSystemPower() }
                )

                // 5. Dual Gauges: Real-time Temperature & Humidity Cards
                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GaugeCard(
                            title = "Chamber Temperature",
                            value = currentReading?.temperature,
                            unit = "°C",
                            minScale = 0.0,
                            maxScale = 50.0,
                            safeMin = thresholds.safeTempMin,
                            safeMax = thresholds.safeTempMax,
                            type = GaugeType.TEMPERATURE,
                            modifier = Modifier.weight(1f)
                        )
                        GaugeCard(
                            title = "Chamber Humidity",
                            value = currentReading?.humidity,
                            unit = "%",
                            minScale = 0.0,
                            maxScale = 100.0,
                            safeMin = thresholds.safeHumidityMin,
                            safeMax = thresholds.safeHumidityMax,
                            type = GaugeType.HUMIDITY,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    GaugeCard(
                        title = "Chamber Temperature",
                        value = currentReading?.temperature,
                        unit = "°C",
                        minScale = 0.0,
                        maxScale = 50.0,
                        safeMin = thresholds.safeTempMin,
                        safeMax = thresholds.safeTempMax,
                        type = GaugeType.TEMPERATURE
                    )
                    GaugeCard(
                        title = "Chamber Humidity",
                        value = currentReading?.humidity,
                        unit = "%",
                        minScale = 0.0,
                        maxScale = 100.0,
                        safeMin = thresholds.safeHumidityMin,
                        safeMax = thresholds.safeHumidityMax,
                        type = GaugeType.HUMIDITY
                    )
                }

                // 6. Live updating line chart (last 30-60 minutes dual y-axis)
                TrendChart(
                    history = historyReadings,
                    currentReading = currentReading,
                    timeWindowMinutes = selectedTimeWindow,
                    onTimeWindowSelected = { viewModel.setTimeWindowMinutes(it) }
                )

                // 7. Today's Min / Max / Average Statistics Card
                StatsCard(stats = dailyStats)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Settings / Configuration Dialog
    if (isSettingsOpen) {
        SettingsDialog(
            currentThresholds = thresholds,
            currentDatabaseUrl = databaseUrl,
            isSimulationMode = isSimulationMode,
            onSaveThresholds = { viewModel.updateThresholds(it) },
            onSaveDatabaseUrl = { viewModel.updateDatabaseUrl(it) },
            onToggleSimulationMode = { viewModel.toggleSimulationMode(it) },
            onDismiss = { viewModel.closeSettings() }
        )
    }
}
