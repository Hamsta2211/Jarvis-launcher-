package com.example.ui

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActionType
import com.example.data.model.ActiveTimer
import com.example.data.model.AppItem
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.data.model.ReminderItem
import com.example.data.service.AppLauncherHelper
import com.example.data.service.GeminiService
import com.example.data.service.JarvisNotificationManager
import com.example.data.service.LocationWeatherService
import com.example.data.service.MicrosoftTtsService
import com.example.data.service.MusicPlaybackService
import com.example.data.service.SettingsManager
import com.example.data.service.SpeechRecognitionHelper
import com.example.data.service.TavilySearchService
import com.example.data.service.WeatherInfo
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val settingsManager = SettingsManager(application)
    val appLauncherHelper = AppLauncherHelper(application)
    val locationWeatherService = LocationWeatherService(application)
    val musicPlaybackService = MusicPlaybackService(application)
    val microsoftTtsService = MicrosoftTtsService(application)
    val jarvisNotificationManager = JarvisNotificationManager.getInstance(application)
    private val geminiService = GeminiService(settingsManager)
    private val tavilySearchService = TavilySearchService()

    // Orbit Apps & Installed Apps
    private val _orbitApps = MutableStateFlow<List<AppItem>>(emptyList())
    val orbitApps: StateFlow<List<AppItem>> = _orbitApps.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    // App Drawer State
    private val _isAppDrawerOpen = MutableStateFlow(false)
    val isAppDrawerOpen: StateFlow<Boolean> = _isAppDrawerOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAlphabet = MutableStateFlow<Char?>(null)
    val selectedAlphabet: StateFlow<Char?> = _selectedAlphabet.asStateFlow()

    // Screen Overlays
    private val _isJarvisOpen = MutableStateFlow(false)
    val isJarvisOpen: StateFlow<Boolean> = _isJarvisOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isAgendaOpen = MutableStateFlow(false)
    val isAgendaOpen: StateFlow<Boolean> = _isAgendaOpen.asStateFlow()

    private val _isVoiceChatMode = MutableStateFlow(false)
    val isVoiceChatMode: StateFlow<Boolean> = _isVoiceChatMode.asStateFlow()

    private val _selectedFolder = MutableStateFlow<AppItem?>(null)
    val selectedFolder: StateFlow<AppItem?> = _selectedFolder.asStateFlow()

    private val _appPickerSlotIndex = MutableStateFlow<Int?>(null)
    val appPickerSlotIndex: StateFlow<Int?> = _appPickerSlotIndex.asStateFlow()

    // Battery & Date/Time
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _currentDate = MutableStateFlow(Date())
    val currentDate: StateFlow<Date> = _currentDate.asStateFlow()

    // Chat / Jarvis State
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isThinkingEnabled = MutableStateFlow(settingsManager.isThinkingEnabled)
    val isThinkingEnabled: StateFlow<Boolean> = _isThinkingEnabled.asStateFlow()

    private val _isVoiceOutputEnabled = MutableStateFlow(settingsManager.isVoiceOutputEnabled)
    val isVoiceOutputEnabled: StateFlow<Boolean> = _isVoiceOutputEnabled.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = microsoftTtsService.isSpeaking
    val isAlarmRinging: StateFlow<String?> = jarvisNotificationManager.isAlarmRinging

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechStatusMessage = MutableStateFlow<String?>(null)
    val speechStatusMessage: StateFlow<String?> = _speechStatusMessage.asStateFlow()

    // Calendar & Reminders
    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    private val _reminders = MutableStateFlow<List<ReminderItem>>(emptyList())
    val reminders: StateFlow<List<ReminderItem>> = _reminders.asStateFlow()

    // Timers
    private val _activeTimer = MutableStateFlow<ActiveTimer?>(null)
    val activeTimer: StateFlow<ActiveTimer?> = _activeTimer.asStateFlow()
    private var countDownTimer: CountDownTimer? = null

    // Arc Reactor Animation State
    private val _isReactorPulse = MutableStateFlow(false)
    val isReactorPulse: StateFlow<Boolean> = _isReactorPulse.asStateFlow()

    // Settings State
    private val _isDarkMode = MutableStateFlow(settingsManager.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _weatherCity = MutableStateFlow(settingsManager.weatherCity)
    val weatherCity: StateFlow<String> = _weatherCity.asStateFlow()

    private val _weatherInfo = MutableStateFlow(WeatherInfo(cityName = settingsManager.weatherCity))
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private val _isRefreshingWeather = MutableStateFlow(false)
    val isRefreshingWeather: StateFlow<Boolean> = _isRefreshingWeather.asStateFlow()

    // Music Player State (Visualizer / Controls)
    private val _isPlayingMusic = MutableStateFlow(false)
    val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

    private val _currentTrack = MutableStateFlow(musicPlaybackService.getCurrentTrackName())
    val currentTrack: StateFlow<String> = _currentTrack.asStateFlow()

    // Speech Recognizer
    private var speechHelper: SpeechRecognitionHelper? = null

    init {
        loadData()
        startClockUpdates()
        startBatteryMonitoring()
        startWeatherMonitoring()

        viewModelScope.launch {
            musicPlaybackService.currentTrackDisplay.collect { track ->
                _currentTrack.value = track
            }
        }
        viewModelScope.launch {
            musicPlaybackService.isPlaying.collect { playing ->
                _isPlayingMusic.value = playing
            }
        }
    }

    private fun createDefaultJarvisGreeting(): ChatMessage {
        return ChatMessage(
            sender = MessageSender.JARVIS,
            text = "Guten Tag, Sir. J.A.R.V.I.S. ist online und alle Systeme operieren mit maximaler Effizienz. Wie kann ich behilflich sein?",
            actionType = ActionType.NONE
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            _orbitApps.value = settingsManager.getOrbitApps()
            _installedApps.value = appLauncherHelper.getAllInstalledApps()
            _calendarEvents.value = settingsManager.getCalendarEvents()
            _reminders.value = settingsManager.getReminders()

            val savedSessions = settingsManager.getChatSessions()
            if (savedSessions.isEmpty()) {
                val defaultSession = ChatSession(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "System Diagnose",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    messages = listOf(createDefaultJarvisGreeting())
                )
                _chatSessions.value = listOf(defaultSession)
                _currentSessionId.value = defaultSession.id
                _chatMessages.value = defaultSession.messages
                settingsManager.saveChatSessions(listOf(defaultSession))
                settingsManager.saveCurrentChatSessionId(defaultSession.id)
            } else {
                _chatSessions.value = savedSessions
                val lastId = settingsManager.getCurrentChatSessionId()
                val current = savedSessions.find { it.id == lastId } ?: savedSessions.first()
                _currentSessionId.value = current.id
                _chatMessages.value = current.messages
            }
        }
    }

    private fun startClockUpdates() {
        viewModelScope.launch {
            while (true) {
                _currentDate.value = Date()
                delay(1000)
            }
        }
    }

    private fun startWeatherMonitoring() {
        viewModelScope.launch {
            while (true) {
                refreshWeather()
                delay(15 * 60 * 1000) // update every 15 minutes
            }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _isRefreshingWeather.value = true
            try {
                val info = locationWeatherService.fetchWeather(settingsManager.weatherCity)
                _weatherInfo.value = info
                _weatherCity.value = info.cityName
            } catch (e: Exception) {
                Log.w("MainViewModel", "refreshWeather failed: ${e.message}")
            } finally {
                _isRefreshingWeather.value = false
            }
        }
    }

    fun hasLocationPermission(): Boolean = locationWeatherService.hasLocationPermission()

    fun createNewChat(initialTitle: String = "Neuer Chat") {
        val newSession = ChatSession(
            id = java.util.UUID.randomUUID().toString(),
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            messages = listOf(createDefaultJarvisGreeting())
        )
        val updatedList = listOf(newSession) + _chatSessions.value
        _chatSessions.value = updatedList
        _currentSessionId.value = newSession.id
        _chatMessages.value = newSession.messages
        settingsManager.saveChatSessions(updatedList)
        settingsManager.saveCurrentChatSessionId(newSession.id)
    }

    fun selectChat(sessionId: String) {
        val session = _chatSessions.value.find { it.id == sessionId } ?: return
        _currentSessionId.value = sessionId
        _chatMessages.value = session.messages
        settingsManager.saveCurrentChatSessionId(sessionId)
    }

    fun renameChat(sessionId: String, newTitle: String) {
        val clean = newTitle.trim().ifEmpty { "Chat" }
        val updated = _chatSessions.value.map {
            if (it.id == sessionId) it.copy(title = clean, updatedAt = System.currentTimeMillis()) else it
        }
        _chatSessions.value = updated
        settingsManager.saveChatSessions(updated)
    }

    fun deleteChat(sessionId: String) {
        val currentList = _chatSessions.value
        val updated = currentList.filter { it.id != sessionId }
        if (updated.isEmpty()) {
            val freshSession = ChatSession(
                id = java.util.UUID.randomUUID().toString(),
                title = "System Diagnose",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                messages = listOf(createDefaultJarvisGreeting())
            )
            _chatSessions.value = listOf(freshSession)
            _currentSessionId.value = freshSession.id
            _chatMessages.value = freshSession.messages
            settingsManager.saveChatSessions(listOf(freshSession))
            settingsManager.saveCurrentChatSessionId(freshSession.id)
        } else {
            _chatSessions.value = updated
            settingsManager.saveChatSessions(updated)
            if (_currentSessionId.value == sessionId) {
                val next = updated.first()
                _currentSessionId.value = next.id
                _chatMessages.value = next.messages
                settingsManager.saveCurrentChatSessionId(next.id)
            }
        }
    }

    fun toggleThinkingEnabled() {
        val newVal = !_isThinkingEnabled.value
        _isThinkingEnabled.value = newVal
        settingsManager.isThinkingEnabled = newVal
    }

    private fun persistCurrentChatMessages(messages: List<ChatMessage>) {
        val activeId = _currentSessionId.value ?: return
        val currentList = _chatSessions.value
        val updated = currentList.map { session ->
            if (session.id == activeId) {
                var title = session.title
                if (title == "Neuer Chat") {
                    val firstUserMsg = messages.firstOrNull { it.sender == MessageSender.USER }?.text
                    if (!firstUserMsg.isNullOrBlank()) {
                        title = if (firstUserMsg.length > 25) firstUserMsg.take(25) + "..." else firstUserMsg
                    }
                }
                session.copy(title = title, messages = messages, updatedAt = System.currentTimeMillis())
            } else session
        }
        _chatSessions.value = updated
        settingsManager.saveChatSessions(updated)
    }

    fun onArcReactorClicked() {
        _isJarvisOpen.value = true
    }

    fun openJarvis() {
        _isJarvisOpen.value = true
    }

    fun closeJarvis() {
        _isJarvisOpen.value = false
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun openAppDrawer() {
        _isAppDrawerOpen.value = true
    }

    fun closeAppDrawer() {
        _isAppDrawerOpen.value = false
        _searchQuery.value = ""
        _selectedAlphabet.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAlphabetFilter(char: Char?) {
        _selectedAlphabet.value = if (_selectedAlphabet.value == char) null else char
    }

    fun openFolder(folder: AppItem) {
        _selectedFolder.value = folder
    }

    fun closeFolder() {
        _selectedFolder.value = null
    }

    fun closeAllOverlays() {
        _isJarvisOpen.value = false
        _isAppDrawerOpen.value = false
        _isSettingsOpen.value = false
        _selectedFolder.value = null
        _appPickerSlotIndex.value = null
    }

    fun requestAppPickerForSlot(slotIndex: Int) {
        _appPickerSlotIndex.value = slotIndex
    }

    fun closeAppPicker() {
        _appPickerSlotIndex.value = null
    }

    fun assignAppToOrbitSlot(slotIndex: Int, app: AppItem) {
        val current = _orbitApps.value.toMutableList()
        if (slotIndex in current.indices) {
            current[slotIndex] = app
        } else {
            current.add(app)
        }
        _orbitApps.value = current
        settingsManager.saveOrbitApps(current)
        closeAppPicker()
    }

    fun setOrbitSlotCount(count: Int) {
        val target = count.coerceIn(4, 16)
        val current = _orbitApps.value.toMutableList()
        if (current.size < target) {
            while (current.size < target) {
                current.add(AppItem(label = "Hinzufügen", iconKey = "empty"))
            }
        } else if (current.size > target) {
            while (current.size > target) {
                current.removeAt(current.size - 1)
            }
        }
        _orbitApps.value = current
        settingsManager.saveOrbitApps(current)
    }

    fun addOrbitSlot() {
        setOrbitSlotCount(_orbitApps.value.size + 1)
    }

    fun removeOrbitSlot(slotIndex: Int) {
        val current = _orbitApps.value.toMutableList()
        if (slotIndex in current.indices && current.size > 4) {
            current.removeAt(slotIndex)
            _orbitApps.value = current
            settingsManager.saveOrbitApps(current)
        }
    }

    fun createFolderInSlot(slotIndex: Int, folderName: String, initialApps: List<AppItem>) {
        val folder = AppItem(
            label = folderName,
            isFolder = true,
            folderName = folderName,
            folderItems = initialApps
        )
        assignAppToOrbitSlot(slotIndex, folder)
    }

    fun launchAppItem(item: AppItem) {
        if (item.iconKey == "flashlight") {
            appLauncherHelper.toggleFlashlight()
            return
        }
        if (item.packageName.isNotBlank()) {
            val launched = appLauncherHelper.launchApp(item.packageName)
            if (!launched && item.label.isNotBlank()) {
                appLauncherHelper.launchAppByName(item.label)
            }
        } else if (item.label.isNotBlank()) {
            appLauncherHelper.launchAppByName(item.label)
        }
    }

    private fun startBatteryMonitoring() {
        viewModelScope.launch {
            while (true) {
                _batteryLevel.value = appLauncherHelper.getBatteryLevel()
                delay(15000)
            }
        }
    }

    // Music Player Controls
    fun togglePlayPauseMusic() {
        musicPlaybackService.togglePlayPause { playing, track ->
            _isPlayingMusic.value = playing
            _currentTrack.value = track
        }
    }

    fun nextMusicTrack() {
        musicPlaybackService.nextTrack { playing, track ->
            _isPlayingMusic.value = playing
            _currentTrack.value = track
        }
    }

    fun openExternalMusicPlayer() {
        val defaultPkg = settingsManager.defaultMusicAppPackage
        val defaultName = settingsManager.defaultMusicAppName

        if (defaultPkg.isNotBlank()) {
            val launched = appLauncherHelper.launchApp(defaultPkg)
            if (launched) return
        }
        if (defaultName.isNotBlank() && defaultName != "Automatisch") {
            val launched = appLauncherHelper.launchAppByName(defaultName).first
            if (launched) return
        }

        val launched = appLauncherHelper.launchAppByName("Spotify").first ||
                appLauncherHelper.launchAppByName("YouTube Music").first ||
                appLauncherHelper.launchAppByName("Apple Music").first ||
                appLauncherHelper.launchAppByName("Musik").first ||
                appLauncherHelper.launchAppByName("Music").first ||
                appLauncherHelper.launchApp("com.spotify.music") ||
                appLauncherHelper.launchApp("com.google.android.apps.youtube.music") ||
                appLauncherHelper.launchApp("com.apple.android.music") ||
                appLauncherHelper.launchApp("deezer.android.app") ||
                appLauncherHelper.launchApp("com.amazon.mp3") ||
                appLauncherHelper.launchApp("com.soundcloud.android") ||
                appLauncherHelper.launchApp("com.sec.android.app.music") ||
                appLauncherHelper.launchApp("com.maxmpz.audioplayer") ||
                appLauncherHelper.launchApp("org.videolan.vlc")

        if (!launched && !musicPlaybackService.hasNotificationListenerPermission()) {
            musicPlaybackService.openNotificationListenerSettings()
        }
    }

    // Speech Command
    fun startVoiceRecognition() {
        speechHelper?.stopListening()
        speechHelper = SpeechRecognitionHelper(
            context = getApplication(),
            onResult = { spokenText ->
                _speechStatusMessage.value = null
                sendUserMessage(spokenText)
            },
            onError = { error ->
                _speechStatusMessage.value = error
                viewModelScope.launch {
                    delay(4000)
                    if (_speechStatusMessage.value == error) {
                        _speechStatusMessage.value = null
                    }
                }
            },
            onListeningStateChanged = { listening ->
                _isListening.value = listening
            }
        )
        speechHelper?.startListening()
    }

    fun stopVoiceRecognition() {
        speechHelper?.stopListening()
    }

    // Chat with Jarvis (Streaming + Thinking)
    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isThinking.value) return

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = trimmed
        )
        val streamingMsgId = java.util.UUID.randomUUID().toString()
        val jarvisStreamingPlaceholder = ChatMessage(
            id = streamingMsgId,
            sender = MessageSender.JARVIS,
            text = "",
            isStreaming = true,
            thoughtText = if (_isThinkingEnabled.value) "" else null
        )

        val updatedWithBoth = _chatMessages.value + userMsg + jarvisStreamingPlaceholder
        _chatMessages.value = updatedWithBoth
        _isThinking.value = true

        viewModelScope.launch {
            // Map history excluding the streaming placeholder
            val history = _chatMessages.value
                .filter { it.id != streamingMsgId }
                .map {
                    Pair(if (it.sender == MessageSender.USER) "user" else "model", it.text)
                }
            val appNames = _installedApps.value.map { it.label }

            val result = geminiService.streamChatWithJarvis(
                userMessage = trimmed,
                conversationHistory = history,
                availableAppNames = appNames,
                isThinkingEnabled = _isThinkingEnabled.value,
                onThoughtChunk = { thoughtSoFar ->
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == streamingMsgId) {
                                msg.copy(thoughtText = thoughtSoFar)
                            } else msg
                        }
                    }
                },
                onTextChunk = { textSoFar ->
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == streamingMsgId) {
                                msg.copy(text = textSoFar)
                            } else msg
                        }
                    }
                }
            )

            _isThinking.value = false

            if (result.isSuccess) {
                val jarvisResult = result.getOrNull()!!
                val keyInfo = if (jarvisResult.keyUsedIndex > 0) {
                    " [Fallback Key #${jarvisResult.keyUsedIndex}]"
                } else ""

                val actions = jarvisResult.parsedActions
                var actionType = ActionType.NONE
                var actionPayload: String? = null
                val executedNotesList = mutableListOf<String>()

                for (action in actions) {
                    when (action.type) {
                        "APP_LAUNCH" -> {
                            val appName = action.rawJson.optString("name")
                            val launchRes = appLauncherHelper.launchAppByName(appName)
                            actionType = ActionType.APP_LAUNCH
                            actionPayload = if (launchRes.first) "App '${launchRes.second}' gestartet." else "Konnte '$appName' nicht finden."
                            executedNotesList.add("🚀 $actionPayload")
                        }
                        "SEARCH_WEB" -> {
                            val query = action.rawJson.optString("query")
                            actionType = ActionType.SEARCH_RESULT
                            actionPayload = query
                            executeTavilySearch(query)
                        }
                        "ADD_CALENDAR" -> {
                            val title = action.rawJson.optString("title", "Termin")
                            val date = action.rawJson.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                            val time = action.rawJson.optString("time", "12:00")
                            val newEvent = CalendarEvent(title = title, date = date, time = time)
                            addCalendarEvent(newEvent)
                            actionType = ActionType.CALENDAR
                            actionPayload = "$title am $date um $time"
                            executedNotesList.add("📅 Termin eingetragen: $title ($date, $time Uhr)")
                        }
                        "ADD_REMINDER" -> {
                            val title = action.rawJson.optString("title", "Erinnerung")
                            val dueTime = action.rawJson.optString("dueTime", "Bald")
                            val newReminder = ReminderItem(title = title, dueTime = dueTime)
                            addReminder(newReminder)
                            actionType = ActionType.REMINDER
                            actionPayload = "$title ($dueTime)"
                            executedNotesList.add("⏰ Erinnerung gespeichert: $title ($dueTime)")
                        }
                        "START_TIMER" -> {
                            val seconds = action.rawJson.optLong("seconds", 300)
                            val label = action.rawJson.optString("label", "Timer")
                            startTimer(seconds, label)
                            actionType = ActionType.TIMER
                            actionPayload = "$label: ${seconds}s"
                            executedNotesList.add("⏱️ Timer gestartet: $seconds Sekunden ($label)")
                        }
                    }
                }

                val executedNote = if (executedNotesList.isNotEmpty()) {
                    "\n\n" + executedNotesList.joinToString("\n")
                } else ""

                val finalSpokenText = (if (jarvisResult.replyText.isNotBlank()) jarvisResult.replyText else "")
                    .replace(Regex("""\[\[ACTION:.*?\]\]""", RegexOption.DOT_MATCHES_ALL), "")
                    .trim()

                if (_isVoiceChatMode.value || _isVoiceOutputEnabled.value) {
                    if (finalSpokenText.isNotEmpty()) {
                        microsoftTtsService.speak(finalSpokenText) {
                            if (_isVoiceChatMode.value) {
                                // Continue voice chat loop: listen again
                                viewModelScope.launch {
                                    delay(400)
                                    startVoiceRecognition()
                                }
                            }
                        }
                    }
                }

                _chatMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == streamingMsgId) {
                            val finalText = if (jarvisResult.replyText.isNotBlank()) jarvisResult.replyText else msg.text
                            msg.copy(
                                text = finalText + executedNote + keyInfo,
                                isStreaming = false,
                                actionType = actionType,
                                actionPayload = actionPayload,
                                thoughtText = jarvisResult.thoughtText ?: msg.thoughtText
                            )
                        } else msg
                    }
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                val errReply = "Sir, ein Problem ist aufgetreten: $errorMsg. Bitte überprüfen Sie Ihre API-Keys in den Einstellungen."
                if (_isVoiceChatMode.value || _isVoiceOutputEnabled.value) {
                    microsoftTtsService.speak("Sir, ein Fehler ist aufgetreten.")
                }
                _chatMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == streamingMsgId) {
                            msg.copy(
                                text = errReply,
                                isStreaming = false,
                                actionType = ActionType.ERROR
                            )
                        } else msg
                    }
                }
            }

            persistCurrentChatMessages(_chatMessages.value)
        }
    }

    fun speakText(text: String) {
        microsoftTtsService.speak(text)
    }

    fun stopSpeaking() {
        microsoftTtsService.stopSpeaking()
    }

    fun toggleVoiceOutput() {
        val newVal = !_isVoiceOutputEnabled.value
        _isVoiceOutputEnabled.value = newVal
        settingsManager.isVoiceOutputEnabled = newVal
        if (!newVal) {
            microsoftTtsService.stopSpeaking()
        }
    }

    fun toggleVoiceChatMode() {
        val newVal = !_isVoiceChatMode.value
        _isVoiceChatMode.value = newVal
        if (newVal) {
            _isJarvisOpen.value = true
            startVoiceRecognition()
            if (_chatMessages.value.isEmpty()) {
                microsoftTtsService.speak("Voice Chat aktiv, Sir. Ich höre Ihnen zu.")
            }
        } else {
            speechHelper?.stopListening()
            microsoftTtsService.stopSpeaking()
        }
    }

    fun openAgenda() {
        _isAgendaOpen.value = true
    }

    fun closeAgenda() {
        _isAgendaOpen.value = false
    }

    fun toggleAgenda() {
        _isAgendaOpen.value = !_isAgendaOpen.value
    }

    fun stopAlarm() {
        jarvisNotificationManager.stopAlarm()
        _activeTimer.update { it?.copy(isFinished = false) }
    }

    private fun executeTavilySearch(query: String) {
        viewModelScope.launch {
            val tavilyKey = settingsManager.tavilyApiKey
            val searchRes = tavilySearchService.search(tavilyKey, query)

            if (searchRes.isSuccess) {
                val data = searchRes.getOrNull()!!
                val answer = data.answer ?: "Suchergebnisse gefunden:"
                val linksSummary = data.results.take(3).joinToString("\n") {
                    "• ${it.title}: ${it.url}"
                }

                val msg = ChatMessage(
                    sender = MessageSender.JARVIS,
                    text = "🌐 Web-Recherche für \"$query\":\n$answer\n\n$linksSummary",
                    actionType = ActionType.SEARCH_RESULT,
                    actionPayload = query
                )
                _chatMessages.update { it + msg }
            } else {
                val err = searchRes.exceptionOrNull()?.message ?: "Suchfehler"
                val msg = ChatMessage(
                    sender = MessageSender.JARVIS,
                    text = "🌐 Tavily-Suche fehlgeschlagen: $err",
                    actionType = ActionType.ERROR
                )
                _chatMessages.update { it + msg }
            }
            persistCurrentChatMessages(_chatMessages.value)
        }
    }

    // Calendar Operations
    fun addCalendarEvent(event: CalendarEvent) {
        val updated = _calendarEvents.value + event
        _calendarEvents.value = updated
        settingsManager.saveCalendarEvents(updated)
        jarvisNotificationManager.scheduleCalendarEvent(event.title, event.date, event.time)
    }

    fun deleteCalendarEvent(id: String) {
        val updated = _calendarEvents.value.filter { it.id != id }
        _calendarEvents.value = updated
        settingsManager.saveCalendarEvents(updated)
    }

    // Reminder Operations
    fun addReminder(reminder: ReminderItem) {
        val updated = _reminders.value + reminder
        _reminders.value = updated
        settingsManager.saveReminders(updated)
        jarvisNotificationManager.scheduleReminder(reminder)
    }

    fun toggleReminder(id: String) {
        val updated = _reminders.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _reminders.value = updated
        settingsManager.saveReminders(updated)
    }

    fun deleteReminder(id: String) {
        val updated = _reminders.value.filter { it.id != id }
        _reminders.value = updated
        settingsManager.saveReminders(updated)
    }

    // Timer Operations
    fun startTimer(totalSeconds: Long, label: String = "Timer") {
        countDownTimer?.cancel()
        _activeTimer.value = ActiveTimer(
            label = label,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = true,
            isFinished = false
        )

        countDownTimer = object : CountDownTimer(totalSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val rem = millisUntilFinished / 1000
                _activeTimer.update { it?.copy(remainingSeconds = rem) }
            }

            override fun onFinish() {
                _activeTimer.update { it?.copy(remainingSeconds = 0, isRunning = false, isFinished = true) }
                jarvisNotificationManager.triggerTimerAlarm(label)
            }
        }.start()
    }

    fun pauseTimer() {
        val current = _activeTimer.value ?: return
        if (!current.isRunning) return
        countDownTimer?.cancel()
        countDownTimer = null
        _activeTimer.value = current.copy(isRunning = false)
    }

    fun resumeTimer() {
        val current = _activeTimer.value ?: return
        if (current.isRunning || current.remainingSeconds <= 0) return
        startTimer(current.remainingSeconds, current.label)
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        _activeTimer.value = null
    }

    // Settings Updates
    fun saveSettings(
        geminiKey: String,
        fallbackKeys: List<String>,
        tavilyKey: String,
        city: String,
        darkMode: Boolean
    ) {
        settingsManager.geminiApiKey = geminiKey
        settingsManager.fallbackApiKeys = fallbackKeys
        settingsManager.tavilyApiKey = tavilyKey
        settingsManager.weatherCity = city
        settingsManager.isDarkMode = darkMode

        _weatherCity.value = city
        _isDarkMode.value = darkMode
        _isThinkingEnabled.value = settingsManager.isThinkingEnabled
        closeSettings()
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        speechHelper?.stopListening()
        microsoftTtsService.release()
        jarvisNotificationManager.stopAlarm()
        musicPlaybackService.release()
    }
}
