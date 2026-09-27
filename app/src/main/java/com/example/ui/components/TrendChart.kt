package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CurrentReading
import com.example.model.HistoryItem
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrendChart(
    history: List<HistoryItem>,
    currentReading: CurrentReading?,
    timeWindowMinutes: Int,
    onTimeWindowSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var scrubIndex by remember { mutableStateOf<Int?>(null) }

    // Combine history with latest reading and filter to selected time window
    val now = System.currentTimeMillis()
    val cutoffTime = now - (timeWindowMinutes * 60 * 1000L)

    val combinedData = remember(history, currentReading, timeWindowMinutes) {
        val list = history.filter { it.timestamp >= cutoffTime }.toMutableList()
        if (currentReading != null && currentReading.timestamp >= cutoffTime) {
            if (list.none { Math.abs(it.timestamp - currentReading.timestamp) < 2000L }) {
                list.add(
                    HistoryItem(
                        id = "current",
                        temperature = currentReading.temperature,
                        humidity = currentReading.humidity,
                        timestamp = currentReading.timestamp
                    )
                )
            }
        }
        list.sortedBy { it.timestamp }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .testTag("trend_chart_card"),
        color = DarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & 30m/60m Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Chart Icon",
                        tint = TelemetryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE SENSOR TRENDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                // Time window chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30, 60).forEach { mins ->
                        val isSelected = timeWindowMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTimeWindowSelected(mins) },
                            label = {
                                Text(
                                    text = "${mins}M",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TelemetryCyan.copy(alpha = 0.2f),
                                selectedLabelColor = TelemetryCyan,
                                containerColor = Color.Transparent,
                                labelColor = TextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) TelemetryCyan else DarkCardBorder
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend Row & Dual Y-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(TelemetryCyan, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Temp (°C)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TelemetryCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(8.dp).background(TelemetryAmber, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Humidity (%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TelemetryAmber)
                }

                Text(
                    text = "Dual Y-Axis",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tooltip if scrubbing
            val scrubbedItem = scrubIndex?.let { idx ->
                if (idx in combinedData.indices) combinedData[idx] else null
            }

            if (scrubbedItem != null) {
                val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(scrubbedItem.timestamp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .border(1.dp, TelemetryCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Time: $timeFmt", fontSize = 11.sp, color = TextMuted)
                    Row {
                        Text(
                            text = "T: ${String.format("%.1f", scrubbedItem.temperature)}°C",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelemetryCyan
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "H: ${String.format("%.1f", scrubbedItem.humidity)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelemetryAmber
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(combinedData) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (combinedData.isNotEmpty()) {
                                    val paddingLeft = 32.dp.toPx()
                                    val paddingRight = 32.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight
                                    val ratio = ((offset.x - paddingLeft) / chartWidth).coerceIn(0f, 1f)
                                    val idx = (ratio * (combinedData.size - 1)).toInt()
                                    scrubIndex = if (scrubIndex == idx) null else idx
                                }
                            }
                        )
                    }
                    .pointerInput(combinedData) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                if (combinedData.isNotEmpty()) {
                                    val paddingLeft = 32.dp.toPx()
                                    val paddingRight = 32.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight
                                    val ratio = ((change.position.x - paddingLeft) / chartWidth).coerceIn(0f, 1f)
                                    val idx = (ratio * (combinedData.size - 1)).toInt()
                                    scrubIndex = idx
                                }
                            },
                            onDragEnd = { scrubIndex = null }
                        )
                    }
            ) {
                if (combinedData.size < 2) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Collecting telemetry history from ESP32...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val paddingLeft = 36.dp.toPx()
                        val paddingRight = 36.dp.toPx()
                        val paddingTop = 12.dp.toPx()
                        val paddingBottom = 24.dp.toPx()

                        val chartWidth = size.width - paddingLeft - paddingRight
                        val chartHeight = size.height - paddingTop - paddingBottom

                        // Value bounds
                        // Temperature scale: dynamic with minimum 0°C to 45°C
                        val minDataTemp = combinedData.minOf { it.temperature }
                        val maxDataTemp = combinedData.maxOf { it.temperature }
                        val tempMin = (Math.floor(minDataTemp / 5.0) * 5.0 - 5.0).coerceAtLeast(0.0)
                        val tempMax = (Math.ceil(maxDataTemp / 5.0) * 5.0 + 5.0).coerceAtLeast(35.0)

                        // Humidity scale: 0% to 100%
                        val humMin = 0.0
                        val humMax = 100.0

                        // 1. Draw horizontal grid lines (4 lines)
                        val gridLines = 4
                        for (i in 0..gridLines) {
                            val y = paddingTop + (chartHeight / gridLines) * i
                            drawLine(
                                color = Color(0xFF1E293B),
                                start = Offset(paddingLeft, y),
                                end = Offset(size.width - paddingRight, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }

                        // 2. Map coordinates for Temperature and Humidity
                        val tempPoints = mutableListOf<Offset>()
                        val humPoints = mutableListOf<Offset>()

                        val n = combinedData.size
                        for (i in 0 until n) {
                            val x = paddingLeft + (chartWidth / (n - 1).coerceAtLeast(1)) * i

                            // Temp y
                            val tNorm = ((combinedData[i].temperature - tempMin) / (tempMax - tempMin)).coerceIn(0.0, 1.0)
                            val tY = paddingTop + chartHeight * (1.0 - tNorm).toFloat()
                            tempPoints.add(Offset(x, tY))

                            // Hum y
                            val hNorm = ((combinedData[i].humidity - humMin) / (humMax - humMin)).coerceIn(0.0, 1.0)
                            val hY = paddingTop + chartHeight * (1.0 - hNorm).toFloat()
                            humPoints.add(Offset(x, hY))
                        }

                        // 3. Draw Temperature gradient fill & line
                        if (tempPoints.isNotEmpty()) {
                            val tempFillPath = Path().apply {
                                moveTo(tempPoints.first().x, paddingTop + chartHeight)
                                tempPoints.forEach { lineTo(it.x, it.y) }
                                lineTo(tempPoints.last().x, paddingTop + chartHeight)
                                close()
                            }
                            drawPath(
                                path = tempFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(TelemetryCyan.copy(alpha = 0.22f), Color.Transparent),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )

                            val tempLinePath = Path().apply {
                                moveTo(tempPoints.first().x, tempPoints.first().y)
                                for (i in 1 until tempPoints.size) {
                                    lineTo(tempPoints[i].x, tempPoints[i].y)
                                }
                            }
                            drawPath(
                                path = tempLinePath,
                                color = TelemetryCyan,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // 4. Draw Humidity gradient fill & line
                        if (humPoints.isNotEmpty()) {
                            val humFillPath = Path().apply {
                                moveTo(humPoints.first().x, paddingTop + chartHeight)
                                humPoints.forEach { lineTo(it.x, it.y) }
                                lineTo(humPoints.last().x, paddingTop + chartHeight)
                                close()
                            }
                            drawPath(
                                path = humFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(TelemetryAmber.copy(alpha = 0.18f), Color.Transparent),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )

                            val humLinePath = Path().apply {
                                moveTo(humPoints.first().x, humPoints.first().y)
                                for (i in 1 until humPoints.size) {
                                    lineTo(humPoints[i].x, humPoints[i].y)
                                }
                            }
                            drawPath(
                                path = humLinePath,
                                color = TelemetryAmber,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // 5. Draw scrub indicator if active
                        scrubIndex?.let { idx ->
                            if (idx in tempPoints.indices && idx in humPoints.indices) {
                                val scrubX = tempPoints[idx].x
                                drawLine(
                                    color = Color.White.copy(alpha = 0.6f),
                                    start = Offset(scrubX, paddingTop),
                                    end = Offset(scrubX, paddingTop + chartHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                                )

                                // Dots on lines
                                drawCircle(color = TelemetryCyan, radius = 5.dp.toPx(), center = tempPoints[idx])
                                drawCircle(color = TelemetryAmber, radius = 5.dp.toPx(), center = humPoints[idx])
                            }
                        }
                    }
                }
            }

            // Dual Y-axis labels & Time Axis footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "◀ 10°C..40°C",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TelemetryCyan
                )
                Text(
                    text = "-${timeWindowMinutes}m ────── Now",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = "0%..100% ▶",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TelemetryAmber
                )
            }
        }
    }
}
