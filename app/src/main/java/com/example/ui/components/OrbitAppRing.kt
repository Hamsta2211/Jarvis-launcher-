package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.service.AppLauncherHelper
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceBorder
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrbitAppRing(
    apps: List<AppItem>,
    appLauncherHelper: AppLauncherHelper,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (Int, AppItem) -> Unit,
    onEmptySlotClick: (Int) -> Unit,
    onJarvisClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFolderClick: (AppItem) -> Unit,
    isPulsing: Boolean,
    transitionProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val centerOffset = Offset(widthPx / 2f, heightPx / 2f)

        // Orbital radius: beautifully proportioned around Arc Reactor
        val orbitRadius = maxWidth * 0.39f
        val orbitRadiusPx = with(density) { orbitRadius.toPx() }

        val hudAlpha = (1f - transitionProgress * 1.6f).coerceIn(0f, 1f)

        // 1. Draw connecting HUD sci-fi lines
        if (hudAlpha > 0.01f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = hudAlpha }
            ) {
                // Outer subtle circular orbit path
                drawCircle(
                    color = JarvisSurfaceBorder.copy(alpha = 0.35f),
                    radius = orbitRadiusPx,
                    center = centerOffset,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )

                // Inner accent ring around reactor
                drawCircle(
                    color = JarvisCyanDark.copy(alpha = 0.25f),
                    radius = orbitRadiusPx * 0.65f,
                    center = centerOffset,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                )

                // If dual orbit (more than 10 apps), draw inner orbit ring
                if (apps.size > 10) {
                    drawCircle(
                        color = JarvisCyan.copy(alpha = 0.18f),
                        radius = orbitRadiusPx * 0.68f,
                        center = centerOffset,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8f)
                    )
                }

                // Connecting lines to top J.A.R.V.I.S pill and bottom SUCHEN pill
                val topPillY = centerOffset.y - orbitRadiusPx * 0.95f
                val botPillY = centerOffset.y + orbitRadiusPx * 0.95f

                drawLine(
                    color = JarvisSurfaceBorder.copy(alpha = 0.5f),
                    start = Offset(centerOffset.x, centerOffset.y - orbitRadiusPx * 0.5f),
                    end = Offset(centerOffset.x, topPillY),
                    strokeWidth = 1.2f
                )

                drawLine(
                    color = JarvisSurfaceBorder.copy(alpha = 0.5f),
                    start = Offset(centerOffset.x, centerOffset.y + orbitRadiusPx * 0.5f),
                    end = Offset(centerOffset.x, botPillY),
                    strokeWidth = 1.2f
                )
            }
        }

        // 2. Central Arc Reactor Button (with outward expansion on transition)
        val reactorScale = 1f + transitionProgress * 0.4f
        val reactorAlpha = (1f - transitionProgress * 1.5f).coerceIn(0f, 1f)
        if (reactorAlpha > 0.01f) {
            ArcReactorButton(
                onClick = onJarvisClick,
                isPulsingShockwave = isPulsing,
                size = maxWidth * 0.38f,
                modifier = Modifier.graphicsLayer {
                    scaleX = reactorScale
                    scaleY = reactorScale
                    alpha = reactorAlpha
                }
            )
        }

        // 3. Top J.A.R.V.I.S Pill & Settings Icon
        if (hudAlpha > 0.01f) {
            val topOffsetDp = -orbitRadius * 0.95f - (50.dp * transitionProgress)
            Column(
                modifier = Modifier
                    .offset { IntOffset(0, with(density) { topOffsetDp.toPx() }.roundToInt()) }
                    .graphicsLayer { alpha = hudAlpha },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(JarvisNavy.copy(alpha = 0.85f))
                        .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(start = 16.dp, end = 6.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Text(
                        text = "J.A.R.V.I.S.",
                        color = JarvisCyanLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Einstellungen",
                            tint = JarvisCyan.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 4. Bottom SUCHEN Pill (App Drawer Trigger)
        if (hudAlpha > 0.01f) {
            val bottomOffsetDp = orbitRadius * 0.95f + (50.dp * transitionProgress)
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, with(density) { bottomOffsetDp.toPx() }.roundToInt()) }
                    .graphicsLayer { alpha = hudAlpha }
                    .clip(RoundedCornerShape(16.dp))
                    .background(JarvisNavy.copy(alpha = 0.85f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .combinedClickable(onClick = onSearchClick)
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SUCHEN",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.8.sp
                )
            }
        }

        // 5. Dynamic Orbit Placements for any number of apps (8, 10, 12, 14, 16+)
        val totalCount = apps.size.coerceAtLeast(8)
        val nodePlacements = remember(totalCount, orbitRadius) {
            calculateDynamicPlacements(totalCount, orbitRadius)
        }

        if (hudAlpha > 0.01f) {
            nodePlacements.forEachIndexed { index, (angleDeg, baseRadiusDp) ->
                val angleRad = Math.toRadians(angleDeg.toDouble())
                // Fly outwards when transitioning:
                val activeRadiusDp = baseRadiusDp * (1f + transitionProgress * 0.95f)
                val xOffsetDp = activeRadiusDp * cos(angleRad).toFloat()
                val yOffsetDp = activeRadiusDp * sin(angleRad).toFloat()

                val appItem = apps.getOrNull(index)

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                with(density) { xOffsetDp.toPx() }.roundToInt(),
                                with(density) { yOffsetDp.toPx() }.roundToInt()
                            )
                        }
                        .graphicsLayer {
                            alpha = hudAlpha
                            scaleX = 1f + transitionProgress * 0.25f
                            scaleY = 1f + transitionProgress * 0.25f
                        }
                ) {
                    OrbitNode(
                        appItem = appItem,
                        slotIndex = index,
                        appLauncherHelper = appLauncherHelper,
                        onClick = {
                            if (appItem != null) {
                                if (appItem.isFolder) {
                                    onFolderClick(appItem)
                                } else if (appItem.iconKey == "empty") {
                                    onEmptySlotClick(index)
                                } else {
                                    onAppClick(appItem)
                                }
                            } else {
                                onEmptySlotClick(index)
                            }
                        },
                        onLongClick = {
                            if (appItem != null) {
                                onAppLongClick(index, appItem)
                            } else {
                                onEmptySlotClick(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Calculates symmetrical placements around left and right arcs, or dual concentric rings for higher counts.
 */
private fun calculateDynamicPlacements(count: Int, orbitRadius: Dp): List<Pair<Float, Dp>> {
    return when {
        count <= 8 -> listOf(
            // 4 left, 4 right
            Pair(-135f, orbitRadius * 0.95f),
            Pair(-168f, orbitRadius * 1.02f),
            Pair(168f, orbitRadius * 1.02f),
            Pair(135f, orbitRadius * 0.95f),
            Pair(-45f, orbitRadius * 0.95f),
            Pair(-12f, orbitRadius * 1.02f),
            Pair(12f, orbitRadius * 1.02f),
            Pair(45f, orbitRadius * 0.95f)
        )
        count <= 10 -> listOf(
            // 5 left, 5 right
            Pair(-125f, orbitRadius * 0.95f),
            Pair(-150f, orbitRadius * 1.02f),
            Pair(180f, orbitRadius * 1.04f),
            Pair(150f, orbitRadius * 1.02f),
            Pair(125f, orbitRadius * 0.95f),
            Pair(-55f, orbitRadius * 0.95f),
            Pair(-30f, orbitRadius * 1.02f),
            Pair(0f, orbitRadius * 1.04f),
            Pair(30f, orbitRadius * 1.02f),
            Pair(55f, orbitRadius * 0.95f)
        )
        count <= 12 -> listOf(
            // 6 left, 6 right
            Pair(-120f, orbitRadius * 0.95f),
            Pair(-144f, orbitRadius * 1.02f),
            Pair(-168f, orbitRadius * 1.04f),
            Pair(168f, orbitRadius * 1.04f),
            Pair(144f, orbitRadius * 1.02f),
            Pair(120f, orbitRadius * 0.95f),
            Pair(-60f, orbitRadius * 0.95f),
            Pair(-36f, orbitRadius * 1.02f),
            Pair(-12f, orbitRadius * 1.04f),
            Pair(12f, orbitRadius * 1.04f),
            Pair(36f, orbitRadius * 1.02f),
            Pair(60f, orbitRadius * 0.95f)
        )
        else -> {
            // Dual concentric rings: Outer ring 8 + Inner ring (count - 8)
            val outerList = listOf(
                Pair(-135f, orbitRadius * 1.05f),
                Pair(-168f, orbitRadius * 1.08f),
                Pair(168f, orbitRadius * 1.08f),
                Pair(135f, orbitRadius * 1.05f),
                Pair(-45f, orbitRadius * 1.05f),
                Pair(-12f, orbitRadius * 1.08f),
                Pair(12f, orbitRadius * 1.08f),
                Pair(45f, orbitRadius * 1.05f)
            )
            val innerCount = (count - 8).coerceAtMost(8)
            val innerRadius = orbitRadius * 0.68f
            val innerAngles = listOf(-140f, 180f, 140f, -40f, 0f, 40f, -90f, 90f).take(innerCount)
            val innerList = innerAngles.map { angle -> Pair(angle, innerRadius) }
            outerList + innerList
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrbitNode(
    appItem: AppItem?,
    slotIndex: Int,
    appLauncherHelper: AppLauncherHelper,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeSize = 50.dp

    val iconBitmap = remember(appItem?.packageName) {
        if (appItem != null && appItem.packageName.isNotBlank()) {
            appLauncherHelper.getAppIcon(appItem.packageName)
        } else null
    }

    Box(
        modifier = modifier
            .size(nodeSize)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (appItem == null || appItem.iconKey == "empty") {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(JarvisNavy.copy(alpha = 0.75f))
                    .border(
                        width = 1.2.dp,
                        color = JarvisSurfaceBorder.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "App hinzufügen",
                    tint = JarvisCyan.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (appItem.isFolder) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(JarvisNavy.copy(alpha = 0.9f))
                    .border(1.2.dp, JarvisCyan.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        MiniFolderIcon(item = appItem.folderItems.getOrNull(0), appLauncherHelper = appLauncherHelper)
                        Spacer(modifier = Modifier.width(2.dp))
                        MiniFolderIcon(item = appItem.folderItems.getOrNull(1), appLauncherHelper = appLauncherHelper)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.Center) {
                        MiniFolderIcon(item = appItem.folderItems.getOrNull(2), appLauncherHelper = appLauncherHelper)
                        Spacer(modifier = Modifier.width(2.dp))
                        MiniFolderIcon(item = appItem.folderItems.getOrNull(3), appLauncherHelper = appLauncherHelper)
                    }
                }
            }
        } else if (iconBitmap != null) {
            // High-resolution real Android icon with sleek subtle glowing border
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(JarvisNavy.copy(alpha = 0.5f))
                    .border(1.2.dp, JarvisCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = appItem.label,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
        } else {
            val iconVector = resolveAppIcon(appItem.iconKey, appItem.label)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(JarvisNavy.copy(alpha = 0.88f))
                    .border(1.2.dp, JarvisCyan.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = appItem.label,
                    tint = JarvisCyanLight,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniFolderIcon(item: AppItem?, appLauncherHelper: AppLauncherHelper) {
    if (item == null) {
        Spacer(modifier = Modifier.size(16.dp))
        return
    }
    val iconBitmap = remember(item.packageName) {
        if (item.packageName.isNotBlank()) {
            appLauncherHelper.getAppIcon(item.packageName)
        } else null
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
        )
    } else {
        Icon(
            imageVector = resolveAppIcon(item.iconKey, item.label),
            contentDescription = null,
            tint = JarvisCyan.copy(alpha = 0.85f),
            modifier = Modifier.size(16.dp)
        )
    }
}

fun resolveAppIcon(key: String, label: String): ImageVector {
    val k = key.lowercase()
    val l = label.lowercase()
    return when {
        k == "whatsapp" || l.contains("whatsapp") -> Icons.Default.ChatBubble
        k == "chrome" || l.contains("chrome") -> Icons.Default.Language
        k == "youtube" || l.contains("youtube") -> Icons.Default.Subscriptions
        k == "music" || l.contains("musik") || l.contains("music") -> Icons.Default.MusicNote
        k == "flashlight" || l.contains("taschenlampe") || l.contains("torch") -> Icons.Default.FlashlightOn
        k == "play" || l.contains("play") -> Icons.Default.PlayArrow
        k == "mail" || l.contains("mail") || l.contains("gmail") -> Icons.Default.Email
        k == "maps" || l.contains("maps") || l.contains("karten") -> Icons.Default.Map
        k == "shopping" || l.contains("shop") || l.contains("amazon") -> Icons.Default.ShoppingBag
        k == "browser" || l.contains("browser") || l.contains("web") -> Icons.Default.Public
        k == "grid" || l.contains("drawer") -> Icons.Default.Widgets
        k == "folder" -> Icons.Default.Folder
        else -> Icons.Default.Widgets
    }
}
