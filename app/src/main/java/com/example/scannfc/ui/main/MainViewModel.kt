package com.example.scannfc.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scannfc.data.AuthRepository
import com.example.scannfc.data.FirestoreRepository
import com.example.scannfc.models.ScanRecord
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val firestoreRepository = FirestoreRepository()

    private val _scanStatus = MutableStateFlow<ScanStatus>(ScanStatus.Idle)
    val scanStatus: StateFlow<ScanStatus> = _scanStatus

    private val _history = MutableStateFlow<List<ScanRecord>>(emptyList())
    val history: StateFlow<List<ScanRecord>> = _history

    private val _isWritingMode = MutableStateFlow(false)
    val isWritingMode: StateFlow<Boolean> = _isWritingMode

    private var textToWrite: String = ""

    init {
        loadHistory()
    }

    fun loadHistory() {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            val data = firestoreRepository.getScanHistory(userId = currentUser.uid)
            _history.value = data.sortedByDescending { it.timestamp }
        }
    }

    fun prepareToWrite(text: String) {
        textToWrite = text
        _isWritingMode.value = true
        _scanStatus.value = ScanStatus.WaitingForTag
    }

    fun onTagScanned(tagId: String, tagContent: String) {
        val currentUser = authRepository.currentUser ?: return

        viewModelScope.launch {
            if (_isWritingMode.value) {
                handleWriteProcess(tagId, currentUser.uid)
            } else {
                handleReadProcess(tagId, tagContent, currentUser.uid)
            }
        }
    }

    private suspend fun handleReadProcess(tagId: String, tagContent: String, uid: String) {
        _scanStatus.value = ScanStatus.Loading
        val userProfile = firestoreRepository.getUserProfile(uid)
        val scanRecord = ScanRecord(
            tagId = tagId,
            tagContent = tagContent,
            userId = uid,
            userName = userProfile?.email ?: "Unknown",
            userGroupId = userProfile?.groupId ?: "No Group",
            timestamp = Timestamp.now()
        )
        if (firestoreRepository.saveScan(scanRecord)) {
            _scanStatus.value = ScanStatus.Success(tagContent)
            loadHistory()
        } else {
            _scanStatus.value = ScanStatus.Error("Ошибка сохранения")
        }
    }

    private suspend fun handleWriteProcess(tagId: String, uid: String) {
        val userProfile = firestoreRepository.getUserProfile(uid)
        val userRole = userProfile?.role ?: "student"

        // Проверяем владельца метки в базе
        val ownerRole = firestoreRepository.getTagOwnerRole(tagId)

        if (ownerRole == "teacher" && userRole == "student") {
            _scanStatus.value = ScanStatus.Error("Запрещено: метка защищена преподавателем")
            _isWritingMode.value = false
            return
        }

        // Переходим в состояние "Готов к физической записи"
        _scanStatus.value = ScanStatus.ReadyToWrite(tagId, textToWrite)
    }

    fun onWriteFinish(success: Boolean, tagId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            if (success) {
                val userProfile = firestoreRepository.getUserProfile(uid)
                firestoreRepository.registerTag(tagId, uid, userProfile?.role ?: "student")
                _scanStatus.value = ScanStatus.WriteSuccess
            } else {
                _scanStatus.value = ScanStatus.Error("Физическая ошибка записи")
            }
            _isWritingMode.value = false
        }
    }

    fun resetStatus() {
        _scanStatus.value = ScanStatus.Idle
        _isWritingMode.value = false
    }
}

sealed class ScanStatus {
    object Idle : ScanStatus()
    object Loading : ScanStatus()
    object WaitingForTag : ScanStatus()
    data class ReadyToWrite(val tagId: String, val text: String) : ScanStatus()
    object WriteSuccess : ScanStatus()
    data class Success(val tagContent: String) : ScanStatus()
    data class Error(val message: String) : ScanStatus()
}
