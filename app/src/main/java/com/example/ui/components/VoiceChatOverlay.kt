package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSurfaceBorder

@Composable
fun VoiceChatOverlay(
    isOpen: Boolean,
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    lastUserSpokenText: String?,
    lastJarvisSpokenText: String?,
    onCloseVoiceChat: () -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onStopSpeaking: () -> Unit
) {
    if (!isOpen) return

    val infiniteTransition = rememberInfiniteTransition(label = "voice_arc_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isSpeaking || isListening) 1.18f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 400 else if (isListening) 550 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arc_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 2500 else if (isThinking) 1800 else 6000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arc_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisDarkBg.copy(alpha = 0.97f))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        // Holographic Background Visualizer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.42f)
            val ringColor = when {
                isListening -> Color(0xFFFF5252)
                isSpeaking -> JarvisCyan
                isThinking -> JarvisAmber
                else -> JarvisCyanDark
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ringColor.copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.65f
                ),
                center = center,
                radius = size.width * 0.65f
            )

            // Outer Orbit Ring
            drawCircle(
                color = ringColor.copy(alpha = 0.4f),
                radius = size.width * 0.38f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isListening) Color(0xFFFF5252) else JarvisCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "J.A.R.V.I.S. VOICE CHAT",
                            color = JarvisCyanLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "MICROSOFT CONRAD NEURAL • DE-DE",
                        color = JarvisCyan.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onCloseVoiceChat,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(JarvisNavy)
                        .border(1.dp, JarvisSurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Schließen",
                        tint = Color.White
                    )
                }
            }

            // Center Holographic Voice Reactor
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(JarvisNavy.copy(alpha = 0.7f))
                        .border(
                            2.5.dp,
                            when {
                                isListening -> Color(0xFFFF5252)
                                isSpeaking -> JarvisCyan
                                isThinking -> JarvisAmber
                                else -> JarvisCyanDark
                            },
                            CircleShape
                        )
                        .clickable {
                            if (isSpeaking) {
                                onStopSpeaking()
                            } else if (isListening) {
                                onStopVoice()
                            } else {
                                onStartVoice()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Inner glowing icon
                    Icon(
                        imageVector = when {
                            isSpeaking -> Icons.Default.VolumeUp
                            isListening -> Icons.Default.Mic
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Voice Status",
                        tint = when {
                            isListening -> Color(0xFFFF5252)
                            isSpeaking -> JarvisCyanLight
                            isThinking -> JarvisAmber
                            else -> JarvisCyan
                        },
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status Text
                Text(
                    text = when {
                        isSpeaking -> "JARVIS ANTWORTET (CONRAD STIMME)..."
                        isListening -> "ICH HÖRE ZU... SPRECHEN SIE, SIR"
                        isThinking -> "VERARBEITE SPRACHBEFEHL..."
                        else -> "BEREIT • TIPPEN ZUM SPRECHEN"
                    },
                    color = when {
                        isSpeaking -> JarvisCyanLight
                        isListening -> Color(0xFFFF8A80)
                        isThinking -> JarvisAmber
                        else -> Color.White.copy(alpha = 0.8f)
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Live Transcript Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(JarvisNavy)
                    .border(1.dp, JarvisCyanDark, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (!lastUserSpokenText.isNullOrBlank()) {
                    Text(
                        text = "SIE: \"$lastUserSpokenText\"",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (!lastJarvisSpokenText.isNullOrBlank()) {
                    Text(
                        text = "JARVIS: ${lastJarvisSpokenText.take(160)}...",
                        color = JarvisCyanLight,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 3
                    )
                } else if (lastUserSpokenText.isNullOrBlank()) {
                    Text(
                        text = "Reden Sie einfach mit Jarvis. Beispiel: 'Erstelle einen Termin für morgen 15 Uhr' oder 'Starte WhatsApp' oder 'Wie wird das Wetter?'",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isListening) onStopVoice() else onStartVoice()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) Color(0xFFFF5252) else JarvisCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "STOPPEN" else "SPRECHEN",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onCloseVoiceChat,
                    modifier = Modifier
                        .weight(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisNavy),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder)
                ) {
                    Text(
                        text = "SCHLIESSEN",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
