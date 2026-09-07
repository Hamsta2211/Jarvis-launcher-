package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppItem
import com.example.data.service.AppLauncherHelper
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceBorder

@Composable
fun FolderDialog(
    folder: AppItem,
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    appLauncherHelper: AppLauncherHelper
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisDarkBg)
                .border(1.5.dp, JarvisCyanDark, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = folder.folderName.ifBlank { folder.label }.uppercase(),
                            color = JarvisCyanLight,
                            fontSize = 16.sp,
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

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(folder.folderItems) { item ->
                        AppGridItem(
                            app = item,
                            onClick = {
                                onAppClick(item)
                                onDismiss()
                            },
                            appLauncherHelper = appLauncherHelper
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AppPickerDialog(
    slotIndex: Int,
    installedApps: List<AppItem>,
    onDismiss: () -> Unit,
    onAppSelected: (AppItem) -> Unit,
    onCreateFolder: (String, List<AppItem>) -> Unit,
    appLauncherHelper: AppLauncherHelper
) {
    var searchQuery by remember { mutableStateOf("") }
    var isCreateFolderMode by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }
    var selectedAppsForFolder by remember { mutableStateOf(setOf<AppItem>()) }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 560.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisDarkBg)
                .border(1.5.dp, JarvisCyanDark, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCreateFolderMode) "ORDNER ERSTELLEN" else "APP ZUWEISEN (SLOT ${slotIndex + 1})",
                        color = JarvisCyanLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isCreateFolderMode) {
                    // Switch to folder creation mode button
                    Button(
                        onClick = { isCreateFolderMode = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisNavy),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stattdessen Ordner erstellen", color = JarvisCyanLight, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("App suchen...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JarvisCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredApps, key = { it.packageName + it.activityName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onAppSelected(app) }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconBitmap = remember(app.packageName) {
                                    appLauncherHelper.getAppIcon(app.packageName)
                                }
                                if (iconBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = iconBitmap,
                                        contentDescription = app.label,
                                        modifier = Modifier.size(36.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = resolveAppIcon("", app.label),
                                        contentDescription = null,
                                        tint = JarvisCyan,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = app.label,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    // Folder Creation Form
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Ordnername (z.B. Tools)", color = JarvisCyanLight) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisSurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wähle Apps für den Ordner (${selectedAppsForFolder.size} gewählt):",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(installedApps) { app ->
                            val isChecked = selectedAppsForFolder.contains(app)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChecked) JarvisCyan.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        selectedAppsForFolder = if (isChecked) {
                                            selectedAppsForFolder - app
                                        } else {
                                            selectedAppsForFolder + app
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isChecked) JarvisCyan else JarvisSurfaceBorder)
                                        .background(if (isChecked) JarvisCyan else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Text("✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = app.label,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isCreateFolderMode = false }) {
                            Text("Zurück", color = Color.White.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val name = folderName.ifBlank { "Ordner" }
                                onCreateFolder(name, selectedAppsForFolder.toList())
                            },
                            enabled = selectedAppsForFolder.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                        ) {
                            Text("Ordner speichern", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
