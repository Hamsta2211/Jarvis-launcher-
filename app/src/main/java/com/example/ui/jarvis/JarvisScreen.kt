package com.example.ui.jarvis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionType
import com.example.data.model.ActiveTimer
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.ui.components.ArcReactorButton
import com.example.ui.components.VoiceChatOverlay
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisGlow
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceBorder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisScreen(
    chatMessages: List<ChatMessage>,
    chatSessions: List<ChatSession>,
    currentSessionId: String?,
    isThinkingEnabled: Boolean,
    isThinking: Boolean,
    isListening: Boolean,
    speechStatusMessage: String?,
    activeTimer: ActiveTimer?,
    isSpeaking: Boolean = false,
    isVoiceOutputEnabled: Boolean = true,
    isVoiceChatMode: Boolean = false,
    onSendMessage: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onToggleThinking: () -> Unit,
    onToggleVoiceOutput: () -> Unit = {},
    onToggleVoiceChatMode: () -> Unit = {},
    onStopSpeaking: () -> Unit = {},
    onSpeakMessage: (String) -> Unit = {},
    onOpenAgenda: () -> Unit = {},
    onNewChat: () -> Unit,
    onSelectChat: (String) -> Unit,
    onRenameChat: (String, String) -> Unit,
    onDeleteChat: (String) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onCancelTimer: () -> Unit,
    onCloseJarvis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showSessionsSheet by remember { mutableStateOf(false) }
    var renameSessionTarget by remember { mutableStateOf<ChatSession?>(null) }
    var deleteSessionTarget by remember { mutableStateOf<ChatSession?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-scroll on new message or during streaming tokens
    val lastMsgLength = chatMessages.lastOrNull()?.text?.length ?: 0
    val lastMsgThoughtsLength = chatMessages.lastOrNull()?.thoughtText?.length ?: 0
    LaunchedEffect(chatMessages.size, lastMsgLength, lastMsgThoughtsLength) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBg)
    ) {
        // Sci-Fi ambient backdrop grid & center glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.45f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(JarvisCyan.copy(alpha = 0.07f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.7f
                ),
                center = center,
                radius = size.width * 0.7f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 1. TOP HUD HEADER BAR
            JarvisHudTopBar(
                isThinkingEnabled = isThinkingEnabled,
                isThinking = isThinking,
                isListening = isListening,
                isSpeaking = isSpeaking,
                isVoiceOutputEnabled = isVoiceOutputEnabled,
                currentSessionTitle = chatSessions.find { it.id == currentSessionId }?.title ?: "J.A.R.V.I.S.",
                onToggleThinking = onToggleThinking,
                onToggleVoiceOutput = onToggleVoiceOutput,
                onOpenVoiceChat = onToggleVoiceChatMode,
                onOpenAgenda = onOpenAgenda,
                onNewChat = onNewChat,
                onOpenHistory = { showSessionsSheet = true },
                onClose = onCloseJarvis
            )

            // 2. ACTIVE TIMER BANNER (IF ACTIVE)
            if (activeTimer != null) {
                ActiveTimerHudBanner(
                    timer = activeTimer,
                    onPause = onPauseTimer,
                    onResume = onResumeTimer,
                    onCancel = onCancelTimer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 3. CHAT MESSAGES LIST
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatMessages.isEmpty()) {
                    JarvisEmptyState(
                        onSelectPrompt = { prompt ->
                            onSendMessage(prompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(6.dp)) }

                        items(chatMessages, key = { it.id }) { message ->
                            ChatMessageItem(
                                message = message,
                                isThinkingEnabled = isThinkingEnabled,
                                isSpeaking = isSpeaking,
                                onCopyText = { text ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Jarvis", text))
                                    Toast.makeText(context, "In Zwischenablage kopiert", Toast.LENGTH_SHORT).show()
                                },
                                onSpeakText = { text ->
                                    if (isSpeaking) onStopSpeaking() else onSpeakMessage(text)
                                },
                                onQuickAction = { actionText ->
                                    onSendMessage(actionText)
                                }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(10.dp)) }
                    }
                }
            }

            // 4. SPEECH RECOGNITION STATUS BANNER
            AnimatedVisibility(
                visible = isListening || speechStatusMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SpeechListeningBanner(
                    isListening = isListening,
                    statusMessage = speechStatusMessage,
                    onStop = onStopVoice,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 5. QUICK PROMPTS CHIPS BAR
            QuickPromptsBar(
                onPromptClicked = { prompt ->
                    onSendMessage(prompt)
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 6. BOTTOM INPUT & VOICE CONTROLS
            JarvisInputBar(
                inputText = inputText,
                onInputChanged = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank() && !isThinking) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                isThinking = isThinking,
                isListening = isListening,
                onToggleVoice = {
                    if (isListening) onStopVoice() else onStartVoice()
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // CHAT SESSIONS MODAL BOTTOM SHEET
        if (showSessionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSessionsSheet = false },
                sheetState = sheetState,
                containerColor = JarvisNavy,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(JarvisCyan.copy(alpha = 0.5f))
                    )
                }
            ) {
                ChatSessionsSheetContent(
                    sessions = chatSessions,
                    currentSessionId = currentSessionId,
                    onSelect = { id ->
                        onSelectChat(id)
                        showSessionsSheet = false
                    },
                    onNewChat = {
                        onNewChat()
                        showSessionsSheet = false
                    },
                    onRename = { session ->
                        renameSessionTarget = session
                    },
                    onDelete = { session ->
                        deleteSessionTarget = session
                    }
                )
            }
        }

        // RENAME CHAT DIALOG
        if (renameSessionTarget != null) {
            var newTitle by remember { mutableStateOf(renameSessionTarget!!.title) }
            AlertDialog(
                onDismissRequest = { renameSessionTarget = null },
                containerColor = JarvisNavy,
                title = {
                    Text(
                        text = "Chat umbenennen",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisSurfaceBorder,
                            cursorColor = JarvisCyan
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            renameSessionTarget?.let { onRenameChat(it.id, newTitle) }
                            renameSessionTarget = null
                        }
                    ) {
                        Text("SPEICHERN", color = JarvisCyan, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameSessionTarget = null }) {
                        Text("ABBRECHEN", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // DELETE CHAT CONFIRMATION DIALOG
        if (deleteSessionTarget != null) {
            AlertDialog(
                onDismissRequest = { deleteSessionTarget = null },
                containerColor = JarvisNavy,
                title = {
                    Text(
                        text = "Chat löschen?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Text(
                        text = "Möchtest du den Verlauf von '${deleteSessionTarget!!.title}' wirklich unwiderruflich löschen?",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deleteSessionTarget?.let { onDeleteChat(it.id) }
                            deleteSessionTarget = null
                        }
                    ) {
                        Text("LÖSCHEN", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteSessionTarget = null }) {
                        Text("ABBRECHEN", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // FULLSCREEN VOICE CHAT OVERLAY (CONRAD NEURAL TTS)
        if (isVoiceChatMode) {
            val lastUserMsg = chatMessages.filter { it.sender == MessageSender.USER }.lastOrNull()?.text
            val lastJarvisMsg = chatMessages.filter { it.sender == MessageSender.JARVIS && !it.isStreaming }.lastOrNull()?.text
            VoiceChatOverlay(
                isOpen = true,
                isListening = isListening,
                isThinking = isThinking,
                isSpeaking = isSpeaking,
                lastUserSpokenText = lastUserMsg,
                lastJarvisSpokenText = lastJarvisMsg,
                onCloseVoiceChat = onToggleVoiceChatMode,
                onStartVoice = onStartVoice,
                onStopVoice = onStopVoice,
                onStopSpeaking = onStopSpeaking
            )
        }
    }
}

// ---------------------------------------------------------
// 1. TOP HUD BAR
// ---------------------------------------------------------
@Composable
private fun JarvisHudTopBar(
    isThinkingEnabled: Boolean,
    isThinking: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    isVoiceOutputEnabled: Boolean,
    currentSessionTitle: String,
    onToggleThinking: () -> Unit,
    onToggleVoiceOutput: () -> Unit,
    onOpenVoiceChat: () -> Unit,
    onOpenAgenda: () -> Unit,
    onNewChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = JarvisNavy.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = JarvisSurfaceBorder)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button + Title + Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Distinct Back Button Box
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisDarkBg)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSpeaking -> JarvisCyanLight
                                            isListening -> Color(0xFFFF5252)
                                            isThinking -> JarvisAmber
                                            else -> JarvisCyan
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Text(
                            text = when {
                                isSpeaking -> "CONRAD SPRICHT..."
                                isListening -> "SPRACHEINGABE..."
                                isThinking -> "ANALYSING..."
                                else -> currentSessionTitle.uppercase()
                            },
                            color = when {
                                isSpeaking -> JarvisCyanLight
                                isListening -> Color(0xFFFF8A80)
                                isThinking -> JarvisAmber
                                else -> JarvisCyan.copy(alpha = 0.75f)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Scrollable Action Buttons Row ensuring zero clipping on any screen width
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    // 1. Voice Chat Pill Button
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisCyan.copy(alpha = 0.25f))
                            .border(1.dp, JarvisCyan, RoundedCornerShape(10.dp))
                            .clickable { onOpenVoiceChat() }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice Chat",
                                tint = JarvisCyanLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VOICE",
                                color = JarvisCyanLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 2. Agenda (Termine/Kalender) Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisDarkBg)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onOpenAgenda() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Agenda",
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 3. Sound Toggle Button (Mute / Unmute)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisDarkBg)
                            .border(
                                1.dp,
                                if (isVoiceOutputEnabled) JarvisCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggleVoiceOutput() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVoiceOutputEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isVoiceOutputEnabled) "Ton an" else "Stumm",
                            tint = if (isVoiceOutputEnabled) JarvisCyan else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 4. History Button (Verlauf)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisDarkBg)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onOpenHistory() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Verlauf",
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 5. New Chat Button (+) - Highlighted Primary Action
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisCyan)
                            .border(1.dp, JarvisCyanLight, RoundedCornerShape(10.dp))
                            .clickable { onNewChat() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Neuer Chat",
                            tint = JarvisDarkBg,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 2. ACTIVE TIMER BANNER
// ---------------------------------------------------------
@Composable
private fun ActiveTimerHudBanner(
    timer: ActiveTimer,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mins = timer.remainingSeconds / 60
    val secs = timer.remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisNavy)
            .border(1.dp, JarvisAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = "Timer",
                    tint = JarvisAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "TIMER: $timeFormatted",
                        color = JarvisAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = timer.label.ifBlank { "Countdown läuft" },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = if (timer.isRunning) onPause else onResume,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timer.isRunning) "Pause" else "Fortsetzen",
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Abbrechen",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 3. CHAT MESSAGE ITEM (USER & JARVIS)
// ---------------------------------------------------------
@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    isThinkingEnabled: Boolean,
    isSpeaking: Boolean = false,
    onCopyText: (String) -> Unit,
    onSpeakText: (String) -> Unit = {},
    onQuickAction: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    var isThoughtsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // USER BUBBLE
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(JarvisCyanDark.copy(alpha = 0.5f), JarvisNavy)
                        )
                    )
                    .border(
                        1.dp,
                        JarvisCyan.copy(alpha = 0.4f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeStr,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        } else {
            // JARVIS HUD CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(JarvisNavy.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        JarvisSurfaceBorder,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    // Header: J.A.R.V.I.S. Badge + Timestamp + Conrad Voice Speaker + Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(JarvisCyan.copy(alpha = 0.2f))
                                    .border(1.dp, JarvisCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(JarvisCyanLight)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = JarvisCyanLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = timeStr,
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Conrad Voice Playback button
                            if (message.text.isNotBlank() && !message.isStreaming) {
                                IconButton(
                                    onClick = { onSpeakText(message.text) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Vorlesen (Conrad)",
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            IconButton(
                                onClick = { onCopyText(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Kopieren",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // THOUGHT TRACE ACCORDION (IF AVAILABLE OR GENERATING)
                    if (!message.thoughtText.isNullOrBlank() || (message.isStreaming && isThinkingEnabled)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisDarkBg.copy(alpha = 0.6f))
                                .border(1.dp, JarvisCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { isThoughtsExpanded = !isThoughtsExpanded }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = "Gedankengang",
                                            tint = JarvisAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (message.isStreaming && message.text.isBlank()) "Gedankenanalyse läuft..." else "Gedankengang anzeigen",
                                            color = JarvisAmber,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Icon(
                                        imageVector = if (isThoughtsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Aufklappen",
                                        tint = JarvisAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isThoughtsExpanded) {
                                    Column(modifier = Modifier.padding(top = 6.dp)) {
                                        Text(
                                            text = message.thoughtText.orEmpty().ifBlank { "Analysiere Systemstatus und Nutzerintention..." },
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // STREAMING OR FINAL TEXT
                    if (message.isStreaming && message.text.isBlank() && message.thoughtText.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = JarvisCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "System verarbeitet Anfrage...",
                                color = JarvisCyan.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }

                    // ACTION BADGES / AFFORDANCES
                    if (message.actionType != ActionType.NONE && !message.actionPayload.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ActionAffordanceBadge(
                            actionType = message.actionType,
                            payload = message.actionPayload,
                            onPerform = { onQuickAction(it) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// ACTION AFFORDANCE BADGE
// ---------------------------------------------------------
@Composable
private fun ActionAffordanceBadge(
    actionType: ActionType,
    payload: String,
    onPerform: (String) -> Unit
) {
    val (badgeText, badgeIcon, badgeColor) = when (actionType) {
        ActionType.APP_LAUNCH -> Triple("App-Befehl ausgeführt", Icons.Default.RocketLaunch, JarvisCyan)
        ActionType.TIMER -> Triple("Timer eingerichtet", Icons.Default.HourglassBottom, JarvisAmber)
        ActionType.CALENDAR -> Triple("Kalendereintrag erstellt", Icons.Default.Check, JarvisCyanLight)
        ActionType.REMINDER -> Triple("Erinnerung aktiviert", Icons.Default.Check, JarvisCyan)
        ActionType.SEARCH_RESULT -> Triple("Websuche verarbeitet", Icons.Default.Search, JarvisCyan)
        ActionType.ERROR -> Triple("Systemhinweis", Icons.Default.Close, Color(0xFFFF5252))
        ActionType.NONE -> Triple("", Icons.Default.Check, Color.Transparent)
    }

    if (badgeText.isNotBlank()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$badgeText: $payload",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 4. SPEECH LISTENING BANNER
// ---------------------------------------------------------
@Composable
private fun SpeechListeningBanner(
    isListening: Boolean,
    statusMessage: String?,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isListening) Color(0xFF1B2A38) else Color(0xFF2C1515))
            .border(
                1.dp,
                if (isListening) JarvisCyan else Color(0xFFFF5252),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Sprachstatus",
                    tint = if (isListening) JarvisCyanLight else Color(0xFFFF5252),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusMessage ?: if (isListening) "Sprich jetzt einen Befehl..." else "Mikrofon inaktiv",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (isListening) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stoppen",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 5. QUICK PROMPTS BAR
// ---------------------------------------------------------
@Composable
private fun QuickPromptsBar(
    onPromptClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts = listOf(
        "🚀 Öffne WhatsApp",
        "⏱️ 5 Min Timer",
        "🔍 Wetter morgen",
        "📅 Neuer Termin",
        "⚡ Systemdiagnose",
        "💡 Was kannst du?"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { prompt ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(JarvisNavy)
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(14.dp))
                    .clickable { onPromptClicked(prompt.substringAfter(" ")) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = prompt,
                    color = JarvisCyanLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 6. BOTTOM INPUT BAR
// ---------------------------------------------------------
@Composable
private fun JarvisInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isThinking: Boolean,
    isListening: Boolean,
    onToggleVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Surface(
        color = JarvisNavy.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Input Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Color(0xFFFF5252).copy(alpha = 0.25f)
                        else JarvisCyan.copy(alpha = 0.12f)
                    )
                    .clickable { onToggleVoice() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                    contentDescription = "Spracheingabe",
                    tint = if (isListening) Color(0xFFFF5252) else JarvisCyan,
                    modifier = Modifier
                        .size(22.dp)
                        .then(if (isListening) Modifier.size((22 * micPulse).dp) else Modifier)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Höre zu..." else "Befehl an J.A.R.V.I.S. eingeben...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = JarvisCyan,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Send Button
            IconButton(
                onClick = onSend,
                enabled = inputText.isNotBlank() && !isThinking,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isThinking) JarvisCyan
                        else Color.White.copy(alpha = 0.08f)
                    )
            ) {
                if (isThinking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = JarvisCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Senden",
                        tint = if (inputText.isNotBlank()) JarvisNavy else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// EMPTY STATE / WELCOME SCREEN
// ---------------------------------------------------------
@Composable
private fun JarvisEmptyState(
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ArcReactorButton(
            onClick = {},
            size = 110.dp,
            modifier = Modifier.size(110.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "J.A.R.V.I.S. ONLINE",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Wie kann ich dir heute behilflich sein, Sir?",
            color = JarvisCyanLight,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Example Capabilities Cards
        val starterPrompts = listOf(
            "Starte einen 10 Minuten Timer für Tee",
            "Trage morgen um 14:00 Kalendertermin 'Team Meeting' ein",
            "Öffne die Kamera App",
            "Erinnere mich um 18:00 an Einkaufen"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            starterPrompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(JarvisNavy)
                        .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectPrompt(prompt) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = prompt,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// SESSIONS LIST BOTTOM SHEET
// ---------------------------------------------------------
@Composable
private fun ChatSessionsSheetContent(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    onSelect: (String) -> Unit,
    onNewChat: () -> Unit,
    onRename: (ChatSession) -> Unit,
    onDelete: (ChatSession) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CHAT-VERLÄUFE",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            // New Chat Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisCyan)
                    .clickable { onNewChat() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Neuer Chat",
                        tint = JarvisNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEUER CHAT",
                        color = JarvisNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sessions, key = { it.id }) { session ->
                val isSelected = session.id == currentSessionId
                val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(session.updatedAt))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) JarvisCyan.copy(alpha = 0.15f) else JarvisDarkBg)
                        .border(
                            1.dp,
                            if (isSelected) JarvisCyan else JarvisSurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(session.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title,
                                color = if (isSelected) JarvisCyanLight else Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$dateStr • ${session.messages.size} Nachrichten",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onRename(session) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Umbenennen",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(session) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Löschen",
                                    tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
