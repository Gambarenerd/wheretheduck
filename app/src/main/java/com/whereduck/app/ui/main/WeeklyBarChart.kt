package com.whereduck.app.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whereduck.app.ui.theme.DuckTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayStats(
    val label: String,
    val sent: Int,
    val received: Int,
    val isToday: Boolean
)

@Composable
fun WeeklyBarChart(
    sentByDay: Map<String, Int>,
    receivedByDay: Map<String, Int>,
    totalSent: Int,
    totalReceived: Int,
    modifier: Modifier = Modifier
) {
    val days = remember {
        val cal = Calendar.getInstance()
        val todayIndex = 6
        val dayOfWeekFmt = SimpleDateFormat("EEEEE", Locale.getDefault())
        val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        cal.add(Calendar.DAY_OF_YEAR, -6)
        (0..6).map { i ->
            val label = dayOfWeekFmt.format(cal.time).take(1).uppercase()
            val key = keyFmt.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            DayStats(
                label = label,
                sent = sentByDay[key] ?: 0,
                received = receivedByDay[key] ?: 0,
                isToday = i == todayIndex
            )
        }
    }

    val maxValue = remember(days) {
        (days.maxOfOrNull { maxOf(it.sent, it.received) } ?: 1).coerceAtLeast(1)
    }

    val sentColor = DuckTheme.colors.chartBarSent
    val receivedColor = DuckTheme.colors.chartBarReceived
    val labelColor = DuckTheme.colors.chartLabel
    val titleColor = DuckTheme.colors.sectionTitle
    val todayColor = DuckTheme.colors.highlight

    Column(modifier = modifier) {
        // "Inflitti: N" / "Subiti: N" — right aligned
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = titleColor, fontWeight = FontWeight.SemiBold)) {
                        append("Inflitti: ")
                    }
                    withStyle(SpanStyle(color = sentColor, fontWeight = FontWeight.Bold)) {
                        append("$totalSent")
                    }
                },
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = titleColor, fontWeight = FontWeight.SemiBold)) {
                        append("Subiti: ")
                    }
                    withStyle(SpanStyle(color = receivedColor, fontWeight = FontWeight.Bold)) {
                        append("$totalReceived")
                    }
                },
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val chartHeight = size.height
            val chartWidth = size.width
            val dayWidth = chartWidth / 7f
            val barWidth = dayWidth * 0.38f
            val gap = dayWidth * 0.03f
            val cornerPx = barWidth / 2f

            days.forEachIndexed { index, day ->
                val centerX = dayWidth * index + dayWidth / 2f

                // Sent bar (left)
                val sentHeight = if (maxValue > 0) (day.sent.toFloat() / maxValue) * (chartHeight * 0.85f) else 0f
                val sentBarHeight = sentHeight.coerceAtLeast(if (day.sent > 0) cornerPx * 2 else 0f)
                if (day.sent > 0) {
                    val left = centerX - barWidth - gap / 2f
                    val top = chartHeight - sentBarHeight
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = left,
                                top = top,
                                right = left + barWidth,
                                bottom = chartHeight,
                                topLeftCornerRadius = CornerRadius(cornerPx, cornerPx),
                                topRightCornerRadius = CornerRadius(cornerPx, cornerPx),
                                bottomLeftCornerRadius = CornerRadius.Zero,
                                bottomRightCornerRadius = CornerRadius.Zero
                            )
                        )
                    }
                    drawPath(path, sentColor)
                }

                // Received bar (right)
                val recHeight = if (maxValue > 0) (day.received.toFloat() / maxValue) * (chartHeight * 0.85f) else 0f
                val recBarHeight = recHeight.coerceAtLeast(if (day.received > 0) cornerPx * 2 else 0f)
                if (day.received > 0) {
                    val left = centerX + gap / 2f
                    val top = chartHeight - recBarHeight
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = left,
                                top = top,
                                right = left + barWidth,
                                bottom = chartHeight,
                                topLeftCornerRadius = CornerRadius(cornerPx, cornerPx),
                                topRightCornerRadius = CornerRadius(cornerPx, cornerPx),
                                bottomLeftCornerRadius = CornerRadius.Zero,
                                bottomRightCornerRadius = CornerRadius.Zero
                            )
                        )
                    }
                    drawPath(path, receivedColor)
                }

                // Baseline dot for empty days
                if (day.sent == 0 && day.received == 0) {
                    drawCircle(
                        color = receivedColor,
                        radius = 3f,
                        center = Offset(centerX, chartHeight - 3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day labels — single letter, today highlighted
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            days.forEach { day ->
                Text(
                    text = day.label,
                    fontSize = 14.sp,
                    fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (day.isToday) todayColor else labelColor
                )
            }
        }
    }
}
