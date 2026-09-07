package com.example.data.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlaybackService(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    val ambientPlaylist = listOf(
        "Stark Tower Synthwave",
        "Mark LXXXV Flight Dynamics",
        "Avengers Hologram Theme",
        "Cyberpunk Neo Linz Overdrive"
    )

    private var currentAmbientIndex = 0
    private var isAmbientPlaying = false

    private val _currentTrackDisplay = MutableStateFlow("Stark Tower Synthwave")
    val currentTrackDisplay: StateFlow<String> = _currentTrackDisplay.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    init {
        // Collect real media state from JarvisMediaListenerService
        scope.launch {
            JarvisMediaListenerService.mediaState.collect { info ->
                if (info.hasActiveSession && info.title.isNotBlank()) {
                    val fullDisplay = if (info.artist.isNotBlank()) {
                        "${info.title} • ${info.artist}"
                    } else {
                        info.title
                    }
                    _currentTrackDisplay.value = fullDisplay
                    _isPlaying.value = info.isPlaying
                } else if (!isAmbientPlaying) {
                    _isPlaying.value = audioManager?.isMusicActive == true
                }
            }
        }
    }

    fun hasNotificationListenerPermission(): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(context.packageName)
    }

    fun openNotificationListenerSettings() {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e("MusicPlaybackService", "Cannot open settings", e2)
            }
        }
    }

    fun getCurrentTrackName(): String = _currentTrackDisplay.value

    fun isMusicPlaying(): Boolean = _isPlaying.value

    fun togglePlayPause(onStateChanged: (Boolean, String) -> Unit) {
        val mediaInfo = JarvisMediaListenerService.mediaState.value

        if (mediaInfo.hasActiveSession) {
            if (mediaInfo.isPlaying) {
                JarvisMediaListenerService.pauseActiveSession()
                _isPlaying.value = false
            } else {
                JarvisMediaListenerService.playActiveSession()
                _isPlaying.value = true
            }
        } else {
            // Check if music is playing in any background player via system
            if (audioManager?.isMusicActive == true || _isPlaying.value) {
                dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                _isPlaying.value = !_isPlaying.value
            } else {
                // Toggle ambient synth
                if (isAmbientPlaying) {
                    stopAmbientSynth()
                    _isPlaying.value = false
                } else {
                    startAmbientSynth()
                    _isPlaying.value = true
                    _currentTrackDisplay.value = ambientPlaylist[currentAmbientIndex]
                }
            }
        }
        onStateChanged(_isPlaying.value, _currentTrackDisplay.value)
    }

    fun nextTrack(onStateChanged: (Boolean, String) -> Unit) {
        val mediaInfo = JarvisMediaListenerService.mediaState.value

        if (mediaInfo.hasActiveSession) {
            val success = JarvisMediaListenerService.skipToNextActiveSession()
            if (!success) {
                dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
            }
        } else {
            dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
            if (isAmbientPlaying) {
                currentAmbientIndex = (currentAmbientIndex + 1) % ambientPlaylist.size
                _currentTrackDisplay.value = ambientPlaylist[currentAmbientIndex]
                stopAmbientSynth()
                startAmbientSynth()
            }
        }
        onStateChanged(_isPlaying.value, _currentTrackDisplay.value)
    }

    fun previousTrack(onStateChanged: (Boolean, String) -> Unit) {
        val mediaInfo = JarvisMediaListenerService.mediaState.value

        if (mediaInfo.hasActiveSession) {
            val success = JarvisMediaListenerService.skipToPreviousActiveSession()
            if (!success) {
                dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            }
        } else {
            dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
            if (isAmbientPlaying) {
                currentAmbientIndex = if (currentAmbientIndex - 1 < 0) ambientPlaylist.size - 1 else currentAmbientIndex - 1
                _currentTrackDisplay.value = ambientPlaylist[currentAmbientIndex]
                stopAmbientSynth()
                startAmbientSynth()
            }
        }
        onStateChanged(_isPlaying.value, _currentTrackDisplay.value)
    }

    private fun dispatchSystemMediaKey(keyCode: Int) {
        try {
            val eventDown = KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keyCode, 0)
            val eventUp = KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0)
            audioManager?.dispatchMediaKeyEvent(eventDown)
            audioManager?.dispatchMediaKeyEvent(eventUp)
        } catch (e: Exception) {
            Log.w("MusicPlaybackService", "dispatchMediaKeyEvent failed: ${e.message}")
        }
    }

    private fun startAmbientSynth() {
        stopAmbientSynth()
        isAmbientPlaying = true

        synthJob = scope.launch {
            try {
                val sampleRate = 44100
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBufSize * 2).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.play()

                val buffer = ShortArray(bufferSize / 2)
                var phase1 = 0.0
                var phase2 = 0.0
                var sampleIndex = 0L

                val baseFreqs = when (currentAmbientIndex) {
                    0 -> doubleArrayOf(130.81, 164.81, 196.00, 261.63)
                    1 -> doubleArrayOf(146.83, 174.61, 220.00, 293.66)
                    2 -> doubleArrayOf(110.00, 130.81, 164.81, 220.00)
                    else -> doubleArrayOf(123.47, 155.56, 185.00, 246.94)
                }

                while (isActive && isAmbientPlaying) {
                    for (i in buffer.indices) {
                        val beatTime = (sampleIndex / (sampleRate * 0.22)).toInt()
                        val chordIndex = beatTime % baseFreqs.size
                        val targetFreq = baseFreqs[chordIndex]

                        val arpStep = (sampleIndex / (sampleRate * 0.11)).toInt() % 4
                        val arpFreq = targetFreq * (when (arpStep) {
                            0 -> 1.0
                            1 -> 1.25
                            2 -> 1.5
                            else -> 2.0
                        })

                        val step1 = 2.0 * Math.PI * targetFreq / sampleRate
                        val step2 = 2.0 * Math.PI * arpFreq / sampleRate

                        phase1 = (phase1 + step1) % (2.0 * Math.PI)
                        phase2 = (phase2 + step2) % (2.0 * Math.PI)

                        val sample1 = Math.sin(phase1) * 0.35
                        val sample2 = Math.sin(phase2) * 0.25
                        val mixed = ((sample1 + sample2) * 32767.0).toInt().coerceIn(-32768, 32767)

                        buffer[i] = mixed.toShort()
                        sampleIndex++
                    }
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.w("MusicPlaybackService", "Ambient synth playback failed", e)
            }
        }
    }

    private fun stopAmbientSynth() {
        isAmbientPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null
    }

    fun release() {
        stopAmbientSynth()
    }
}
