package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseChamberRepository
import com.example.data.FirebaseConfigConstants
import com.example.model.ActiveAlert
import com.example.model.AlertType
import com.example.model.ChamberSystemStatus
import com.example.model.ChamberThresholds
import com.example.model.ConnectionStatus
import com.example.model.CurrentReading
import com.example.model.DailyStatistics
import com.example.model.HistoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class ChamberDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseChamberRepository(application.applicationContext)

    val systemStatus: StateFlow<ChamberSystemStatus> = repository.systemStatus
    val connectionStatus: StateFlow<ConnectionStatus> = repository.connectionStatus
    val currentReading: StateFlow<CurrentReading?> = repository.currentReading
    val historyReadings: StateFlow<List<HistoryItem>> = repository.historyReadings
    val hasReceivedInitialData: StateFlow<Boolean> = repository.hasReceivedInitialData
    val errorMessage: StateFlow<String?> = repository.errorMessage
    val isSimulationMode: StateFlow<Boolean> = repository.isSimulationMode

    private val _thresholds = MutableStateFlow(ChamberThresholds())
    val thresholds: StateFlow<ChamberThresholds> = _thresholds.asStateFlow()

    private val _selectedTimeWindowMinutes = MutableStateFlow(60)
    val selectedTimeWindowMinutes: StateFlow<Int> = _selectedTimeWindowMinutes.asStateFlow()

    private val _dismissedAlertKey = MutableStateFlow<String?>(null)

    private val _databaseUrl = MutableStateFlow(FirebaseConfigConstants.DEFAULT_DATABASE_URL)
    val databaseUrl: StateFlow<String> = _databaseUrl.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    // Daily statistics calculated from history readings
    val dailyStats: StateFlow<DailyStatistics> = historyReadings.combine(currentReading) { history, current ->
        calculateDailyStats(history, current)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyStatistics())

    // Active alert calculation
    val activeAlert: StateFlow<ActiveAlert?> = combine(
        currentReading,
        thresholds,
        _dismissedAlertKey
    ) { current, thresh, dismissedKey ->
        if (current == null) return@combine null

        val candidateAlert = when {
            current.temperature > thresh.alertTempMax -> {
                ActiveAlert(
                    type = AlertType.TEMP_HIGH,
                    title = "High Temperature Alert",
                    message = "Chamber temperature is ${String.format("%.1f", current.temperature)}°C (Limit: ${thresh.alertTempMax}°C)",
                    currentValue = current.temperature,
                    thresholdValue = thresh.alertTempMax,
                    timestamp = current.timestamp
                )
            }
            current.temperature < thresh.alertTempMin -> {
                ActiveAlert(
                    type = AlertType.TEMP_LOW,
                    title = "Low Temperature Alert",
                    message = "Chamber temperature is ${String.format("%.1f", current.temperature)}°C (Limit: ${thresh.alertTempMin}°C)",
                    currentValue = current.temperature,
                    thresholdValue = thresh.alertTempMin,
                    timestamp = current.timestamp
                )
            }
            current.humidity > thresh.alertHumidityMax -> {
                ActiveAlert(
                    type = AlertType.HUMIDITY_HIGH,
                    title = "High Humidity Alert",
                    message = "Chamber humidity is ${String.format("%.1f", current.humidity)}% (Limit: ${thresh.alertHumidityMax}%)",
                    currentValue = current.humidity,
                    thresholdValue = thresh.alertHumidityMax,
                    timestamp = current.timestamp
                )
            }
            current.humidity < thresh.alertHumidityMin -> {
                ActiveAlert(
                    type = AlertType.HUMIDITY_LOW,
                    title = "Low Humidity Alert",
                    message = "Chamber humidity is ${String.format("%.1f", current.humidity)}% (Limit: ${thresh.alertHumidityMin}%)",
                    currentValue = current.humidity,
                    thresholdValue = thresh.alertHumidityMin,
                    timestamp = current.timestamp
                )
            }
            else -> null
        }

        if (candidateAlert != null) {
            val key = "${candidateAlert.type}_${Math.round(candidateAlert.currentValue)}"
            if (key == dismissedKey) null else candidateAlert
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Human-readable relative timestamp ticker
    private val _lastUpdatedText = MutableStateFlow("Waiting for data...")
    val lastUpdatedText: StateFlow<String> = _lastUpdatedText.asStateFlow()

    init {
        // Coroutine to update "Last updated X seconds ago" every second
        viewModelScope.launch {
            while (isActive) {
                val reading = currentReading.value
                if (reading != null && reading.timestamp > 0) {
                    val now = System.currentTimeMillis()
                    val diffSec = ((now - reading.timestamp) / 1000).coerceAtLeast(0)
                    _lastUpdatedText.value = when {
                        diffSec <= 1 -> "Last updated just now"
                        diffSec < 60 -> "Last updated ${diffSec}s ago"
                        diffSec < 3600 -> "Last updated ${diffSec / 60}m ${diffSec % 60}s ago"
                        else -> "Last updated ${diffSec / 3600}h ago"
                    }
                } else {
                    _lastUpdatedText.value = "No readings received yet"
                }
                delay(1000L)
            }
        }
    }

    fun toggleSystemPower() {
        repository.toggleSystemStatus()
    }

    fun setTimeWindowMinutes(minutes: Int) {
        _selectedTimeWindowMinutes.value = minutes
    }

    fun dismissCurrentAlert(alert: ActiveAlert) {
        _dismissedAlertKey.value = "${alert.type}_${Math.round(alert.currentValue)}"
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun updateThresholds(newThresholds: ChamberThresholds) {
        _thresholds.value = newThresholds
        _dismissedAlertKey.value = null // reset dismissal on threshold change
    }

    fun updateDatabaseUrl(newUrl: String) {
        _databaseUrl.value = newUrl.trim()
        repository.initializeFirebase(newUrl.trim())
    }

    fun toggleSimulationMode(enabled: Boolean) {
        repository.setSimulationMode(enabled)
    }

    private fun calculateDailyStats(history: List<HistoryItem>, current: CurrentReading?): DailyStatistics {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        // Filter items from today, or fallback to all available history if history items are recent
        val todayItems = history.filter { it.timestamp >= startOfToday }
        val allItems = if (todayItems.isNotEmpty()) todayItems else history

        val combinedTemps = mutableListOf<Double>()
        val combinedHums = mutableListOf<Double>()

        for (item in allItems) {
            combinedTemps.add(item.temperature)
            combinedHums.add(item.humidity)
        }

        if (current != null && current.timestamp > 0) {
            combinedTemps.add(current.temperature)
            combinedHums.add(current.humidity)
        }

        if (combinedTemps.isEmpty()) {
            return DailyStatistics()
        }

        val minTemp = combinedTemps.minOrNull() ?: 0.0
        val maxTemp = combinedTemps.maxOrNull() ?: 0.0
        val avgTemp = combinedTemps.average()

        val minHum = combinedHums.minOrNull() ?: 0.0
        val maxHum = combinedHums.maxOrNull() ?: 0.0
        val avgHum = combinedHums.average()

        return DailyStatistics(
            minTemp = Math.round(minTemp * 10.0) / 10.0,
            maxTemp = Math.round(maxTemp * 10.0) / 10.0,
            avgTemp = Math.round(avgTemp * 10.0) / 10.0,
            minHumidity = Math.round(minHum * 10.0) / 10.0,
            maxHumidity = Math.round(maxHum * 10.0) / 10.0,
            avgHumidity = Math.round(avgHum * 10.0) / 10.0,
            count = combinedTemps.size
        )
    }
}
