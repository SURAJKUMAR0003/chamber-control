package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FirebaseConfigConstants
import com.example.model.ChamberThresholds
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

@Composable
fun SettingsDialog(
    currentThresholds: ChamberThresholds,
    currentDatabaseUrl: String,
    isSimulationMode: Boolean,
    onSaveThresholds: (ChamberThresholds) -> Unit,
    onSaveDatabaseUrl: (String) -> Unit,
    onToggleSimulationMode: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var safeTempMin by remember { mutableStateOf(currentThresholds.safeTempMin.toString()) }
    var safeTempMax by remember { mutableStateOf(currentThresholds.safeTempMax.toString()) }
    var alertTempMin by remember { mutableStateOf(currentThresholds.alertTempMin.toString()) }
    var alertTempMax by remember { mutableStateOf(currentThresholds.alertTempMax.toString()) }

    var safeHumidityMin by remember { mutableStateOf(currentThresholds.safeHumidityMin.toString()) }
    var safeHumidityMax by remember { mutableStateOf(currentThresholds.safeHumidityMax.toString()) }
    var alertHumidityMin by remember { mutableStateOf(currentThresholds.alertHumidityMin.toString()) }
    var alertHumidityMax by remember { mutableStateOf(currentThresholds.alertHumidityMax.toString()) }

    var databaseUrl by remember { mutableStateOf(currentDatabaseUrl) }
    var simulationEnabled by remember { mutableStateOf(isSimulationMode) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
                .testTag("settings_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = TelemetryCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MONITOR CONFIGURATION",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Safety Simulation Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .border(1.dp, if (simulationEnabled) TelemetryAmber.copy(alpha = 0.5f) else DarkCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (simulationEnabled) TelemetryAmber else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Safety Simulation Mode",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (simulationEnabled) TelemetryAmber else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Generates realistic virtual chamber telemetry for local UI verification before ESP32 is powered.",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = simulationEnabled,
                        onCheckedChange = {
                            simulationEnabled = it
                            onToggleSimulationMode(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = TelemetryAmber
                        ),
                        modifier = Modifier.testTag("simulation_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Firebase RTDB URL
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = TelemetryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FIREBASE REALTIME DATABASE URL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = databaseUrl,
                    onValueChange = { databaseUrl = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_url_input"),
                    placeholder = { Text("https://your-rtdb.firebaseio.com", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TelemetryCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Temperature Thresholds
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = TelemetryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TEMPERATURE THRESHOLDS (°C)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberField(
                        label = "Safe Min",
                        value = safeTempMin,
                        onValueChange = { safeTempMin = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryGreen
                    )
                    NumberField(
                        label = "Safe Max",
                        value = safeTempMax,
                        onValueChange = { safeTempMax = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryGreen
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberField(
                        label = "Alert Min",
                        value = alertTempMin,
                        onValueChange = { alertTempMin = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryRed
                    )
                    NumberField(
                        label = "Alert Max",
                        value = alertTempMax,
                        onValueChange = { alertTempMax = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryRed
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Humidity Thresholds
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = TelemetryAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HUMIDITY THRESHOLDS (%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberField(
                        label = "Safe Min",
                        value = safeHumidityMin,
                        onValueChange = { safeHumidityMin = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryGreen
                    )
                    NumberField(
                        label = "Safe Max",
                        value = safeHumidityMax,
                        onValueChange = { safeHumidityMax = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryGreen
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NumberField(
                        label = "Alert Min",
                        value = alertHumidityMin,
                        onValueChange = { alertHumidityMin = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryRed
                    )
                    NumberField(
                        label = "Alert Max",
                        value = alertHumidityMax,
                        onValueChange = { alertHumidityMax = it },
                        modifier = Modifier.weight(1f),
                        accentColor = TelemetryRed
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val defaults = ChamberThresholds()
                            safeTempMin = defaults.safeTempMin.toString()
                            safeTempMax = defaults.safeTempMax.toString()
                            alertTempMin = defaults.alertTempMin.toString()
                            alertTempMax = defaults.alertTempMax.toString()
                            safeHumidityMin = defaults.safeHumidityMin.toString()
                            safeHumidityMax = defaults.safeHumidityMax.toString()
                            alertHumidityMin = defaults.alertHumidityMin.toString()
                            alertHumidityMax = defaults.alertHumidityMax.toString()
                            databaseUrl = FirebaseConfigConstants.DEFAULT_DATABASE_URL
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_defaults_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val newThresh = ChamberThresholds(
                                safeTempMin = safeTempMin.toDoubleOrNull() ?: currentThresholds.safeTempMin,
                                safeTempMax = safeTempMax.toDoubleOrNull() ?: currentThresholds.safeTempMax,
                                alertTempMin = alertTempMin.toDoubleOrNull() ?: currentThresholds.alertTempMin,
                                alertTempMax = alertTempMax.toDoubleOrNull() ?: currentThresholds.alertTempMax,
                                safeHumidityMin = safeHumidityMin.toDoubleOrNull() ?: currentThresholds.safeHumidityMin,
                                safeHumidityMax = safeHumidityMax.toDoubleOrNull() ?: currentThresholds.safeHumidityMax,
                                alertHumidityMin = alertHumidityMin.toDoubleOrNull() ?: currentThresholds.alertHumidityMin,
                                alertHumidityMax = alertHumidityMax.toDoubleOrNull() ?: currentThresholds.alertHumidityMax
                            )
                            onSaveThresholds(newThresh)
                            if (databaseUrl.trim() != currentDatabaseUrl) {
                                onSaveDatabaseUrl(databaseUrl.trim())
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_settings_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TelemetryCyan,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = DarkCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = TextMuted
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
