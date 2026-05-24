package com.example.scannfc.models

import com.google.firebase.Timestamp

data class ScanRecord(
    val id: String = "",
    val tagId: String = "",
    val tagContent: String = "",
    val userId: String = "",
    val userName: String = "",
    val userGroupId: String = "",
    val timestamp: Timestamp? = null
)
