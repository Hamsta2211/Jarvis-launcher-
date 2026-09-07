package com.example.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.data.service.AppLauncherHelper
import com.example.data.service.SettingsManager
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceBorder

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    settingsManager: SettingsManager,
    installedApps: List<AppItem> = emptyList(),
    currentOrbitSlotCount: Int = 8,
    onOrbitSlotCountChanged: (Int) -> Unit = {},
    onSave: (geminiKey: String, fallbackKeys: List<String>, tavilyKey: String, city: String, darkMode: Boolean) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val allInstalledAppsList = remember(installedApps) {
        if (installedApps.isNotEmpty()) installedApps
        else AppLauncherHelper(context).getAllInstalledApps()
    }
    var primaryKey by remember { mutableStateOf(settingsManager.geminiApiKey) }
    val fallbackKeys = remember { mutableStateListOf<String>().apply { addAll(settingsManager.fallbackApiKeys) } }
    var tavilyKey by remember { mutableStateOf(settingsManager.tavilyApiKey) }
    var weatherCity by remember { mutableStateOf(settingsManager.weatherCity) }
    var isDarkMode by remember { mutableStateOf(settingsManager.isDarkMode) }
    var isThinkingEnabled by remember { mutableStateOf(settingsManager.isThinkingEnabled) }
    var orbitSlotCount by remember { mutableIntStateOf(currentOrbitSlotCount) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 620.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisDarkBg)
                .border(1.5.dp, JarvisCyanDark, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "J.A.R.V.I.S. SYSTEM-SETUP",
                            color = JarvisCyanLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Model & Failover Info Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisNavy)
                        .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "⚡ INTELLIGENTER API-KEY FAILOVER",
                            color = JarvisCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wenn ein Key das Kontingent erreicht, wechselt Jarvis sofort zum nächsten Schlüssel. Jeden Tag um 00:00 Uhr wird automatisch wieder mit dem Primär-Schlüssel gestartet.",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Orbit Home Screen Slots Configuration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Startbildschirm Apps ($orbitSlotCount Slots):",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (orbitSlotCount > 4) {
                                    orbitSlotCount--
                                    onOrbitSlotCountChanged(orbitSlotCount)
                                }
                            },
                            enabled = orbitSlotCount > 4,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Slot entfernen",
                                tint = if (orbitSlotCount > 4) JarvisCyan else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$orbitSlotCount",
                            color = JarvisCyanLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                if (orbitSlotCount < 16) {
                                    orbitSlotCount++
                                    onOrbitSlotCountChanged(orbitSlotCount)
                                }
                            },
                            enabled = orbitSlotCount < 16,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Slot hinzufügen",
                                tint = if (orbitSlotCount < 16) JarvisCyan else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Quick Slot count buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(8, 10, 12, 14, 16).forEach { count ->
                        val isSelected = orbitSlotCount == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) JarvisCyan else JarvisNavy)
                                .border(1.dp, if (isSelected) JarvisCyanLight else JarvisSurfaceBorder, RoundedCornerShape(6.dp))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    orbitSlotCount = count
                                    onOrbitSlotCountChanged(count)
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Default Music App Selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisNavy)
                        .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Standard-Musik-App:",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Wähle eine App, die beim Antippen des Musik-Widgets geöffnet werden soll:",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    var selectedMusicPackage by remember { mutableStateOf(settingsManager.defaultMusicAppPackage) }
                    var selectedMusicName by remember { mutableStateOf(settingsManager.defaultMusicAppName) }
                    var musicAppSearchQuery by remember { mutableStateOf("") }
                    var showAllAppsPicker by remember { mutableStateOf(false) }

                    // Current Selected App Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisCyan.copy(alpha = 0.15f))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aktuell aktiv: $selectedMusicName",
                                color = JarvisCyanLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            if (selectedMusicPackage.isNotEmpty()) {
                                Text(
                                    text = selectedMusicPackage,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val popularMusicApps = listOf(
                        Triple("Automatisch", "", "⚡"),
                        Triple("Spotify", "com.spotify.music", "🟢"),
                        Triple("YouTube Music", "com.google.android.apps.youtube.music", "🔴"),
                        Triple("Apple Music", "com.apple.android.music", "🍎"),
                        Triple("Deezer", "deezer.android.app", "🔵"),
                        Triple("TIDAL", "com.aspiro.tidal", "🌊"),
                        Triple("Amazon Music", "com.amazon.mp3", "📦"),
                        Triple("SoundCloud", "com.soundcloud.android", "🟠"),
                        Triple("Poweramp", "com.maxmpz.audioplayer", "🎵")
                    )

                    // Popular Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        popularMusicApps.chunked(3).forEach { rowApps ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowApps.forEach { (name, pkg, icon) ->
                                    val isSelected = selectedMusicPackage == pkg
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) JarvisCyan.copy(alpha = 0.35f) else JarvisDarkBg)
                                            .border(
                                                1.dp,
                                                if (isSelected) JarvisCyan else JarvisSurfaceBorder,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                selectedMusicPackage = pkg
                                                selectedMusicName = name
                                                settingsManager.defaultMusicAppPackage = pkg
                                                settingsManager.defaultMusicAppName = name
                                            }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$icon $name",
                                            color = if (isSelected) JarvisCyanLight else Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle Button for searching ALL Installed Apps
                    OutlinedButton(
                        onClick = { showAllAppsPicker = !showAllAppsPicker },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = if (showAllAppsPicker) Icons.Default.Close else Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showAllAppsPicker) "App-Suche schließen" else "Alle installierten Apps durchsuchen (${allInstalledAppsList.size} Apps)...",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (showAllAppsPicker) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Search Input
                        OutlinedTextField(
                            value = musicAppSearchQuery,
                            onValueChange = { musicAppSearchQuery = it },
                            placeholder = { Text("App Name suchen...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisSurfaceBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val filteredApps = remember(musicAppSearchQuery, allInstalledAppsList) {
                            if (musicAppSearchQuery.isBlank()) {
                                allInstalledAppsList
                            } else {
                                val query = musicAppSearchQuery.trim().lowercase()
                                allInstalledAppsList.filter {
                                    it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisDarkBg)
                                .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(6.dp))
                                .verticalScroll(rememberScrollState())
                                .padding(4.dp)
                        ) {
                            Column {
                                if (filteredApps.isEmpty()) {
                                    Text(
                                        text = "Keine passenden Apps gefunden.",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    filteredApps.forEach { app ->
                                        val isSelected = selectedMusicPackage == app.packageName
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) JarvisCyan.copy(alpha = 0.25f) else Color.Transparent)
                                                .clickable {
                                                    selectedMusicPackage = app.packageName
                                                    selectedMusicName = app.label
                                                    settingsManager.defaultMusicAppPackage = app.packageName
                                                    settingsManager.defaultMusicAppName = app.label
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = app.label,
                                                    color = if (isSelected) JarvisCyanLight else Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = app.packageName,
                                                    color = Color.White.copy(alpha = 0.45f),
                                                    fontSize = 10.sp
                                                )
                                            }
                                            if (isSelected) {
                                                Text("✓", color = JarvisCyanLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Music Notification Permission Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisNavy)
                        .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Musik-Titelanzeige (Spotify etc.)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Benachrichtigungszugriff in Android",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
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
                                    // Ignore
                                }
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Aktivieren", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Gemini API Key
                Text(
                    text = "Google Gemini API-Key (Primär):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = primaryKey,
                    onValueChange = { primaryKey = it },
                    placeholder = { Text("API-Key eingeben...", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fallback API Keys Header & Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fallback API-Keys (${fallbackKeys.size}):",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = { fallbackKeys.add("") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Key hinzufügen", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (fallbackKeys.isEmpty()) {
                    Text(
                        text = "Keine Fallback-Keys hinterlegt. Füge weitere Schlüssel hinzu für nahtlose Ausfallsicherheit.",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                } else {
                    fallbackKeys.forEachIndexed { index, keyVal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = keyVal,
                                onValueChange = { fallbackKeys[index] = it },
                                placeholder = { Text("Fallback-Schlüssel #${index + 1}", color = Color.White.copy(alpha = 0.35f)) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisSurfaceBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )
                            IconButton(onClick = { fallbackKeys.removeAt(index) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Löschen",
                                    tint = JarvisRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tavily API Key
                Text(
                    text = "Tavily Search API-Key (Web-Recherche):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tavilyKey,
                    onValueChange = { tavilyKey = it },
                    placeholder = { Text("tvly-...", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Weather City
                Text(
                    text = "Wetter-Standort (Stadt):",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = weatherCity,
                    onValueChange = { weatherCity = it },
                    placeholder = { Text("LINZ", color = Color.White.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = JarvisCyan) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisSurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Dark Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dark Mode / Cyber HUD",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Optimiert für Jarvis Hologramm-Ästhetik",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisNavy
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Jarvis Thinking Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Jarvis Denkprozess (Thinking)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Erweiterte Gedankenkette von Gemini anzeigen",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isThinkingEnabled,
                        onCheckedChange = { isThinkingEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisNavy
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        settingsManager.isThinkingEnabled = isThinkingEnabled
                        onSave(
                            primaryKey.trim(),
                            fallbackKeys.map { it.trim() }.filter { it.isNotEmpty() },
                            tavilyKey.trim(),
                            weatherCity.trim().ifBlank { "LINZ" },
                            isDarkMode
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "KONFIGURATION SPEICHERN",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
