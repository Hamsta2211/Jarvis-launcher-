package com.example.data.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveMediaInfo(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val packageName: String = "",
    val hasActiveSession: Boolean = false
)

class JarvisMediaListenerService : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null
    private var sessionsChangedListener: MediaSessionManager.OnActiveSessionsChangedListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activeControllers = mutableListOf<MediaController>()
    private val controllerCallbacks = mutableMapOf<MediaController, MediaController.Callback>()

    companion object {
        private const val TAG = "JarvisMediaListener"

        private val _mediaState = MutableStateFlow(ActiveMediaInfo())
        val mediaState: StateFlow<ActiveMediaInfo> = _mediaState.asStateFlow()

        private var activeMediaController: MediaController? = null

        fun getActiveController(): MediaController? = activeMediaController

        fun playActiveSession(): Boolean {
            return try {
                activeMediaController?.transportControls?.play()
                true
            } catch (e: Exception) {
                Log.e(TAG, "play failed", e)
                false
            }
        }

        fun pauseActiveSession(): Boolean {
            return try {
                activeMediaController?.transportControls?.pause()
                true
            } catch (e: Exception) {
                Log.e(TAG, "pause failed", e)
                false
            }
        }

        fun skipToNextActiveSession(): Boolean {
            return try {
                activeMediaController?.transportControls?.skipToNext()
                true
            } catch (e: Exception) {
                Log.e(TAG, "skipToNext failed", e)
                false
            }
        }

        fun skipToPreviousActiveSession(): Boolean {
            return try {
                activeMediaController?.transportControls?.skipToPrevious()
                true
            } catch (e: Exception) {
                Log.e(TAG, "skipToPrevious failed", e)
                false
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "JarvisMediaListenerService connected")
        initMediaSessions()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        unregisterControllers()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Re-check media sessions when a new media notification appears
        updateActiveMediaState()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveMediaState()
    }

    private fun initMediaSessions() {
        try {
            sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(this, JarvisMediaListenerService::class.java)

            sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
                mainHandler.post {
                    registerControllers(controllers ?: emptyList())
                    updateActiveMediaState()
                }
            }

            sessionManager?.let { sm ->
                sm.addOnActiveSessionsChangedListener(sessionsChangedListener!!, componentName)
                val initialControllers = sm.getActiveSessions(componentName)
                registerControllers(initialControllers)
                updateActiveMediaState()
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException on getActiveSessions: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing media sessions", e)
        }
    }

    private fun registerControllers(controllers: List<MediaController>) {
        unregisterControllers()
        activeControllers.clear()
        activeControllers.addAll(controllers)

        for (controller in controllers) {
            val callback = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) {
                    mainHandler.post { updateActiveMediaState() }
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    mainHandler.post { updateActiveMediaState() }
                }

                override fun onSessionDestroyed() {
                    mainHandler.post { updateActiveMediaState() }
                }
            }
            controllerCallbacks[controller] = callback
            try {
                controller.registerCallback(callback)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register controller callback: ${e.message}")
            }
        }
    }

    private fun unregisterControllers() {
        for ((controller, callback) in controllerCallbacks) {
            try {
                controller.unregisterCallback(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
        controllerCallbacks.clear()
    }

    private fun updateActiveMediaState() {
        try {
            // Find playing controller first, then recent controller
            val playingController = activeControllers.firstOrNull { controller ->
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: activeControllers.firstOrNull()

            activeMediaController = playingController

            if (playingController != null) {
                val metadata = playingController.metadata
                val playbackState = playingController.playbackState
                val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

                val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                    ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                    ?: ""

                val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                    ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
                    ?: ""

                val pkg = playingController.packageName ?: ""

                if (title.isNotBlank() || isPlaying) {
                    _mediaState.value = ActiveMediaInfo(
                        title = title.ifBlank { "Unbekannter Titel" },
                        artist = artist,
                        isPlaying = isPlaying,
                        packageName = pkg,
                        hasActiveSession = true
                    )
                    return
                }
            }

            // No active session found with metadata
            _mediaState.value = ActiveMediaInfo(
                title = "",
                artist = "",
                isPlaying = false,
                packageName = "",
                hasActiveSession = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating active media state", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterControllers()
        sessionsChangedListener?.let { listener ->
            try {
                sessionManager?.removeOnActiveSessionsChangedListener(listener)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
