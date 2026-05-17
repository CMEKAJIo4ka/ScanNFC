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

    init {
        loadHistory()
    }

    fun loadHistory() {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            val data = firestoreRepository.getScanHistory(userId = currentUser.uid)
            // Сортируем список вручную на устройстве, чтобы не требовать создания индексов в Firestore
            _history.value = data.sortedByDescending { it.timestamp }
        }
    }

    fun onTagScanned(tagId: String, tagContent: String) {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _scanStatus.value = ScanStatus.Error("Пользователь не авторизован")
            return
        }

        _scanStatus.value = ScanStatus.Loading
        
        viewModelScope.launch {
            val userProfile = firestoreRepository.getUserProfile(currentUser.uid)
            
            val scanRecord = ScanRecord(
                tagId = tagId,
                tagContent = tagContent,
                userId = currentUser.uid,
                userName = userProfile?.email ?: "Unknown",
                userGroupId = userProfile?.groupId ?: "No Group",
                timestamp = Timestamp.now()
            )

            val success = firestoreRepository.saveScan(scanRecord)
            if (success) {
                _scanStatus.value = ScanStatus.Success(tagContent)
                loadHistory() // Обновляем историю после нового сканирования
            } else {
                _scanStatus.value = ScanStatus.Error("Ошибка сохранения в базу")
            }
        }
    }

    fun resetStatus() {
        _scanStatus.value = ScanStatus.Idle
    }
}

sealed class ScanStatus {
    object Idle : ScanStatus()
    object Loading : ScanStatus()
    data class Success(val tagContent: String) : ScanStatus()
    data class Error(val message: String) : ScanStatus()
}
