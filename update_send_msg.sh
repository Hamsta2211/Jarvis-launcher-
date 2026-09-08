sed -i 's/attachedMimeType: String? = null/attachedMimeType: String? = null,\n        isVoiceCommand: Boolean = false/g' app/src/main/java/com/example/ui/MainViewModel.kt

sed -i 's/isThinkingEnabled = _isThinkingEnabled.value,/isThinkingEnabled = _isThinkingEnabled.value,\n                isLiveVoiceChat = isVoiceCommand,/g' app/src/main/java/com/example/ui/MainViewModel.kt

sed -i 's/attachedMimeType = if (latestVoiceCameraFrames.isNotEmpty()) "video\/mp4" else null/attachedMimeType = if (latestVoiceCameraFrames.isNotEmpty()) "video\/mp4" else null,\n                    isVoiceCommand = true/g' app/src/main/java/com/example/ui/MainViewModel.kt

# Also remove duplicate attachedVideoFramesBase64
sed -i '618d' app/src/main/java/com/example/ui/MainViewModel.kt

