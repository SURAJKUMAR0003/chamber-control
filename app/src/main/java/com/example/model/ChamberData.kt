package com.example.model

enum class ChamberSystemStatus {
    ON,
    OFF,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): ChamberSystemStatus {
            return when (value?.trim()?.uppercase()) {
                "ON" -> ON
                "OFF" -> OFF
                else -> UNKNOWN
            }
        }
    }
}

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING
}

data class CurrentReading(
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val timestamp: Long = 0L
)

data class HistoryItem(
    val id: String = "",
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val timestamp: Long = 0L
)

data class DailyStatistics(
    val minTemp: Double = 0.0,
    val maxTemp: Double = 0.0,
    val avgTemp: Double = 0.0,
    val minHumidity: Double = 0.0,
    val maxHumidity: Double = 0.0,
    val avgHumidity: Double = 0.0,
    val count: Int = 0
)

data class ChamberThresholds(
    val safeTempMin: Double = 18.0,
    val safeTempMax: Double = 26.0,
    val alertTempMin: Double = 10.0,
    val alertTempMax: Double = 32.0,
    val safeHumidityMin: Double = 40.0,
    val safeHumidityMax: Double = 65.0,
    val alertHumidityMin: Double = 25.0,
    val alertHumidityMax: Double = 80.0
)

enum class AlertType {
    TEMP_HIGH,
    TEMP_LOW,
    HUMIDITY_HIGH,
    HUMIDITY_LOW
}

data class ActiveAlert(
    val type: AlertType,
    val title: String,
    val message: String,
    val currentValue: Double,
    val thresholdValue: Double,
    val timestamp: Long = System.currentTimeMillis()
)
