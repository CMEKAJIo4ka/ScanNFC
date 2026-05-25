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

    private val _isDeleteMode = MutableStateFlow(false)
    val isDeleteMode: StateFlow<Boolean> = _isDeleteMode

    private var textToWrite: String = ""

    companion object {
        const val EMPTY_TAG_MESSAGE = "Метка без данных"
    }

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
        _isDeleteMode.value = false
        _scanStatus.value = ScanStatus.WaitingForTag
    }

    fun prepareToDelete() {
        _isDeleteMode.value = true
        _isWritingMode.value = false
        _scanStatus.value = ScanStatus.WaitingForTag
    }

    fun onTagScanned(tagId: String, tagContent: String) {
        viewModelScope.launch {
            if (_isWritingMode.value || _isDeleteMode.value) {
                val uid = authRepository.currentUser?.uid
                if (uid == null) {
                    _scanStatus.value = ScanStatus.Error("Ошибка авторизации")
                    return@launch
                }
                handleWriteOrDeleteProcess(tagId, uid)
            } else {
                _scanStatus.value = ScanStatus.Success(tagContent)
                
                authRepository.currentUser?.uid?.let { uid ->
                    handleReadProcess(tagId, tagContent, uid)
                }
            }
        }
    }

    private suspend fun handleReadProcess(tagId: String, tagContent: String, uid: String) {
        if (tagContent == EMPTY_TAG_MESSAGE || tagContent.isBlank()) return

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
            loadHistory()
        }
    }

    private suspend fun handleWriteOrDeleteProcess(tagId: String, uid: String) {
        val userProfile = firestoreRepository.getUserProfile(uid)
        val userRole = userProfile?.role ?: "student"
        val ownerRole = firestoreRepository.getTagOwnerRole(tagId)

        if (ownerRole == "teacher" && userRole == "student") {
            val action = if (_isDeleteMode.value) "удалять" else "изменять"
            _scanStatus.value = ScanStatus.Error("Запрещено $action метку преподавателя")
            _isWritingMode.value = false
            _isDeleteMode.value = false
            return
        }

        if (_isDeleteMode.value) {
            _scanStatus.value = ScanStatus.ReadyToDelete(tagId)
        } else {
            _scanStatus.value = ScanStatus.ReadyToWrite(tagId, textToWrite)
        }
    }

    fun onWriteFinish(success: Boolean, tagId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            if (success) {
                if (_isDeleteMode.value) {
                    val userProfile = firestoreRepository.getUserProfile(uid)
                    if (userProfile?.role == "teacher") {
                        firestoreRepository.unregisterTag(tagId)
                    }
                    _scanStatus.value = ScanStatus.DeleteSuccess
                } else {
                    val userProfile = firestoreRepository.getUserProfile(uid)
                    firestoreRepository.registerTag(tagId, uid, userProfile?.role ?: "student")
                    _scanStatus.value = ScanStatus.WriteSuccess
                }
            } else {
                _scanStatus.value = ScanStatus.Error("Ошибка записи")
            }
            _isWritingMode.value = false
            _isDeleteMode.value = false
        }
    }

    fun resetStatus() {
        _scanStatus.value = ScanStatus.Idle
        _isWritingMode.value = false
        _isDeleteMode.value = false
    }
}

sealed class ScanStatus {
    object Idle : ScanStatus()
    object Loading : ScanStatus()
    object WaitingForTag : ScanStatus()
    data class ReadyToWrite(val tagId: String, val text: String) : ScanStatus()
    object WriteSuccess : ScanStatus()
    data class ReadyToDelete(val tagId: String) : ScanStatus()
    object DeleteSuccess : ScanStatus()
    data class Success(val tagContent: String) : ScanStatus()
    data class Error(val message: String) : ScanStatus()
}
