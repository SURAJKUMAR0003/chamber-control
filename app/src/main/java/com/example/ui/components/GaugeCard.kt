package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryBlue
import com.example.ui.theme.TelemetryCyan
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class GaugeType {
    TEMPERATURE,
    HUMIDITY
}

@Composable
fun GaugeCard(
    title: String,
    value: Double?,
    unit: String,
    minScale: Double,
    maxScale: Double,
    safeMin: Double,
    safeMax: Double,
    type: GaugeType,
    modifier: Modifier = Modifier
) {
    val displayValue = value ?: 0.0
    val hasData = value != null

    // Determine status color and label
    val (statusColor, statusLabel) = when {
        !hasData -> Pair(TextMuted, "NO DATA")
        type == GaugeType.TEMPERATURE -> when {
            displayValue < safeMin -> Pair(TelemetryBlue, "COLD")
            displayValue > safeMax -> Pair(TelemetryRed, "TOO HOT")
            else -> Pair(TelemetryGreen, "NORMAL")
        }
        else -> when {
            displayValue < safeMin -> Pair(TelemetryBlue, "DRY")
            displayValue > safeMax -> Pair(TelemetryAmber, "HUMID")
            else -> Pair(TelemetryGreen, "OPTIMAL")
        }
    }

    val animatedStatusColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(500),
        label = "statusColor"
    )

    // Calculate fraction for gauge arc (0.0 to 1.0)
    val fraction = ((displayValue - minScale) / (maxScale - minScale))
        .coerceIn(0.0, 1.0)
        .toFloat()

    val animatedFraction by animateFloatAsState(
        targetValue = if (hasData) fraction else 0f,
        animationSpec = tween(600),
        label = "gaugeFraction"
    )

    val safeMinFraction = ((safeMin - minScale) / (maxScale - minScale)).coerceIn(0.0, 1.0).toFloat()
    val safeMaxFraction = ((safeMax - minScale) / (maxScale - minScale)).coerceIn(0.0, 1.0).toFloat()

    val icon: ImageVector = if (type == GaugeType.TEMPERATURE) Icons.Default.Thermostat else Icons.Default.WaterDrop

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .testTag(if (type == GaugeType.TEMPERATURE) "temperature_gauge_card" else "humidity_gauge_card"),
        color = DarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$title Icon",
                        tint = animatedStatusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .background(
                            color = animatedStatusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = animatedStatusColor.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedStatusColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gauge Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1.2f)
                ) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth * 2, size.height * 1.5f - strokeWidth * 2)
                    val topLeft = Offset(strokeWidth, strokeWidth)
                    val startAngle = 150f
                    val totalSweep = 240f

                    // 1. Background full track
                    drawArc(
                        color = Color(0xFF26334D),
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 2. Safe zone range indicator on track
                    val safeStartAngle = startAngle + (totalSweep * safeMinFraction)
                    val safeSweepAngle = totalSweep * (safeMaxFraction - safeMinFraction)
                    if (safeSweepAngle > 0f) {
                        drawArc(
                            color = TelemetryGreen.copy(alpha = 0.35f),
                            startAngle = safeStartAngle,
                            sweepAngle = safeSweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Butt)
                        )
                    }

                    // 3. Active progress arc
                    if (animatedFraction > 0.005f) {
                        drawArc(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    animatedStatusColor.copy(alpha = 0.7f),
                                    animatedStatusColor
                                )
                            ),
                            startAngle = startAngle,
                            sweepAngle = totalSweep * animatedFraction,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Centered Value & Unit
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    if (hasData) {
                        Text(
                            text = String.format("%.1f", displayValue),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = unit,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = animatedStatusColor
                        )
                    } else {
                        Text(
                            text = "--",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted
                        )
                        Text(
                            text = "Awaiting ESP32",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Min and Max scale markers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${minScale.toInt()}$unit",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = "Safe: ${safeMin.toInt()}–${safeMax.toInt()}$unit",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TelemetryGreen.copy(alpha = 0.85f)
                )
                Text(
                    text = "${maxScale.toInt()}$unit",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
