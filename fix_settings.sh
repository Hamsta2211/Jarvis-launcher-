cat app/src/main/java/com/example/data/service/SettingsManager.kt | awk '
BEGIN { in_get = 0; in_save = 0 }

/fun getChatSessions\(\): List<ChatSession>/ {
    in_get = 1
    print "    fun getChatSessions(): List<ChatSession> {"
    print "        val file = java.io.File(context.filesDir, \"chat_sessions.json\")"
    print "        val jsonStr = if (file.exists()) file.readText() else prefs.getString(KEY_CHAT_SESSIONS, \"[]\") ?: \"[]\""
    print "        return try {"
    print "            val array = JSONArray(jsonStr)"
    print "            val list = mutableListOf<ChatSession>()"
    print "            for (i in 0 until array.length()) {"
    print "                val obj = array.getJSONObject(i)"
    print "                val id = obj.getString(\"id\")"
    print "                val title = obj.getString(\"title\")"
    print "                val createdAt = obj.optLong(\"createdAt\", System.currentTimeMillis())"
    print "                val updatedAt = obj.optLong(\"updatedAt\", System.currentTimeMillis())"
    print "                val msgArr = obj.getJSONArray(\"messages\")"
    print "                val messages = mutableListOf<ChatMessage>()"
    print "                for (j in 0 until msgArr.length()) {"
    print "                    val mObj = msgArr.getJSONObject(j)"
    print "                    val framesArr = mObj.optJSONArray(\"attachedVideoFramesBase64\")"
    print "                    val framesList = if (framesArr != null) {"
    print "                        val l = mutableListOf<String>()"
    print "                        for(k in 0 until framesArr.length()) l.add(framesArr.getString(k))"
    print "                        l"
    print "                    } else null"
    print "                    messages.add("
    print "                        ChatMessage("
    print "                            id = mObj.optString(\"id\", java.util.UUID.randomUUID().toString()),"
    print "                            sender = if (mObj.getString(\"sender\") == \"USER\") MessageSender.USER else MessageSender.JARVIS,"
    print "                            text = mObj.getString(\"text\"),"
    print "                            timestamp = mObj.optLong(\"timestamp\", System.currentTimeMillis()),"
    print "                            thoughtText = if (mObj.has(\"thoughtText\")) mObj.optString(\"thoughtText\").ifBlank { null } else null,"
    print "                            actionType = ActionType.valueOf(mObj.optString(\"actionType\", \"NONE\")),"
    print "                            actionPayload = if (mObj.has(\"actionPayload\")) mObj.optString(\"actionPayload\") else null,"
    print "                            attachedImageBase64 = if (mObj.has(\"attachedImageBase64\")) mObj.optString(\"attachedImageBase64\") else null,"
    print "                            attachedFileName = if (mObj.has(\"attachedFileName\")) mObj.optString(\"attachedFileName\") else null,"
    print "                            attachedMimeType = if (mObj.has(\"attachedMimeType\")) mObj.optString(\"attachedMimeType\") else null,"
    print "                            attachedVideoFramesBase64 = framesList"
    print "                        )"
    print "                    )"
    print "                }"
    print "                list.add("
    print "                    ChatSession("
    print "                        id = id,"
    print "                        title = title,"
    print "                        createdAt = createdAt,"
    print "                        updatedAt = updatedAt,"
    print "                        messages = messages"
    print "                    )"
    print "                )"
    print "            }"
    print "            list.sortedByDescending { it.updatedAt }"
    print "        } catch (e: Exception) {"
    print "            emptyList()"
    print "        }"
    print "    }"
    next
}

/fun saveChatSessions\(sessions: List<ChatSession>\)/ {
    in_save = 1
    print "    fun saveChatSessions(sessions: List<ChatSession>) {"
    print "        val array = JSONArray()"
    print "        sessions.forEach { s ->"
    print "            val sObj = JSONObject().apply {"
    print "                put(\"id\", s.id)"
    print "                put(\"title\", s.title)"
    print "                put(\"createdAt\", s.createdAt)"
    print "                put(\"updatedAt\", s.updatedAt)"
    print "                val mArr = JSONArray()"
    print "                s.messages.forEach { m ->"
    print "                    val mObj = JSONObject().apply {"
    print "                        put(\"id\", m.id)"
    print "                        put(\"sender\", m.sender.name)"
    print "                        put(\"text\", m.text)"
    print "                        put(\"timestamp\", m.timestamp)"
    print "                        m.thoughtText?.let { put(\"thoughtText\", it) }"
    print "                        put(\"actionType\", m.actionType.name)"
    print "                        m.actionPayload?.let { put(\"actionPayload\", it) }"
    print "                        m.attachedImageBase64?.let { put(\"attachedImageBase64\", it) }"
    print "                        m.attachedFileName?.let { put(\"attachedFileName\", it) }"
    print "                        m.attachedMimeType?.let { put(\"attachedMimeType\", it) }"
    print "                        m.attachedVideoFramesBase64?.let {"
    print "                            val fArr = JSONArray()"
    print "                            it.forEach { f -> fArr.put(f) }"
    print "                            put(\"attachedVideoFramesBase64\", fArr)"
    print "                        }"
    print "                    }"
    print "                    mArr.put(mObj)"
    print "                }"
    print "                put(\"messages\", mArr)"
    print "            }"
    print "            array.put(sObj)"
    print "        }"
    print "        java.io.File(context.filesDir, \"chat_sessions.json\").writeText(array.toString())"
    print "    }"
    next
}

in_get == 1 && /^    }/ {
    in_get = 0
    next
}
in_save == 1 && /^    }/ {
    in_save = 0
    next
}
in_get == 0 && in_save == 0 {
    print $0
}
' > temp_settings.kt

mv temp_settings.kt app/src/main/java/com/example/data/service/SettingsManager.kt
