package com.example.ui.jarvis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceDark
import com.example.ui.theme.JarvisSurfaceBorder
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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
    onSendMessage: (text: String, imageBase64: String?, fileName: String?, mimeType: String?) -> Unit,
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
    onOpenSettings: () -> Unit = {},
    onUpdateVoiceCameraFrame: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var attachedImageBase64 by remember { mutableStateOf<String?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedMimeType by remember { mutableStateOf<String?>(null) }
    var showMegaMenuSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var showSessionsSheet by remember { mutableStateOf(false) }
    var renameSessionTarget by remember { mutableStateOf<ChatSession?>(null) }
    var deleteSessionTarget by remember { mutableStateOf<ChatSession?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Drawer search state
    var drawerSearchQuery by remember { mutableStateOf("") }
    var isDrawerSearchExpanded by remember { mutableStateOf(false) }

    // Media and File pickers
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    attachedImageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    attachedFileName = "Bild_" + SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date()) + ".jpg"
                    attachedMimeType = context.contentResolver.getType(it) ?: "image/jpeg"
                    Toast.makeText(context, "Bild ausgewählt: $attachedFileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Fehler beim Laden des Bildes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            attachedImageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            attachedFileName = "Kamerafoto_" + SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date()) + ".jpg"
            attachedMimeType = "image/jpeg"
            Toast.makeText(context, "Foto aufgenommen", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val mime = context.contentResolver.getType(it) ?: "application/octet-stream"
                    if (mime.startsWith("image/")) {
                        attachedImageBase64 = base64
                    }
                    attachedFileName = "Datei_" + SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
                    attachedMimeType = mime
                    Toast.makeText(context, "Datei ausgewählt: $attachedFileName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Fehler beim Lesen der Datei", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-scroll when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = JarvisDarkBg,
                modifier = Modifier.width(320.dp)
            ) {
                val navColors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = JarvisCyan.copy(alpha = 0.2f),
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = JarvisCyan,
                    unselectedIconColor = Color.White.copy(alpha = 0.85f),
                    selectedTextColor = JarvisCyanLight,
                    unselectedTextColor = Color.White.copy(alpha = 0.95f)
                )

                // Drawer Header with Search Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 40.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(JarvisCyan)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "J.A.R.V.I.S.",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row {
                        // Search icon button - toggles search bar
                        IconButton(
                            onClick = {
                                isDrawerSearchExpanded = !isDrawerSearchExpanded
                            }
                        ) {
                            Icon(
                                imageVector = if (isDrawerSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = "Suchen",
                                tint = if (isDrawerSearchExpanded) JarvisCyan else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Expandable Search Bar in Drawer
                AnimatedVisibility(
                    visible = isDrawerSearchExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = drawerSearchQuery,
                            onValueChange = { drawerSearchQuery = it },
                            placeholder = {
                                Text("Chats durchsuchen...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                            },
                            singleLine = true,
                            trailingIcon = {
                                if (drawerSearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            onSendMessage("Suche im Internet nach: $drawerSearchQuery", null, null, null)
                                            drawerSearchQuery = ""
                                            isDrawerSearchExpanded = false
                                            coroutineScope.launch { drawerState.close() }
                                        }
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = "Websuche", tint = JarvisCyan)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisSurfaceBorder,
                                cursorColor = JarvisCyan,
                                focusedContainerColor = JarvisNavy.copy(alpha = 0.6f),
                                unfocusedContainerColor = JarvisNavy.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Neuer Chat Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            onNewChat()
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Neuer Chat", tint = JarvisNavy)
                        Spacer(Modifier.width(8.dp))
                        Text("Neuer Chat", color = JarvisNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // All Feature Buttons from former Top Bar & Navigation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    // Bilder (Gallery / Photo analysis)
                    NavigationDrawerItem(
                        label = { Text("Bilder & Fotos", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            galleryPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Image, contentDescription = "Bilder", tint = JarvisCyan) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Bibliothek (Full Chat Archive & Sessions Sheet)
                    NavigationDrawerItem(
                        label = { Text("Bibliothek & Archiv", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            showSessionsSheet = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = "Bibliothek", tint = JarvisCyanLight) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Live Voice Chat + Camera Mode
                    NavigationDrawerItem(
                        label = { Text("Voice-Chat (Live Mode)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        selected = isVoiceChatMode,
                        onClick = {
                            onToggleVoiceChatMode()
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice Chat", tint = if (isVoiceChatMode) JarvisCyan else Color.White.copy(alpha = 0.8f)) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Agenda & Termine
                    NavigationDrawerItem(
                        label = { Text("Kalender & Agenda", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            onOpenAgenda()
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Agenda", tint = JarvisAmber) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Deep Think Mode Toggle
                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Deep Think", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (isThinkingEnabled) "AN" else "AUS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isThinkingEnabled) JarvisCyan else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        },
                        selected = isThinkingEnabled,
                        onClick = {
                            onToggleThinking()
                        },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "Denkmodus", tint = if (isThinkingEnabled) JarvisCyan else Color.White.copy(alpha = 0.6f)) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Sprachausgabe (TTS) Toggle
                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sprachausgabe (TTS)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (isVoiceOutputEnabled) "AN" else "AUS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVoiceOutputEnabled) JarvisCyan else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        },
                        selected = isVoiceOutputEnabled,
                        onClick = {
                            onToggleVoiceOutput()
                        },
                        icon = {
                            Icon(
                                if (isVoiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "TTS",
                                tint = if (isVoiceOutputEnabled) JarvisCyan else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Einstellungen
                    NavigationDrawerItem(
                        label = { Text("Einstellungen", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            onOpenSettings()
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Einstellungen", tint = Color.White.copy(alpha = 0.8f)) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )

                    // Schließen
                    NavigationDrawerItem(
                        label = { Text("Zurück zum Launcher", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            onCloseJarvis()
                        },
                        icon = { Icon(Icons.Default.Close, contentDescription = "Schließen", tint = Color(0xFFFF5252)) },
                        colors = navColors,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = JarvisSurfaceBorder.copy(alpha = 0.5f), thickness = 1.dp)

                // Recent Chats Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Letzte Chats",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${chatSessions.size}",
                        color = JarvisCyanLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Recent Chats List (filtered by search if user typed)
                val filteredSessions = if (drawerSearchQuery.isBlank()) {
                    chatSessions
                } else {
                    chatSessions.filter { it.title.contains(drawerSearchQuery, ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (filteredSessions.isEmpty()) {
                        item {
                            Text(
                                text = if (drawerSearchQuery.isBlank()) "Keine vergangenen Chats" else "Kein Chat gefunden",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    } else {
                        items(filteredSessions) { session ->
                            val isSelected = session.id == currentSessionId
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        session.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    onSelectChat(session.id)
                                    coroutineScope.launch { drawerState.close() }
                                },
                                icon = {
                                    Icon(
                                        Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = navColors,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    ) {
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
                // Sleek minimal floating top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hamburger Menu Button (opens side drawer)
                    IconButton(
                        onClick = { coroutineScope.launch { drawerState.open() } },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menü öffnen",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Session title / Status chip
                    Surface(
                        color = JarvisNavy.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showSessionsSheet = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isThinking) JarvisAmber else JarvisCyan)
                            )
                            Spacer(Modifier.width(8.dp))
                            val currentTitle = chatSessions.firstOrNull { it.id == currentSessionId }?.title ?: "J.A.R.V.I.S."
                            Text(
                                text = currentTitle,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 160.dp)
                            )
                        }
                    }

                    // Quick right actions: Close back to launcher
                    IconButton(
                        onClick = onCloseJarvis,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

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
                                onSendMessage(prompt, null, null, null)
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
                            item { Spacer(modifier = Modifier.height(4.dp)) }
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
                                        onSendMessage(actionText, null, null, null)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
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
                        onSendMessage(prompt, null, null, null)
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // 6. BOTTOM INPUT & VOICE CONTROLS
                JarvisInputBar(
                    inputText = inputText,
                    onInputChanged = { inputText = it },
                    onSend = {
                        if ((inputText.isNotBlank() || attachedImageBase64 != null) && !isThinking) {
                            onSendMessage(
                                inputText,
                                attachedImageBase64,
                                attachedFileName,
                                attachedMimeType
                            )
                            inputText = ""
                            attachedImageBase64 = null
                            attachedFileName = null
                            attachedMimeType = null
                        }
                    },
                    isThinking = isThinking,
                    isListening = isListening,
                    onToggleVoice = {
                        if (isListening) onStopVoice() else onStartVoice()
                    },
                    onOpenMegaMenu = { showMegaMenuSheet = true },
                    attachedFileName = attachedFileName,
                    onRemoveAttachment = {
                        attachedImageBase64 = null
                        attachedFileName = null
                        attachedMimeType = null
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // CHATGPT MEGA MENU BOTTOM SHEET
            if (showMegaMenuSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showMegaMenuSheet = false },
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
                    JarvisMegaMenuContent(
                        isThinkingEnabled = isThinkingEnabled,
                        onOptionSelected = { action ->
                            showMegaMenuSheet = false
                            when (action) {
                                "camera" -> cameraCaptureLauncher.launch(null)
                                "gallery" -> galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                                "file" -> filePickerLauncher.launch("*/*")
                                "voice" -> onToggleVoiceChatMode()
                                "think" -> onToggleThinking()
                                "agenda" -> onOpenAgenda()
                                "search" -> {
                                    if (inputText.isBlank()) inputText = "Suche im Internet nach: "
                                }
                            }
                        }
                    )
                }
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
                    onStopSpeaking = onStopSpeaking,
                    onUpdateCameraFrame = onUpdateVoiceCameraFrame
                )
            }
        }
    }
}

// =========================================================================
// SUB-COMPONENTS
// =========================================================================

@Composable
fun ActiveTimerHudBanner(
    timer: ActiveTimer,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = JarvisNavy.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = "Timer",
                    tint = JarvisCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    val minutes = timer.remainingSeconds / 60
                    val seconds = timer.remainingSeconds % 60
                    Text(
                        text = "${timer.label.ifBlank { "Timer" }}: " + String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (timer.isRunning) "Countdown läuft" else "Pausiert",
                        color = if (timer.isRunning) JarvisCyanLight else JarvisAmber,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (timer.isRunning) onPause() else onResume() }) {
                    Icon(
                        imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timer.isRunning) "Pause" else "Fortsetzen",
                        tint = JarvisCyan
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Abbrechen",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isThinkingEnabled: Boolean,
    isSpeaking: Boolean,
    onCopyText: (String) -> Unit,
    onSpeakText: (String) -> Unit,
    onQuickAction: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    var isThoughtExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Badge
        Text(
            text = if (isUser) "Du" else "J.A.R.V.I.S.",
            color = if (isUser) Color.White.copy(alpha = 0.6f) else JarvisCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // Thought Bubble (Deep Think) - Only shown when model actually provides reasoning/thought text
        if (!isUser && isThinkingEnabled && !message.thoughtText.isNullOrBlank()) {
            Surface(
                color = JarvisSurfaceDark.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 6.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isThoughtExpanded = !isThoughtExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = JarvisCyanLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Gedankengang (Deep Think)",
                                color = JarvisCyanLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (isThoughtExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = JarvisCyanLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (isThoughtExpanded) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = message.thoughtText,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Message Content Box
        Surface(
            color = if (isUser) JarvisCyan.copy(alpha = 0.15f) else JarvisNavy.copy(alpha = 0.85f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) JarvisCyan.copy(alpha = 0.4f) else JarvisSurfaceBorder
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Attached Image preview
                if (message.attachedImageBase64 != null) {
                    val bitmap = remember(message.attachedImageBase64) {
                        try {
                            val decoded = Base64.decode(message.attachedImageBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Angehängtes Bild",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Attached File preview
                if (message.attachedFileName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JarvisSurfaceDark, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = message.attachedFileName,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Message Text
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                if (isUser) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isSending) {
                            CircularProgressIndicator(
                                color = JarvisCyan,
                                strokeWidth = 1.dp,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Wird gesendet...",
                                color = JarvisCyanLight,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = "Gesendet ✓",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Action Affordance Badge
                if (message.actionType != ActionType.NONE && !message.actionPayload.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    ActionAffordanceBadge(
                        type = message.actionType,
                        payload = message.actionPayload,
                        onQuickAction = onQuickAction
                    )
                }

                // Streaming indicator
                if (message.isStreaming) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = JarvisCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "J.A.R.V.I.S. generiert...",
                            color = JarvisCyanLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Bottom utilities for JARVIS response (TTS & Copy)
                if (!isUser && !message.isStreaming) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onSpeakText(message.text) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Vorlesen",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onCopyText(message.text) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Kopieren",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionAffordanceBadge(
    type: ActionType,
    payload: String,
    onQuickAction: (String) -> Unit
) {
    val (label, icon, color) = when (type) {
        ActionType.APP_LAUNCH -> Triple("App starten: $payload", Icons.Default.RocketLaunch, JarvisCyan)
        ActionType.CALENDAR -> Triple("Kalendereintrag erstellt: $payload", Icons.Default.CalendarMonth, JarvisAmber)
        ActionType.REMINDER -> Triple("Erinnerung aktiviert: $payload", Icons.Default.Check, JarvisCyanLight)
        ActionType.TIMER -> Triple("Timer eingerichtet: $payload", Icons.Default.HourglassBottom, JarvisCyan)
        ActionType.SEARCH_RESULT -> Triple("Web-Recherche: $payload", Icons.Default.Search, JarvisCyan)
        ActionType.ERROR -> Triple("Systemmeldung: $payload", Icons.Default.Close, Color(0xFFFF5252))
        ActionType.NONE -> Triple(payload, Icons.Default.Check, JarvisCyan)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onQuickAction(payload) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SpeechListeningBanner(
    isListening: Boolean,
    statusMessage: String?,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = JarvisNavy.copy(alpha = 0.95f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Sprachstatus",
                    tint = JarvisCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isListening) "Sprich jetzt einen Befehl..." else "Mikrofon inaktiv",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!statusMessage.isNullOrBlank()) {
                        Text(
                            text = statusMessage,
                            color = JarvisCyanLight,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stoppen",
                    tint = Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
fun QuickPromptsBar(
    onPromptClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts = remember {
        listOf(
            "🚀 Öffne WhatsApp",
            "⏱️ 5 Min Timer",
            "🔍 Wetter morgen",
            "📅 Neuer Termin",
            "⚡ Systemdiagnose",
            "💡 Was kannst du?"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { prompt ->
            Surface(
                color = JarvisSurfaceDark.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                modifier = Modifier.clickable {
                    onPromptClicked(prompt.substringAfter(" "))
                }
            ) {
                Text(
                    text = prompt,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun JarvisInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isThinking: Boolean,
    isListening: Boolean,
    onToggleVoice: () -> Unit,
    onOpenMegaMenu: () -> Unit,
    attachedFileName: String?,
    onRemoveAttachment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Attachment Chip
        if (attachedFileName != null) {
            Surface(
                color = JarvisSurfaceDark,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f)),
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = attachedFileName,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Entfernen",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemoveAttachment() }
                    )
                }
            }
        }

        // Main input bar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mega Menu Button (+)
            Surface(
                color = JarvisNavy.copy(alpha = 0.8f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onOpenMegaMenu() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Mega Menü (+)",
                        tint = JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Arc Reactor Mic Button
            ArcReactorButton(
                onClick = onToggleVoice,
                isPulsingShockwave = isListening,
                size = 44.dp,
                modifier = Modifier.size(44.dp)
            )

            // Input TextField
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Höre zu..." else "Befehl / Frage eingeben...",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 14.sp
                    )
                },
                trailingIcon = {
                    val canSend = (inputText.isNotBlank() || attachedFileName != null) && !isThinking
                    IconButton(
                        onClick = onSend,
                        enabled = canSend
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Senden",
                            tint = if (canSend) JarvisCyan else Color.White.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisSurfaceBorder,
                    cursorColor = JarvisCyan,
                    focusedContainerColor = JarvisNavy.copy(alpha = 0.6f),
                    unfocusedContainerColor = JarvisNavy.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            )
        }
    }
}

@Composable
fun JarvisMegaMenuContent(
    isThinkingEnabled: Boolean,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "ERWEITERTE FUNKTIONEN",
            color = JarvisCyanLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val options = listOf(
            Triple("camera", "📷 Foto aufnehmen & analysieren", "Öffnet die Kamera für Live-Bildanalyse"),
            Triple("gallery", "🖼️ Bild aus Galerie wählen", "Lade ein Foto oder Bild aus der Galerie hoch"),
            Triple("file", "📁 Datei / PDF anhängen", "Dokument oder Datei für Kontext anhängen"),
            Triple("voice", "🎙️ Sprachchat + Kamera-Modus", "Live Audio- und visueller Gesprächsmodus"),
            Triple("think", "💡 Deep Think (Modell-Gedanken)", if (isThinkingEnabled) "Aktiviert (Gedankenschritte sichtbar)" else "Deaktiviert (Nur finale Antwort)"),
            Triple("search", "🌐 Internet-Suche", "Recherche im Web via Tavily"),
            Triple("agenda", "📅 Kalender & Termine", "Tagesübersicht und geplante Events")
        )

        options.forEach { (action, title, desc) ->
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(action) }
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun JarvisEmptyState(
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = JarvisCyan.copy(alpha = 0.1f),
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(2.dp, JarvisCyan.copy(alpha = 0.4f)),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "J.A.R.V.I.S. ONLINE",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Wie kann ich dir heute behilflich sein, Sir?",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        val samplePrompts = listOf(
            "Starte einen 10 Minuten Timer für Tee",
            "Trage morgen um 14:00 Kalendertermin 'Team Meeting' ein",
            "Öffne die Kamera App",
            "Erinnere mich um 18:00 an Einkaufen"
        )

        samplePrompts.forEach { prompt ->
            Surface(
                color = JarvisNavy.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPrompt(prompt) }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = prompt,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ChatSessionsSheetContent(
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHAT-BIBLIOTHEK",
                color = JarvisCyanLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Button(
                onClick = onNewChat,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                shape = RoundedCornerShape(50),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = JarvisNavy, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Neuer Chat", color = JarvisNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keine gespeicherten Chats vorhanden.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(sessions) { session ->
                    val isSelected = session.id == currentSessionId
                    Surface(
                        color = if (isSelected) JarvisCyan.copy(alpha = 0.15f) else JarvisSurfaceDark,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) JarvisCyan else JarvisSurfaceBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(session.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val dateFormat = SimpleDateFormat("dd. MMM yyyy, HH:mm", Locale.getDefault())
                                Text(
                                    text = dateFormat.format(Date(session.updatedAt)),
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 11.sp
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onRename(session) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Umbenennen",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDelete(session) },
                                    modifier = Modifier.size(32.dp)
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
        Spacer(Modifier.height(16.dp))
    }
}
