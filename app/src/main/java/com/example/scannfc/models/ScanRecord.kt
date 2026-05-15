package com.example.scannfc.models

import com.google.firebase.Timestamp

data class ScanRecord(
    val id: String = "",
    val tagId: String = "",      // Уникальный ID метки (из "железа")
    val tagContent: String = "", // Информация, записанная в метку
    val userId: String = "",     // Кто сканировал
    val userName: String = "",   // Имя для быстрого отображения в истории
    val userGroupId: String = "",// Группа пользователя на момент сканирования
    val timestamp: Timestamp? = null
)
