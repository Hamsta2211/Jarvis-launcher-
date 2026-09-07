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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.service.AppLauncherHelper
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisSurfaceBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    installedApps: List<AppItem>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedAlphabet: Char?,
    onAlphabetSelected: (Char?) -> Unit,
    onAppClick: (AppItem) -> Unit,
    appLauncherHelper: AppLauncherHelper
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val alphabet = ('A'..'Z').toList()

    // Filter apps by search query and alphabet letter
    val filteredApps = remember(installedApps, searchQuery, selectedAlphabet) {
        installedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() || app.label.contains(searchQuery, ignoreCase = true)
            val matchesAlpha = selectedAlphabet == null || app.label.startsWith(selectedAlphabet, ignoreCase = true)
            matchesSearch && matchesAlpha
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = JarvisDarkBg.copy(alpha = 0.96f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ANWENDUNGEN",
                        color = JarvisCyanLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${filteredApps.size})",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Schließen",
                        tint = JarvisCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JarvisNavy)
                    .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(12.dp))
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Apps durchsuchen...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = JarvisCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Löschen",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = JarvisCyan,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content: Alphabet rail on the right, App Grid on the left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 6.dp)
            ) {
                // Apps Grid
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keine Anwendungen gefunden",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps, key = { it.packageName + it.activityName + it.label }) { app ->
                            AppGridItem(
                                app = app,
                                onClick = {
                                    onAppClick(app)
                                    onDismiss()
                                },
                                appLauncherHelper = appLauncherHelper
                            )
                        }
                    }
                }

                // Alphabetical Quick-Filter Index Bar
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    alphabet.forEach { char ->
                        val isSelected = selectedAlphabet == char
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) JarvisCyan else Color.Transparent)
                                .clickable { onAlphabetSelected(char) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char.toString(),
                                color = if (isSelected) Color.Black else JarvisCyanLight.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppGridItem(
    app: AppItem,
    onClick: () -> Unit,
    appLauncherHelper: AppLauncherHelper
) {
    val iconBitmap = remember(app.packageName) {
        if (app.packageName.isNotBlank()) {
            appLauncherHelper.getAppIcon(app.packageName)
        } else null
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(JarvisNavy)
                .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (iconBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier.size(38.dp)
                )
            } else {
                Icon(
                    imageVector = resolveAppIcon(app.iconKey, app.label),
                    contentDescription = app.label,
                    tint = JarvisCyanLight,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif
        )
    }
}
