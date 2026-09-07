package com.example.data.service

import android.util.Log
import com.example.data.model.JarvisParsedAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class GeminiJarvisResult(
    val replyText: String,
    val keyUsedIndex: Int = 0,
    val parsedActions: List<JarvisParsedAction> = emptyList(),
    val parsedAction: JarvisParsedAction? = parsedActions.firstOrNull(),
    val thoughtText: String? = null
)

class GeminiService(private val settingsManager: SettingsManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val candidateModels = listOf(
        "gemini-3.8-flash",
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-2.5-pro"
    )

    suspend fun streamChatWithJarvis(
        userMessage: String,
        conversationHistory: List<Pair<String, String>>,
        availableAppNames: List<String>,
        isThinkingEnabled: Boolean,
        onThoughtChunk: (accumulatedThought: String) -> Unit,
        onTextChunk: (accumulatedText: String) -> Unit
    ): Result<GeminiJarvisResult> = withContext(Dispatchers.IO) {
        val orderedKeys = settingsManager.getOrderedGeminiKeysWithFailover()

        if (orderedKeys.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Kein Google Gemini API-Key hinterlegt. Bitte in den Einstellungen eintragen.")
            )
        }

        var lastError: Exception? = null

        for ((originalIndex, key) in orderedKeys) {
            val cleanKey = key.trim()
            if (cleanKey.isBlank()) continue

            var keySucceeded = false
            for (model in candidateModels) {
                try {
                    val result = callGeminiApiStream(
                        apiKey = cleanKey,
                        modelName = model,
                        userMessage = userMessage,
                        conversationHistory = conversationHistory,
                        availableAppNames = availableAppNames,
                        isThinkingEnabled = isThinkingEnabled,
                        onThoughtChunk = onThoughtChunk,
                        onTextChunk = onTextChunk
                    )
                    keySucceeded = true
                    return@withContext Result.success(
                        GeminiJarvisResult(
                            replyText = result.first,
                            keyUsedIndex = originalIndex,
                            parsedActions = result.second,
                            thoughtText = result.third
                        )
                    )
                } catch (e: Exception) {
                    lastError = e
                    Log.w("GeminiService", "Key #$originalIndex with model $model failed: ${e.message}")
                }
            }

            // If all models failed with this key, mark key as failed for today so next requests start with the fallback
            if (!keySucceeded) {
                Log.w("GeminiService", "Marking Key #$originalIndex as failed for today. Switching to next key.")
                settingsManager.markGeminiKeyFailedForToday(originalIndex)
            }
        }

        Result.failure(
            lastError ?: Exception("Alle Gemini Modelle und API Keys sind fehlgeschlagen oder das Kontingent ist erschöpft.")
        )
    }

    private fun callGeminiApiStream(
        apiKey: String,
        modelName: String,
        userMessage: String,
        conversationHistory: List<Pair<String, String>>,
        availableAppNames: List<String>,
        isThinkingEnabled: Boolean,
        onThoughtChunk: (String) -> Unit,
        onTextChunk: (String) -> Unit
    ): Triple<String, List<JarvisParsedAction>, String?> {
        val appsListHint = availableAppNames.take(200).joinToString(", ")
        val currentDateStr = SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMANY).format(Date())
        val currentIsoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val systemInstructionText = buildString {
            appendLine("Du bist J.A.R.V.I.S. (Just A Rather Very Intelligent System), die hochentwickelte KI aus Iron Man, integriert in diesen Android Sci-Fi Launcher.")
            appendLine("Dein Tonfall: Höflich, intelligent, loyal, britisch-aristokratisch (\"Sehr wohl, Sir.\", \"Ich leite das unverzüglich ein.\", \"Systeme einsatzbereit.\"). Antworte prägnant und elegant auf Deutsch.")
            appendLine("AKTUELLE SYSTEM-ZEIT: $currentDateStr (ISO-Datum: $currentIsoDate, Uhrzeit: $currentTimeStr Uhr)")
            appendLine("Installierte Apps auf diesem Gerät: $appsListHint")
            if (isThinkingEnabled) {
                appendLine("WICHTIG (Denkprozess): Analysiere kurz Deine kognitiven Schritte und Systemparameter im internen Denkprozess.")
            }
            appendLine("WICHTIG ZU KALENDER, ERINNERUNGEN UND TIMERN:")
            appendLine("- Wenn der Nutzer einen Termin / Kalendereintrag möchte, erstelle zwingend [[ACTION:{\"type\":\"ADD_CALENDAR\",\"title\":\"Terminname\",\"date\":\"$currentIsoDate\",\"time\":\"HH:MM\"}]]")
            appendLine("- Wenn der Nutzer eine Erinnerung möchte, erstelle zwingend [[ACTION:{\"type\":\"ADD_REMINDER\",\"title\":\"Aufgabe\",\"dueTime\":\"HH:MM\"}]]")
            appendLine("- Wenn der Nutzer einen Countdown / Timer möchte (z.B. in 1 Min), erstelle zwingend [[ACTION:{\"type\":\"START_TIMER\",\"seconds\":SekundenZahl,\"label\":\"Grund\"}]]")
            appendLine("- Wenn der Nutzer MEHRERE Dinge fordert (z.B. Erinnerung UND Termin), erstelle MEHRERE ACTION-Zeilen am Ende Deiner Antwort!")
            appendLine("- Behaupte NIEMALS, dass Du einen Termin, eine Erinnerung oder einen Timer erstellt hast, ohne die entsprechende [[ACTION:...]] anzuhängen!")
            appendLine("- Für 'Erinnerungen' erstelle immer ADD_REMINDER. Öffne NICHT die Reminder-App mit APP_LAUNCH, außer der Nutzer verlangt explizit 'Öffne die Reminder-App'.")
            appendLine("")
            appendLine("VERFÜGBARE AKTIONEN:")
            appendLine("- App starten: [[ACTION:{\"type\":\"APP_LAUNCH\",\"name\":\"App-Name\"}]]")
            appendLine("- Internetsuche (Web): [[ACTION:{\"type\":\"SEARCH_WEB\",\"query\":\"Suchbegriff\"}]]")
            appendLine("- Kalendertermin anlegen: [[ACTION:{\"type\":\"ADD_CALENDAR\",\"title\":\"Terminname\",\"date\":\"YYYY-MM-DD\",\"time\":\"HH:MM\"}]]")
            appendLine("- Erinnerung erstellen: [[ACTION:{\"type\":\"ADD_REMINDER\",\"title\":\"Aufgabe\",\"dueTime\":\"HH:MM\"}]]")
            appendLine("- Timer starten: [[ACTION:{\"type\":\"START_TIMER\",\"seconds\":SekundenZahl,\"label\":\"Grund\"}]]")
            appendLine("Wenn keine Systemaktion nötig ist, füge kein [[ACTION:...]] hinzu.")
        }

        val contentsArray = JSONArray()

        val recentHistory = conversationHistory.takeLast(6)
        for ((role, text) in recentHistory) {
            val partObj = JSONObject().put("text", text)
            val partsArray = JSONArray().put(partObj)
            val contentObj = JSONObject().put("role", role).put("parts", partsArray)
            contentsArray.put(contentObj)
        }

        // Current message
        val currentPart = JSONObject().put("text", userMessage)
        val currentContent = JSONObject().put("role", "user").put("parts", JSONArray().put(currentPart))
        contentsArray.put(currentContent)

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.5)
                    put("maxOutputTokens", 4096)
                    if (isThinkingEnabled) {
                        put(
                            "thinkingConfig",
                            JSONObject().apply {
                                put("thinkingBudget", 2048)
                            }
                        )
                    }
                }
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:streamGenerateContent?key=$apiKey&alt=sse"

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Kein Fehlertext"
            throw Exception("HTTP ${response.code}: $errorBody")
        }

        val responseBody = response.body ?: throw Exception("Leerer Response Body von Gemini")
        val source = responseBody.source()

        val fullThought = StringBuilder()
        val fullResponse = StringBuilder()

        while (!source.exhausted()) {
            val line = source.readUtf8Line() ?: break
            if (line.startsWith("data:")) {
                val dataContent = line.removePrefix("data:").trim()
                if (dataContent.isEmpty() || dataContent == "[DONE]") continue

                try {
                    val jsonObj = JSONObject(dataContent)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        if (content != null) {
                            val parts = content.optJSONArray("parts")
                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)
                                    val isThoughtPart = part.optBoolean("thought", false) ||
                                            part.optString("thought").equals("true", ignoreCase = true)
                                    val rawText = part.optString("text")

                                    if (isThoughtPart) {
                                        if (rawText.isNotEmpty()) {
                                            fullThought.append(rawText)
                                            onThoughtChunk(fullThought.toString())
                                        }
                                    } else {
                                        // Check if rawText contains inline <thought>...</thought> tags
                                        if (rawText.contains("<thought>") || fullThought.isNotEmpty() && !fullThought.contains("</thought>") && rawText.contains("</thought>")) {
                                            // Extract thinking tag contents
                                            val thoughtMatch = Regex("""<thought>(.*?)</thought>""", RegexOption.DOT_MATCHES_ALL).find(rawText)
                                            if (thoughtMatch != null) {
                                                fullThought.append(thoughtMatch.groupValues[1])
                                                onThoughtChunk(fullThought.toString())
                                                val remaining = rawText.replace(thoughtMatch.value, "")
                                                if (remaining.isNotEmpty()) {
                                                    fullResponse.append(remaining)
                                                    onTextChunk(fullResponse.toString().replace(Regex("""\[\[ACTION:(\{.*?\})\]\]""", RegexOption.DOT_MATCHES_ALL), "").trimEnd())
                                                }
                                            } else {
                                                fullThought.append(rawText.replace("<thought>", "").replace("</thought>", ""))
                                                onThoughtChunk(fullThought.toString())
                                            }
                                        } else {
                                            if (rawText.isNotEmpty()) {
                                                fullResponse.append(rawText)
                                                val currentClean = fullResponse.toString()
                                                    .replace(Regex("""\[\[ACTION:(\{.*?\})\]\]""", RegexOption.DOT_MATCHES_ALL), "")
                                                    .trimEnd()
                                                onTextChunk(currentClean)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore SSE json chunk errors
                }
            }
        }

        val rawFullText = fullResponse.toString()
        val actionRegex = Regex("""\[\[ACTION:(\{.*?\})\]\]""", RegexOption.DOT_MATCHES_ALL)
        val matches = actionRegex.findAll(rawFullText).toList()
        val parsedActions = mutableListOf<JarvisParsedAction>()
        var cleanText = rawFullText

        for (match in matches) {
            cleanText = cleanText.replace(match.value, "").trim()
            try {
                val actionJson = JSONObject(match.groupValues[1])
                val type = actionJson.optString("type")
                parsedActions.add(JarvisParsedAction(type = type, rawJson = actionJson))
            } catch (e: Exception) {
                // ignore action json parse error
            }
        }

        val thoughtResult: String? = if (fullThought.isNotEmpty()) {
            fullThought.toString().trim()
        } else null

        val finalOutputText = cleanText.ifEmpty { "Keine Antwort erhalten." }
        return Triple<String, List<JarvisParsedAction>, String?>(finalOutputText, parsedActions, thoughtResult)
    }
}
