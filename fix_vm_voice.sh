sed -i 's/speechHelper?.stopListening()/speechHelper?.stopListening()\n        clearVoiceCameraFrames()/g' app/src/main/java/com/example/ui/MainViewModel.kt

sed -i 's/attachedImageBase64 = latestVoiceCameraFrame,/attachedVideoFramesBase64 = latestVoiceCameraFrames.toList(),/g' app/src/main/java/com/example/ui/MainViewModel.kt

sed -i 's/attachedFileName = if (latestVoiceCameraFrame != null) "Camera.jpg" else null,/attachedFileName = if (latestVoiceCameraFrames.isNotEmpty()) "VoiceVideo.mp4" else null,/g' app/src/main/java/com/example/ui/MainViewModel.kt

sed -i 's/attachedMimeType = if (latestVoiceCameraFrame != null) "image\/jpeg" else null/attachedMimeType = if (latestVoiceCameraFrames.isNotEmpty()) "video\/mp4" else null/g' app/src/main/java/com/example/ui/MainViewModel.kt
