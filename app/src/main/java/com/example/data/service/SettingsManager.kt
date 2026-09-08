package com.example.data.service

import java.io.File
import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.ActionType
import com.example.data.model.AppItem
import com.example.data.model.CalendarEvent
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.data.model.ReminderItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_launcher_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GEMINI_KEY = "gemini_api_key"
        private const val KEY_FALLBACK_KEYS = "gemini_fallback_keys"
        private const val KEY_FAILED_KEYS_DATE = "gemini_failed_keys_date"
        private const val KEY_FAILED_KEY_INDEXES = "gemini_failed_key_indexes"
        private const val KEY_TAVILY_KEY = "tavily_api_key"
        private const val KEY_WEATHER_CITY = "weather_city"
        private const val KEY_DARK_MODE = "is_dark_mode"
        private const val KEY_ORBIT_APPS = "orbit_apps_json"
        private const val KEY_CALENDAR_EVENTS = "calendar_events_json"
        private const val KEY_REMINDERS = "reminders_json"
        private const val KEY_FIRST_RUN = "is_first_run"
        private const val KEY_THINKING_ENABLED = "jarvis_thinking_enabled"
        private const val KEY_VOICE_OUTPUT = "jarvis_voice_output_enabled"
        private const val KEY_DEFAULT_MUSIC_PACKAGE = "default_music_package"
        private const val KEY_DEFAULT_MUSIC_NAME = "default_music_name"
        private const val KEY_JARVIS_VOICE = "jarvis_voice_name"
        private const val KEY_CHAT_SESSIONS = "chat_sessions_json"
        private const val KEY_CURRENT_SESSION_ID = "current_chat_session_id"
        private const val KEY_MODELFILE = "jarvis_modelfile"
    }

    var modelfile: String
        get() {
            val defaultModelfile = """
                FROM gemini-2.5-flash
                SYSTEM Du bist J.A.R.V.I.S. (Just A Rather Very Intelligent System), die hochentwickelte KI aus Iron Man, integriert in diesen Android Sci-Fi Launcher. Dein Tonfall: Höflich, intelligent, loyal, britisch-aristokratisch ("Sehr wohl, Sir.", "Ich leite das unverzüglich ein.", "Systeme einsatzbereit."). Antworte prägnant und elegant auf Deutsch.
                PARAMETER temperature 0.5
                PARAMETER thinkingBudget 2048
            """.trimIndent()
            return prefs.getString(KEY_MODELFILE, defaultModelfile) ?: defaultModelfile
        }
        set(value) = prefs.edit().putString(KEY_MODELFILE, value.trim()).apply()

    var defaultMusicAppPackage: String
        get() = prefs.getString(KEY_DEFAULT_MUSIC_PACKAGE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DEFAULT_MUSIC_PACKAGE, value.trim()).apply()

    var defaultMusicAppName: String
        get() = prefs.getString(KEY_DEFAULT_MUSIC_NAME, "Automatisch") ?: "Automatisch"
        set(value) = prefs.edit().putString(KEY_DEFAULT_MUSIC_NAME, value.trim()).apply()

    var jarvisVoiceName: String
        get() = prefs.getString(KEY_JARVIS_VOICE, "de-DE-ConradNeural") ?: "de-DE-ConradNeural"
        set(value) = prefs.edit().putString(KEY_JARVIS_VOICE, value.trim()).apply()

    var isVoiceOutputEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_OUTPUT, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_OUTPUT, value).apply()

    var isFirstRun: Boolean
        get() = prefs.getBoolean(KEY_FIRST_RUN, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_RUN, value).apply()

    var geminiApiKey: String
        get() {
            val saved = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
            if (saved.isNotBlank()) return saved
            return try {
                val buildConfigKey = BuildConfig.GEMINI_API_KEY
                if (buildConfigKey != "MY_GEMINI_API_KEY" && buildConfigKey.isNotBlank()) {
                    buildConfigKey
                } else ""
            } catch (e: Exception) {
                ""
            }
        }
        set(value) {
            prefs.edit().putString(KEY_GEMINI_KEY, value.trim()).apply()
            clearFailedKeys()
        }

    var fallbackApiKeys: List<String>
        get() {
            val jsonStr = prefs.getString(KEY_FALLBACK_KEYS, "[]") ?: "[]"
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val key = array.getString(i).trim()
                    if (key.isNotEmpty()) list.add(key)
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            val array = JSONArray()
            value.map { it.trim() }.filter { it.isNotEmpty() }.forEach { array.put(it) }
            prefs.edit().putString(KEY_FALLBACK_KEYS, array.toString()).apply()
            clearFailedKeys()
        }

    var tavilyApiKey: String
        get() {
            val saved = prefs.getString(KEY_TAVILY_KEY, "") ?: ""
            if (saved.isNotBlank()) return saved
            return try {
                val buildConfigKey = BuildConfig.TAVILY_API_KEY
                if (buildConfigKey != "MY_TAVILY_API_KEY" && buildConfigKey.isNotBlank()) {
                    buildConfigKey
                } else ""
            } catch (e: Exception) {
                ""
            }
        }
        set(value) = prefs.edit().putString(KEY_TAVILY_KEY, value.trim()).apply()

    var weatherCity: String
        get() = prefs.getString(KEY_WEATHER_CITY, "LINZ") ?: "LINZ"
        set(value) = prefs.edit().putString(KEY_WEATHER_CITY, value.trim()).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var isThinkingEnabled: Boolean
        get() = prefs.getBoolean(KEY_THINKING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_THINKING_ENABLED, value).apply()

    fun getAllGeminiKeys(): List<String> {
        val keys = mutableListOf<String>()
        val primary = geminiApiKey.trim()
        if (primary.isNotEmpty()) {
            keys.add(primary)
        }
        fallbackApiKeys.forEach { fb ->
            val clean = fb.trim()
            if (clean.isNotEmpty() && !keys.contains(clean)) {
                keys.add(clean)
            }
        }
        return keys
    }

    /**
     * Returns ordered Gemini API keys with automatic failover.
     * If a key failed today, it is moved to the end of the queue.
     * When a new day begins, all keys are reset and the primary key is tried first again.
     */
    fun getOrderedGeminiKeysWithFailover(): List<Pair<Int, String>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val recordedDate = prefs.getString(KEY_FAILED_KEYS_DATE, "") ?: ""

        val failedIndices = if (recordedDate == today) {
            getFailedKeyIndices()
        } else {
            // A new day has started: automatically reset failures so the primary key is tested fresh!
            clearFailedKeys()
            emptySet()
        }

        val allKeys = getAllGeminiKeys().mapIndexed { idx, key -> Pair(idx, key) }
        val nonFailed = allKeys.filter { it.first !in failedIndices }
        val failedOnes = allKeys.filter { it.first in failedIndices }
        return nonFailed + failedOnes
    }

    fun markGeminiKeyFailedForToday(keyIndex: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val current = getFailedKeyIndices().toMutableSet()
        current.add(keyIndex)
        val array = JSONArray()
        current.forEach { array.put(it) }
        prefs.edit()
            .putString(KEY_FAILED_KEYS_DATE, today)
            .putString(KEY_FAILED_KEY_INDEXES, array.toString())
            .apply()
    }

    fun clearFailedKeys() {
        prefs.edit()
            .remove(KEY_FAILED_KEYS_DATE)
            .remove(KEY_FAILED_KEY_INDEXES)
            .apply()
    }

    private fun getFailedKeyIndices(): Set<Int> {
        val str = prefs.getString(KEY_FAILED_KEY_INDEXES, "[]") ?: "[]"
        return try {
            val array = JSONArray(str)
            val set = mutableSetOf<Int>()
            for (i in 0 until array.length()) {
                set.add(array.getInt(i))
            }
            set
        } catch (e: Exception) {
            emptySet()
        }
    }

    // Save & Load Orbit Apps
    fun getOrbitApps(): List<AppItem> {
        val jsonStr = prefs.getString(KEY_ORBIT_APPS, null)
        if (jsonStr == null) {
            return getDefaultOrbitApps()
        }
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AppItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(deserializeAppItem(obj))
            }
            if (list.isNotEmpty()) {
                list
            } else {
                getDefaultOrbitApps()
            }
        } catch (e: Exception) {
            getDefaultOrbitApps()
        }
    }

    fun saveOrbitApps(apps: List<AppItem>) {
        val array = JSONArray()
        apps.forEach { app ->
            array.put(serializeAppItem(app))
        }
        prefs.edit().putString(KEY_ORBIT_APPS, array.toString()).apply()
    }

    // Calendar Events
    fun getCalendarEvents(): List<CalendarEvent> {
        val jsonStr = prefs.getString(KEY_CALENDAR_EVENTS, "[]") ?: "[]"
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CalendarEvent>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalendarEvent(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        date = obj.getString("date"),
                        time = obj.getString("time"),
                        description = obj.optString("description", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCalendarEvents(events: List<CalendarEvent>) {
        val array = JSONArray()
        events.forEach { ev ->
            val obj = JSONObject().apply {
                put("id", ev.id)
                put("title", ev.title)
                put("date", ev.date)
                put("time", ev.time)
                put("description", ev.description)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CALENDAR_EVENTS, array.toString()).apply()
    }

    // Reminders
    fun getReminders(): List<ReminderItem> {
        val jsonStr = prefs.getString(KEY_REMINDERS, "[]") ?: "[]"
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ReminderItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ReminderItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        dueTime = obj.getString("dueTime"),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveReminders(reminders: List<ReminderItem>) {
        val array = JSONArray()
        reminders.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("dueTime", r.dueTime)
                put("isCompleted", r.isCompleted)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_REMINDERS, array.toString()).apply()
    }

    // Chat Sessions
    fun getCurrentChatSessionId(): String {
        return prefs.getString(KEY_CURRENT_SESSION_ID, "") ?: ""
    }

    fun saveCurrentChatSessionId(id: String) {
        prefs.edit().putString(KEY_CURRENT_SESSION_ID, id).apply()
    }

    fun getChatSessions(): List<ChatSession> {
        val file = java.io.File(context.filesDir, "chat_sessions.json")
        val jsonStr = if (file.exists()) file.readText() else prefs.getString(KEY_CHAT_SESSIONS, "[]") ?: "[]"
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ChatSession>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                val msgArr = obj.getJSONArray("messages")
                val messages = mutableListOf<ChatMessage>()
                for (j in 0 until msgArr.length()) {
                    val mObj = msgArr.getJSONObject(j)
                    val framesArr = mObj.optJSONArray("attachedVideoFramesBase64")
                    val framesList = if (framesArr != null) {
                        val l = mutableListOf<String>()
                        for(k in 0 until framesArr.length()) l.add(framesArr.getString(k))
                        l
                    } else null
                    messages.add(
                        ChatMessage(
                            id = mObj.optString("id", java.util.UUID.randomUUID().toString()),
                            sender = if (mObj.getString("sender") == "USER") MessageSender.USER else MessageSender.JARVIS,
                            text = mObj.getString("text"),
                            timestamp = mObj.optLong("timestamp", System.currentTimeMillis()),
                            thoughtText = if (mObj.has("thoughtText")) mObj.optString("thoughtText").ifBlank { null } else null,
                            actionType = ActionType.valueOf(mObj.optString("actionType", "NONE")),
                            actionPayload = if (mObj.has("actionPayload")) mObj.optString("actionPayload") else null,
                            attachedImageBase64 = if (mObj.has("attachedImageBase64")) mObj.optString("attachedImageBase64") else null,
                            attachedFileName = if (mObj.has("attachedFileName")) mObj.optString("attachedFileName") else null,
                            attachedMimeType = if (mObj.has("attachedMimeType")) mObj.optString("attachedMimeType") else null,
                            attachedVideoFramesBase64 = framesList
                        )
                    )
                }
                list.add(
                    ChatSession(
                        id = id,
                        title = title,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        messages = messages
                    )
                )
            }
            list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveChatSessions(sessions: List<ChatSession>) {
        val array = JSONArray()
        sessions.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("createdAt", s.createdAt)
                put("updatedAt", s.updatedAt)
                val mArr = JSONArray()
                s.messages.forEach { m ->
                    val mObj = JSONObject().apply {
                        put("id", m.id)
                        put("sender", m.sender.name)
                        put("text", m.text)
                        put("timestamp", m.timestamp)
                        m.thoughtText?.let { put("thoughtText", it) }
                        put("actionType", m.actionType.name)
                        m.actionPayload?.let { put("actionPayload", it) }
                        m.attachedImageBase64?.let { put("attachedImageBase64", it) }
                        m.attachedFileName?.let { put("attachedFileName", it) }
                        m.attachedMimeType?.let { put("attachedMimeType", it) }
                        m.attachedVideoFramesBase64?.let {
                            val fArr = JSONArray()
                            it.forEach { f -> fArr.put(f) }
                            put("attachedVideoFramesBase64", fArr)
                        }
                    }
                    mArr.put(mObj)
                }
                put("messages", mArr)
            }
            array.put(sObj)
        }
        java.io.File(context.filesDir, "chat_sessions.json").writeText(array.toString())
    }

    private fun serializeAppItem(app: AppItem): JSONObject {
        val obj = JSONObject()
        obj.put("id", app.id)
        obj.put("packageName", app.packageName)
        obj.put("activityName", app.activityName)
        obj.put("label", app.label)
        obj.put("isFolder", app.isFolder)
        obj.put("folderName", app.folderName)
        obj.put("iconKey", app.iconKey)
        if (app.isFolder && app.folderItems.isNotEmpty()) {
            val folderArr = JSONArray()
            app.folderItems.forEach { child ->
                folderArr.put(serializeAppItem(child))
            }
            obj.put("folderItems", folderArr)
        }
        return obj
    }

    private fun deserializeAppItem(obj: JSONObject): AppItem {
        val folderItems = mutableListOf<AppItem>()
        if (obj.has("folderItems")) {
            val arr = obj.getJSONArray("folderItems")
            for (j in 0 until arr.length()) {
                folderItems.add(deserializeAppItem(arr.getJSONObject(j)))
            }
        }
        return AppItem(
            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
            packageName = obj.optString("packageName", ""),
            activityName = obj.optString("activityName", ""),
            label = obj.optString("label", ""),
            isFolder = obj.optBoolean("isFolder", false),
            folderName = obj.optString("folderName", ""),
            folderItems = folderItems,
            iconKey = obj.optString("iconKey", "")
        )
    }

    fun getDefaultOrbitApps(): List<AppItem> {
        return listOf(
            AppItem(label = "WhatsApp", packageName = "com.whatsapp", iconKey = "whatsapp"),
            AppItem(label = "YouTube", packageName = "com.google.android.youtube", iconKey = "youtube"),
            AppItem(label = "Play Store", packageName = "com.android.vending", iconKey = "play"),
            AppItem(label = "Maps", packageName = "com.google.android.apps.maps", iconKey = "maps"),
            AppItem(label = "Chrome", packageName = "com.android.chrome", iconKey = "chrome"),
            AppItem(label = "Musik", packageName = "com.google.android.apps.youtube.music", iconKey = "music"),
            AppItem(label = "Taschenlampe", iconKey = "flashlight"),
            AppItem(
                label = "Hub",
                isFolder = true,
                folderName = "Tools",
                folderItems = listOf(
                    AppItem(label = "Mail", iconKey = "mail"),
                    AppItem(label = "Browser", iconKey = "browser"),
                    AppItem(label = "Rechner", iconKey = "notes"),
                    AppItem(label = "Shop", iconKey = "shopping")
                ),
                iconKey = "folder"
            )
        )
    }
}
