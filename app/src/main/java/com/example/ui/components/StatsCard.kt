package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyStatistics
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

@Composable
fun StatsCard(
    stats: DailyStatistics,
    modifier: Modifier = Modifier
) {
    val hasData = stats.count > 0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .testTag("stats_card"),
        color = DarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = "Stats Icon",
                        tint = TelemetryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TODAY'S METRICS & EXTREMES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = if (hasData) "${stats.count} datapoints" else "No records",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Temperature section
            Text(
                text = "TEMPERATURE (°C)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = TelemetryCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    title = "Min",
                    value = if (hasData) String.format("%.1f°", stats.minTemp) else "--",
                    color = TelemetryBlue,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Avg",
                    value = if (hasData) String.format("%.1f°", stats.avgTemp) else "--",
                    color = TelemetryCyan,
                    icon = Icons.Default.Functions,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Max",
                    value = if (hasData) String.format("%.1f°", stats.maxTemp) else "--",
                    color = TelemetryRed,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Humidity section
            Text(
                text = "HUMIDITY (%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = TelemetryAmber
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    title = "Min",
                    value = if (hasData) String.format("%.1f%%", stats.minHumidity) else "--",
                    color = TelemetryBlue,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Avg",
                    value = if (hasData) String.format("%.1f%%", stats.avgHumidity) else "--",
                    color = TelemetryAmber,
                    icon = Icons.Default.Functions,
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Max",
                    value = if (hasData) String.format("%.1f%%", stats.maxHumidity) else "--",
                    color = TelemetryRed,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}
