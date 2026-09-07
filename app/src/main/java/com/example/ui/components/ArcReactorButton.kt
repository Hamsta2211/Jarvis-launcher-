package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlow
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceBorder

@Composable
fun ArcReactorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPulsingShockwave: Boolean = false,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

    // Continuous smooth slow rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reactor_rotation"
    )

    // Counter rotation for inner ring
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "reactor_counter_rotation"
    )

    // Ambient breathing core glow
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reactor_core_pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Main Arc Reactor Canvas
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.minDimension / 2f - 4.dp.toPx()

            // 1. Dark outer ring background
            drawCircle(
                color = JarvisNavy.copy(alpha = 0.9f),
                radius = maxRadius,
                center = center
            )

            // 2. Outer segmented tech border
            drawCircle(
                color = JarvisSurfaceBorder,
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // 3. Dial tick marks around the perimeter (Hardware-accelerated rotation)
            withTransform({
                rotate(rotation, center)
            }) {
                val tickCount = 48
                val stepDeg = 360f / tickCount
                for (i in 0 until tickCount) {
                    val angleRad = Math.toRadians((i * stepDeg).toDouble())
                    val isMajor = i % 4 == 0
                    val tickLen = if (isMajor) 8.dp.toPx() else 4.dp.toPx()

                    val startX = center.x + (maxRadius - tickLen) * Math.cos(angleRad).toFloat()
                    val startY = center.y + (maxRadius - tickLen) * Math.sin(angleRad).toFloat()
                    val endX = center.x + maxRadius * Math.cos(angleRad).toFloat()
                    val endY = center.y + maxRadius * Math.sin(angleRad).toFloat()

                    val tickColor = if (isMajor) JarvisCyanLight else JarvisCyanDark.copy(alpha = 0.6f)
                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2f else 1.2f
                    )
                }
            }

            // 4. Middle concentric ring
            val midRadius = maxRadius * 0.78f
            drawCircle(
                color = JarvisCyanDark.copy(alpha = 0.35f),
                radius = midRadius,
                center = center,
                style = Stroke(width = 2f)
            )

            // Rotating segmented arcs on middle ring (Hardware-accelerated counter-rotation)
            withTransform({
                rotate(counterRotation, center)
            }) {
                val arcAngles = listOf(0f, 90f, 180f, 270f)
                arcAngles.forEach { startAngle ->
                    drawArc(
                        color = JarvisCyan,
                        startAngle = startAngle,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 5. Inner glow circle
            val innerRadius = maxRadius * 0.54f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisCyan.copy(alpha = 0.45f * ambientPulse),
                        JarvisCyanDark.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = JarvisCyanLight,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.8f)
            )

            // 6. Central Glowing Equilateral Triangle (Iconic Arc Reactor Core)
            val triangleSize = innerRadius * 0.72f * ambientPulse
            val trianglePath = Path().apply {
                val topX = center.x
                val topY = center.y - triangleSize
                val rightX = center.x + triangleSize * 0.866f
                val rightY = center.y + triangleSize * 0.5f
                val leftX = center.x - triangleSize * 0.866f
                val leftY = center.y + triangleSize * 0.5f

                moveTo(topX, topY)
                lineTo(rightX, rightY)
                lineTo(leftX, leftY)
                close()
            }

            // Outer triangle stroke
            drawPath(
                path = trianglePath,
                color = JarvisCyanLight,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Inner slightly smaller filled triangle with cyan/white gradient
            val innerTrianglePath = Path().apply {
                val scale = 0.7f
                val topX = center.x
                val topY = center.y - triangleSize * scale
                val rightX = center.x + (triangleSize * 0.866f) * scale
                val rightY = center.y + (triangleSize * 0.5f) * scale
                val leftX = center.x - (triangleSize * 0.866f) * scale
                val leftY = center.y + (triangleSize * 0.5f) * scale

                moveTo(topX, topY)
                lineTo(rightX, rightY)
                lineTo(leftX, leftY)
                close()
            }
            drawPath(
                path = innerTrianglePath,
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, JarvisCyan.copy(alpha = 0.7f), Color.Transparent),
                    center = center,
                    radius = triangleSize * 0.6f
                ),
                style = Fill
            )

            // 7. Bright Center Reactor Core Light
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, JarvisGlow, Color.Transparent),
                    center = center,
                    radius = 12.dp.toPx() * ambientPulse
                ),
                radius = 12.dp.toPx() * ambientPulse,
                center = center
            )
        }
    }
}
