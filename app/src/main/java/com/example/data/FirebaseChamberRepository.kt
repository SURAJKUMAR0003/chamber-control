package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.ChamberSystemStatus
import com.example.model.ConnectionStatus
import com.example.model.CurrentReading
import com.example.model.HistoryItem
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class FirebaseChamberRepository(private val context: Context) {

    private val tag = "ChamberRepository"

    private val _systemStatus = MutableStateFlow(ChamberSystemStatus.UNKNOWN)
    val systemStatus: StateFlow<ChamberSystemStatus> = _systemStatus.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.CONNECTING)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentReading = MutableStateFlow<CurrentReading?>(null)
    val currentReading: StateFlow<CurrentReading?> = _currentReading.asStateFlow()

    private val _historyReadings = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyReadings: StateFlow<List<HistoryItem>> = _historyReadings.asStateFlow()

    private val _hasReceivedInitialData = MutableStateFlow(false)
    val hasReceivedInitialData: StateFlow<Boolean> = _hasReceivedInitialData.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private var database: FirebaseDatabase? = null
    private var chamberRef: DatabaseReference? = null
    private var connectedRef: DatabaseReference? = null
    private var chamberListener: ValueEventListener? = null
    private var connectedListener: ValueEventListener? = null

    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private var currentDatabaseUrl: String = FirebaseConfigConstants.DEFAULT_DATABASE_URL

    init {
        initializeFirebase(currentDatabaseUrl)
    }

    fun initializeFirebase(databaseUrl: String) {
        currentDatabaseUrl = databaseUrl.trim()
        detachListeners()

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.chambermonitor.rqlw")
                    .setApiKey(FirebaseConfigConstants.DEFAULT_API_KEY)
                    .setDatabaseUrl(currentDatabaseUrl)
                    .setProjectId(FirebaseConfigConstants.DEFAULT_PROJECT_ID)
                    .build()
                FirebaseApp.initializeApp(context, options)
            }

            database = try {
                FirebaseDatabase.getInstance(currentDatabaseUrl)
            } catch (e: Exception) {
                Log.w(tag, "Using default FirebaseDatabase instance: ${e.message}")
                FirebaseDatabase.getInstance()
            }

            attachListeners()
        } catch (e: Exception) {
            Log.e(tag, "Error initializing Firebase RTDB: ${e.message}", e)
            _errorMessage.value = "Failed to connect to Firebase: ${e.localizedMessage}"
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
        }
    }

    private fun attachListeners() {
        val db = database ?: return

        // 1. Connection status listener from .info/connected
        connectedRef = db.getReference(".info/connected")
        connectedListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (!_isSimulationMode.value) {
                    _connectionStatus.value = if (connected) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(tag, "Connection listener cancelled: ${error.message}")
                if (!_isSimulationMode.value) {
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
            }
        }
        connectedRef?.addValueEventListener(connectedListener!!)

        // 2. Real-time chamber root listener
        chamberRef = db.getReference(FirebaseConfigConstants.RTDB_PATH_CHAMBER)
        chamberListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_isSimulationMode.value) return

                _hasReceivedInitialData.value = true
                _errorMessage.value = null

                // System status: ON / OFF
                val statusStr = snapshot.child("status").getValue(String::class.java)
                _systemStatus.value = ChamberSystemStatus.fromString(statusStr)

                // Current reading
                val currentSnap = snapshot.child("current")
                if (currentSnap.exists()) {
                    val temp = parseNumber(currentSnap.child("temperature").value)
                    val hum = parseNumber(currentSnap.child("humidity").value)
                    val ts = parseLong(currentSnap.child("timestamp").value) ?: System.currentTimeMillis()

                    if (temp != null && hum != null) {
                        _currentReading.value = CurrentReading(
                            temperature = temp,
                            humidity = hum,
                            timestamp = ts
                        )
                    }
                }

                // History readings
                val historySnap = snapshot.child("history")
                val historyList = mutableListOf<HistoryItem>()
                for (child in historySnap.children) {
                    val temp = parseNumber(child.child("temperature").value)
                    val hum = parseNumber(child.child("humidity").value)
                    val ts = parseLong(child.child("timestamp").value) ?: 0L
                    if (temp != null && hum != null) {
                        historyList.add(
                            HistoryItem(
                                id = child.key ?: "",
                                temperature = temp,
                                humidity = hum,
                                timestamp = ts
                            )
                        )
                    }
                }
                historyList.sortBy { it.timestamp }
                _historyReadings.value = historyList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Chamber listener cancelled: ${error.message}")
                if (!_isSimulationMode.value) {
                    _errorMessage.value = "Firebase error: ${error.message}"
                }
            }
        }
        chamberRef?.addValueEventListener(chamberListener!!)
    }

    private fun detachListeners() {
        chamberListener?.let { chamberRef?.removeEventListener(it) }
        connectedListener?.let { connectedRef?.removeEventListener(it) }
        chamberListener = null
        connectedListener = null
    }

    fun toggleSystemStatus() {
        val newStatus = if (_systemStatus.value == ChamberSystemStatus.ON) {
            ChamberSystemStatus.OFF
        } else {
            ChamberSystemStatus.ON
        }

        _systemStatus.value = newStatus

        if (!_isSimulationMode.value) {
            chamberRef?.child("status")?.setValue(newStatus.name)?.addOnFailureListener { error ->
                Log.e(tag, "Failed to update chamber status: ${error.message}")
            }
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
        if (enabled) {
            startSimulation()
        } else {
            stopSimulation()
            // Reset to real data state
            _hasReceivedInitialData.value = false
            initializeFirebase(currentDatabaseUrl)
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        _connectionStatus.value = ConnectionStatus.CONNECTED
        _systemStatus.value = ChamberSystemStatus.ON
        _hasReceivedInitialData.value = true

        // Populate initial simulated history (last 45 minutes)
        val now = System.currentTimeMillis()
        val initialHistory = mutableListOf<HistoryItem>()
        var simTemp = 22.4
        var simHum = 52.0

        for (i in 45 downTo 1) {
            val pastTs = now - (i * 60 * 1000L)
            simTemp += (Random.nextDouble(-0.4, 0.4))
            simHum += (Random.nextDouble(-0.6, 0.6))
            simTemp = simTemp.coerceIn(16.0, 31.0)
            simHum = simHum.coerceIn(35.0, 75.0)

            initialHistory.add(
                HistoryItem(
                    id = "sim_$pastTs",
                    temperature = Math.round(simTemp * 10.0) / 10.0,
                    humidity = Math.round(simHum * 10.0) / 10.0,
                    timestamp = pastTs
                )
            )
        }
        _historyReadings.value = initialHistory
        _currentReading.value = CurrentReading(
            temperature = Math.round(simTemp * 10.0) / 10.0,
            humidity = Math.round(simHum * 10.0) / 10.0,
            timestamp = now
        )

        simulationJob = scope.launch {
            while (isActive) {
                delay(3000L)
                if (_systemStatus.value == ChamberSystemStatus.ON) {
                    val prevTemp = _currentReading.value?.temperature ?: 22.0
                    val prevHum = _currentReading.value?.humidity ?: 50.0

                    val newTemp = Math.round((prevTemp + Random.nextDouble(-0.5, 0.5)).coerceIn(15.0, 36.0) * 10.0) / 10.0
                    val newHum = Math.round((prevHum + Random.nextDouble(-0.8, 0.8)).coerceIn(30.0, 85.0) * 10.0) / 10.0
                    val ts = System.currentTimeMillis()

                    val newReading = CurrentReading(newTemp, newHum, ts)
                    _currentReading.value = newReading

                    val updatedHistory = _historyReadings.value.toMutableList().apply {
                        add(HistoryItem(id = "sim_$ts", temperature = newTemp, humidity = newHum, timestamp = ts))
                        if (size > 100) removeAt(0)
                    }
                    _historyReadings.value = updatedHistory
                }
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun parseNumber(value: Any?): Double? {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    private fun parseLong(value: Any?): Long? {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}
