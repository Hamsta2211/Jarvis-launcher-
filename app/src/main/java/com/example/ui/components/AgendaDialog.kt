package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ActiveTimer
import com.example.data.model.CalendarEvent
import com.example.data.model.ReminderItem
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBg
import com.example.ui.theme.JarvisNavy
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisSurfaceBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgendaDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    calendarEvents: List<CalendarEvent>,
    reminders: List<ReminderItem>,
    activeTimer: ActiveTimer?,
    onAddCalendarEvent: (CalendarEvent) -> Unit,
    onDeleteCalendarEvent: (String) -> Unit,
    onAddReminder: (ReminderItem) -> Unit,
    onToggleReminder: (String) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onStopTimer: () -> Unit
) {
    if (!isOpen) return

    var selectedTab by remember { mutableIntStateOf(0) }
    var isAddingItem by remember { mutableStateOf(false) }

    // New Event Inputs
    var eventTitle by remember { mutableStateOf("") }
    var eventDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var eventTime by remember { mutableStateOf("14:00") }

    // New Reminder Inputs
    var reminderTitle by remember { mutableStateOf("") }
    var reminderDueTime by remember { mutableStateOf("18:00") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 580.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(JarvisDarkBg)
                .border(1.5.dp, JarvisCyanDark, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "J.A.R.V.I.S. AGENDA & MISSIONS-LOG",
                            color = JarvisCyanLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
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

                Spacer(modifier = Modifier.height(8.dp))

                // Active Timer HUD Banner (if running)
                if (activeTimer != null) {
                    val minutes = activeTimer.remainingSeconds / 60
                    val seconds = activeTimer.remainingSeconds % 60
                    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (activeTimer.isFinished) JarvisOrange.copy(alpha = 0.25f) else JarvisNavy)
                            .border(1.dp, if (activeTimer.isFinished) JarvisOrange else JarvisCyan, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (activeTimer.isFinished) JarvisOrange else JarvisCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (activeTimer.isFinished) "TIMER ABGELAUFEN!" else "AKTIVER TIMER: ${activeTimer.label}",
                                        color = if (activeTimer.isFinished) JarvisOrange else JarvisCyanLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (activeTimer.isFinished) "Alarm aktiv" else "Verbleibend: $timeFormatted",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Button(
                                onClick = onStopTimer,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeTimer.isFinished) JarvisOrange else Color.Transparent
                                ),
                                shape = RoundedCornerShape(6.dp),
                                border = if (!activeTimer.isFinished) androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan) else null
                            ) {
                                Text(
                                    text = if (activeTimer.isFinished) "STOPPEN" else "ABBRECHEN",
                                    color = if (activeTimer.isFinished) Color.Black else JarvisCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Tabs: Termine (Calendar) | Erinnerungen (Reminders)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = JarvisNavy,
                    contentColor = JarvisCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = JarvisCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; isAddingItem = false },
                        text = {
                            Text(
                                "TERMINE (${calendarEvents.size})",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (selectedTab == 0) JarvisCyan else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; isAddingItem = false },
                        text = {
                            Text(
                                "ERINNERUNGEN (${reminders.size})",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (selectedTab == 1) JarvisCyan else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content List & Add Form
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isAddingItem) {
                        // Adding new item form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (selectedTab == 0) {
                                Text("Neuen Termin erfassen:", color = JarvisCyanLight, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = eventTitle,
                                    onValueChange = { eventTitle = it },
                                    label = { Text("Termintitel (z.B. Meeting mit Stark)", color = JarvisCyanLight) },
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
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = eventDate,
                                        onValueChange = { eventDate = it },
                                        label = { Text("Datum (YYYY-MM-DD)", color = JarvisCyanLight) },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = JarvisCyan,
                                            unfocusedBorderColor = JarvisSurfaceBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = eventTime,
                                        onValueChange = { eventTime = it },
                                        label = { Text("Uhrzeit (HH:MM)", color = JarvisCyanLight) },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = JarvisCyan,
                                            unfocusedBorderColor = JarvisSurfaceBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true
                                    )
                                }
                            } else {
                                Text("Neue Erinnerung anlegen:", color = JarvisCyanLight, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = reminderTitle,
                                    onValueChange = { reminderTitle = it },
                                    label = { Text("Aufgabe / Erinnerungstext", color = JarvisCyanLight) },
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
                                OutlinedTextField(
                                    value = reminderDueTime,
                                    onValueChange = { reminderDueTime = it },
                                    label = { Text("Fällig um (z.B. 18:00 oder 2026-09-07 18:00)", color = JarvisCyanLight) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JarvisCyan,
                                        unfocusedBorderColor = JarvisSurfaceBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { isAddingItem = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Text("Abbrechen", color = Color.White.copy(alpha = 0.7f))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (selectedTab == 0) {
                                            if (eventTitle.isNotBlank()) {
                                                onAddCalendarEvent(
                                                    CalendarEvent(
                                                        title = eventTitle.trim(),
                                                        date = eventDate.trim(),
                                                        time = eventTime.trim()
                                                    )
                                                )
                                                eventTitle = ""
                                                isAddingItem = false
                                            }
                                        } else {
                                            if (reminderTitle.isNotBlank()) {
                                                onAddReminder(
                                                    ReminderItem(
                                                        title = reminderTitle.trim(),
                                                        dueTime = reminderDueTime.trim()
                                                    )
                                                )
                                                reminderTitle = ""
                                                isAddingItem = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                                ) {
                                    Text("Speichern", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Displaying Items
                        if (selectedTab == 0) {
                            if (calendarEvents.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Keine anstehenden Termine.\nBefehlen Sie Jarvis: 'Erstelle einen Termin...'",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(calendarEvents, key = { it.id }) { event ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(JarvisNavy)
                                                .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = event.title,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "📅 ${event.date} • ⏰ ${event.time} Uhr",
                                                    color = JarvisCyanLight,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                            IconButton(onClick = { onDeleteCalendarEvent(event.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Löschen",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (reminders.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Keine Erinnerungen aktiv.\nBefehlen Sie Jarvis: 'Erinnere mich um 15 Uhr an...'",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(reminders, key = { it.id }) { reminder ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(JarvisNavy)
                                                .border(
                                                    1.dp,
                                                    if (reminder.isCompleted) JarvisSurfaceBorder else JarvisCyanDark,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { onToggleReminder(reminder.id) }
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .border(1.dp, if (reminder.isCompleted) JarvisCyan else JarvisSurfaceBorder)
                                                        .background(if (reminder.isCompleted) JarvisCyan else Color.Transparent),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (reminder.isCompleted) {
                                                        Text("✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = reminder.title,
                                                        color = if (reminder.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                                                        fontSize = 13.sp,
                                                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                                    )
                                                    Text(
                                                        text = "⏰ Fällig: ${reminder.dueTime}",
                                                        color = if (reminder.isCompleted) Color.White.copy(alpha = 0.4f) else JarvisCyanLight,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                            IconButton(onClick = { onDeleteReminder(reminder.id) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Löschen",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Add Button
                if (!isAddingItem) {
                    Button(
                        onClick = { isAddingItem = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisNavy),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTab == 0) "+ Termin manuell hinzufügen" else "+ Erinnerung manuell hinzufügen",
                            color = JarvisCyanLight,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
