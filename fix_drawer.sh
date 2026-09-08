cat app/src/main/java/com/example/ui/jarvis/JarvisScreen.kt | awk '
BEGIN { in_drawer = 0 }
/ModalDrawerSheet\(/ {
    in_drawer = 1
    print "            ModalDrawerSheet("
    print "                drawerContainerColor = JarvisDarkBg,"
    print "                modifier = Modifier.width(320.dp)"
    print "            ) {"
    print "                val navColors = NavigationDrawerItemDefaults.colors("
    print "                    selectedContainerColor = JarvisCyan.copy(alpha=0.2f),"
    print "                    unselectedContainerColor = Color.Transparent,"
    print "                    selectedIconColor = JarvisCyan,"
    print "                    unselectedIconColor = Color.White.copy(alpha = 0.8f),"
    print "                    selectedTextColor = JarvisCyanLight,"
    print "                    unselectedTextColor = Color.White.copy(alpha = 0.9f)"
    print "                )"
    print "                "
    print "                Row(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 48.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {"
    print "                    Text(\"J.A.R.V.I.S.\", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)"
    print "                    Row {"
    print "                        androidx.compose.material3.IconButton(onClick = {}) {"
    print "                            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = \"Suchen\", tint = Color.White.copy(alpha=0.7f))"
    print "                        }"
    print "                    }"
    print "                }"
    print "                "
    print "                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {"
    print "                    NavigationDrawerItem(label = { Text(\"Bilder\", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }, selected = false, onClick = {}, icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.Image, null) }, colors = navColors, modifier = Modifier.padding(vertical = 4.dp))"
    print "                    NavigationDrawerItem(label = { Text(\"Bibliothek\", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }, selected = false, onClick = {}, icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.LibraryBooks, null) }, colors = navColors, modifier = Modifier.padding(vertical = 4.dp))"
    print "                    NavigationDrawerItem(label = { Text(\"Einstellungen\", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }, selected = false, onClick = { onOpenSettings(); coroutineScope.launch { drawerState.close() } }, icon = { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.Settings, null) }, colors = navColors, modifier = Modifier.padding(vertical = 4.dp))"
    print "                }"
    print "                "
    print "                Spacer(Modifier.height(16.dp))"
    print "                "
    print "                Text("
    print "                    text = \"Letzte\","
    print "                    color = Color.White.copy(alpha = 0.8f),"
    print "                    fontSize = 16.sp,"
    print "                    fontWeight = FontWeight.Bold,"
    print "                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)"
    print "                )"
    print "                "
    print "                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {"
    print "                    items(chatSessions) { session ->"
    print "                        val isSelected = session.id == currentSessionId"
    print "                        NavigationDrawerItem("
    print "                            label = { Text(session.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 15.sp) },"
    print "                            selected = isSelected,"
    print "                            onClick = {"
    print "                                onSelectChat(session.id)"
    print "                                coroutineScope.launch { drawerState.close() }"
    print "                            },"
    print "                            colors = navColors,"
    print "                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)"
    print "                        )"
    print "                    }"
    print "                }"
    print "                "
    print "                // Floating New Chat Button Area"
    print "                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {"
    print "                    androidx.compose.material3.Button("
    print "                        onClick = { onNewChat(); coroutineScope.launch { drawerState.close() } },"
    print "                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = JarvisCyan),"
    print "                        shape = RoundedCornerShape(percent = 50),"
    print "                        modifier = Modifier.height(48.dp)"
    print "                    ) {"
    print "                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = \"Neuer Chat\", tint = JarvisNavy)"
    print "                        Spacer(Modifier.width(8.dp))"
    print "                        Text(\"Chat\", color = JarvisNavy, fontWeight = FontWeight.Bold)"
    print "                    }"
    print "                }"
    print "            }"
    next
}
in_drawer == 1 && /^            }/ {
    in_drawer = 0
    next
}
in_drawer == 0 {
    print $0
}
' > temp.kt

mv temp.kt app/src/main/java/com/example/ui/jarvis/JarvisScreen.kt
