package com.example.data.model

data class AppItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val packageName: String = "",
    val activityName: String = "",
    val label: String = "",
    val isFolder: Boolean = false,
    val folderName: String = "",
    val folderItems: List<AppItem> = emptyList(),
    val iconKey: String = "" // Special fallback icons like "whatsapp", "chrome", "youtube", "music", "flashlight"
)

data class CalendarEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val time: String,
    val description: String = ""
)

data class ReminderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dueTime: String,
    val isCompleted: Boolean = false
)

data class ActiveTimer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String = "Timer",
    val totalSeconds: Long,
    val remainingSeconds: Long,
    val isRunning: Boolean = true,
    val isFinished: Boolean = false
)

enum class MessageSender {
    USER, JARVIS, SYSTEM
}

enum class ActionType {
    NONE, APP_LAUNCH, SEARCH_RESULT, CALENDAR, REMINDER, TIMER, ERROR
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: ActionType = ActionType.NONE,
    val actionPayload: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val thoughtText: String? = null,
    val isStreaming: Boolean = false,
    val attachedImageBase64: String? = null,
    val attachedVideoFramesBase64: List<String>? = null,
    val attachedFileName: String? = null,
    val attachedMimeType: String? = null
)

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "Neuer Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)

data class TavilySearchItem(
    val title: String,
    val url: String,
    val content: String
)

data class TavilySearchResponse(
    val query: String,
    val answer: String?,
    val results: List<TavilySearchItem>
)

data class JarvisParsedAction(
    val type: String,
    val rawJson: org.json.JSONObject
)
