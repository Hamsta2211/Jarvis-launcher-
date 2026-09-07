package com.example.data.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.AppItem
import java.util.concurrent.ConcurrentHashMap

class AppLauncherHelper(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    private var isTorchOn = false
    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()
    private var cachedApps: List<AppItem>? = null

    fun getAllInstalledApps(forceRefresh: Boolean = false): List<AppItem> {
        if (!forceRefresh && cachedApps != null) {
            return cachedApps!!
        }

        return try {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

            val list = resolveInfoList.map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val activityName = resolveInfo.activityInfo.name
                val label = resolveInfo.loadLabel(packageManager).toString()

                AppItem(
                    id = packageName,
                    packageName = packageName,
                    activityName = activityName,
                    label = label,
                    iconKey = ""
                )
            }.sortedBy { it.label.lowercase() }

            cachedApps = list
            list
        } catch (e: Exception) {
            cachedApps ?: emptyList()
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchAppByName(nameQuery: String): Pair<Boolean, String> {
        val apps = getAllInstalledApps()
        val query = nameQuery.trim().lowercase()

        // 1. Direct label match or contains
        var matched = apps.firstOrNull { it.label.lowercase() == query }
            ?: apps.firstOrNull { it.label.lowercase().contains(query) }
            ?: apps.firstOrNull { query.contains(it.label.lowercase()) }

        // 2. Package name match
        if (matched == null) {
            matched = apps.firstOrNull { it.packageName.lowercase().contains(query) }
        }

        // 3. Known popular app package aliases
        if (matched == null) {
            val aliasPackages = when {
                query.contains("whatsapp") || query == "wa" || query.contains("whats app") ->
                    listOf("com.whatsapp", "com.whatsapp.w4b")
                query.contains("spotify") || query.contains("musik") || query.contains("music") ->
                    listOf("com.spotify.music", "com.google.android.apps.youtube.music", "com.apple.android.music", "com.amazon.mp3")
                query.contains("youtube") || query == "yt" ->
                    listOf("com.google.android.youtube", "com.google.android.apps.youtube.music")
                query.contains("chrome") || query.contains("browser") || query.contains("internet") ->
                    listOf("com.android.chrome", "org.mozilla.firefox", "com.opera.browser", "com.microsoft.emmx")
                query.contains("kamera") || query.contains("camera") || query.contains("foto") ->
                    listOf("com.google.android.GoogleCamera", "com.sec.android.app.camera", "com.android.camera")
                query.contains("galerie") || query.contains("gallery") || query.contains("fotos") ->
                    listOf("com.google.android.apps.photos", "com.sec.android.gallery3d", "com.android.gallery3d")
                query.contains("rechner") || query.contains("calculator") || query.contains("calc") ->
                    listOf("com.google.android.calculator", "com.sec.android.app.popupcalculator", "com.android.calculator2")
                query.contains("maps") || query.contains("karten") || query.contains("navigation") ->
                    listOf("com.google.android.apps.maps", "com.waze")
                query.contains("uhr") || query.contains("wecker") || query.contains("alarm") || query.contains("timer") ->
                    listOf("com.google.android.deskclock", "com.sec.android.app.clockpackage")
                query.contains("telefon") || query.contains("phone") || query.contains("anrufen") ->
                    listOf("com.google.android.dialer", "com.samsung.android.dialer", "com.android.dialer")
                query.contains("nachrichten") || query.contains("messages") || query.contains("sms") ->
                    listOf("com.google.android.apps.messaging", "com.samsung.android.messaging")
                query.contains("einstellungen") || query.contains("settings") ->
                    listOf("com.android.settings")
                else -> emptyList()
            }

            for (pkg in aliasPackages) {
                if (launchApp(pkg)) {
                    val appLabel = apps.firstOrNull { it.packageName == pkg }?.label ?: nameQuery
                    return Pair(true, appLabel)
                }
            }
        }

        return if (matched != null) {
            val success = launchApp(matched.packageName)
            Pair(success, matched.label)
        } else {
            Pair(false, "")
        }
    }

    fun toggleFlashlight(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return false
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            isTorchOn
        } catch (e: Exception) {
            false
        }
    }

    fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getAppIcon(packageName: String): ImageBitmap? {
        val cached = iconCache[packageName]
        if (cached != null) return cached

        return try {
            val drawable: Drawable = packageManager.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(drawable).asImageBitmap()
            iconCache[packageName] = bitmap
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 144
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 144
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
