package com.example.scannfc.models

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "student", // "student" или "teacher"
    val groupId: String = ""      // ID группы из коллекции groups
)
