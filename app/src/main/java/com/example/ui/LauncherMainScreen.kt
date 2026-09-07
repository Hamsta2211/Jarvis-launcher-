package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.ui.components.AgendaDialog
import com.example.ui.components.AlarmRingingDialog
import com.example.ui.components.AppDrawerSheet
import com.example.ui.components.AppPickerDialog
import com.example.ui.components.BottomMusicWidget
import com.example.ui.components.FolderDialog
import com.example.ui.components.HudClockWidget
import com.example.ui.components.OrbitAppRing
import com.example.ui.components.WeatherBatteryWidget
import com.example.ui.components.WeekdayHeader
import com.example.ui.jarvis.JarvisScreen
import com.example.ui.settings.SettingsDialog
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy

@Composable
fun LauncherMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orbitApps by viewModel.orbitApps.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isAppDrawerOpen by viewModel.isAppDrawerOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedAlphabet by viewModel.selectedAlphabet.collectAsState()
    val isJarvisOpen by viewModel.isJarvisOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val appPickerSlotIndex by viewModel.appPickerSlotIndex.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val isReactorPulse by viewModel.isReactorPulse.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val weatherCity by viewModel.weatherCity.collectAsState()
    val weatherInfo by viewModel.weatherInfo.collectAsState()
    val isRefreshingWeather by viewModel.isRefreshingWeather.collectAsState()

    // Agenda & Alarm states
    val isAgendaOpen by viewModel.isAgendaOpen.collectAsState()
    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val activeTimer by viewModel.activeTimer.collectAsState()

    // Voice Chat & TTS states
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isVoiceOutputEnabled by viewModel.isVoiceOutputEnabled.collectAsState()
    val isVoiceChatMode by viewModel.isVoiceChatMode.collectAsState()

    // Request Location permission for real GPS Weather & City
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            viewModel.refreshWeather()
        }
    }

    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Jarvis state
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val isThinkingEnabled by viewModel.isThinkingEnabled.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val speechStatusMessage by viewModel.speechStatusMessage.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisDarkBg)
    ) {
        // Sci-Fi background ambient glow & grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.52f)
            // Center subtle radial glow behind arc reactor
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(JarvisCyan.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.6f
                ),
                center = center,
                radius = size.width * 0.6f
            )
        }

        val density = LocalDensity.current

        // Smooth transition progress: 0f (Home Screen) -> 1f (Jarvis Assistant)
        val jarvisTransitionProgress by animateFloatAsState(
            targetValue = if (isJarvisOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 750, easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)),
            label = "jarvis_transition"
        )

        val homeAlpha = (1f - jarvisTransitionProgress).coerceIn(0f, 1f)
        val topDisperseY = with(density) { (-120.dp).toPx() } * jarvisTransitionProgress
        val botDisperseY = with(density) { (120.dp).toPx() } * jarvisTransitionProgress

        // Launcher Main Content Column (Flies outward smoothly when Jarvis opens)
        if (homeAlpha > 0.005f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1 & 2. Top Header & Clock Row (Slide Upwards & Fade Out)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = topDisperseY
                            alpha = homeAlpha
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WeekdayHeader(
                        currentDate = currentDate,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        HudClockWidget(currentDate = currentDate)
                        WeatherBatteryWidget(
                            weatherCity = weatherInfo.cityName,
                            temperatureCelsius = weatherInfo.temperatureCelsius,
                            weatherCode = weatherInfo.weatherCode,
                            conditionDescription = weatherInfo.conditionDescription,
                            isGpsLocation = weatherInfo.isGpsLocation,
                            isRefreshing = isRefreshingWeather,
                            batteryLevel = batteryLevel,
                            onWeatherClick = {
                                if (!viewModel.hasLocationPermission()) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    viewModel.refreshWeather()
                                }
                            }
                        )
                    }
                }

                // 3. Middle Section: Floating Orbit App Ring & Central Arc Reactor (Fly Outward)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    OrbitAppRing(
                        apps = orbitApps,
                        appLauncherHelper = viewModel.appLauncherHelper,
                        onAppClick = { app -> viewModel.launchAppItem(app) },
                        onAppLongClick = { slotIndex, _ -> viewModel.requestAppPickerForSlot(slotIndex) },
                        onEmptySlotClick = { slotIndex -> viewModel.requestAppPickerForSlot(slotIndex) },
                        onJarvisClick = { viewModel.onArcReactorClicked() },
                        onSettingsClick = { viewModel.openSettings() },
                        onSearchClick = { viewModel.openAppDrawer() },
                        onFolderClick = { folder -> viewModel.openFolder(folder) },
                        isPulsing = isReactorPulse,
                        transitionProgress = jarvisTransitionProgress
                    )
                }

                // 4. Bottom Section: Music Widget & Swipe-Up Indicator (Slide Downward & Fade Out)
                BottomMusicWidget(
                    isPlaying = isPlayingMusic,
                    trackName = currentTrack,
                    onPlayPause = { viewModel.togglePlayPauseMusic() },
                    onNext = { viewModel.nextMusicTrack() },
                    onOpenDrawer = { viewModel.openAppDrawer() },
                    onOpenMusicPlayer = { viewModel.openExternalMusicPlayer() },
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -20f && !isAppDrawerOpen && !isJarvisOpen) {
                                    viewModel.openAppDrawer()
                                }
                            }
                        }
                        .graphicsLayer {
                            translationY = botDisperseY
                            alpha = homeAlpha
                        }
                )
            }
        }

        // App Drawer Bottom Sheet
        AppDrawerSheet(
            isOpen = isAppDrawerOpen,
            onDismiss = { viewModel.closeAppDrawer() },
            installedApps = installedApps,
            searchQuery = searchQuery,
            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
            selectedAlphabet = selectedAlphabet,
            onAlphabetSelected = { viewModel.setAlphabetFilter(it) },
            onAppClick = { app -> viewModel.launchAppItem(app) },
            appLauncherHelper = viewModel.appLauncherHelper
        )

        // Settings Dialog
        SettingsDialog(
            isOpen = isSettingsOpen,
            onDismiss = { viewModel.closeSettings() },
            settingsManager = viewModel.settingsManager,
            installedApps = installedApps,
            currentOrbitSlotCount = orbitApps.size,
            onOrbitSlotCountChanged = { count -> viewModel.setOrbitSlotCount(count) },
            onSave = { geminiKey, fallbackKeys, tavilyKey, city, darkMode ->
                viewModel.saveSettings(geminiKey, fallbackKeys, tavilyKey, city, darkMode)
            }
        )

        // Folder Dialog
        if (selectedFolder != null) {
            FolderDialog(
                folder = selectedFolder!!,
                onDismiss = { viewModel.closeFolder() },
                onAppClick = { app -> viewModel.launchAppItem(app) },
                appLauncherHelper = viewModel.appLauncherHelper
            )
        }

        // App Picker Dialog
        if (appPickerSlotIndex != null) {
            AppPickerDialog(
                slotIndex = appPickerSlotIndex!!,
                installedApps = installedApps,
                onDismiss = { viewModel.closeAppPicker() },
                onAppSelected = { selectedApp ->
                    viewModel.assignAppToOrbitSlot(appPickerSlotIndex!!, selectedApp)
                },
                onCreateFolder = { folderName, folderApps ->
                    viewModel.createFolderInSlot(appPickerSlotIndex!!, folderName, folderApps)
                },
                appLauncherHelper = viewModel.appLauncherHelper
            )
        }

        // Holographic activation shockwave pulse while transitioning
        if (jarvisTransitionProgress in 0.02f..0.98f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.52f)
                val maxRadius = size.width * 0.85f
                val ringRadius = maxRadius * jarvisTransitionProgress
                val ringAlpha = (1f - kotlin.math.abs(jarvisTransitionProgress - 0.5f) * 2f).coerceIn(0f, 0.7f)
                drawCircle(
                    color = JarvisCyan.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(JarvisCyan.copy(alpha = ringAlpha * 0.25f), Color.Transparent),
                        center = center,
                        radius = ringRadius
                    ),
                    center = center,
                    radius = ringRadius
                )
            }
        }

        // Fullscreen Animated Jarvis Assistant Window (Glides In smoothly from Arc Reactor)
        if (jarvisTransitionProgress > 0.005f) {
            val jarvisAlpha = (jarvisTransitionProgress * 1.15f).coerceIn(0f, 1f)
            val jarvisScale = 0.86f + 0.14f * jarvisTransitionProgress
            val jarvisOffsetY = with(density) { 60.dp.toPx() } * (1f - jarvisTransitionProgress)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = jarvisAlpha
                        scaleX = jarvisScale
                        scaleY = jarvisScale
                        translationY = jarvisOffsetY
                    }
            ) {
                JarvisScreen(
                    chatMessages = chatMessages,
                    chatSessions = chatSessions,
                    currentSessionId = currentSessionId,
                    isThinkingEnabled = isThinkingEnabled,
                    isThinking = isThinking,
                    isListening = isListening,
                    speechStatusMessage = speechStatusMessage,
                    activeTimer = activeTimer,
                    isSpeaking = isSpeaking,
                    isVoiceOutputEnabled = isVoiceOutputEnabled,
                    isVoiceChatMode = isVoiceChatMode,
                    onSendMessage = { text -> viewModel.sendUserMessage(text) },
                    onStartVoice = { viewModel.startVoiceRecognition() },
                    onStopVoice = { viewModel.stopVoiceRecognition() },
                    onToggleThinking = { viewModel.toggleThinkingEnabled() },
                    onToggleVoiceOutput = { viewModel.toggleVoiceOutput() },
                    onToggleVoiceChatMode = { viewModel.toggleVoiceChatMode() },
                    onStopSpeaking = { viewModel.stopSpeaking() },
                    onSpeakMessage = { text -> viewModel.speakText(text) },
                    onOpenAgenda = { viewModel.openAgenda() },
                    onNewChat = { viewModel.createNewChat() },
                    onSelectChat = { id -> viewModel.selectChat(id) },
                    onRenameChat = { id, title -> viewModel.renameChat(id, title) },
                    onDeleteChat = { id -> viewModel.deleteChat(id) },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onResumeTimer = { viewModel.resumeTimer() },
                    onCancelTimer = { viewModel.cancelTimer() },
                    onCloseJarvis = { viewModel.closeJarvis() }
                )
            }
        }

        // Agenda Dialog (Termine, Kalender & Erinnerungen)
        AgendaDialog(
            isOpen = isAgendaOpen,
            onDismiss = { viewModel.closeAgenda() },
            calendarEvents = calendarEvents,
            reminders = reminders,
            activeTimer = activeTimer,
            onAddCalendarEvent = { event -> viewModel.addCalendarEvent(event) },
            onDeleteCalendarEvent = { id -> viewModel.deleteCalendarEvent(id) },
            onAddReminder = { reminder -> viewModel.addReminder(reminder) },
            onToggleReminder = { id -> viewModel.toggleReminder(id) },
            onDeleteReminder = { id -> viewModel.deleteReminder(id) },
            onStopTimer = { viewModel.cancelTimer() }
        )

        // Alarm Ringing Dialog (Timer abgelaufen / Wecker klingelt)
        if (isAlarmRinging != null) {
            AlarmRingingDialog(
                alarmLabel = isAlarmRinging!!,
                onStopAlarm = { viewModel.stopAlarm() }
            )
        }
    }
}
