package com.example.data.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service delivering Microsoft Neural TTS with the famous "de-DE-ConradNeural" voice
 * (Edge-TTS protocol, German Conrad - iconic Jarvis assistant voice) with multiple
 * fallbacks ensuring high-quality, non-robotic sci-fi assistant speech.
 */
class MicrosoftTtsService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var androidTts: TextToSpeech? = null
    private var isAndroidTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    init {
        // Initialize Android TTS with deep Jarvis male voice profile
        androidTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    val res = androidTts?.setLanguage(Locale.GERMAN)
                    if (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isAndroidTtsReady = true
                        // Configure deep Jarvis timbre
                        androidTts?.setPitch(0.85f)
                        androidTts?.setSpeechRate(1.02f)

                        // Attempt to select German Male neural voice
                        val voices = androidTts?.voices
                        val maleGermanVoice = voices?.firstOrNull { voice ->
                            voice.locale.language == "de" && (
                                voice.name.contains("male", ignoreCase = true) ||
                                voice.name.contains("deb", ignoreCase = true) ||
                                voice.name.contains("dea", ignoreCase = true) ||
                                voice.name.contains("deg", ignoreCase = true) ||
                                voice.name.contains("de-de-x", ignoreCase = true)
                            )
                        } ?: voices?.firstOrNull { it.locale.language == "de" }

                        if (maleGermanVoice != null) {
                            androidTts?.voice = maleGermanVoice
                            Log.d("MicrosoftTtsService", "Android TTS selected voice: ${maleGermanVoice.name}")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("MicrosoftTtsService", "Error configuring Android TTS voice: ${e.message}")
                }
            }
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        val cleanText = cleanTextForSpeech(text)
        if (cleanText.isBlank()) {
            onFinished?.invoke()
            return
        }

        stopSpeaking()
        _isSpeaking.value = true

        scope.launch {
            // 1. Try Microsoft Conrad Neural Voice via Edge-TTS WebSocket
            var success = synthesizeWithMicrosoftConrad(cleanText, onFinished)

            // 2. If WebSocket failed, try HTTP Neural Speech Endpoint
            if (!success) {
                Log.w("MicrosoftTtsService", "Edge-TTS WS failed, trying HTTP Neural TTS...")
                success = synthesizeWithHttpNeural(cleanText, onFinished)
            }

            // 3. Fallback to customized Android Deep Male TTS
            if (!success) {
                Log.w("MicrosoftTtsService", "Neural TTS failed, using Jarvis male Android TTS profile")
                speakWithAndroidFallback(cleanText, onFinished)
            }
        }
    }

    fun stopSpeaking() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.release()
                mediaPlayer = null
            }
            if (isAndroidTtsReady) {
                androidTts?.stop()
            }
        } catch (e: Exception) {
            Log.e("MicrosoftTtsService", "Error stopping speech: ${e.message}")
        } finally {
            _isSpeaking.value = false
        }
    }

    private suspend fun synthesizeWithMicrosoftConrad(
        text: String,
        onFinished: (() -> Unit)?
    ): Boolean = withContext(Dispatchers.IO) {
        val connectionId = UUID.randomUUID().toString().replace("-", "")
        val requestId = UUID.randomUUID().toString().replace("-", "")
        val audioStream = ByteArrayOutputStream()
        val isFinishedSignal = AtomicBoolean(false)
        val hasError = AtomicBoolean(false)

        val timestamp = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)", Locale.US).format(Date())

        val wssUrl = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaheadedge/v1" +
                "?TrustedClientToken=6A5AA1D4EA65112B8E83DF91D6E7943F" +
                "&ConnectionId=$connectionId"

        val request = Request.Builder()
            .url(wssUrl)
            .header("Pragma", "no-cache")
            .header("Cache-Control", "no-cache")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0")
            .header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
            .header("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
            .build()

        var currentWebSocket: WebSocket? = null

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                try {
                    // 1. Send speech config with Timestamp
                    val configJson = "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"false\"},\"outputFormat\":\"audio-24khz-48kbitrate-mono-mp3\"}}}}"
                    val configMsg = "X-Timestamp:$timestamp\r\nContent-Type:application/json; charset=utf-8\r\nPath:speech.config\r\n\r\n$configJson"
                    webSocket.send(configMsg)

                    // 2. Send SSML with Microsoft German voice 'Microsoft Server Speech Text to Speech Voice (de-DE, ConradNeural)'
                    val escapedSsml = escapeXml(text)
                    val ssml = "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='de-DE'>" +
                            "<voice name='Microsoft Server Speech Text to Speech Voice (de-DE, ConradNeural)'>" +
                            "<prosody pitch='+0Hz' rate='+0%'>" +
                            escapedSsml +
                            "</prosody></voice></speak>"

                    val ssmlMsg = "X-RequestId:$requestId\r\nX-Timestamp:$timestamp\r\nContent-Type:application/ssml+xml\r\nPath:ssml\r\n\r\n$ssml"
                    webSocket.send(ssmlMsg)
                } catch (e: Exception) {
                    Log.e("MicrosoftTtsService", "Error sending Edge WS frame: ${e.message}")
                    hasError.set(true)
                    isFinishedSignal.set(true)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val data = bytes.toByteArray()
                if (data.size > 2) {
                    val headerLength = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                    val audioOffset = 2 + headerLength
                    if (data.size > audioOffset) {
                        audioStream.write(data, audioOffset, data.size - audioOffset)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (text.contains("Path:turn.end")) {
                    isFinishedSignal.set(true)
                    webSocket.close(1000, "Done")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("MicrosoftTtsService", "Edge TTS WebSocket failure: ${t.message}")
                hasError.set(true)
                isFinishedSignal.set(true)
            }
        }

        try {
            currentWebSocket = client.newWebSocket(request, listener)

            val startTime = System.currentTimeMillis()
            while (!isFinishedSignal.get() && System.currentTimeMillis() - startTime < 8000) {
                Thread.sleep(40)
            }

            currentWebSocket.cancel()

            val audioBytes = audioStream.toByteArray()
            if (audioBytes.isNotEmpty() && !hasError.get()) {
                withContext(Dispatchers.Main) {
                    playMp3Bytes(audioBytes, onFinished)
                }
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("MicrosoftTtsService", "Error during Edge-TTS: ${e.message}")
        }

        return@withContext false
    }

    private suspend fun synthesizeWithHttpNeural(
        text: String,
        onFinished: (() -> Unit)?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // High quality Google Neural / Translate TTS endpoint
            val encodedText = URLEncoder.encode(text.take(200), "UTF-8")
            val url = "https://translate.google.com/translate_tts?ie=UTF-8&q=$encodedText&tl=de-DE&total=1&idx=0&textlen=${text.length}&client=tw-ob"

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val bytes = resp.body?.bytes()
                if (bytes != null && bytes.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        playMp3Bytes(bytes, onFinished)
                    }
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.w("MicrosoftTtsService", "HTTP TTS failed: ${e.message}")
        }
        return@withContext false
    }

    private fun playMp3Bytes(audioBytes: ByteArray, onFinished: (() -> Unit)?) {
        try {
            val tempFile = File(context.cacheDir, "jarvis_voice_${System.currentTimeMillis()}.mp3")
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .build()
                )
                setDataSource(tempFile.absolutePath)
                setOnCompletionListener {
                    _isSpeaking.value = false
                    tempFile.delete()
                    onFinished?.invoke()
                }
                setOnErrorListener { _, _, _ ->
                    _isSpeaking.value = false
                    tempFile.delete()
                    onFinished?.invoke()
                    true
                }
                prepare()
                start()
                _isSpeaking.value = true
            }
        } catch (e: Exception) {
            Log.e("MicrosoftTtsService", "MediaPlayer error: ${e.message}")
            _isSpeaking.value = false
            onFinished?.invoke()
        }
    }

    private fun speakWithAndroidFallback(text: String, onFinished: (() -> Unit)?) {
        if (!isAndroidTtsReady || androidTts == null) {
            _isSpeaking.value = false
            onFinished?.invoke()
            return
        }

        val utteranceId = UUID.randomUUID().toString()
        androidTts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(id: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(id: String?) {
                Handler(Looper.getMainLooper()).post {
                    _isSpeaking.value = false
                    onFinished?.invoke()
                }
            }

            override fun onError(id: String?) {
                Handler(Looper.getMainLooper()).post {
                    _isSpeaking.value = false
                    onFinished?.invoke()
                }
            }
        })

        androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun cleanTextForSpeech(input: String): String {
        return input
            .replace(Regex("""\[\[ACTION:.*?\]\]""", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("""[#*_`~]"""), "")
            .replace(Regex("""https?://\S+"""), "Link")
            .replace(Regex("""\n+"""), " ")
            .trim()
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun release() {
        stopSpeaking()
        androidTts?.shutdown()
        androidTts = null
    }
}
