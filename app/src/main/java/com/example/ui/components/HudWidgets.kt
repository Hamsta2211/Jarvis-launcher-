package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlow
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceBorder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WeekdayHeader(
    currentDate: Date,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance().apply { time = currentDate }
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    // German standard day abbreviations Monday through Sunday (Uniform 3-char format)
    val days = listOf(
        Pair(Calendar.MONDAY, "Mo."),
        Pair(Calendar.TUESDAY, "Di."),
        Pair(Calendar.WEDNESDAY, "Mi."),
        Pair(Calendar.THURSDAY, "Do."),
        Pair(Calendar.FRIDAY, "Fr."),
        Pair(Calendar.SATURDAY, "Sa."),
        Pair(Calendar.SUNDAY, "So.")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sci-Fi Tech Brackets Top
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(20f, h)
                lineTo(w * 0.35f, h)
                lineTo(w * 0.38f, 2f)
                lineTo(w * 0.47f, 2f)
                lineTo(w * 0.5f, h)
                lineTo(w * 0.53f, 2f)
                lineTo(w * 0.62f, 2f)
                lineTo(w * 0.65f, h)
                lineTo(w - 20f, h)
            }
            drawPath(
                path = path,
                color = JarvisSurfaceBorder,
                style = Stroke(width = 1.5f, cap = StrokeCap.Round)
            )
            // Center neon down chevron
            drawCircle(color = JarvisCyan, radius = 2f, center = Offset(w * 0.5f, h - 2f))
        }

        // Days Row with uniform alignment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { (calDay, label) ->
                val isToday = calDay == currentDayOfWeek
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisCyan.copy(alpha = 0.15f))
                                .border(1.dp, JarvisCyan.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = JarvisCyanLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = label,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Sci-Fi Tech Brackets Bottom
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(30f, 0f)
                lineTo(w * 0.45f, 0f)
                lineTo(w * 0.5f, h - 2f)
                lineTo(w * 0.55f, 0f)
                lineTo(w - 30f, 0f)
            }
            drawPath(
                path = path,
                color = JarvisSurfaceBorder,
                style = Stroke(width = 1.5f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun HudClockWidget(
    currentDate: Date,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
    val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
    val monthDayFormat = SimpleDateFormat("MMMM\ndd", Locale.GERMAN)

    val timeStr = timeFormat.format(currentDate)
    val amPmStr = amPmFormat.format(currentDate)
    val monthDayStr = monthDayFormat.format(currentDate).uppercase()

    val infiniteTransition = rememberInfiniteTransition(label = "clock_arc")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clock_pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(116.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(116.dp)) {
                val stroke = 3.dp.toPx()
                val center = size / 2f
                val radius = (size.minDimension - stroke) / 2f

                // Dial ticks
                val tickCount = 36
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (Math.PI / 180f)
                    val tickLen = if (i % 6 == 0) 10.dp.toPx() else 5.dp.toPx()
                    val startX = center.width + (radius - tickLen) * Math.cos(angle).toFloat()
                    val startY = center.height + (radius - tickLen) * Math.sin(angle).toFloat()
                    val endX = center.width + radius * Math.cos(angle).toFloat()
                    val endY = center.height + radius * Math.sin(angle).toFloat()
                    val tickColor = if (i % 6 == 0) JarvisCyan.copy(alpha = 0.8f) else JarvisSurfaceBorder
                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.5f
                    )
                }

                // Glowing outer arc indicator
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(JarvisCyan.copy(alpha = 0.1f), JarvisCyan, JarvisGlow)
                    ),
                    startAngle = -90f,
                    sweepAngle = 240f * pulse,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                // Indicator dot
                val dotAngle = (-90f + 240f * pulse) * (Math.PI / 180f)
                val dotX = center.width + radius * Math.cos(dotAngle).toFloat()
                val dotY = center.height + radius * Math.sin(dotAngle).toFloat()
                drawCircle(color = JarvisCyanLight, radius = 3.5.dp.toPx(), center = Offset(dotX, dotY))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeStr,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = amPmStr,
                    color = JarvisCyanLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = monthDayStr,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun WeatherBatteryWidget(
    weatherCity: String,
    temperatureCelsius: Int = 18,
    weatherCode: Int = 0,
    conditionDescription: String = "Klar",
    isGpsLocation: Boolean = false,
    isRefreshing: Boolean = false,
    batteryLevel: Int = 100,
    onWeatherClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (weatherIcon, weatherColor) = when (weatherCode) {
        0 -> Pair(Icons.Default.WbSunny, Color(0xFFFFD54F))
        1, 2 -> Pair(Icons.Default.WbCloudy, Color(0xFFFFE082))
        3 -> Pair(Icons.Default.Cloud, Color(0xFFB0BEC5))
        45, 48 -> Pair(Icons.Default.Cloud, Color(0xFF90A4AE))
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Pair(Icons.Default.WaterDrop, Color(0xFF4FC3F7))
        71, 73, 75, 77, 85, 86 -> Pair(Icons.Default.AcUnit, Color(0xFFE1F5FE))
        95, 96, 99 -> Pair(Icons.Default.Thunderstorm, Color(0xFFFFB74D))
        else -> Pair(Icons.Default.WbSunny, Color(0xFFFFD54F))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Weather Row - Clickable for Instant Refresh & Location Request
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onWeatherClick() }
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${temperatureCelsius}°C",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isRefreshing) Icons.Default.Refresh else weatherIcon,
                    contentDescription = conditionDescription,
                    tint = if (isRefreshing) JarvisCyan else weatherColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGpsLocation) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS aktiv",
                        tint = JarvisCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }

                Text(
                    text = weatherCity.uppercase(),
                    color = if (isGpsLocation) JarvisCyanLight else Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Battery HUD Bar
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(JarvisNavy)
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(3.dp))
            ) {
                // Progress
                val fillRatio = (batteryLevel.coerceIn(0, 100)) / 100f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillRatio)
                        .height(14.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(JarvisCyanDark, JarvisCyan, JarvisGlow)
                            )
                        )
                )

                // Lightning Icon
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Battery",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                )
            }

            // Battery measurement ticks
            Canvas(
                modifier = Modifier
                    .width(130.dp)
                    .height(5.dp)
                    .padding(top = 1.dp)
            ) {
                val tickCount = 10
                val step = size.width / tickCount
                for (i in 0..tickCount) {
                    val x = i * step
                    drawLine(
                        color = JarvisSurfaceBorder,
                        start = Offset(x, 0f),
                        end = Offset(x, 4f),
                        strokeWidth = 1f
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Batterie $batteryLevel%",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun BottomMusicWidget(
    isPlaying: Boolean,
    trackName: String,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenMusicPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimalist Sci-Fi Music Capsule (Only shown when music is actually playing)
        AnimatedVisibility(
            visible = isPlaying,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(JarvisNavy.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        JarvisCyan.copy(alpha = 0.6f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenMusicPlayer() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Musik",
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = trackName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Play/Pause",
                                tint = JarvisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNext,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Minimalist swipe-up indicator
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onOpenDrawer() }
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Apps öffnen",
                    tint = JarvisCyan.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
