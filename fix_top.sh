cat << 'INNER_EOF' > app/src/main/java/com/example/data/service/SettingsManager.kt.new
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
INNER_EOF

# Skip the corrupted first line which probably contains all imports + the class declaration
tail -n +2 app/src/main/java/com/example/data/service/SettingsManager.kt >> app/src/main/java/com/example/data/service/SettingsManager.kt.new

mv app/src/main/java/com/example/data/service/SettingsManager.kt.new app/src/main/java/com/example/data/service/SettingsManager.kt
